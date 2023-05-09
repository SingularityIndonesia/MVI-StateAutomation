package com.singularity_indonesia.mvi_stateautomations.domain.model

import com.singularity_indonesia.mvi_stateautomations.util.pattern.JsonConvertible

data class Todo(
    val id: String,
    val title: String,
    val detail: String,
    val lastModifiedTime: Long
)
