package com.dyescape.dataformat.nbt.writer

import com.dyescape.dataformat.nbt.tag.NBTTagType
import com.fasterxml.jackson.core.json.DupDetector
import java.io.DataOutputStream

class RootCompoundNBTWriter(
    private val includeName: Boolean,
    output: DataOutputStream,
    duplicateDetector: DupDetector?,
) : CompoundNBTWriter(parent = null, duplicateDetector, output) {
    private var started = false

    override fun start() {
        output.writeByte(NBTTagType.COMPOUND.id)
        if (includeName) {
            output.writeUTF("")
        }
        started = true
    }

    override fun startCompound(): NBTWriter {
        if (!started) {
            start()
            return this
        }

        return super.startCompound()
    }

    override fun acceptsValue(): Boolean {
        return !started || super.acceptsValue()
    }
}
