package com.tk.daystrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.tk.daystrack.DateUtils.toTitleCase

enum class SortOption {
    DATE_ASCENDING,
    DATE_DESCENDING,
    ALPHABETICAL,
    CUSTOM
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
    
    private val _showEventListHintBanner = MutableStateFlow(!repository.getHasSeenEventListHintBanner())
    val showEventListHintBanner: StateFlow<Boolean> = _showEventListHintBanner.asStateFlow()

    fun dismissEventListHintBanner() {
        _showEventListHintBanner.value = false
        repository.setHasSeenEventListHintBanner(true)
    }
    
    private val _showToggleDateHint = MutableStateFlow(!repository.getHasSeenToggleDateHint())
    val showToggleDateHint: StateFlow<Boolean> = _showToggleDateHint.asStateFlow()

    fun dismissToggleDateHint() {
        _showToggleDateHint.value = false
        repository.setHasSeenToggleDateHint(true)
    }
    
    init {
        loadEvents()
    }
    
    private fun loadEvents() {
        viewModelScope.launch {
            _unsortedEvents = repository.loadEvents()
            sortEvents()
        }
    }
    
    fun addEvent(name: String) {
        viewModelScope.launch {
            val newEvent = Event(name = name.trim(), instances = emptyList())
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
    
    fun reorderEvents(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            val mutableList = _unsortedEvents.toMutableList()
            val item = mutableList.removeAt(fromIndex)
            mutableList.add(toIndex, item)
            _unsortedEvents = mutableList
            repository.saveEvents(_unsortedEvents)
            _currentSortOption.value = SortOption.CUSTOM
            sortEvents(forceNoSort = true)
        }
    }

    private fun sortEvents(forceNoSort: Boolean = false) {
        _events.value = if (forceNoSort || _currentSortOption.value == SortOption.CUSTOM) {
            _unsortedEvents
        } else {
            when (_currentSortOption.value) {
                SortOption.DATE_ASCENDING -> _unsortedEvents.sortedBy {
                    if (it.instances.isNotEmpty()) it.instances.last().date else java.time.LocalDate.MIN
                }
                SortOption.DATE_DESCENDING -> _unsortedEvents.sortedByDescending {
                    if (it.instances.isNotEmpty()) it.instances.last().date else java.time.LocalDate.MIN
                }
                SortOption.ALPHABETICAL -> _unsortedEvents.sortedBy { it.name }
                SortOption.CUSTOM -> _unsortedEvents // fallback, should be handled above
            }
        }
    }

    fun exportEventsJson(): String {
        return repository.exportEventsJson()
    }

    fun importEventsJson(json: String) {
        viewModelScope.launch {
            repository.importEventsJson(json)
            _unsortedEvents = repository.loadEvents()
            sortEvents()
        }
    }

    fun updateEventInstance(newName: String, newDate: LocalDate, note: String?) {
        val eventId = _eventToUpdate.value?.id ?: return
        viewModelScope.launch {
            _unsortedEvents = repository.updateEvent(eventId, newName.toTitleCase(), EventInstance(newDate, note))
            sortEvents()
            hideUpdateDialog()
        }
    }

    fun addInstanceToEvent(eventId: String, name: String, instance: EventInstance) {
        viewModelScope.launch {
            _unsortedEvents = repository.updateEvent(eventId, name.toTitleCase(), instance)
            sortEvents()
        }
    }

    fun updateEventInstanceNote(eventId: String, date: LocalDate, note: String) {
        viewModelScope.launch {
            _unsortedEvents = repository.updateEventInstanceNote(eventId, date, note)
            sortEvents()
        }
    }
} 