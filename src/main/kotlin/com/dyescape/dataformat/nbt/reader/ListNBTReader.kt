package com.dyescape.dataformat.nbt.reader

import com.dyescape.dataformat.nbt.tag.NBTTagType
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.json.DupDetector
import java.io.DataInputStream

class ListNBTReader(
    input: DataInputStream,
    private val contentType: NBTTagType,
    private val size: Int,
    duplicateDetector: DupDetector?,
    parent: NBTReader?,
) : NBTReader(TYPE_ARRAY, duplicateDetector, input, parent) {
    init {
        _index = 0
    }

    override fun getCurrentName(): String? {
        return null
    }

    override fun setCurrentName(name: String) {
        error("Cannot set current name for list tag")
    }

    override fun readToken(): NBTReadResult {
        if (_index > size) {
            error("Array tag is finished")
        }

        if (_index == size) {
            return NBTReadResult(JsonToken.END_ARRAY, parent)
        }

        val result = readValueToken(contentType)

        _index++

        return result
    }

    override fun allowEndOfContent(): Boolean {
        // End of content is only allowed in the root compound
        return false
    }
}
