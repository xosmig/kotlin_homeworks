package xosmig.ftp

import xosmig.ftp.operations.Operation
import xosmig.ftp.operations.OperationGet
import xosmig.ftp.operations.Reader
import xosmig.ftp.operations.Writer
import java.io.OutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.nio.channels.WritableByteChannel
import java.util.function.Consumer
import kotlin.reflect.KClass

class Client(val address: String = "localhost") {
    private fun connect(): SocketChannel = SocketChannel.open().apply {
        this.connect(InetSocketAddress("localhost", Server.PORT))
    }

    private fun<R: Any> perform(path: String, operation: Operation<R>, consumer: Consumer<R>) {
        val selector = Selector.open()

        fun dispose() {
            for (key in selector.keys()) {
                key.cancel()
                key.channel().close()
            }
        }

        fun connected(channel: SocketChannel) {
            channel.register(selector, SelectionKey.OP_WRITE, operation.request(channel, path))
        }

        SocketChannel.open().use { channel ->
            channel.configureBlocking(false)
            if (channel.connect(InetSocketAddress(address, Server.PORT))) {
                connected(channel)
            } else {
                channel.register(selector, SelectionKey.OP_CONNECT)
            }
        }

        while (true) {
            selector.select()
            if (Thread.interrupted()) {
                dispose()
                throw InterruptedException()
            }

            val selectedKeys = selector.selectedKeys()

            for (key in selectedKeys) {
                (key.channel() as SocketChannel).use { channel ->
                    when {
                        key.isWritable -> {
                            if ((key.attachment() as Writer).write()) {
                                channel.register(selector, SelectionKey.OP_READ, operation.getResponse(channel))
                            }
                        }

                        key.isReadable -> {
                            val res = (key.attachment() as Reader<*>).read()
                            operation.clazz.isInstance(res.token)
                            @Suppress("UNCHECKED_CAST")
                            consumer.accept(res.token as R)
                            if (res.finished) {
                                dispose()
                                return@perform
                            }
                        }

                        key.isConnectable -> {
                            if (channel.finishConnect()) {
                                connected(channel)
                            } else {
                                assert(false) { "unreachable" }
                            }
                        }
                    }
                }
            }
        }
    }

    fun get(path: String, output: WritableByteChannel) {
        perform(path, OperationGet(), Consumer { buffer ->
            output.write(buffer)
        })
    }
}

