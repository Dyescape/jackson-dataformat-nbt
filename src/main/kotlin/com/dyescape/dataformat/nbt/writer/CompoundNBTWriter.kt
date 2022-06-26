package com.dyescape.dataformat.nbt.writer

import com.dyescape.dataformat.nbt.tag.NBTValue
import com.dyescape.dataformat.nbt.tag.NBTTagType
import com.fasterxml.jackson.core.JsonGenerationException
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.json.DupDetector
import java.io.DataOutputStream

open class CompoundNBTWriter(
    parent: NBTWriter?,
    private val duplicateDetector: DupDetector?,
    output: DataOutputStream,
) : NBTWriter(parent, output) {
    private var currentName: String? = null

    override fun start() {
    }

    override fun acceptsName(): Boolean {
        return currentName == null
    }

    override fun acceptsValue(): Boolean {
        return currentName != null
    }

    override fun acceptsFurtherElements(): Boolean {
        return true
    }

    fun canEnd(): Boolean {
        return currentName == null
    }

    override fun writeName(name: String) {
        if (currentName != null) {
            error("Expected value")
        }

        if (duplicateDetector?.isDup(name) == true) {
            val source = duplicateDetector.source

            throw JsonGenerationException("Duplicate field $name", source as? JsonGenerator)
        }

        currentName = name
    }

    override fun writePrimitive(value: NBTValue): Unit = consumeName { name ->
        output.writeByte(value.type.id)
        output.writeUTF(name)
        value.writeTo(output)
    }

    override fun skipValue() = consumeName {
        // skip
    }

    override fun startList(size: Int?, type: NBTTagType?): NBTWriter = consumeName { name ->
        output.writeByte(NBTTagType.LIST.id)
        output.writeUTF(name)

        val writer = ListNBTWriter(this, duplicateDetector?.child(), output, size, type)

        writer.start()

        writer
    }

    override fun startCompound(): NBTWriter = consumeName { name ->
        output.writeByte(NBTTagType.COMPOUND.id)
        output.writeUTF(name)

        val writer = CompoundNBTWriter(this, duplicateDetector?.child(), output)

        writer.start()

        writer
    }

    override fun writeEnd() {
        output.writeByte(NBTTagType.END.id)
    }

    private inline fun <R> consumeName(consumer: (name: String) -> R): R {
        val result = consumer(currentName ?: error("Expected name"))

        currentName = null

        return result
    }
}
