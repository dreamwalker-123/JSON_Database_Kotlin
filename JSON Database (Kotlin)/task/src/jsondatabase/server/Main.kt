package jsondatabase.server

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.system.exitProcess

fun main() {
    val database = Database()
    val executor: ExecutorService = Executors.newCachedThreadPool()
    val serverAddress = "127.0.0.1"
    val serverPort = 23456
    try {
        ServerSocket(serverPort, 50, InetAddress.getByName(serverAddress)).use { server ->
            println("Server started!")
            while (true) {
                executor.submit(Session(server.accept(), server, executor, database))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

class Session(
    private val socket: Socket,
    private val server: ServerSocket,
    private val executor: ExecutorService,
    private val database: Database): Runnable {

    override fun run() {
        DataInputStream(socket.getInputStream()).use { input ->
            DataOutputStream(socket.getOutputStream()).use { output ->
                val request = Json.decodeFromString<Request>(input.readUTF())
                if (request.type == "exit") {
                    output.writeUTF(Json.encodeToString(Response("OK")))
                    server.close()
                    executor.shutdown()
                } else {
                    output.writeUTF(Json.encodeToString(database.query(request)))
                }
            }
        }
    }
}

@Serializable
data class Request(val type: String, var key: String = "", var value: String = "")

@Serializable
data class Response(val response: String, val value: String = "", val reason: String = "")

@Serializable
data class Table(val data: MutableMap<String, String> = mutableMapOf())

class Database {
    private val file = File("src/jsondatabase/server/data/db.json")
    private val errorMsg = "ERROR"
    private val successMsg = "OK"
    private val lock: ReadWriteLock = ReentrantReadWriteLock()
    private val readLock: Lock = lock.readLock()
    private val writeLock: Lock = lock.writeLock()

    fun query(request: Request): Response {
        return when (request.type) {
            "get" -> get(request.key)
            "set" -> writeFile(request.key, request.value)
            "delete" -> writeFile(request.key, delete = true)
            else -> Response(errorMsg, "Invalid command")
        }
    }

    fun get(key: String): Response {
        val table = readDbFile()
        return if (table.data.containsKey(key)) {
            Response(successMsg, value = table.data[key] ?: "")
        } else {
            Response(errorMsg, reason = "No such key")
        }
    }

    private fun readDbFile(): Table {
        return try {
            readLock.lock()
            val table = Json.decodeFromString<Table>(file.readText())
            readLock.unlock()
            table
        } catch (e:Exception) {
            val table = Table()
            file.writeText(Json.encodeToString(table))
            table
        }
    }

    private fun writeFile(key: String, value: String = "", delete: Boolean = false): Response {
        writeLock.lock()
        val table = readDbFile()
        if (delete) {
            if (!table.data.containsKey(key)) {
                writeLock.unlock()
                return Response(errorMsg, reason = "No such key")
            }
            table.data.remove(key)
        } else {
            table.data[key] = value
        }
        file.writeText(Json.encodeToString(table))
        writeLock.unlock()
        return Response(successMsg)
    }
}