package com.mslxl.provlegistotracker.ws

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.mslxl.provlegistotracker.util.RoomManager
import com.mslxl.provlegistotracker.util.SessionManager
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Component
class WebSocketSubscribeHandler(
    val roomManager: RoomManager,
    val objectMapper: ObjectMapper,
    val sessionManager: SessionManager
) : TextWebSocketHandler() {
    companion object {
        val scheduledExecutorService: ScheduledExecutorService = Executors.newScheduledThreadPool(5)
        val topicSession = HashMap<String, LinkedHashSet<WebSocketSession>>()

        fun subscribeTopic(session: WebSocketSession, topic: String, room: Int) {
            val tag = "$room-$topic"
            topicSession.getOrPut(tag) {
                LinkedHashSet()
            }.add(session)
        }

        fun unsubscribeTopic(session: WebSocketSession, topic: String, room: Int) {
            val tag = "$room-$topic"
            val sessionSet = topicSession[tag] ?: return
            sessionSet.remove(session)
            if (sessionSet.isEmpty()) {
                topicSession.remove(tag)
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
        val roomId = attr["room"] as Int

        // greet and ping
        session.sendJsonMessage(
            Response.Greet(
                roomId.toString(), attr["username"].toString()
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
        val roomId = attr["room"] as Int
        val sessionUuid = attr["uuid"] as UUID
        futurePing.cancel(true)
        // clear subscribe
        subscribedTopics.forEach {
            unsubscribeTopic(session, it, roomId)
        }
        subscribedTopics.clear()
        sessionManager.closeSession(sessionUuid)
    }


    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val request = objectMapper.readValue(message.payload, Request::class.java)
        val roomId = session.attributes["room"] as Int
        when (request) {
            is Request.Ping -> session.sendJsonMessage(Response.Pong)
            is Request.Pong -> pongReceived = true
            is Request.Subscribe -> {
                request.topics.forEach {
                    subscribeTopic(session, it, roomId)
                    subscribedTopics.add(it)
                }
            }

            is Request.Unsubscribe -> {
                request.topics.forEach {
                    unsubscribeTopic(session, it, roomId)
                    subscribedTopics.add(it)
                }
            }

            is Request.Publish -> {
                println(message)
                val tag = "$roomId-${request.topic}"
                topicSession[tag]?.let { sessions ->
                    val response = Response.Publish(request.topic, sessions.size, request.otherField)
                    sessions.asSequence()
                        .filter { it.isOpen }
                        .forEach { it.sendJsonMessage(response) }
                }
            }
        }
    }
}