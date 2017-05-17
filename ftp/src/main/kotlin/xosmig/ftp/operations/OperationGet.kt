package xosmig.ftp.operations

import xosmig.ftp.Server
import xosmig.ftp.operations.Writer.Companion.writer
import xosmig.ftp.utils.toBuffer
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel

class OperationGet: Operation<ByteBuffer> {
    override fun response(server: Server, clientChannel: WritableByteChannel, command: String): Writer {
        val fileChannel = FileChannel.open(server.root.resolve(command))
        val fileLen: Long = fileChannel.size()
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

    override fun getResponse(inputChannel: ReadableByteChannel, tokenHandler: (ByteBuffer) -> Unit): Writer {
        val buf = ByteBuffer.allocate(1024 * 4)
        return writer {
            buf.clear()
            if (inputChannel.read(buf) == -1) {
                true
            } else {
                buf.flip()
                tokenHandler(buf)
                false
            }
        }
    }

    override val id: Int get() = 2
}
