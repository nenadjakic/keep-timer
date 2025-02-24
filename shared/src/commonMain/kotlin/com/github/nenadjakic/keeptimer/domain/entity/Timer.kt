package com.github.nenadjakic.keeptimer.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Timer(
    val id: Long? = null,
    val startTime: Long,
    var endTime: Long? = null,
    var manual: Boolean = false
) {
    fun deepCopy(): Timer {
        return Timer(
            id = this.id,
            startTime = this.startTime,
            endTime = this.endTime,
            manual = this.manual
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Timer) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
