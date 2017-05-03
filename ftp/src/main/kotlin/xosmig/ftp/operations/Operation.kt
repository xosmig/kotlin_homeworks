package xosmig.ftp.operations

import xosmig.ftp.Server
import xosmig.ftp.utils.BYTES_IN_INT
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel

interface Operation<R> {

    fun response(server: Server, clientChannel: WritableByteChannel, command: String): Writer

    fun request(serverChannel: WritableByteChannel, path: String): Writer {
        val pathBytes = path.toByteArray()
        val buf = ByteBuffer.allocate(BYTES_IN_INT + pathBytes.size)
        buf.putInt(id)
        buf.put(pathBytes)
        buf.flip()
        return Writer.writer {
            serverChannel.write(buf)
            !buf.hasRemaining()
        }
    }

    fun getResponse(inputChannel: ReadableByteChannel, tokenHandler: (R) -> Unit): Writer

    val id: Int
}
