package com.dyescape.dataformat.nbt.tag

enum class NBTTagType(val id: Int) {
    END(0),
    BYTE(1),
    SHORT(2),
    INT(3),
    LONG(4),
    FLOAT(5),
    DOUBLE(6),
    BYTE_ARRAY(7),
    STRING(8),
    LIST(9),
    COMPOUND(10),
    INT_ARRAY(11),
    LONG_ARRAY(12);

    companion object {
        private val mapping = entries.associateBy { it.id }

        fun byId(id: Int) = mapping[id] ?: error("Unknown NBT tag id: $id")

        fun byIdOrNull(id: Int) = mapping[id]
    }
}
