package com.dyescape.dataformat.nbt

import com.fasterxml.jackson.core.util.JacksonFeature
import com.fasterxml.jackson.core.util.JacksonFeatureSet

enum class NBTFeature(private val defaultState: Boolean) : JacksonFeature {
    /**
     * Whether to write the name of the root tag (normally an empty string) or not.
     *
     * This is used for NBT files, but not for sending NBT across the network.
     */
    INCLUDE_ROOT_TAG_NAME(true);

    private val mask = 1 shl ordinal

    override fun enabledByDefault() = defaultState

    override fun getMask() = mask

    override fun enabledIn(flags: Int) = (flags and mask) != 0

    interface Configurator<R> {
        fun configure(feature: NBTFeature, state: Boolean): R

        fun isEnabled(feature: NBTFeature): Boolean

        fun enable(feature: NBTFeature): R = configure(feature, true)

        fun disable(feature: NBTFeature): R = configure(feature, false)
    }

    companion object {
        val DEFAULTS = JacksonFeatureSet.fromDefaults(NBTFeature.entries.toTypedArray())
    }
}
