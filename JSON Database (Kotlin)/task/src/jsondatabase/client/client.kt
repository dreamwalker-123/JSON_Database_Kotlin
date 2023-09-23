package jsondatabase.client

import jsondatabase.server.Request
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.net.InetAddress
import java.net.Socket

fun main(args: Array<String>) {
    val request = buildRequest(args)
    val serverAddress = "127.0.0.1"
    val serverPort = 23456

    Socket(InetAddress.getByName(serverAddress), serverPort).use { socket ->
        println("Client started!")
        DataOutputStream(socket.getOutputStream()).use { output ->
            DataInputStream(socket.getInputStream()).use { input ->
                output.writeUTF(request)
                println("""Sent: $request""")
                println("Received: ${input.readUTF()}")
            }
        }
    }
}

fun buildRequest(args: Array<String>): String {
    if (args[0] == "-in") {
        val file = File("src/jsondatabase/client/data/${args[1]}")
        return file.readText()
    }

    val param = args.filter { !it.startsWith('-') }
    val type = param.getOrNull(0) ?: ""
    val key = param.getOrNull(1) ?: ""
    val value = param.getOrNull(2) ?: ""
    val request = Request(type)
    if (key.isNotEmpty()) request.key = key
    if (value.isNotEmpty()) request.value = value
    return Json.encodeToString(request)
}


class Value {
    var name: String? = null
    var car: Car? = null
    var rocket: Rocket? = null
}

class Rocket {
    var name: String? = null
    var launches: String? = null
}

class Car {
    var model: String? = null
    var year: String? = null
}