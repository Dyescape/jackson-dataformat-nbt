package com.dyescape.dataformat.nbt

import com.dyescape.dataformat.nbt.tag.NBTValue
import com.dyescape.dataformat.nbt.writer.CompoundNBTWriter
import com.dyescape.dataformat.nbt.writer.ListNBTWriter
import com.dyescape.dataformat.nbt.writer.NBTWriter
import com.fasterxml.jackson.core.Base64Variant
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.core.base.GeneratorBase
import com.fasterxml.jackson.core.json.DupDetector
import java.io.OutputStream
import java.math.BigDecimal
import java.math.BigInteger

class NBTGenerator(
    features: Int,
    codec: ObjectCodec?,
    out: OutputStream,
) : GeneratorBase(features, codec) {
    private var finished = false
    private var writer = NBTWriter.root(out, createDuplicateDetector(features))

    override fun flush() {
        writer.flush()
    }

    override fun writeStartArray() {
        _verifyValueWrite("start a list tag")
        writer = writer.startList()
    }

    override fun writeStartArray(forValue: Any?) {
        _verifyValueWrite("start a list tag")
        writer = writer.startList()
        currentValue = forValue
    }

    override fun writeStartArray(forValue: Any?, size: Int) {
        _verifyValueWrite("start a list tag")
        writer = writer.startList(size = size)
        currentValue = forValue
    }

    @Deprecated(
        "Deprecated in jackson",
        ReplaceWith("writeStartArray(value, size)"),
    )
    override fun writeStartArray(size: Int) {
        _verifyValueWrite("start a list tag")
        writer = writer.startList(size = size)
    }

    override fun writeEndArray(): Unit = consumeWriter<ListNBTWriter> { writer ->
        if (!writer.isComplete()) {
            reportError("Can not end list tag before all values are written")
        }
    }

    override fun writeStartObject() {
        _verifyValueWrite("start a compound tag")
        writer = writer.startCompound()
    }

    override fun writeEndObject(): Unit = consumeWriter<CompoundNBTWriter> { writer ->
        if (!writer.canEnd()) {
            reportError("Can not end compound tag after name")
        }
    }

    override fun writeFieldName(name: String) {
        if (finished) {
            reportError("NBT generator is finished")
        }

        if (!writer.acceptsName()) {
            reportError("Unexpected field name: '$name'")
        }

        writer.writeName(name)
    }

    override fun writeString(text: String?) {
        if (text == null) return writeNull()

        _verifyValueWrite("write a string tag")
        writer.writePrimitive(NBTValue.StringTag(text))
    }

    override fun writeString(buffer: CharArray, offset: Int, length: Int) {
        writeString(String(buffer, offset, length))
    }

    override fun writeRawUTF8String(buffer: ByteArray, offset: Int, length: Int) {
        writeString(String(buffer, offset, length, Charsets.UTF_8))
    }

    override fun writeUTF8String(buffer: ByteArray, offset: Int, length: Int) {
        writeRawUTF8String(buffer, offset, length)
    }

    override fun writeRaw(value: String?) {
        throw UnsupportedOperationException("Binary format does not support raw string writes")
    }

    override fun writeRaw(value: String?, p1: Int, p2: Int) {
        throw UnsupportedOperationException("Binary format does not support raw string writes")
    }

    override fun writeRaw(value: CharArray?, p1: Int, p2: Int) {
        throw UnsupportedOperationException("Binary format does not support raw string writes")
    }

    override fun writeRaw(value: Char) {
        throw UnsupportedOperationException("Binary format does not support raw string writes")
    }

    override fun writeBinary(bv: Base64Variant, data: ByteArray, offset: Int, len: Int) {
        _verifyValueWrite("write a binary value")
        writer.writePrimitive(NBTValue.ByteArrayTag(data, offset, len))
    }

    override fun writeNumber(v: Int) {
        _verifyValueWrite("write an int value")
        writer.writePrimitive(NBTValue.IntTag(v))
    }

    override fun writeNumber(v: Long) {
        _verifyValueWrite("write a long value")
        writer.writePrimitive(NBTValue.LongTag(v))
    }

    override fun writeNumber(v: BigInteger?) {
        throw UnsupportedOperationException("NBT does not support big integers")
    }

    override fun writeNumber(v: Double) {
        _verifyValueWrite("write a double value")
        writer.writePrimitive(NBTValue.DoubleTag(v))
    }

    override fun writeNumber(v: Float) {
        _verifyValueWrite("write a float value")
        writer.writePrimitive(NBTValue.FloatTag(v))
    }

    override fun writeNumber(v: BigDecimal?) {
        throw UnsupportedOperationException("NBT does not support big decimals")
    }

    override fun writeNumber(encodedValue: String?) {
        writeString(encodedValue)
    }

    override fun writeBoolean(state: Boolean) {
        _verifyValueWrite("write a boolean value")
        writer.writePrimitive(NBTValue.ByteTag(if (state) 1 else 0))
    }

    override fun writeNull() {
        _verifyValueWrite("write null")
        writer.skipValue()
    }

    override fun writeEmbeddedObject(value: Any?) {
        if (value != null && value is NBTValue) {
            _verifyValueWrite("write an nbt primitive of type '${value.type}'")
            writer.writePrimitive(value)
            return
        }

        super.writeEmbeddedObject(value)
    }

    override fun _releaseBuffers() {

    }

    override fun _verifyValueWrite(type: String) {
        if (finished) {
            reportError("NBT generator is finished")
        }

        if (!writer.acceptsFurtherElements()) {
            reportError("Failed to $type, the element is already full")
        }

        if (!writer.acceptsValue()) {
            reportError("Can not $type, expecting field name")
        }
    }

    private inline fun <reified W : NBTWriter> consumeWriter(consumer: (writer: W) -> Unit) {
        if (finished) {
            reportError("NBT generator is finished")
        }

        val currentWriter = writer

        if (currentWriter !is W) {
            reportError("Expected writer to be ${W::class.simpleName}, was ${currentWriter.javaClass.simpleName}")
        }

        consumer(currentWriter)

        val parentWriter = currentWriter.end()

        if (parentWriter != null) {
            writer = parentWriter
        } else {
            finished = true
        }
    }

    private fun reportError(value: String): Nothing {
        _reportError(value)

        error("???")
    }

    private fun createDuplicateDetector(features: Int): DupDetector? {
        return when {
            Feature.STRICT_DUPLICATE_DETECTION.enabledIn(features) -> DupDetector.rootDetector(this)
            else                                                   -> null
        }
    }
}
