package com.dyescape.dataformat.nbt.writer

import com.dyescape.dataformat.nbt.tag.NBTValue
import com.dyescape.dataformat.nbt.tag.NBTTagType
import com.fasterxml.jackson.core.json.DupDetector
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class ListNBTWriter(
    parent: NBTWriter?,
    private val duplicateDetector: DupDetector?,
    output: DataOutputStream,
    private var size: Int? = null,
    private var type: NBTTagType? = if (size == null || size > 0) null else NBTTagType.END,
) : NBTWriter(parent, output) {
    private var index = 0
    private val bufferStorage = ByteArrayOutputStream()
    private var buffer: DataOutputStream = DataOutputStream(bufferStorage)

    override fun start() {
        switchToDirectIfReady()
    }

    override fun acceptsName(): Boolean {
        return false
    }

    override fun acceptsValue(): Boolean {
        return true
    }

    override fun acceptsFurtherElements(): Boolean {
        val currentSize = size

        return currentSize == null || index < currentSize
    }

    fun isComplete(): Boolean {
        val currentSize = size

        return currentSize == null || index == currentSize
    }

    override fun writeName(name: String) {
        error("Cannot write name in list")
    }

    override fun writePrimitive(value: NBTValue) {
        checkWriteAllowed(value.type)
        this.type = value.type

        switchToDirectIfReady()

        value.writeTo(buffer)

        this.index++
    }

    override fun skipValue() {
        buffer.writeByte(NBTTagType.END.id)
    }

    override fun startList(size: Int?, type: NBTTagType?): NBTWriter {
        checkWriteAllowed(NBTTagType.LIST)
        this.type = NBTTagType.LIST

        switchToDirectIfReady()

        val writer = ListNBTWriter(this, duplicateDetector?.child(), buffer, size, type)

        this.index++

        return writer
    }

    override fun startCompound(): NBTWriter {
        checkWriteAllowed(NBTTagType.COMPOUND)
        this.type = NBTTagType.COMPOUND

        switchToDirectIfReady()

        val writer = CompoundNBTWriter(this, duplicateDetector?.child(), buffer)

        this.index++

        return writer
    }

    override fun writeEnd() {
        if (size == null) {
            size = index
        }

        if (type == null) {
            type = NBTTagType.END
        }

        switchToDirectIfReady()
    }

    private fun switchToDirectIfReady() {
        if (buffer == output) return

        val currentSize = size
        val currentType = type

        if (currentSize != null && currentType != null) {
            val buffered = bufferStorage.toByteArray()

            buffer = output

            buffer.writeByte(currentType.id)
            buffer.writeInt(currentSize)
            buffer.write(buffered)
        }
    }

    private fun checkWriteAllowed(type: NBTTagType) {
        if (this.type != null && this.type != type) {
            error("Cannot write tag of type '$type' to list with type '${this.type}'")
        }

        val currentSize = this.size
        if (currentSize != null && index >= currentSize) {
            error("List is already full")
        }
    }
}
