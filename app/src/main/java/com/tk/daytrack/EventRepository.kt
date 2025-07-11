package com.tk.daytrack

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EventRepository(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("events_prefs", Context.MODE_PRIVATE)
    
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, JsonSerializer<LocalDate> { src, _, _ ->
            com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE))
        })
        .registerTypeAdapter(LocalDate::class.java, JsonDeserializer<LocalDate> { json, _, _ ->
            LocalDate.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE)
        })
        .create()
    
    fun saveEvents(events: List<Event>) {
        val json = gson.toJson(events)
        sharedPreferences.edit()
            .putString("events", json)
            .apply()
    }
    
    fun loadEvents(): List<Event> {
        val json = sharedPreferences.getString("events", null) ?: return emptyList()
        val type = object : TypeToken<List<Event>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun addEvent(event: Event): List<Event> {
        val currentEvents = loadEvents().toMutableList()
        currentEvents.add(event)
        // Sort by date (most recent first)
        val sortedEvents = currentEvents.sortedByDescending { it.date }
        saveEvents(sortedEvents)
        return sortedEvents
    }
    
    fun removeEvent(eventId: String): List<Event> {
        val currentEvents = loadEvents().toMutableList()
        currentEvents.removeAll { it.id == eventId }
        saveEvents(currentEvents)
        return currentEvents
    }
    
    fun updateEvent(eventId: String, newName: String, newDate: LocalDate): List<Event> {
        val currentEvents = loadEvents().toMutableList()
        val index = currentEvents.indexOfFirst { it.id == eventId }
        if (index != -1) {
            val event = currentEvents[index]
            currentEvents[index] = event.copy(name = newName.trim(), date = newDate)
            saveEvents(currentEvents)
        }
        return currentEvents
    }
} 