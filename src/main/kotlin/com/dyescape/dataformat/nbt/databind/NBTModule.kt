package com.dyescape.dataformat.nbt.databind

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.cfg.PackageVersion
import com.fasterxml.jackson.databind.module.SimpleSerializers

class NBTModule : Module() {
    override fun version(): Version {
        return PackageVersion.VERSION
    }

    override fun getModuleName(): String {
        return "NBT"
    }

    override fun setupModule(context: SetupContext) {
        val serializers = SimpleSerializers()

        serializers.addSerializer(NBTByteSerializer(true))
        serializers.addSerializer(NBTByteSerializer(false))
        serializers.addSerializer(NBTShortSerializer(true))
        serializers.addSerializer(NBTShortSerializer(false))
        serializers.addSerializer(NBTByteArraySerializer)
        serializers.addSerializer(NBTIntArraySerializer)
        serializers.addSerializer(NBTLongArraySerializer)

        context.addSerializers(serializers)
    }
}
