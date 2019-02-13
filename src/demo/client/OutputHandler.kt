package demo.client

import java.io.BufferedReader
import java.io.DataOutputStream
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class OutputHandler(private val inReader: BufferedReader) : Runnable {
    private var running: Boolean = true
    private var builder: StringBuilder = StringBuilder()

    override fun run() {
        pushToChat("https://api.kik.com/v1/config", """
{
    "webhook": "http://297df90b.ngrok.io",
    "features": {
        "receiveReadReceipts": true,
        "receiveIsTyping": true,
        "manuallySendReadReceipts": true,
        "receiveDeliveryReceipts": true
    }
}
""".trimIndent())
        while (running) {
            if (inReader.ready()) {
                handleInput(inReader.readLine())
            } else {
                Thread.sleep(100)
            }
        }
    }

    private fun handleInput(readLine: String?) {
        if (readLine == null) {
            return
        }

        if (readLine == "exit") {
            stop()
        }

        // If shift?
        if (readLine == "/send") {
            //outMessage(builder.toString(), )
        } else {

        }
    }

    private fun stop() {
        running = false
    }
}

fun pushToChat(serverURL: String, message: String) {
    val url = URL(serverURL)
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.connectTimeout = 300000
    connection.doOutput = true

    val postData: ByteArray = message.toByteArray(StandardCharsets.UTF_8)

    connection.setRequestProperty("charset", "utf-8")
    connection.setRequestProperty("Content-length", postData.size.toString())
    connection.setRequestProperty("Content-Type", "application/json")

    try {
        val outputStream = DataOutputStream(connection.outputStream)
        outputStream.write(postData)
        outputStream.flush()
    } catch (exception: Exception) {
        println(exception.message)
    }

    when (connection.responseCode) {
        HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED -> {
            println("Send correctly")
            println(connection.responseMessage)
        }
        else -> println("There was error while connecting the chat")
    }
}
