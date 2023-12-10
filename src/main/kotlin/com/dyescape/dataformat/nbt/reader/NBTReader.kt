package com.dyescape.dataformat.nbt.reader

import com.dyescape.dataformat.nbt.tag.NBTTagType
import com.fasterxml.jackson.core.JsonParser.NumberType
import com.fasterxml.jackson.core.JsonStreamContext
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.json.DupDetector
import java.io.DataInputStream
import java.io.InputStream

abstract class NBTReader(
    type: Int,
    protected val duplicateDetector: DupDetector?,
    protected val input: DataInputStream,
    private val parent: NBTReader?,
) : JsonStreamContext(type, -1) {
    private var readValue: Any? = null

    override fun getParent(): NBTReader? {
        return parent
    }

    abstract fun readToken(): NBTReadResult

    abstract fun setCurrentName(name: String)

    protected fun readValueToken(type: NBTTagType) = when (type) {
        NBTTagType.END        -> {
            readValue = null
            NBTReadResult(JsonToken.VALUE_NULL, this)
        }
        NBTTagType.BYTE       -> {
            readValue = input.readByte()
            NBTReadResult(JsonToken.VALUE_NUMBER_INT, this)
        }
        NBTTagType.SHORT      -> {
            readValue = input.readShort()
            NBTReadResult(JsonToken.VALUE_NUMBER_INT, this)
        }
        NBTTagType.INT        -> {
            readValue = input.readInt()
            NBTReadResult(JsonToken.VALUE_NUMBER_INT, this)
        }
        NBTTagType.LONG       -> {
            readValue = input.readLong()
            NBTReadResult(JsonToken.VALUE_NUMBER_INT, this)
        }
        NBTTagType.FLOAT      -> {
            readValue = input.readFloat()
            NBTReadResult(JsonToken.VALUE_NUMBER_FLOAT, this)
        }
        NBTTagType.DOUBLE     -> {
            readValue = input.readDouble()
            NBTReadResult(JsonToken.VALUE_NUMBER_FLOAT, this)
        }
        NBTTagType.BYTE_ARRAY -> {
            readValue = null
            readListTag(NBTTagType.BYTE)
        }
        NBTTagType.INT_ARRAY  -> {
            readValue = null
            readListTag(NBTTagType.INT)
        }
        NBTTagType.LONG_ARRAY -> {
            readValue = null
            readListTag(NBTTagType.LONG)
        }
        NBTTagType.STRING     -> {
            readValue = input.readUTF()
            NBTReadResult(JsonToken.VALUE_STRING, this)
        }
        NBTTagType.LIST       -> {
            readValue = null
            val contentType = NBTTagType.byId(input.readByte().toInt())
            readListTag(contentType)
        }
        NBTTagType.COMPOUND   -> {
            readValue = null
            NBTReadResult(JsonToken.START_OBJECT, CompoundNBTReader(input, this, duplicateDetector))
        }
    }

    private fun readListTag(contentType: NBTTagType): NBTReadResult {
        val size = input.readInt()

        return NBTReadResult(
            JsonToken.START_ARRAY,
            ListNBTReader(
                input,
                contentType,
                size,
                duplicateDetector,
                this,
            ),
        )
    }

    fun stringValue(): String {
        val value = readValue ?: error("No value")

        return value.toString()
    }

    fun numberValue(): Number {
        val value = readValue ?: error("No value")

        if (value !is Number) {
            error("Not a number: '$value'")
        }

        return value
    }

    fun numberType(): NumberType {
        return when (readValue) {
            is Byte   -> NumberType.INT
            is Short  -> NumberType.INT
            is Int    -> NumberType.INT
            is Long   -> NumberType.LONG
            is Float  -> NumberType.FLOAT
            is Double -> NumberType.DOUBLE
            else      -> error("Not a number: '$readValue'")
        }
    }

    abstract fun allowEndOfContent(): Boolean

    companion object {
        fun root(input: InputStream, duplicateDetector: DupDetector?): NBTReader {
            return RootCompoundNBTReader(DataInputStream(input), duplicateDetector)
        }
    }
}
