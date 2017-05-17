package xosmig.ftp

import com.nhaarman.mockito_kotlin.mock
import org.junit.Test
import java.nio.channels.WritableByteChannel

class ServerTest: ServerTestFields() {

    @Test(timeout = 2000)
    fun testGetDoesntFail() {
        val (path, _) = createFile("subdir/my_file.txt", "Hello, World!")
        client.get(path, mock<WritableByteChannel> {})
    }

    @Test(timeout = 2000)
    fun testGetSimple() {
        val (path, _) = createFile("subdir/my_file.txt", "Hello, World!")
        assertGet(path)
    }

    @Test(timeout = 2000)
    fun testListSimple() {
        
    }
}
