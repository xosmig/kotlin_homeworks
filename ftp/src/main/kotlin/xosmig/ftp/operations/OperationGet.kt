package xosmig.ftp.operations

import xosmig.ftp.Server
import xosmig.ftp.operations.Writer.Companion.writer
import xosmig.ftp.utils.BYTES_IN_INT
import xosmig.ftp.utils.swapElements
import xosmig.ftp.utils.toBuffer
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel

class OperationGet: Operation {
    override fun response(server: Server, command: String): Writer {
        val fileChannel = FileChannel.open(server.root.resolve(command))
        val fileLen = fileChannel.size()
        val sizeBuf = fileLen.toBuffer()
        var filePos: Long = 0

        return writer { clientChannel ->
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

    override fun request(path: String): Writer {
        val pathBytes = path.toByteArray()
        val buf = ByteBuffer.allocate(BYTES_IN_INT + pathBytes.size)
        buf.putInt(id)
        buf.put(pathBytes)
        buf.flip()
        return writer { channel ->
            channel.write(buf)
            !buf.hasRemaining()
        }
    }

    /*override fun getResponse(channel: ReadableByteChannel): Writer {
        val buf = ByteBuffer.allocate(1024 * 4)
        return writer { outputChannel ->
            buf.clear()
            if (channel.read(buf) == -1) {
                return@writer true
            }
            buf.flip()
            outputChannel.write(buf)
            false
        }
    }*/

    override val id: Int get() = 2
}
