package com.dyescape.dataformat.nbt.tag

import java.io.DataOutput

sealed class NBTValue(val type: NBTTagType) {
    abstract fun writeTo(output: DataOutput)

    class ByteTag(private val data: Byte) : NBTValue(NBTTagType.BYTE) {
        override fun writeTo(output: DataOutput) {
            output.writeByte(data.toInt())
        }
    }

    class ShortTag(private val data: Short) : NBTValue(NBTTagType.SHORT) {
        override fun writeTo(output: DataOutput) {
            output.writeShort(data.toInt())
        }
    }

    class IntTag(private val data: Int) : NBTValue(NBTTagType.INT) {
        override fun writeTo(output: DataOutput) {
            output.writeInt(data)
        }
    }

    class LongTag(private val data: Long) : NBTValue(NBTTagType.LONG) {
        override fun writeTo(output: DataOutput) {
            output.writeLong(data)
        }
    }

    class FloatTag(private val data: Float) : NBTValue(NBTTagType.FLOAT) {
        override fun writeTo(output: DataOutput) {
            output.writeFloat(data)
        }
    }

    class DoubleTag(private val data: Double) : NBTValue(NBTTagType.DOUBLE) {
        override fun writeTo(output: DataOutput) {
            output.writeDouble(data)
        }
    }

    class ByteArrayTag(
        private val data: ByteArray,
        private val offset: Int = 0,
        private val length: Int = data.size - offset,
    ) : NBTValue(NBTTagType.BYTE_ARRAY) {
        override fun writeTo(output: DataOutput) {
            output.writeInt(length)
            output.write(data, offset, length)
        }
    }

    class IntArrayTag(private val data: IntArray) : NBTValue(NBTTagType.INT_ARRAY) {
        override fun writeTo(output: DataOutput) {
            output.writeInt(data.size)
            for (datum in data) {
                output.writeInt(datum)
            }
        }
    }

    class LongArrayTag(private val data: LongArray) : NBTValue(NBTTagType.LONG_ARRAY) {
        override fun writeTo(output: DataOutput) {
            output.writeInt(data.size)
            for (datum in data) {
                output.writeLong(datum)
            }
        }
    }

    class StringTag(private val data: String) : NBTValue(NBTTagType.STRING) {
        override fun writeTo(output: DataOutput) {
            output.writeUTF(data)
        }
    }
}
