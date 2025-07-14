package com.tk.daytrack

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.google.gson.JsonElement
import com.google.gson.JsonObject
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
        .registerTypeAdapter(Event::class.java, JsonSerializer<Event> { src, _, _ ->
            val jsonObject = com.google.gson.JsonObject()
            jsonObject.addProperty("id", src.id)
            jsonObject.addProperty("name", src.name)
            val datesArray = com.google.gson.JsonArray()
            src.dates.forEach { date ->
                datesArray.add(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
            }
            jsonObject.add("dates", datesArray)
            jsonObject
        })
        .registerTypeAdapter(Event::class.java, JsonDeserializer<Event> { json, _, _ ->
            val jsonObject = json.asJsonObject
            val id = jsonObject.get("id").asString
            val name = jsonObject.get("name").asString
            
            // Check if this is the new format (has dates array)
            if (jsonObject.has("dates")) {
                val datesArray = jsonObject.getAsJsonArray("dates")
                val dates = datesArray.map { element ->
                    LocalDate.parse(element.asString, DateTimeFormatter.ISO_LOCAL_DATE)
                }
                Event(id = id, name = name, dates = dates)
            } else {
                // Old format: has date and possibly previousDate
                val date = LocalDate.parse(jsonObject.get("date").asString, DateTimeFormatter.ISO_LOCAL_DATE)
                val dates = mutableListOf<LocalDate>()
                dates.add(date)
                
                // If there's a previousDate, add it to the list
                if (jsonObject.has("previousDate") && !jsonObject.get("previousDate").isJsonNull) {
                    val previousDate = LocalDate.parse(jsonObject.get("previousDate").asString, DateTimeFormatter.ISO_LOCAL_DATE)
                    dates.add(0, previousDate) // Add at beginning since it's older
                }
                
                Event(id = id, name = name, dates = dates)
            }
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
        val sortedEvents = currentEvents.sortedByDescending { it.dates.last() }
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
            val updatedDates = if (event.dates.last() != newDate) event.dates + newDate else event.dates
            currentEvents[index] = event.copy(
                name = newName.trim(),
                dates = updatedDates
            )
            saveEvents(currentEvents)
        }
        return currentEvents
    }
    
    fun deleteEventDate(eventId: String, dateToDelete: LocalDate): List<Event> {
        val currentEvents = loadEvents().toMutableList()
        val index = currentEvents.indexOfFirst { it.id == eventId }
        if (index != -1) {
            val event = currentEvents[index]
            // Don't delete if it's the only date left
            if (event.dates.size > 1) {
                val updatedDates = event.dates.filter { it != dateToDelete }
                currentEvents[index] = event.copy(dates = updatedDates)
                saveEvents(currentEvents)
            }
        }
        return currentEvents
    }
} 