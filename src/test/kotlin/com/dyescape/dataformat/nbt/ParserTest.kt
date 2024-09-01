package com.dyescape.dataformat.nbt

import com.dyescape.dataformat.nbt.test.*
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("NBT Parser")
class ParserTest {
    @Test
    @DisplayName("parse simple compound")
    fun simple() {
        val input = nbtFixture("simple")
        val mapper = createTestMapper()

        val result = mapper.readValue<Simple>(input)

        assertEquals(simpleFixtureValue, result)
    }


    @Test
    @DisplayName("parse every possible tag type")
    fun bigTest() {
        val input = nbtFixture("bigtest")
        val mapper = createTestMapper()

        val result = mapper.readValue<BigTest>(input)

        assertEquals(bigTestFixtureValue, result)
    }

    @Test
    @DisplayName("parse simple compound (network)")
    fun simpleNetwork() {
        val input = nbtFixture("simple_network")
        val mapper = createTestMapper().disable(NBTFeature.INCLUDE_ROOT_TAG_NAME)

        val result = mapper.readValue<Simple>(input)

        assertEquals(simpleFixtureValue, result)
    }
}
