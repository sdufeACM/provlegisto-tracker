package com.mslxl.provlegistotracker.util

import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Component
class SessionManager(
    val roomManager: RoomManager
) {
    companion object {
        val scheduledExecutorService: ScheduledExecutorService = Executors.newScheduledThreadPool(5)
    }

    private val sessionRoom = HashMap<UUID, Int>()
    private val sessionDelayCloser = HashMap<UUID, Future<*>>()

    private fun removeSession(uuid: UUID) {
        sessionRoom[uuid]?.run(roomManager::dec)
        sessionRoom.remove(uuid)
        sessionDelayCloser.remove(uuid)

    }

    fun createSession(roomId: Int): UUID {
        val uuid = UUID.randomUUID()
        sessionRoom[uuid] = roomId
        roomManager.inc(roomId)
        sessionDelayCloser[uuid] = scheduledExecutorService.schedule({
            removeSession(uuid)
        }, 30, TimeUnit.SECONDS)
        return uuid
    }

    fun openSession(uuid: UUID): Int? {
        if (!sessionRoom.contains(uuid)) return null
        if (sessionDelayCloser.contains(uuid)) {
            sessionDelayCloser[uuid]?.cancel(true)
            sessionDelayCloser.remove(uuid)
        }
        return sessionRoom[uuid]!!
    }

    fun closeSession(uuid: UUID) {
        if (!sessionDelayCloser.contains(uuid)) {
            sessionDelayCloser[uuid] = scheduledExecutorService.schedule({
                removeSession(uuid)
            }, 30, TimeUnit.SECONDS)
        }
    }

}