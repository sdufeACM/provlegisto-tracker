package com.mslxl.provlegistotracker.ws

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.ObjectMapper
import com.mslxl.provlegistotracker.util.RoomStorage
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Component
class WebSocketSubscribeHandler(
    val roomStorage: RoomStorage, val objectMapper: ObjectMapper
) : TextWebSocketHandler() {
    companion object {
        val scheduledExecutorService = Executors.newScheduledThreadPool(10)
        val topicSession = HashMap<String, LinkedHashSet<WebSocketSession>>()

        fun subscribeRoom(session: WebSocketSession, topic: String) {
            topicSession.getOrPut(topic) {
                LinkedHashSet()
            }.add(session)
        }

        fun unsubscribeRoom(session: WebSocketSession, topic: String) {
            val sessionSet = topicSession[topic] ?: return
            sessionSet.remove(session)
            if (sessionSet.isEmpty()) {
                topicSession.remove(topic)
            }
        }
    }

    fun WebSocketSession.sendJsonMessage(message: Response) {
        val content = objectMapper.writeValueAsString(message)
        sendMessage(TextMessage(content))
    }


    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    sealed class Request {
        @JsonTypeName("ping")
        data object Ping : Request()

        @JsonTypeName("pong")
        data object Pong : Request()

        @JsonTypeName("subscribe")
        data class Subscribe(val topics: List<String>) : Request()

        @JsonTypeName("unsubscribe")
        data class Unsubscribe(val topics: List<String>) : Request()

        @JsonTypeName("publish")
        data class Publish(val topic: String) : Request() {
            val otherField = HashMap<String, Any>()
                @JsonAnyGetter get

            @JsonAnySetter
            fun setOtherField(name: String, value: Any) {
                otherField[name] = value
            }
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    sealed class Response {
        @JsonTypeName("greet")
        data class Greet(
            @JsonIgnore val uuid: String, @JsonIgnore val username: String
        ) : Response() {
            val content get() = "hello from $uuid, $username"
        }

        @JsonTypeName("pong")
        data object Pong : Response()

        @JsonTypeName("ping")
        data object Ping : Response()

        @JsonTypeName("publish")
        data class Publish(val topic: String, val clients: Int, @JsonAnyGetter val other: Map<String, Any>) :
            Response()

    }


    var pongReceived = true
    lateinit var futurePing: ScheduledFuture<*>
    var subscribedTopics = LinkedHashSet<String>()
    override fun afterConnectionEstablished(session: WebSocketSession) {
        // init
        pongReceived = true
        val attr = session.attributes
        val uuid = attr["room"] as UUID
        roomStorage.inc(uuid)

        // greet and ping
        session.sendJsonMessage(
            Response.Greet(
                uuid.toString(), attr["username"].toString()
            )
        )
        futurePing = scheduledExecutorService.scheduleWithFixedDelay({
            if (pongReceived) {
                pongReceived = false
                session.sendJsonMessage(Response.Ping)
            } else {
                if (session.isOpen) {
                    session.close()
                }
            }
        }, 0, 30, TimeUnit.SECONDS)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val attr = session.attributes
        val uuid = attr["room"] as UUID
        futurePing.cancel(true)
        roomStorage.dec(uuid)
        // clear subscribe
        subscribedTopics.forEach {
            unsubscribeRoom(session, it)
        }
        subscribedTopics.clear()
    }


    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val request = objectMapper.readValue(message.payload, Request::class.java)
        val uuid = session.attributes["room"] as UUID
        when (request) {
            is Request.Ping -> session.sendJsonMessage(Response.Pong)
            is Request.Pong -> pongReceived = true
            is Request.Subscribe -> {
                request.topics.forEach {
                    subscribeRoom(session, it)
                    subscribedTopics.add(it)
                }
            }
            is Request.Unsubscribe -> {
                request.topics.forEach {
                    unsubscribeRoom(session, it)
                    subscribedTopics.add(it)
                }
            }
            is Request.Publish -> {
                println(message)
                topicSession[request.topic]?.let { sessions ->
                    val response = Response.Publish(request.topic, sessions.size, request.otherField)
                    sessions.asSequence()
                        .filter { it.isOpen }
                        .forEach { it.sendJsonMessage(response) }
                }
            }
        }
    }
}