package com.github.nenadjakic.keeptimer.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Project (
    val id: Long? = null,
    var name: String,
    val timers: MutableSet<Timer> = mutableSetOf()
) {
    fun deepCopy(): Project {
        return Project(
            id = this.id,
            name = this.name,
            timers = this.timers.map { it.deepCopy() }.toMutableSet()
        )
    }
}