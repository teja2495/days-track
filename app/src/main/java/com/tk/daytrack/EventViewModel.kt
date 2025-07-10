package com.tk.daytrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class EventViewModel(private val repository: EventRepository) : ViewModel() {
    
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()
    
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()
    
    init {
        loadEvents()
    }
    
    private fun loadEvents() {
        viewModelScope.launch {
            _events.value = repository.loadEvents()
        }
    }
    
    fun addEvent(name: String, date: LocalDate) {
        viewModelScope.launch {
            val newEvent = Event(name = name.trim(), date = date)
            _events.value = repository.addEvent(newEvent)
            _showAddDialog.value = false
        }
    }
    
    fun removeEvent(eventId: String) {
        viewModelScope.launch {
            _events.value = repository.removeEvent(eventId)
        }
    }
    
    fun showAddDialog() {
        _showAddDialog.value = true
    }
    
    fun hideAddDialog() {
        _showAddDialog.value = false
    }
} 