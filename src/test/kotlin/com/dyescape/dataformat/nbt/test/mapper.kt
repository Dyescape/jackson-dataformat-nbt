package com.dyescape.dataformat.nbt.test

import com.dyescape.dataformat.nbt.databind.NBTMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

fun createTestMapper(): NBTMapper {
    val mapper = NBTMapper()

    mapper.registerKotlinModule()

    return mapper
}
