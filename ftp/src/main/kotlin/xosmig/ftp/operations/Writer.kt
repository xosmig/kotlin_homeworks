package xosmig.ftp.operations

@FunctionalInterface
interface Writer {
    companion object {
        fun writer(writeImpl: () -> Boolean) = object : Writer {
            override fun write() = writeImpl()
        }
    }

    fun write(): Boolean
}
