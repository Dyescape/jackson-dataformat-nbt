package com.dyescape.dataformat.nbt

import com.fasterxml.jackson.core.TSFBuilder

class NBTFactoryBuilder(
    base: NBTFactory? = null,
) : TSFBuilder<NBTFactory, NBTFactoryBuilder>(base),
    NBTFeature.Configurator<NBTFactoryBuilder> {
    internal var nbtFeatures = base?.nbtFeatures ?: NBTFeature.DEFAULTS
        private set

    override fun configure(feature: NBTFeature, state: Boolean): NBTFactoryBuilder {
        nbtFeatures = when (state) {
            true  -> nbtFeatures.with(feature)
            false -> nbtFeatures.without(feature)
        }

        return this
    }

    override fun isEnabled(feature: NBTFeature): Boolean {
        return nbtFeatures.isEnabled(feature)
    }

    override fun build(): NBTFactory {
        return NBTFactory(this)
    }
}
