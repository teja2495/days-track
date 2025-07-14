package com.tk.daytrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.tk.daytrack.DateUtils.toTitleCase

enum class SortOption {
    DATE_ASCENDING,
    DATE_DESCENDING,
    ALPHABETICAL
}

class EventViewModel(private val repository: EventRepository) : ViewModel() {
    
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()
    
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()
    
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()
    
    private val _currentSortOption = MutableStateFlow(SortOption.DATE_ASCENDING)
    val currentSortOption: StateFlow<SortOption> = _currentSortOption.asStateFlow()
    
    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()
    
    private val _eventToUpdate = MutableStateFlow<Event?>(null)
    val eventToUpdate: StateFlow<Event?> = _eventToUpdate.asStateFlow()
    
    private var _unsortedEvents = listOf<Event>()
    
    init {
        loadEvents()
    }
    
    private fun loadEvents() {
        viewModelScope.launch {
            _unsortedEvents = repository.loadEvents()
            sortEvents()
        }
    }
    
    fun addEvent(name: String, date: LocalDate) {
        viewModelScope.launch {
            val newEvent = Event(name = name.trim(), dates = listOf(date))
            _unsortedEvents = repository.addEvent(newEvent)
            sortEvents()
            _showAddDialog.value = false
        }
    }
    
    fun removeEvent(eventId: String) {
        viewModelScope.launch {
            _unsortedEvents = repository.removeEvent(eventId)
            sortEvents()
        }
    }
    
    fun showAddDialog() {
        _showAddDialog.value = true
    }
    
    fun hideAddDialog() {
        _showAddDialog.value = false
    }
    
    fun showUpdateDialog(event: Event) {
        _eventToUpdate.value = event
        _showUpdateDialog.value = true
    }
    
    fun hideUpdateDialog() {
        _showUpdateDialog.value = false
        _eventToUpdate.value = null
    }
    
    fun updateEventDate(newName: String, newDate: LocalDate) {
        val eventId = _eventToUpdate.value?.id ?: return
        viewModelScope.launch {
            _unsortedEvents = repository.updateEvent(eventId, newName.toTitleCase(), newDate)
            sortEvents()
            hideUpdateDialog()
        }
    }
    
    fun deleteEventDate(eventId: String, dateToDelete: LocalDate) {
        viewModelScope.launch {
            _unsortedEvents = repository.deleteEventDate(eventId, dateToDelete)
            sortEvents()
        }
    }
    
    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
    }
    
    fun setEditMode(editMode: Boolean) {
        _isEditMode.value = editMode
    }
    
    fun setSortOption(sortOption: SortOption) {
        if (_currentSortOption.value != sortOption) {
            _currentSortOption.value = sortOption
            sortEvents()
        }
    }
    
    private fun sortEvents() {
        _events.value = when (_currentSortOption.value) {
            SortOption.DATE_ASCENDING -> _unsortedEvents.sortedBy { it.dates.last() }
            SortOption.DATE_DESCENDING -> _unsortedEvents.sortedByDescending { it.dates.last() }
            SortOption.ALPHABETICAL -> _unsortedEvents.sortedBy { it.name }
        }
    }
} 