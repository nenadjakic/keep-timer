package com.github.nenadjakic.keeptimer.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Timer(
    val id: Long? = null,
    val startTime: Long,
    val endTime: Long,
    var manual: Boolean = false
)
