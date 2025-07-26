package com.tk.daystrack

import java.util.UUID
import java.time.LocalDate

data class EventInstance(
    val date: LocalDate,
    val note: String? = null
)

data class Event(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val instances: List<EventInstance>
) 