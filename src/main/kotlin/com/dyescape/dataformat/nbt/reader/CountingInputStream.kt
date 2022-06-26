package com.dyescape.dataformat.nbt.reader

import java.io.InputStream

class CountingInputStream(private val delegate: InputStream) : InputStream() {
    var checkpoint = 0L
        private set

    var read = 0L
        private set

    override fun read(): Int {
        val value = delegate.read()

        if (value >= 0) {
            read++
        }

        return value
    }

    fun markCheckpoint() {
        checkpoint = read
    }
}
