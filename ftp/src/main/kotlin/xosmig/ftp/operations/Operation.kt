package xosmig.ftp.operations

import xosmig.ftp.Server

interface Operation {
    fun response(server: Server, command: String): Writer
    fun request(path: String): Writer
//    fun getResponse(channel: ReadableByteChannel): Writer
    val id: Int
}
