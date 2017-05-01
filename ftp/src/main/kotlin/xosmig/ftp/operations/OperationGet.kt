package xosmig.ftp.operations

import xosmig.ftp.Server
import xosmig.ftp.operations.Reader.Companion.reader
import xosmig.ftp.operations.Writer.Companion.writer
import xosmig.ftp.utils.BYTES_IN_INT
import xosmig.ftp.utils.toBuffer
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel
import kotlin.reflect.KClass

class OperationGet: Operation<ByteBuffer> {
    override fun response(server: Server, clientChannel: WritableByteChannel, command: String): Writer {
        val fileChannel = FileChannel.open(server.root.resolve(command))
        val fileLen = fileChannel.size()
        val sizeBuf = fileLen.toBuffer()
        var filePos: Long = 0

        return writer {
            if (sizeBuf.hasRemaining()) {
                clientChannel.write(sizeBuf)
                false
            } else {
                val count = fileChannel.transferTo(filePos, fileLen - filePos, clientChannel)
                filePos += count
                filePos == fileLen
            }
        }
    }

    override fun request(serverChannel: WritableByteChannel, path: String): Writer {
        val pathBytes = path.toByteArray()
        val buf = ByteBuffer.allocate(BYTES_IN_INT + pathBytes.size)
        buf.putInt(id)
        buf.put(pathBytes)
        buf.flip()
        return writer {
            serverChannel.write(buf)
            !buf.hasRemaining()
        }
    }

    override fun getResponse(inputChannel: ReadableByteChannel): Reader<ByteBuffer> {
        val buf = ByteBuffer.allocate(1024 * 4)
        return reader {
            buf.clear()
            if (inputChannel.read(buf) == -1) {
                Reader.Result(true, null)
            } else {
                buf.flip()
                Reader.Result(false, buf)
            }
        }
    }

    override val clazz: KClass<ByteBuffer> get() = ByteBuffer::class
    override val id: Int get() = 2
}
