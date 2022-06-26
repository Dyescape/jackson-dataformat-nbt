package com.dyescape.dataformat.nbt.reader

import com.dyescape.dataformat.nbt.tag.NBTTagType
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.json.DupDetector
import java.io.DataInputStream

open class CompoundNBTReader(
    input: DataInputStream,
    parent: NBTReader?,
    duplicateDetector: DupDetector?,
    type: Int = TYPE_OBJECT,
) : NBTReader(type, duplicateDetector, input, parent) {
    private var nextTokenIsName = true
    protected var finished = false
    private lateinit var currentType: NBTTagType
    private lateinit var currentName: String

    override fun getCurrentName(): String? {
        return currentName
    }

    override fun setCurrentName(name: String) {
        this.currentName = name
    }

    override fun readToken(): NBTReadResult {
        if (finished) {
            error("Compound tag is finished")
        }

        if (nextTokenIsName) {
            nextTokenIsName = false

            val type = NBTTagType.byId(input.readByte().toInt())

            if (type == NBTTagType.END) {
                finished = true
                return NBTReadResult(JsonToken.END_OBJECT, parent)
            }

            currentName = input.readUTF()
            currentType = type

            if (duplicateDetector?.isDup(currentName) == true) {
                val source = duplicateDetector.source

                throw JsonParseException(source as? JsonParser, "Duplicate field $currentName")
            }

            return NBTReadResult(JsonToken.FIELD_NAME, this)
        }

        nextTokenIsName = true
        return readValueToken(currentType)
    }

    override fun allowEndOfContent(): Boolean {
        // End of content is only allowed in the root compound
        return false
    }
}
