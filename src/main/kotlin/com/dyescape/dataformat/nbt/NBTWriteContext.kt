package com.dyescape.dataformat.nbt

import com.dyescape.dataformat.nbt.tag.NBTTagType
import com.fasterxml.jackson.core.JsonGenerationException
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonStreamContext
import com.fasterxml.jackson.core.json.DupDetector

class NBTWriteContext(
    private var type: NBTTagType,
    private val duplicateDetector: DupDetector?,
    private var value: Any?,
    private val expectedElements: Int? = null,
    private val parent: NBTWriteContext? = null,
    private val elementAccumulator: MutableList<Any?>? = null,
) : JsonStreamContext() {
    private var currentElements = 0
    private var currentFieldName: String? = null

    override fun getParent() = parent

    override fun getCurrentName() = currentFieldName

    override fun setCurrentValue(v: Any?) {
        value = v
    }

    override fun getCurrentValue(): Any? {
        return value
    }

    fun isRoot() = parent == null

    fun isAccumulatingElements() = elementAccumulator != null

    fun appendElement(element: Any?) {
        if (elementAccumulator == null) {
            error("Not accumulating elements")
        }

        elementAccumulator.add(element)
    }

    fun accumulatedElements() = elementAccumulator?.toList() ?: error("Not accumulating elements")

    fun childCompoundContext(value: Any?): NBTWriteContext {
        return NBTWriteContext(
            NBTTagType.COMPOUND,
            duplicateDetector,
            value,
            parent = this,
        )
    }

    fun childListContext(value: Any?, expectedElements: Int?): NBTWriteContext {
        return NBTWriteContext(
            NBTTagType.LIST,
            duplicateDetector,
            value,
            expectedElements,
            this,
        )
    }

    fun acceptsFieldName(name: String): Boolean {
        if (type != NBTTagType.COMPOUND) return false
        if (currentFieldName != null) return false

        if (duplicateDetector?.isDup(name) == true) {
            val source = duplicateDetector.source

            throw JsonGenerationException("Duplicate field $name", source as? JsonGenerator)
        }

        return true
    }

    fun markAsNamed(name: String) {
        currentFieldName = name
    }

    fun acceptsFurtherElements(): Boolean {
        return expectedElements == null || currentElements < expectedElements
    }

    fun isCompleted(): Boolean {
        return expectedElements == null || currentElements == expectedElements
    }

    fun acceptsValue(): Boolean {
        return when (type) {
            NBTTagType.COMPOUND -> currentFieldName != null
            else                -> true
        }
    }

    fun markAsValueWritten() {
        currentFieldName = null
        currentElements++
    }

    fun acceptsEnd(): Boolean {
        return when (type) {
            NBTTagType.COMPOUND -> currentFieldName == null
            else                -> true
        }
    }

    companion object {
        fun rootContext(duplicateDetector: DupDetector?): NBTWriteContext {
            return NBTWriteContext(
                NBTTagType.COMPOUND,
                duplicateDetector,
                null,
            )
        }
    }
}
