package com.github.nenadjakic.keeptimer.domain.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Timer(
    val id: Long? = null,
    val startTime: Long,
    val endTime: Long,
    var manual: Boolean = false
)
