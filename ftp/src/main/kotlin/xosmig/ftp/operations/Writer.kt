package xosmig.ftp.operations

import java.nio.channels.WritableByteChannel

interface Writer {
    companion object {
        fun writer(lambda: (WritableByteChannel) -> Boolean) = object : Writer {
            override fun write(channel: WritableByteChannel) = lambda(channel)
        }
    }

    fun write(channel: WritableByteChannel): Boolean

    fun complete(channel: WritableByteChannel) {
        while (!write(channel)) { }
    }
}
