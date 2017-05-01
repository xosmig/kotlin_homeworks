package xosmig.ftp.operations

@FunctionalInterface
interface Reader<out T> {
    companion object {
        fun<T> reader(readImpl: () -> Result<T>) = object : Reader<T> {
            override fun read() = readImpl()
        }
    }

    data class Result<out T>(val finished: Boolean, val token: T?)

    fun read(): Result<T>
}
