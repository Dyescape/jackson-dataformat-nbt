package com.dyescape.dataformat.nbt.test

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.InputStream

fun nbtFixture(name: String): InputStream {
    return Thread.currentThread().contextClassLoader.getResourceAsStream("fixtures/$name.nbt") ?: run {
        error("Unknown fixture: $name")
    }
}

val simpleFixtureValue = Simple("Hello World!")

data class Simple(
    val test: String,
)

// from https://wiki.vg/NBT#bigtest.nbt
val bigTestFixtureValue = BigTest(
    32767,
    9223372036854775807,
    127,
    ByteArray(1000) { ((it * it * 255 + it * 7) % 100).toByte() },
    listOf(11L, 12L, 13L, 14L, 15L),
    0.49823147f,
    0.4931287132182315,
    2147483647,
    listOf(
        BigTest.CompoundListTest(1264099775885, "Compound tag #0"),
        BigTest.CompoundListTest(1264099775885, "Compound tag #1"),
    ),
    buildMap {
        put("egg", BigTest.NestedCompound("Eggbert", 0.5f))
        put("ham", BigTest.NestedCompound("Hampus", 0.75f))
    },
    "HELLO WORLD THIS IS A TEST STRING ÅÄÖ!",
)

data class BigTest(
    val shortTest: Short,
    val longTest: Long,
    val byteTest: Byte,
    @JsonProperty("byteArrayTest (the first 1000 values of (n*n*255+n*7)%100, starting with n=0 (0, 62, 34, 16, 8, ...))")
    val byteArrayTest: ByteArray,
    @JsonProperty("listTest (long)")
    val longListTest: List<Long>,
    val floatTest: Float,
    val doubleTest: Double,
    val intTest: Int,
    @JsonProperty("listTest (compound)")
    val compoundListTest: List<CompoundListTest>,
    @JsonProperty("nested compound test")
    val nestedCompounds: Map<String, NestedCompound>,
    val stringTest: String,
) {
    data class CompoundListTest(
        @JsonProperty("created-on")
        val createdOn: Long,
        val name: String,
    )

    data class NestedCompound(
        val name: String,
        val value: Float,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BigTest) return false

        if (shortTest != other.shortTest) return false
        if (longTest != other.longTest) return false
        if (byteTest != other.byteTest) return false
        if (!byteArrayTest.contentEquals(other.byteArrayTest)) return false
        if (longListTest != other.longListTest) return false
        if (floatTest != other.floatTest) return false
        if (doubleTest != other.doubleTest) return false
        if (intTest != other.intTest) return false
        if (compoundListTest != other.compoundListTest) return false
        if (nestedCompounds != other.nestedCompounds) return false
        if (stringTest != other.stringTest) return false

        return true
    }

    override fun hashCode(): Int {
        var result = shortTest.toInt()
        result = 31 * result + longTest.hashCode()
        result = 31 * result + byteTest
        result = 31 * result + byteArrayTest.contentHashCode()
        result = 31 * result + longListTest.hashCode()
        result = 31 * result + floatTest.hashCode()
        result = 31 * result + doubleTest.hashCode()
        result = 31 * result + intTest
        result = 31 * result + compoundListTest.hashCode()
        result = 31 * result + nestedCompounds.hashCode()
        result = 31 * result + stringTest.hashCode()
        return result
    }
}
