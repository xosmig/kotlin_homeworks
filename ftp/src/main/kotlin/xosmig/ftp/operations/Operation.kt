package xosmig.ftp.operations

import xosmig.ftp.Server
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel
import kotlin.reflect.KClass

interface Operation<R: Any> {
    fun response(server: Server, clientChannel: WritableByteChannel, command: String): Writer
    fun request(serverChannel: WritableByteChannel, path: String): Writer
    fun getResponse(inputChannel: ReadableByteChannel): Reader<R>

    val clazz: KClass<R>
    val id: Int
}
