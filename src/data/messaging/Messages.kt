package demo.data.messaging

import java.util.*

open class Message(open val type: String = "InvalidMessage")

data class outMessage(val body: String, val to: String, val type: String, val chatId: String)

data class InTextMessage(val chatId: String, val id: String, val type: String, val from: String,
                         val participants: Array<String>, val body: String, val timestamp: Long,
                         val readReceiptRequested: Boolean, val mentions: String?, val chatType: String)

data class inPictureMessage(val chatId: String, val type: String, val from: String,
                            val participants: Array<String>, val id: String, val picUrl: String, val timestamp: Long,
                            val readReceiptRequested: Boolean, val mentions: String?, val chatType: String)

data class outReadReceipt(val chatId: String, val type: String, val to: String, val id: String, val messageIds: Array<String>)

data class InReadReceipt(val chatId: String, val id: String, val type: String, val from: String,
                         val participants: Array<String>, val timestamp: Long, val isTyping: Boolean,
                         val readReceiptRequested: Boolean, val mentions: Any?, val metadata: Any?,
                         val chatType: String)

data class Messages(val messages: Array<Message>)
data class InReadReceiptMessage(val messages: Array<InReadReceipt>)
data class InTextMessageMessage(val messages: Array<InTextMessage>)


fun readReceiptFromTextMessage(textMessage: InTextMessage) : outReadReceipt {
    return outReadReceipt(textMessage.chatId, "read-receipt", textMessage.from, UUID.randomUUID().toString(),
        arrayOf(textMessage.id))
}

fun readReceiptFromPictureMessage(pictureMessage: inPictureMessage) : outReadReceipt {
    return outReadReceipt(pictureMessage.chatId, "read-receipt", pictureMessage.from, UUID.randomUUID().toString(),
        arrayOf(pictureMessage.id))
}