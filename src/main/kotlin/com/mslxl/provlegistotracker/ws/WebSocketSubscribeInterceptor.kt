package com.mslxl.provlegistotracker.ws

import com.mslxl.provlegistotracker.util.RoomManager
import com.mslxl.provlegistotracker.util.SessionManager
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import java.util.*

@Component
class WebSocketSubscribeInterceptor(
    val roomManager: RoomManager,
    val sessionManager: SessionManager
) : HandshakeInterceptor {
    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val sessionUuid = try {
            request.uri.path.substringAfter("/session/")
                .let { UUID.fromString(it) }
        } catch (e: Exception) {
            // room id is illegal or not exists
            return false
        }

        val id = sessionManager.openSession(sessionUuid) ?: return false

        attributes["uuid"] = sessionUuid
        attributes["room"] = id
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