package com.github.nenadjakic.keeptimer.domain.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Project (
    val id: Long? = null,
    var name: String,
    val timers: MutableSet<Timer> = mutableSetOf()
)