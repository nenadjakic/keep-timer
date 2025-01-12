package com.github.nenadjakic.keeptimer.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Project (
    val id: Long? = null,
    var name: String,
    val timers: MutableSet<Timer> = mutableSetOf()
)