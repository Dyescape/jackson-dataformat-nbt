package com.dyescape.dataformat.nbt

import com.dyescape.dataformat.nbt.tag.NBTTagType
import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.core.format.InputAccessor
import com.fasterxml.jackson.core.format.MatchStrength
import com.fasterxml.jackson.core.io.IOContext
import com.fasterxml.jackson.core.util.JacksonFeatureSet
import java.io.*
import java.net.URL

class NBTFactory : JsonFactory, NBTFeature.Configurator<NBTFactory> {
    internal var nbtFeatures: JacksonFeatureSet<NBTFeature>
        private set

    constructor() : super() {
        nbtFeatures = NBTFeature.DEFAULTS
    }

    constructor(codec: ObjectCodec?) : super(codec) {
        nbtFeatures = NBTFeature.DEFAULTS
    }

    constructor(source: NBTFactory, codec: ObjectCodec?) : super(source, codec) {
        nbtFeatures = source.nbtFeatures
    }

    constructor(builder: NBTFactoryBuilder) : super(builder, false) {
        nbtFeatures = builder.nbtFeatures
    }

    override fun rebuild(): NBTFactoryBuilder {
        return NBTFactoryBuilder(this)
    }

    override fun configure(feature: NBTFeature, state: Boolean): NBTFactory {
        nbtFeatures = when (state) {
            true  -> nbtFeatures.with(feature)
            false -> nbtFeatures.without(feature)
        }

        return this
    }

    override fun isEnabled(feature: NBTFeature): Boolean {
        return nbtFeatures.isEnabled(feature)
    }

    override fun copy(): NBTFactory {
        _checkInvalidCopy(NBTFactory::class.java)

        return NBTFactory(this, null)
    }

    override fun readResolve(): Any {
        return NBTFactory(this, codec)
    }

    override fun getFormatName() = FORMAT_NAME

    override fun canUseCharArrays(): Boolean {
        return false
    }

    override fun canHandleBinaryNatively(): Boolean {
        return true
    }

    override fun createParser(f: File): NBTParser {
        val ctxt = _createContext(_createContentReference(f), true)
        return _createParser(_decorate(FileInputStream(f), ctxt), ctxt)
    }

    override fun createParser(url: URL): NBTParser {
        val ctxt = _createContext(_createContentReference(url), true)
        return _createParser(_decorate(_optimizedStreamFromURL(url), ctxt), ctxt)
    }

    override fun createParser(input: InputStream): NBTParser {
        val ctxt = _createContext(_createContentReference(input), false)
        return _createParser(_decorate(input, ctxt), ctxt)
    }

    override fun createParser(data: ByteArray): NBTParser {
        return createParser(data, 0, data.size)
    }

    override fun createParser(data: ByteArray, offset: Int, len: Int): NBTParser {
        val ctxt = _createContext(_createContentReference(data, offset, len), true)
        if (_inputDecorator != null) {
            val inputStream = _inputDecorator.decorate(ctxt, data, 0, data.size)
            if (inputStream != null) {
                return _createParser(inputStream, ctxt)
            }
        }
        return _createParser(data, offset, len, ctxt)
    }

    override fun createGenerator(out: OutputStream, enc: JsonEncoding): NBTGenerator {
        return createGenerator(out)
    }

    override fun createGenerator(out: OutputStream): NBTGenerator {
        val ctxt = _createContext(_createContentReference(out), false)
        return createNBTGenerator(_generatorFeatures, _objectCodec, _decorate(out, ctxt), ctxt)
    }

    override fun _createParser(inputStream: InputStream, ctxt: IOContext): NBTParser {
        return NBTParser(nbtFeatures, ctxt, _parserFeatures, inputStream, _objectCodec)
    }

    override fun _createParser(data: ByteArray, offset: Int, len: Int, ctxt: IOContext): NBTParser {
        val inputStream = ByteArrayInputStream(data, offset, len)

        return NBTParser(nbtFeatures, ctxt, _parserFeatures, inputStream, _objectCodec)
    }

    override fun _createGenerator(out: Writer?, ctxt: IOContext): NBTGenerator {
        nonByteTarget()
    }

    override fun _createUTF8Generator(out: OutputStream, ctxt: IOContext): NBTGenerator {
        return createNBTGenerator(_generatorFeatures, _objectCodec, out, ctxt)
    }

    override fun _createWriter(out: OutputStream?, enc: JsonEncoding?, ctxt: IOContext?): Writer? {
        nonByteTarget()
    }

    private fun createNBTGenerator(
        features: Int,
        codec: ObjectCodec,
        out: OutputStream,
        ioContext: IOContext?,
    ): NBTGenerator {
        return NBTGenerator(nbtFeatures, features, codec, out, ioContext)
    }

    private fun nonByteTarget(): Nothing {
        throw UnsupportedOperationException("Can not create generator for non-byte-based target")
    }

    override fun hasFormat(acc: InputAccessor): MatchStrength {
        // lmao here we go

        if (!acc.hasMoreBytes()) {
            // NBT always has data
            return MatchStrength.NO_MATCH
        }

        val byte = acc.nextByte()

        if (byte != NBTTagType.COMPOUND.id.toByte()) {
            if (byte == NBTTagType.END.id.toByte() && !acc.hasMoreBytes()) {
                // NBT can consist of a single END tag
                return MatchStrength.SOLID_MATCH
            }

            // NBT can only have a COMPOUND or END tag
            return MatchStrength.NO_MATCH
        }

        var improperUse = false

        if (isEnabled(NBTFeature.INCLUDE_ROOT_TAG_NAME)) {
            if (!acc.hasMoreBytes()) {
                // Compound tag type needs to be followed by a short for the string length
                return MatchStrength.NO_MATCH
            }

            val mostSignificant = acc.nextByte()

            if (!acc.hasMoreBytes()) {
                // Short needs to have two bytes
                return MatchStrength.NO_MATCH
            }

            val leastSignificant = acc.nextByte()

            val potentialStringLength = (mostSignificant.toInt() shl 8 or leastSignificant.toInt()).toShort()

            if (potentialStringLength > 0) {
                // Normally, the root compound name should just have length 0.
                // This could, however, still be improper use of NBT.

                // Try to skip the string
                repeat(potentialStringLength.toInt()) {
                    if (!acc.hasMoreBytes()) {
                        // The potential string length is greater than the number of bytes, so it cannot be a valid string
                        // length
                        return MatchStrength.NO_MATCH
                    }

                    acc.nextByte()
                }

                improperUse = true
            }
        }

        if (!acc.hasMoreBytes()) {
            // The potential string has to be followed by more bytes
            return MatchStrength.NO_MATCH
        }

        val potentialType = acc.nextByte()

        if (NBTTagType.byIdOrNull(potentialType.toInt()) == null) {
            // The potential string has to be followed by an NBT tag
            return MatchStrength.NO_MATCH
        }

        if (improperUse) {
            // Improper use is a weak match
            return MatchStrength.WEAK_MATCH
        }

        return MatchStrength.SOLID_MATCH
    }

    companion object {
        const val FORMAT_NAME = "NBT"
    }
}
