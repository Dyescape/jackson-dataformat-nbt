package com.dyescape.dataformat.nbt.reader

import com.dyescape.dataformat.nbt.tag.NBTTagType
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.json.DupDetector
import java.io.DataInputStream

class RootCompoundNBTReader(
    input: DataInputStream,
    duplicateDetector: DupDetector?,
) : CompoundNBTReader(input, null, duplicateDetector, type = TYPE_ROOT) {
    private var startSkipped = false
    private var nullReturned = false

    override fun readToken(): NBTReadResult {
        if (finished) {
            if (!nullReturned) {
                nullReturned = true
                return NBTReadResult(null, parent)
            }

            error("Compound tag is finished")
        }

        if (!startSkipped) {
            val type = NBTTagType.byId(input.readByte().toInt())

            if (type == NBTTagType.END) {
                finished = true
                startSkipped = true
                nullReturned = true
                return NBTReadResult(null, parent)
            }

            if (type != NBTTagType.COMPOUND) {
                error("Root tag must be of type compound")
            }

            input.readUTF()
            startSkipped = true
            return NBTReadResult(JsonToken.START_OBJECT, this)
        }

        return super.readToken()
    }

    override fun allowEndOfContent(): Boolean {
        return finished
    }
}
