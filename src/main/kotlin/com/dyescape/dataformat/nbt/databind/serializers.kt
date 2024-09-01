package com.dyescape.dataformat.nbt.databind

import com.dyescape.dataformat.nbt.tag.NBTValue
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import kotlin.reflect.KClass

abstract class NBTSerializer<T : Any>(
    type: KClass<T>,
    primitive: Boolean,
) : StdScalarSerializer<T>(type.javaType(primitive))

class NBTByteSerializer(primitive: Boolean) : NBTSerializer<Byte>(Byte::class, primitive) {
    override fun serialize(value: Byte?, generator: JsonGenerator, provider: SerializerProvider) {
        generator.writeEmbeddedObject(value?.let { NBTValue.ByteTag(it) })
    }
}

class NBTShortSerializer(primitive: Boolean) : NBTSerializer<Short>(Short::class, primitive) {
    override fun serialize(value: Short?, generator: JsonGenerator, provider: SerializerProvider) {
        generator.writeEmbeddedObject(value?.let { NBTValue.ShortTag(it) })
    }
}

object NBTByteArraySerializer : StdScalarSerializer<ByteArray>(ByteArray::class.java) {
    override fun serialize(value: ByteArray?, generator: JsonGenerator, provider: SerializerProvider) {
        generator.writeEmbeddedObject(value?.let { NBTValue.ByteArrayTag(it) })
    }

    private fun readResolve(): Any = NBTByteArraySerializer
}

object NBTIntArraySerializer : StdScalarSerializer<IntArray>(IntArray::class.java) {
    override fun serialize(value: IntArray?, generator: JsonGenerator, provider: SerializerProvider) {
        generator.writeEmbeddedObject(value?.let { NBTValue.IntArrayTag(it) })
    }

    private fun readResolve(): Any = NBTIntArraySerializer
}

object NBTLongArraySerializer : StdScalarSerializer<LongArray>(LongArray::class.java) {
    override fun serialize(value: LongArray?, generator: JsonGenerator, provider: SerializerProvider) {
        generator.writeEmbeddedObject(value?.let { NBTValue.LongArrayTag(it) })
    }

    private fun readResolve(): Any = NBTLongArraySerializer
}

private fun <T : Any> KClass<T>.javaType(primitive: Boolean): Class<T> {
    return if (primitive) (javaPrimitiveType ?: error("No primitive: ${this.simpleName}")) else java
}
