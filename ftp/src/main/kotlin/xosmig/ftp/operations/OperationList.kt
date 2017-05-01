package xosmig.ftp.operations

import xosmig.ftp.Server
import xosmig.ftp.operations.Writer.Companion.writer
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel
import java.nio.file.Files.isDirectory
import java.nio.file.Files.newDirectoryStream
import kotlin.reflect.KClass

class OperationList: Operation<String> {
    override fun response(server: Server, clientChannel: WritableByteChannel, command: String): Writer {
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
        return writer {
            clientChannel.write(buf)
            !buf.hasRemaining()
        }
    }

    override fun request(serverChannel: WritableByteChannel, path: String): Writer {
        TODO("list request")
    }

    override fun getResponse(inputChannel: ReadableByteChannel): Reader<String> {
        TODO("list get response")
    }

    override val clazz: KClass<String> get() = String::class
    override val id: Int get() = 1
}
