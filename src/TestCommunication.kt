package demo

import client.InputHandler
import demo.client.OutputHandler
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Authenticator
import java.net.PasswordAuthentication
import java.net.ServerSocket
import java.util.*

const val username = "miniwolf_chat"
val password: String = "b8c2e45a-5f7d-4d7a-9980-ba50d1518e71"

fun main(args : Array<String>) {
    val auth = object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(username, password.toCharArray())
        }
    }
    Authenticator.setDefault(auth)

    setupUserInput()

    val server = setupConnection()
    var i = 0

    while (true) {
        setupNewConnection(server, i++)
    }
}

fun setupUserInput() {
    val inReader = BufferedReader(InputStreamReader(System.`in`))
    val outHandler = OutputHandler(inReader)
    Thread(outHandler).start()
}

var handlers = ArrayList<InputHandler>()

fun setupNewConnection(server: ServerSocket, id: Int) {
    val socket = server.accept()
    val handler = InputHandler(socket)
    handlers.add(handler)
    Thread(handler, "InputHandler - $id").start()
}

fun setupConnection(): ServerSocket {
    return ServerSocket(8080)
}

class NotConnected : java.lang.Exception()
