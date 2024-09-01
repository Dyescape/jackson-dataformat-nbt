package com.dyescape.dataformat.nbt.databind

import com.dyescape.dataformat.nbt.NBTFactory
import com.dyescape.dataformat.nbt.NBTFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.cfg.MapperBuilder

class NBTMapper : ObjectMapper, NBTFeature.Configurator<NBTMapper> {
    constructor() : this(NBTFactory())

    constructor(factory: NBTFactory) : super(factory) {
        registerModule(NBTModule())
    }

    constructor(src: NBTMapper) : super(src)

    override fun copy(): ObjectMapper {
        _checkInvalidCopy(NBTMapper::class.java)

        return NBTMapper(this)
    }

    override fun getFactory(): NBTFactory {
        return super.getFactory() as NBTFactory
    }

    override fun configure(feature: NBTFeature, state: Boolean): NBTMapper {
        factory.configure(feature, state)

        return this
    }

    override fun isEnabled(feature: NBTFeature): Boolean {
        return factory.isEnabled(feature)
    }

    class Builder(mapper: NBTMapper) : MapperBuilder<NBTMapper, Builder>(mapper), NBTFeature.Configurator<Builder> {
        private val factory = mapper.factory

        override fun streamFactory(): NBTFactory {
            return factory
        }

        override fun configure(feature: NBTFeature, state: Boolean): Builder {
            factory.configure(feature, state)

            return this
        }

        override fun isEnabled(feature: NBTFeature): Boolean {
            return factory.isEnabled(feature)
        }
    }

    companion object {
        fun builder() = Builder(NBTMapper())

        fun builder(factory: NBTFactory) = Builder(NBTMapper(factory))
    }
}
