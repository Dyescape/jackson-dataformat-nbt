package com.dyescape.dataformat.nbt.writer

import com.dyescape.dataformat.nbt.NBTFeature
import com.dyescape.dataformat.nbt.tag.NBTTagType
import com.dyescape.dataformat.nbt.tag.NBTValue
import com.fasterxml.jackson.core.json.DupDetector
import com.fasterxml.jackson.core.util.JacksonFeatureSet
import java.io.DataOutputStream
import java.io.OutputStream

abstract class NBTWriter(
    private val parent: NBTWriter?,
    protected val output: DataOutputStream,
) {
    abstract fun start()

    abstract fun acceptsName(): Boolean

    abstract fun acceptsValue(): Boolean

    abstract fun acceptsFurtherElements(): Boolean

    abstract fun writeName(name: String)

    abstract fun writePrimitive(value: NBTValue)

    abstract fun skipValue()

    abstract fun startList(size: Int? = null, type: NBTTagType? = null): NBTWriter

    abstract fun startCompound(): NBTWriter

    fun end(): NBTWriter? {
        writeEnd()

        return parent
    }

    protected abstract fun writeEnd()

    fun flush() {
        output.flush()
    }

    companion object {
        fun root(
            feature: JacksonFeatureSet<NBTFeature>,
            output: OutputStream,
            duplicateDetector: DupDetector?,
        ): NBTWriter {
            return RootCompoundNBTWriter(
                feature.isEnabled(NBTFeature.INCLUDE_ROOT_TAG_NAME),
                DataOutputStream(output),
                duplicateDetector,
            )
        }
    }
}
