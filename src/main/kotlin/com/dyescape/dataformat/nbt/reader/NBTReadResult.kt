package com.dyescape.dataformat.nbt.reader

import com.fasterxml.jackson.core.JsonToken

data class NBTReadResult(
    val token: JsonToken?,
    val reader: NBTReader?,
)
