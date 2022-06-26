package com.dyescape.dataformat.nbt

import com.fasterxml.jackson.core.TSFBuilder

class NBTFactoryBuilder(base: NBTFactory? = null) : TSFBuilder<NBTFactory, NBTFactoryBuilder>(base) {
    override fun build(): NBTFactory {
        return NBTFactory(this)
    }
}
