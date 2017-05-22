package xosmig.ftp.operations

import xosmig.ftp.Server
import xosmig.ftp.operations.Writer.Companion.writer
import xosmig.ftp.utils.getBytes
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel
import java.nio.file.Files.isDirectory
import java.nio.file.Files.newDirectoryStream

class OperationList: Operation<List<String>> {
    override fun response(server: Server, clientChannel: WritableByteChannel, command: String): Writer {
        val children = newDirectoryStream(server.root.resolve(command)).toList()
        ByteArrayOutputStream().use { content ->
            ObjectOutputStream(content).use { objectStream ->
                objectStream.writeInt(children.size)
                for (child in children) {
                    objectStream.writeObject(child.fileName)
                    objectStream.writeBoolean(isDirectory(child))
                }
            }
            val buf = ByteBuffer.wrap(content.toByteArray())
            return writer {
                clientChannel.write(buf)
                !buf.hasRemaining()
            }
        }
    }

    override fun getResponse(inputChannel: ReadableByteChannel, tokenHandler: (List<String>) -> Unit): Writer {
        val buf = ByteBuffer.allocate(1024 * 8)
        return writer {
            if (inputChannel.read(buf) == -1) {
                buf.flip()
                val bytes = buf.getBytes()
                ByteArrayInputStream(bytes).use { content ->
                    ObjectInputStream(content).use { objectStream ->
                        val count = objectStream.readInt()
                        val res = ArrayList<String>(count)
                        for (i in 1..count) {
                            res.add(objectStream.readObject() as String)
                        }
                        assert(objectStream.available() == 0)
                        tokenHandler(res)
                    }
                }
                true
            } else {
                false
            }
        }
    }

    override val id: Int get() = 1
}
