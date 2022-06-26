package com.dyescape.dataformat.nbt.databind

import com.dyescape.dataformat.nbt.NBTFactory
import com.fasterxml.jackson.core.TokenStreamFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.cfg.MapperBuilder

class NBTMapper : ObjectMapper {
    constructor() : this(NBTFactory())

    constructor(factory: NBTFactory) : super(factory) {
        registerModule(NBTModule())
    }

    constructor(src: NBTMapper) : super(src)

    class Builder(mapper: NBTMapper) : MapperBuilder<NBTMapper, Builder>(mapper) {
        private val factory = mapper.factory

        override fun streamFactory(): TokenStreamFactory {
            return factory
        }
    }

    override fun copy(): ObjectMapper {
        _checkInvalidCopy(NBTMapper::class.java)

        return NBTMapper(this)
    }

    override fun getFactory(): NBTFactory {
        return super.getFactory() as NBTFactory
    }

    companion object {
        fun builder() = Builder(NBTMapper())

        fun builder(factory: NBTFactory) = Builder(NBTMapper(factory))
    }
}
