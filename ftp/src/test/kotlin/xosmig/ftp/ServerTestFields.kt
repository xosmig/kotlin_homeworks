package xosmig.ftp

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import xosmig.ftp.utils.getBytes
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.SocketChannel
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Files.readAllBytes
import java.nio.file.Path

abstract class ServerTestFields {

    val fs: FileSystem = Jimfs.newFileSystem(Configuration.unix())
    val rootDirName = "root"
    val root: Path = fs.getPath("/$rootDirName")
    val serverThread = Thread(Server(root))
    val client = Client()

    @Before
    fun initServer() {
        Files.createDirectories(root)
        serverThread.start()
        Thread.sleep(500)
    }

    @After
    fun stopServer() {
        serverThread.interrupt()
        Thread.sleep(500)
    }

    fun connect(): SocketChannel = SocketChannel.open().apply {
        this.connect(InetSocketAddress("localhost", Server.PORT))
    }

    fun createFile(relativePath: String, content: String = ""): Pair<String, Path> {
        val path = root.resolve(relativePath)
        Files.createDirectories(path.parent)
        Files.write(path, content.toByteArray())
        return Pair(relativePath, path)
    }

    fun assertGet(path: String) {
        val resultBuf = ByteArrayOutputStream().use { byteStream ->
            Channels.newChannel(byteStream).use { outputChannel ->
                client.get(path, outputChannel)
            }
            ByteBuffer.wrap(byteStream.toByteArray())
        }

        val size = resultBuf.getLong().toInt()
        val content = resultBuf.getBytes()
        assertEquals(size, content.size)
        assertArrayEquals(readAllBytes(root.resolve(path)), content)
    }

    /*fun assertList(path: String) {
        connect().use { channel ->
            OperationList().request(path).complete(channel)
            channel.shutdownOutput()
            val resultBuf = ByteBuffer.wrap(OperationList().getFullResponse(channel))
            val childrenCount = resultBuf.getInt()

        }
    }*/
}
