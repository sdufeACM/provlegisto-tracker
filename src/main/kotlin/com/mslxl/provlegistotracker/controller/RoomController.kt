package com.mslxl.provlegistotracker.controller

import com.mslxl.provlegistotracker.mapper.UserMapper
import com.mslxl.provlegistotracker.pojo.PResult
import com.mslxl.provlegistotracker.pojo.Room
import com.mslxl.provlegistotracker.pojo.RoomCreateRequest
import com.mslxl.provlegistotracker.util.RoomStorage
import jakarta.servlet.http.HttpSession
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.random.Random

@RestController("/room")
class RoomController(
    val roomStorage: RoomStorage,
    val userMapper: UserMapper
) {

    @GetMapping("/room")
    fun listRoom(@RequestParam name: String?, @RequestParam page: Int?): PResult<List<Room>> {
        val page = page ?: 0
        val rooms = if (name == null) {
            roomStorage.list(page)
        } else {
            roomStorage.searchName(name, page)
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
        val newRoom = Room(
            uuid,
            room.name,
            username,
            room.max,
            room.allowAnonymous,
            ZonedDateTime.now(),
            room.password
        )
        roomStorage.createRoom(newRoom)
        return PResult.ok(uuid.toString())
    }
}