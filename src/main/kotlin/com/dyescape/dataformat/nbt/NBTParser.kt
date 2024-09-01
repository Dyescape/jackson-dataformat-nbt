package com.dyescape.dataformat.nbt

import com.dyescape.dataformat.nbt.reader.CountingInputStream
import com.dyescape.dataformat.nbt.reader.NBTReader
import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.core.base.ParserMinimalBase
import com.fasterxml.jackson.core.io.IOContext
import com.fasterxml.jackson.core.json.DupDetector
import com.fasterxml.jackson.core.json.PackageVersion
import com.fasterxml.jackson.core.util.JacksonFeatureSet
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.math.BigDecimal
import java.math.BigInteger

class NBTParser(
    nbtFeatures: JacksonFeatureSet<NBTFeature>,
    private val context: IOContext,
    features: Int,
    input: InputStream,
    private var objectCodec: ObjectCodec?,
) : ParserMinimalBase(features, context.streamReadConstraints()) {
    private var closed = false
    private val countingInput = CountingInputStream(input)
    private var reader = NBTReader.root(nbtFeatures, countingInput, createDuplicateDetector(features))

    override fun version(): Version {
        return PackageVersion.VERSION
    }

    override fun getCodec(): ObjectCodec? {
        return objectCodec
    }

    override fun setCodec(oc: ObjectCodec?) {
        objectCodec = oc
    }

    override fun close() {
        closed = true
    }

    override fun isClosed(): Boolean {
        return closed
    }

    override fun getParsingContext(): JsonStreamContext {
        return reader
    }

    @Deprecated(message = "Deprecated since 2.17", replaceWith = ReplaceWith("currentLocation()"))
    override fun getCurrentLocation(): JsonLocation {
        return JsonLocation(
            context.contentReference(),
            countingInput.read,
            -1,
            -1,
            countingInput.read.toInt(),
        )
    }

    @Deprecated(message = "Deprecated since 2.17", replaceWith = ReplaceWith("currentTokenLocation()"))
    override fun getTokenLocation(): JsonLocation {
        return JsonLocation(
            context.contentReference(),
            countingInput.checkpoint,
            -1,
            -1,
            countingInput.checkpoint.toInt(),
        )
    }

    override fun nextToken(): JsonToken? {
        countingInput.markCheckpoint()
        val (token, reader) = reader.readToken()

        if (reader != null) {
            this.reader = reader
        }

        _currToken = token
        return token
    }

    override fun overrideCurrentName(name: String?) {
        reader.currentName = name
    }

    @Deprecated(message = "Deprecated since 2.17", replaceWith = ReplaceWith("currentName()"))
    override fun getCurrentName(): String {
        return reader.currentName
    }

    override fun getText(): String {
        return reader.stringValue()
    }

    override fun getTextCharacters(): CharArray {
        return reader.stringValue().toCharArray()
    }

    override fun getTextLength(): Int {
        return reader.stringValue().length
    }

    override fun getTextOffset(): Int {
        return 0
    }

    override fun hasTextCharacters(): Boolean {
        return false
    }

    override fun getNumberValue(): Number {
        return reader.numberValue()
    }

    override fun getNumberType(): NumberType {
        return reader.numberType()
    }

    override fun getIntValue(): Int {
        return reader.numberValue().toInt()
    }

    override fun getLongValue(): Long {
        return reader.numberValue().toLong()
    }

    override fun getBigIntegerValue(): BigInteger {
        throw UnsupportedOperationException("NBT does not support big integers")
    }

    override fun getFloatValue(): Float {
        return reader.numberValue().toFloat()
    }

    override fun getDoubleValue(): Double {
        return reader.numberValue().toDouble()
    }

    override fun getDecimalValue(): BigDecimal {
        throw UnsupportedOperationException("NBT does not support big decimals")
    }

    override fun getBinaryValue(b64variant: Base64Variant?): ByteArray {
        val startToken = nextToken()
        if (startToken != JsonToken.START_ARRAY) {
            reportError("Expected an array")
        }

        val bytes = ByteArrayOutputStream()

        var token = nextToken()
        while (token != JsonToken.END_ARRAY) {
            bytes.write(reader.numberValue().toInt())
            token = nextToken()
        }

        return bytes.toByteArray()
    }

    override fun _handleEOF() {
        if (!reader.allowEndOfContent()) {
            throw JsonParseException(this, "Unexpected end of content")
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
