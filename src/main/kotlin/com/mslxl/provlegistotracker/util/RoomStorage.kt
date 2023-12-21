package com.mslxl.provlegistotracker.util

import com.mslxl.provlegistotracker.pojo.Room
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

@Component
class RoomStorage {
    private val rooms = LinkedHashMap<UUID, Room>()
    private val clientInRooms = HashMap<UUID, Int>()
    private val numPerPage = 20

    fun createRoom(room: Room) {
        rooms[room.uuid] = room
    }

    fun removeRoom(uuid: UUID) {
        rooms.remove(uuid)
    }

    fun list(page: Int): List<Room> {
        return rooms.asSequence()
            .drop(page * numPerPage)
            .take(numPerPage)
            .map { it.value }
            .toList()
    }

    fun searchName(name: String, page: Int): List<Room> {
        return rooms.asSequence()
            .filter { it.value.name.contains(name) || it.value.owner.contains(name) }
            .drop(page * numPerPage)
            .take(numPerPage)
            .map { it.value }
            .toList()
    }

    fun getRoom(uuid: UUID): Room? {
        return rooms[uuid]
    }

    fun inc(uuid: UUID) {
        clientInRooms[uuid] = clientInRooms.getOrDefault(uuid, 0) + 1
    }

    fun dec(uuid: UUID) {
        if (!clientInRooms.contains(uuid)) return
        val cur = clientInRooms[uuid]!!
        if (cur == 1) {
            clientInRooms.remove(uuid)
            rooms.remove(uuid)
        } else {
            clientInRooms[uuid] = cur - 1
        }
    }
    fun clear(){
        var iter = rooms.iterator()
        while(iter.hasNext()){
            val value = iter.next()
            if(!clientInRooms.contains(value.key) || clientInRooms[value.key]!! == 0){
                iter.remove()
            }
        }
    }
}