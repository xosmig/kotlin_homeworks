package xosmig.ftp

import xosmig.ftp.operations.OperationGet
import xosmig.ftp.operations.OperationList
import xosmig.ftp.operations.Writer
import xosmig.ftp.utils.getString
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.*
import java.nio.file.Path

class Server(val root: Path): Runnable {

    companion object {
        const val PORT: Int = 9365
        const val MAX_REQ_SIZE: Int = 1024
        val OPERATIONS = arrayOf(
                OperationList(),
                OperationGet()
        )
    }

    override fun run() {
        val selector = Selector.open()

        run {
            val serverChannel = ServerSocketChannel.open()
            serverChannel.bind(InetSocketAddress(PORT))
            serverChannel.configureBlocking(false)
            serverChannel.register(selector, SelectionKey.OP_ACCEPT)
        }

        while (true) {
            selector.select()
            if (Thread.interrupted()) {
                for (key in selector.keys()) {
                    key.cancel()
                    key.channel().close()
                }
                return
            }
            val selectedKeys = selector.selectedKeys()

            for (key in selectedKeys) {
                try {
                    key.channel().use { channel ->
                        when {
                            key.isAcceptable -> {
                                val clientChannel = (channel as ServerSocketChannel).accept()
                                clientChannel.configureBlocking(false)
                                val buffer = ByteBuffer.allocate(MAX_REQ_SIZE)
                                clientChannel.register(selector, SelectionKey.OP_READ).attach(buffer)
                            }

                            key.isReadable -> {
                                val buffer = key.attachment() as ByteBuffer
                                channel as SocketChannel

                                if (channel.read(buffer) == -1 && buffer.position() != 0) {
                                    buffer.flip()
                                    val type = buffer.getInt()
                                    val command = buffer.getString().trim()
                                    val writer = OPERATIONS
                                            .filter { type == it.id }
                                            .first()
                                            .response(this@Server, channel, command)
                                    channel.register(selector, SelectionKey.OP_WRITE, writer)
                                }
                            }

                            key.isWritable -> {
                                val writer = key.attachment() as Writer
                                if (writer.write()) {
                                    key.cancel()
                                    key.channel().close()
                                }
                            }
                        }
                        run { /* just to turn the when expression to an statement */ }
                    }
                } catch (e: Exception) {
                    System.err.println("Exception while working with a client")
                    e.printStackTrace()
                }
            }
            selectedKeys.clear()
        }
    }
}
