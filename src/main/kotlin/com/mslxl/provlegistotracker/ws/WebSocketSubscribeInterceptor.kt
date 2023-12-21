package com.mslxl.provlegistotracker.ws

import com.mslxl.provlegistotracker.util.RoomStorage
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import java.util.UUID
import kotlin.random.Random

@Component
class WebSocketSubscribeInterceptor(
    val roomStorage: RoomStorage
) : HandshakeInterceptor {
    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val roomUuid = try {
            request.uri.path.substringAfter("/room/")
                .let { UUID.fromString(it) }
        } catch (e: Exception) {
            // room id is illegal or not exists
            return false
        }

        val room = roomStorage.getRoom(roomUuid) ?: return false

        if (!attributes.containsKey("uid")) {
            if (!room.allowAnonymous) {
                // not login while the room not allow anonymous
                return false
            } else {
                attributes["username"] = "anonymous" + Random.nextInt(1, 1000)
            }
        }

        if (room.passwordRequired) {
            val inpPassword = request.headers["password"]?.firstOrNull()
            if (room.password != inpPassword) {
                // wrong password
                return false
            }
        }

        attributes["room"] = roomUuid

        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
    }
}