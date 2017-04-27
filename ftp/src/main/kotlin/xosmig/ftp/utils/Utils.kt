package xosmig.ftp.utils

import java.nio.ByteBuffer
import java.util.*

const val BYTES_IN_INT = java.lang.Integer.SIZE / java.lang.Byte.SIZE
const val BYTES_IN_LONG = java.lang.Long.SIZE / java.lang.Byte.SIZE

fun Int.toBuffer(): ByteBuffer = ByteBuffer.allocate(BYTES_IN_INT).putInt(this).apply { this.flip() }
fun Long.toBuffer(): ByteBuffer = ByteBuffer.allocate(BYTES_IN_LONG).putLong(this).apply { this.flip() }
fun String.toBuffer(): ByteBuffer = ByteBuffer.wrap(this.toByteArray())

fun ByteBuffer.getString(): String {
    val res = String(this.array(), this.position(), this.limit() - this.position())
    this.position(this.limit())
    return res
}

fun ByteBuffer.getBytes(): ByteArray = Arrays.copyOfRange(this.array(), this.position(), this.limit())

fun<T> Array<T>.swapElements(a: Int, b: Int) {
    val tmp = this[a]
    this[a] = this[b]
    this[b] = tmp
}
