package com.tk.daytrack

import java.time.LocalDate
import java.util.UUID

data class Event(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val date: LocalDate
) 