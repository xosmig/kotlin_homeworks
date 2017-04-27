package xosmig.ftp

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import xosmig.ftp.operations.OperationGet
import xosmig.ftp.operations.OperationList
import xosmig.ftp.utils.getBytes
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.file.FileSystem
import java.nio.file.Files.*
import java.nio.file.Path

class ServerTest {
    val fs: FileSystem = Jimfs.newFileSystem(Configuration.unix())
    val rootDirName = "root"
    val root: Path = fs.getPath("/$rootDirName")
    val serverThread = Thread(Server(root))

    @Before
    fun initServer() {
        createDirectories(root)
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
        createDirectories(path.parent)
        write(path, content.toByteArray())
        return Pair(relativePath, path)
    }

    fun assertGet(path: String) {
        connect().use { channel ->
            OperationGet().request(path).complete(channel)
            channel.shutdownOutput()
            val buf = ByteBuffer.allocate(2048)
            while (channel.read(buf) != -1) {}
            buf.flip()
            val size = buf.getLong().toInt()
            val content = buf.getBytes()
            assertEquals(size, content.size)
            assertArrayEquals(readAllBytes(root.resolve(path)), content)
        }
    }

    fun assertList(path: String) {
        connect().use { channel ->
            OperationList().request(path).complete(channel)
            channel.shutdownOutput()
            val buf = ByteBuffer.allocate(2048)
            while (channel.read(buf) != -1) {}
            buf.flip()
            val count = buf.getInt()
        }
    }

    @Test
    fun testGetSimple() {
        val (path) = createFile("subdir/my_file.txt", "Hello, World!")
        assertGet(path)
    }

    @Test
    fun testListSimple() {
    }
}
