package com.dyescape.dataformat.nbt

import com.dyescape.dataformat.nbt.test.bigTestFixtureValue
import com.dyescape.dataformat.nbt.test.createTestMapper
import com.dyescape.dataformat.nbt.test.nbtFixture
import com.dyescape.dataformat.nbt.test.simpleFixtureValue
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("NBT Generator")
class GeneratorTest {
    @Test
    @DisplayName("write simple compound")
    fun simple() {
        val mapper = createTestMapper()
        val result = mapper.writeValueAsBytes(simpleFixtureValue)
        val expected = nbtFixture("simple").readAllBytes()

        assertArrayEquals(expected, result)
    }

    @Test
    @DisplayName("write every possible tag type")
    fun bigTest() {
        val mapper = createTestMapper()
        val result = mapper.writeValueAsBytes(bigTestFixtureValue)
        val expected = nbtFixture("bigtest").readAllBytes()

        assertArrayEquals(expected, result)
    }
}
