package xosmig.ftp

import xosmig.ftp.operations.Operation
import xosmig.ftp.operations.OperationGet
import xosmig.ftp.operations.Writer
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey.*
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.nio.channels.WritableByteChannel

class Client(val address: String = "localhost") {

    private fun<R> perform(path: String, operation: Operation<R>, tokenHandler: (R) -> Unit) {
        val selector = Selector.open()

        fun dispose() {
            for (key in selector.keys()) {
                key.channel().close()
            }
        }

        fun connected(channel: SocketChannel) {
            channel.register(selector, OP_WRITE, operation.request(channel, path))
        }

        run {
            val channel = SocketChannel.open()
            channel.configureBlocking(false)
            if (channel.connect(InetSocketAddress(address, Server.PORT))) {
                connected(channel)
            } else {
                channel.register(selector, OP_CONNECT)
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
                if (!key.isValid) {
                    continue
                }

                val channel = key.channel() as SocketChannel
                when {
                    key.isWritable -> {
                        if ((key.attachment() as Writer).write()) {
                            channel.shutdownOutput()
                            channel.register(selector, OP_READ, operation.getResponse(channel, tokenHandler))
                        }
                    }

                    key.isReadable -> {
                        if ((key.attachment() as Writer).write()) {
                            dispose()
                            return@perform
                        }
                    }

                    key.isConnectable -> {
                        try {
                            if (channel.finishConnect()) {
                                connected(channel)
                            }
                        } catch (e: IOException) {
                            TODO("CONNECTION FAILURE")
                        }
                    }
                }
            }
        }
    }

    fun get(path: String, output: WritableByteChannel) {
        perform(path, OperationGet()) { buffer ->
            output.write(buffer)
        }
    }
}
