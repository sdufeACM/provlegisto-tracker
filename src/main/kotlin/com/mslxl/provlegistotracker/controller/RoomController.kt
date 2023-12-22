package com.mslxl.provlegistotracker.controller

import com.mslxl.provlegistotracker.mapper.UserMapper
import com.mslxl.provlegistotracker.pojo.PResult
import com.mslxl.provlegistotracker.pojo.Room
import com.mslxl.provlegistotracker.pojo.RoomCreateRequest
import com.mslxl.provlegistotracker.util.RoomManager
import com.mslxl.provlegistotracker.util.SessionManager
import jakarta.servlet.http.HttpSession
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime
import java.util.*
import kotlin.random.Random

@RestController("/room")
class RoomController(
    val roomManager: RoomManager,
    val sessionManager: SessionManager,
    val userMapper: UserMapper
) {
    companion object {
        var roomCounter: Int = 0
    }

    @GetMapping("/room")
    fun listRoom(@RequestParam name: String?, @RequestParam page: Int?): PResult<List<Room>> {
        val page = page ?: 0
        val rooms = if (name == null) {
            roomManager.list(page)
        } else {
            roomManager.list(name, page)
        }
        return PResult.ok(rooms)
    }

    @PostMapping("/room")
    fun newRoom(@RequestBody room: RoomCreateRequest, session: HttpSession): PResult<String> {
        val uid = session.getAttribute("uid")
        if (uid == null && !room.allowAnonymous) {
            return PResult.err(PResult.ERROR_PROMPT, "You are not login, the limitation is available")
        }
        val uuid = UUID.randomUUID()
        val username =
            if (uid == null) "anonymous" + Random.nextInt(1, 1000) else session.getAttribute("username") as String
        session.setAttribute("room", uuid.toString())
        session.setAttribute("username", username)
        synchronized(RoomController.Companion) {
            val newRoom = Room(
                roomCounter,
                room.name,
                username,
                room.max,
                room.allowAnonymous,
                ZonedDateTime.now(),
                room.password
            )
            roomManager.createRoom(newRoom)
        }
        return PResult.ok(uuid.toString())
    }

    @GetMapping("/room/{id}")
    fun enterRoom(
        @PathVariable("id") id: String,
        @RequestParam("password") password: String?,
        httpSession: HttpSession,
    ): PResult<String> {
        val id = id.toIntOrNull() ?: return PResult.err(PResult.ERROR_PROMPT, "Illegal room id")
        val room = roomManager.getRoom(id) ?: return PResult.err(PResult.ERROR_PROMPT, "Room not exists")
        val uid = httpSession.getAttribute("uid")
        if (uid == null && !room.allowAnonymous) return PResult.err(
            PResult.ERROR_PROMPT,
            "Anonymous user are not allow to enter the room"
        )
        if (room.passwordRequired) {
            if (room.password != password) return PResult.err(PResult.ERROR_PROMPT, "Password not matched")
        }
        val uuid = sessionManager.createSession(id)
        return PResult.ok(uuid.toString())
    }
}