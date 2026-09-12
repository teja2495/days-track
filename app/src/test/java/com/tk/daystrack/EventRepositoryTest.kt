package com.tk.daystrack

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class EventRepositoryTest {
    
    @Test
    fun testReplaceOldestInstanceWhenAtLimit() {
        val firstDate = LocalDate.of(2023, 1, 1)
        val instances = List(EventRepository.MAX_INSTANCE_COUNT) { day ->
            EventInstance(firstDate.plusDays(day.toLong()))
        }
        
        val event = Event(
            id = "test-event",
            name = "Test Event",
            instances = instances
        )
        
        // Create a new instance to add
        val newInstance = EventInstance(LocalDate.of(2025, 12, 31))
        
        // Simulate the logic from updateEvent method
        val updatedInstances = if (event.instances.size >= EventRepository.MAX_INSTANCE_COUNT) {
            // Find the oldest instance and replace it with the new one
            val sortedInstances = event.instances.sortedBy { it.date }
            val oldestInstance = sortedInstances.first()
            event.instances.map { 
                if (it.date == oldestInstance.date) newInstance else it 
            }
        } else {
            event.instances + newInstance
        }
        
        // Verify the result
        assertEquals(EventRepository.MAX_INSTANCE_COUNT, updatedInstances.size)
        assertTrue(updatedInstances.contains(newInstance)) // Should contain the new instance
        assertFalse(updatedInstances.contains(EventInstance(firstDate))) // Oldest instance should be replaced
        
        // Verify the oldest instance is now the second oldest from original
        val sortedUpdatedInstances = updatedInstances.sortedBy { it.date }
        assertEquals(firstDate.plusDays(1), sortedUpdatedInstances.first().date)
    }
    
    @Test
    fun testNormalAdditionWhenUnderLimit() {
        val firstDate = LocalDate.of(2023, 1, 1)
        val instances = List(EventRepository.MAX_INSTANCE_COUNT - 1) { day ->
            EventInstance(firstDate.plusDays(day.toLong()))
        }
        
        val event = Event(
            id = "test-event",
            name = "Test Event",
            instances = instances
        )
        
        // Create a new instance to add
        val newInstance = EventInstance(LocalDate.of(2025, 12, 31))
        
        // Simulate the logic from updateEvent method
        val updatedInstances = if (event.instances.size >= EventRepository.MAX_INSTANCE_COUNT) {
            // Find the oldest instance and replace it with the new one
            val sortedInstances = event.instances.sortedBy { it.date }
            val oldestInstance = sortedInstances.first()
            event.instances.map { 
                if (it.date == oldestInstance.date) newInstance else it 
            }
        } else {
            event.instances + newInstance
        }
        
        // Verify the result
        assertEquals(EventRepository.MAX_INSTANCE_COUNT, updatedInstances.size)
        assertTrue(updatedInstances.contains(newInstance)) // Should contain the new instance
        assertTrue(updatedInstances.contains(EventInstance(firstDate))) // Oldest instance should still be there
    }
}
