package com.tk.daystrack

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
        .registerTypeAdapter(EventInstance::class.java, JsonSerializer<EventInstance> { src, _, _ ->
            val jsonObject = com.google.gson.JsonObject()
            jsonObject.addProperty("date", src.date.format(DateTimeFormatter.ISO_LOCAL_DATE))
            if (!src.note.isNullOrBlank()) jsonObject.addProperty("note", src.note)
            jsonObject
        })
        .registerTypeAdapter(EventInstance::class.java, JsonDeserializer<EventInstance> { json, _, _ ->
            val jsonObject = json.asJsonObject
            val date = LocalDate.parse(jsonObject.get("date").asString, DateTimeFormatter.ISO_LOCAL_DATE)
            val note = if (jsonObject.has("note")) jsonObject.get("note").asString else null
            EventInstance(date, note)
        })
        .registerTypeAdapter(Event::class.java, JsonSerializer<Event> { src, _, _ ->
            val jsonObject = com.google.gson.JsonObject()
            jsonObject.addProperty("id", src.id)
            jsonObject.addProperty("name", src.name)
            val instancesArray = com.google.gson.JsonArray()
            src.instances.forEach { instance ->
                val instanceJson = com.google.gson.JsonObject()
                instanceJson.addProperty("date", instance.date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                if (!instance.note.isNullOrBlank()) instanceJson.addProperty("note", instance.note)
                instancesArray.add(instanceJson)
            }
            jsonObject.add("instances", instancesArray)
            jsonObject
        })
        .registerTypeAdapter(Event::class.java, JsonDeserializer<Event> { json, _, _ ->
            val jsonObject = json.asJsonObject
            val id = jsonObject.get("id").asString
            val name = jsonObject.get("name").asString
            if (jsonObject.has("instances")) {
                val instancesArray = jsonObject.getAsJsonArray("instances")
                val instances = instancesArray.map { element ->
                    val obj = element.asJsonObject
                    val date = LocalDate.parse(obj.get("date").asString, DateTimeFormatter.ISO_LOCAL_DATE)
                    val note = if (obj.has("note")) obj.get("note").asString else null
                    EventInstance(date, note)
                }
                Event(id = id, name = name, instances = instances)
            } else if (jsonObject.has("dates")) {
                val datesArray = jsonObject.getAsJsonArray("dates")
                val instances = datesArray.map { element ->
                    EventInstance(LocalDate.parse(element.asString, DateTimeFormatter.ISO_LOCAL_DATE))
                }
                Event(id = id, name = name, instances = instances)
            } else {
                val date = LocalDate.parse(jsonObject.get("date").asString, DateTimeFormatter.ISO_LOCAL_DATE)
                val instances = mutableListOf<EventInstance>()
                instances.add(EventInstance(date))
                if (jsonObject.has("previousDate") && !jsonObject.get("previousDate").isJsonNull) {
                    val previousDate = LocalDate.parse(jsonObject.get("previousDate").asString, DateTimeFormatter.ISO_LOCAL_DATE)
                    instances.add(0, EventInstance(previousDate))
                }
                Event(id = id, name = name, instances = instances)
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
            val events: List<Event> = gson.fromJson(json, type) ?: emptyList()
            // Limit total events to prevent memory issues
            if (events.size > 1000) {
                val limitedEvents = events.take(1000)
                saveEvents(limitedEvents)
                limitedEvents
            } else {
                events
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun addEvent(event: Event): List<Event> {
        val currentEvents = loadEvents().toMutableList()
        currentEvents.add(event)
        val sortedEvents = currentEvents.sortedByDescending {
            if (it.instances.isNotEmpty()) it.instances.maxByOrNull { it.date }?.date ?: java.time.LocalDate.MIN else java.time.LocalDate.MIN
        }
        saveEvents(sortedEvents)
        return sortedEvents
    }
    
    fun removeEvent(eventId: String): List<Event> {
        val currentEvents = loadEvents().toMutableList()
        currentEvents.removeAll { it.id == eventId }
        saveEvents(currentEvents)
        return currentEvents
    }
    
    fun updateEvent(eventId: String, newName: String, newInstance: EventInstance): List<Event> {
        val currentEvents = loadEvents().toMutableList()
        val index = currentEvents.indexOfFirst { it.id == eventId }
        if (index != -1) {
            val event = currentEvents[index]
            val updatedInstances = if (event.instances.isEmpty() || !event.instances.any { it.date == newInstance.date }) {
                // Check if adding this instance would exceed 50 instances
                if (event.instances.size >= 50) {
                    // Find the oldest instance and replace it with the new one
                    val sortedInstances = event.instances.sortedBy { it.date }
                    val oldestInstance = sortedInstances.first()
                    event.instances.map { 
                        if (it.date == oldestInstance.date) newInstance else it 
                    }
                } else {
                    event.instances + newInstance
                }
            } else {
                event.instances
            }
            currentEvents[index] = event.copy(
                name = newName.trim(),
                instances = updatedInstances
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
            val updatedInstances = event.instances.filter { it.date != dateToDelete }
            currentEvents[index] = event.copy(instances = updatedInstances)
            saveEvents(currentEvents)
        }
        return currentEvents
    }

    fun updateEventInstanceNote(eventId: String, date: LocalDate, note: String): List<Event> {
        val currentEvents = loadEvents().toMutableList()
        val index = currentEvents.indexOfFirst { it.id == eventId }
        if (index != -1) {
            val event = currentEvents[index]
            val updatedInstances = event.instances.map {
                if (it.date == date) it.copy(note = note) else it
            }
            currentEvents[index] = event.copy(instances = updatedInstances)
            saveEvents(currentEvents)
        }
        return currentEvents
    }

    fun exportEventsJson(): String {
        val events = loadEvents()
        return gson.toJson(events)
    }

    fun importEventsJson(json: String): Boolean {
        val type = object : TypeToken<List<Event>>() {}.type
        val events: List<Event> = try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            return false
        }
        saveEvents(events)
        return true
    }

    fun getHasSeenNoteHintBanner(): Boolean {
        return sharedPreferences.getBoolean("hasSeenNoteHintBanner", false)
    }
    fun setHasSeenNoteHintBanner(value: Boolean) {
        sharedPreferences.edit().putBoolean("hasSeenNoteHintBanner", value).apply()
    }

    fun getHasSeenEventListHintBanner(): Boolean {
        return sharedPreferences.getBoolean("hasSeenEventListHintBanner", false)
    }
    fun setHasSeenEventListHintBanner(value: Boolean) {
        sharedPreferences.edit().putBoolean("hasSeenEventListHintBanner", value).apply()
    }

    fun getHasSeenToggleDateHint(): Boolean {
        return sharedPreferences.getBoolean("hasSeenToggleDateHint", false)
    }
    fun setHasSeenToggleDateHint(value: Boolean) {
        sharedPreferences.edit().putBoolean("hasSeenToggleDateHint", value).apply()
    }
    
    fun getHasSeen50InstancesLimitHint(): Boolean {
        return sharedPreferences.getBoolean("hasSeen50InstancesLimitHint", false)
    }
    
    fun setHasSeen50InstancesLimitHint(value: Boolean) {
        sharedPreferences.edit().putBoolean("hasSeen50InstancesLimitHint", value).apply()
    }
    
    fun getFontSize(): FontSize {
        val fontSizeString = sharedPreferences.getString("fontSize", "MEDIUM") ?: "MEDIUM"
        return try {
            FontSize.valueOf(fontSizeString)
        } catch (e: IllegalArgumentException) {
            FontSize.MEDIUM
        }
    }
    
    fun setFontSize(fontSize: FontSize) {
        sharedPreferences.edit().putString("fontSize", fontSize.name).apply()
    }
} 