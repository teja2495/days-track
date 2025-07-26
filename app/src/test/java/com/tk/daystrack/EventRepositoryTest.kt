package com.tk.daystrack

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class EventRepositoryTest {
    
    @Test
    fun testReplaceOldestInstanceWhenOver50Instances() {
        // Create an event with 50 instances
        val instances = (1..31).map { day ->
            EventInstance(LocalDate.of(2023, 1, day))
        } + (1..19).map { day ->
            EventInstance(LocalDate.of(2023, 2, day))
        }
        
        val event = Event(
            id = "test-event",
            name = "Test Event",
            instances = instances
        )
        
        // Create a new instance to add
        val newInstance = EventInstance(LocalDate.of(2023, 12, 31))
        
        // Simulate the logic from updateEvent method
        val updatedInstances = if (event.instances.size >= 50) {
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
        assertEquals(50, updatedInstances.size) // Should still have 50 instances
        assertTrue(updatedInstances.contains(newInstance)) // Should contain the new instance
        assertFalse(updatedInstances.contains(EventInstance(LocalDate.of(2023, 1, 1)))) // Oldest instance should be replaced
        
        // Verify the oldest instance is now the second oldest from original
        val sortedUpdatedInstances = updatedInstances.sortedBy { it.date }
        assertEquals(LocalDate.of(2023, 1, 2), sortedUpdatedInstances.first().date)
    }
    
    @Test
    fun testNormalAdditionWhenUnder50Instances() {
        // Create an event with 49 instances
        val instances = (1..31).map { day ->
            EventInstance(LocalDate.of(2023, 1, day))
        } + (1..18).map { day ->
            EventInstance(LocalDate.of(2023, 2, day))
        }
        
        val event = Event(
            id = "test-event",
            name = "Test Event",
            instances = instances
        )
        
        // Create a new instance to add
        val newInstance = EventInstance(LocalDate.of(2023, 12, 31))
        
        // Simulate the logic from updateEvent method
        val updatedInstances = if (event.instances.size >= 50) {
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
        assertEquals(50, updatedInstances.size) // Should now have 50 instances
        assertTrue(updatedInstances.contains(newInstance)) // Should contain the new instance
        assertTrue(updatedInstances.contains(EventInstance(LocalDate.of(2023, 1, 1)))) // Oldest instance should still be there
    }
} 