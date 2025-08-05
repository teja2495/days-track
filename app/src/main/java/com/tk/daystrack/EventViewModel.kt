package com.tk.daystrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class SortOption {
    DATE_ASCENDING,
    DATE_DESCENDING,
    ALPHABETICAL,
    CUSTOM
}

enum class FontSize {
    SMALL,
    MEDIUM,
    LARGE
}

class EventViewModel(private val repository: EventRepository) : ViewModel() {
    
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()
    
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()
    
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()
    
    private val _currentSortOption = MutableStateFlow(SortOption.CUSTOM)
    val currentSortOption: StateFlow<SortOption> = _currentSortOption.asStateFlow()
    
    private val _currentFontSize = MutableStateFlow(FontSize.MEDIUM)
    val currentFontSize: StateFlow<FontSize> = _currentFontSize.asStateFlow()
    
    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()
    
    private val _eventToUpdate = MutableStateFlow<Event?>(null)
    val eventToUpdate: StateFlow<Event?> = _eventToUpdate.asStateFlow()
    
    private var _unsortedEvents = listOf<Event>()
    
    private val _showEventListHintBanner = MutableStateFlow(!repository.getHasSeenEventListHintBanner())
    val showEventListHintBanner: StateFlow<Boolean> = _showEventListHintBanner.asStateFlow()

    // Computed property that shows hint only when there are at least 2 events
    val shouldShowEventListHintBanner: StateFlow<Boolean> = combine(
        _showEventListHintBanner,
        _events
    ) { showHint, events ->
        showHint && events.size >= 2
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

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
        loadFontSize()
    }
    
    private fun loadEvents() {
        viewModelScope.launch {
            _unsortedEvents = repository.loadEvents()
            sortEvents()
        }
    }
    
    private fun loadFontSize() {
        _currentFontSize.value = repository.getFontSize()
    }
    
    fun setFontSize(fontSize: FontSize) {
        _currentFontSize.value = fontSize
        repository.setFontSize(fontSize)
    }
    
    private var context: android.content.Context? = null
    
    fun setContext(context: android.content.Context) {
        this.context = context
    }
    
    private fun notifyWidgets() {
        context?.let { repository.notifyWidgetsToUpdate(it) }
    }
    
    fun addEvent(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return
        
        viewModelScope.launch {
            try {
                val newEvent = Event(name = trimmedName, instances = emptyList())
                _unsortedEvents = repository.addEvent(newEvent)
                sortEvents()
                _showAddDialog.value = false
                notifyWidgets()
            } catch (e: Exception) {
                android.util.Log.e("EventViewModel", "Error adding event: ${e.message}", e)
                // Don't hide dialog on error, let user try again
            }
        }
    }
    
    fun removeEvent(eventId: String) {
        viewModelScope.launch {
            _unsortedEvents = repository.removeEvent(eventId)
            sortEvents()
            // Exit edit mode if all events are removed
            if (_unsortedEvents.isEmpty()) {
                _isEditMode.value = false
            }
            notifyWidgets()
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
            notifyWidgets()
        }
    }

    fun deleteAllInstancesExceptLatest(eventId: String) {
        viewModelScope.launch {
            _unsortedEvents = repository.deleteAllInstancesExceptLatest(eventId)
            sortEvents()
            notifyWidgets()
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
            notifyWidgets()
        }
    }

    private fun sortEvents(forceNoSort: Boolean = false) {
        _events.value = if (forceNoSort || _currentSortOption.value == SortOption.CUSTOM) {
            _unsortedEvents
        } else {
            when (_currentSortOption.value) {
                SortOption.DATE_ASCENDING -> _unsortedEvents.sortedBy {
                    it.instances.maxByOrNull { it.date }?.date ?: java.time.LocalDate.MIN
                }
                SortOption.DATE_DESCENDING -> _unsortedEvents.sortedByDescending {
                    it.instances.maxByOrNull { it.date }?.date ?: java.time.LocalDate.MIN
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
            val success = repository.importEventsJson(json)
            if (success) {
                _unsortedEvents = repository.loadEvents()
                sortEvents()
                notifyWidgets()
            }
        }
    }

    fun updateEventInstance(newName: String, newDate: LocalDate, note: String?) {
        val eventId = _eventToUpdate.value?.id ?: return
        viewModelScope.launch {
            _unsortedEvents = repository.updateEvent(eventId, newName, EventInstance(newDate, note))
            sortEvents()
            hideUpdateDialog()
            notifyWidgets()
        }
    }

    fun addInstanceToEvent(eventId: String, name: String, instance: EventInstance) {
        viewModelScope.launch {
            _unsortedEvents = repository.updateEvent(eventId, name, instance)
            sortEvents()
            notifyWidgets()
        }
    }

    fun updateEventInstanceNote(eventId: String, date: LocalDate, note: String) {
        viewModelScope.launch {
            _unsortedEvents = repository.updateEventInstanceNote(eventId, date, note)
            sortEvents()
            notifyWidgets()
        }
    }

    fun updateEventName(eventId: String, newName: String) {
        viewModelScope.launch {
            val currentEvents = repository.loadEvents().toMutableList()
            val index = currentEvents.indexOfFirst { it.id == eventId }
            if (index != -1) {
                val event = currentEvents[index]
                currentEvents[index] = event.copy(name = newName.trim())
                repository.saveEvents(currentEvents)
                _unsortedEvents = currentEvents
                sortEvents()
                notifyWidgets()
            }
        }
    }
    
    fun updateEvent(event: Event) {
        viewModelScope.launch {
            val currentEvents = repository.loadEvents().toMutableList()
            val index = currentEvents.indexOfFirst { it.id == event.id }
            if (index != -1) {
                currentEvents[index] = event
                repository.saveEvents(currentEvents)
                _unsortedEvents = currentEvents
                sortEvents()
                notifyWidgets()
            }
        }
    }
} 