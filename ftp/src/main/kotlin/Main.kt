import xosmig.ftp.Server
import java.nio.file.Paths

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.exit(2)
    }
    Server(Paths.get(args[0])).run()
}
