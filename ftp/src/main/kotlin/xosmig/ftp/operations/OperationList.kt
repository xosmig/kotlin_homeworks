package xosmig.ftp.operations

import xosmig.ftp.Server
import xosmig.ftp.operations.Writer.Companion.writer
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel
import java.nio.file.Files.isDirectory
import java.nio.file.Files.newDirectoryStream

class OperationList: Operation {
    override fun response(server: Server, command: String): Writer {
        val children = newDirectoryStream(server.root.resolve(command)).toList()
        val content = ByteArrayOutputStream()
        val objectStream = ObjectOutputStream(content)

        objectStream.writeInt(children.size)
        for (child in children) {
            objectStream.writeObject(child.fileName)
            objectStream.writeBoolean(isDirectory(child))
        }

        objectStream.flush()
        val buf = ByteBuffer.wrap(content.toByteArray())
        return writer { clientChannel ->
            clientChannel.write(buf)
            !buf.hasRemaining()
        }
    }

    override fun request(path: String): Writer {
        TODO("list request")
    }

    /*override fun getResponse(channel: ReadableByteChannel): Writer {
        TODO("list get response")
    }*/

    override val id: Int get() = 1
}
