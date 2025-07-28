package com.tk.daystrack

import java.util.UUID
import java.time.LocalDate
import com.google.gson.annotations.SerializedName

data class EventInstance(
    @SerializedName("date")
    val date: LocalDate,
    @SerializedName("note")
    val note: String? = null
)

data class Event(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),
    @SerializedName("name")
    val name: String,
    @SerializedName("instances")
    val instances: List<EventInstance>
) 