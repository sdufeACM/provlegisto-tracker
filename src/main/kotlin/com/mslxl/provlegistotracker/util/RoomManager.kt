package com.mslxl.provlegistotracker.util

import com.mslxl.provlegistotracker.pojo.Room
import org.springframework.stereotype.Component
import java.util.*

@Component
class RoomManager {
    private val rooms = LinkedHashMap<Int, Room>()
    private val clientInRooms = HashMap<Int, Int>()
    private val numPerPage = 20

    fun createRoom(room: Room): UUID {
        rooms[room.id] = room
        val roomUuid = UUID.randomUUID()
        return roomUuid
    }

    fun removeRoom(id: Int) {
        rooms.remove(id)
        clientInRooms.remove(id)
    }

    fun list(page: Int): List<Room> {
        return rooms.asSequence()
            .drop(page * numPerPage)
            .take(numPerPage)
            .map { it.value }
            .toList()
    }

    fun list(name: String, page: Int): List<Room> {
        return rooms.asSequence()
            .filter { it.value.name.contains(name) || it.value.owner.contains(name) }
            .drop(page * numPerPage)
            .take(numPerPage)
            .map { it.value }
            .toList()
    }

    fun getRoom(id: Int): Room? {
        return rooms[id]
    }

    fun inc(id: Int) {
        clientInRooms[id] = clientInRooms.getOrDefault(id, 0) + 1
    }

    fun dec(id: Int) {
        if (!clientInRooms.contains(id)) return
        val cur = clientInRooms[id]!!
        if (cur == 1) {
            removeRoom(id)
        } else {
            clientInRooms[id] = cur - 1
        }
    }

    fun clear() {
        val iter = rooms.iterator()
        synchronized(clientInRooms) {
            val deleteQueue = ArrayList<Int>()
            while (iter.hasNext()) {
                val value = iter.next()
                val id = value.key
                if (!clientInRooms.contains(id) || clientInRooms[id]!! == 0) {
                    deleteQueue.add(value.key)
                }
            }
            deleteQueue.forEach {
                removeRoom(it)
            }
        }
    }
}