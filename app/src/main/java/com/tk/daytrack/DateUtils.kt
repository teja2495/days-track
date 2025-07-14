package com.tk.daytrack

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

object DateUtils {
    
    fun formatTimeDifference(eventDate: LocalDate): String {
        val today = LocalDate.now()
        val daysDifference = ChronoUnit.DAYS.between(today, eventDate)
        
        return when {
            daysDifference == 0L -> "Today"
            daysDifference > 0 -> formatFuture(daysDifference)
            else -> formatPast(abs(daysDifference))
        }
    }
    
    private fun formatFuture(days: Long): String {
        return when {
            days == 1L -> "1 day until"
            days < 30 -> "$days days until"
            else -> {
                val months = days / 30
                val remainingDays = days % 30
                when {
                    months == 1L && remainingDays == 0L -> "1 month ($days days) until"
                    months == 1L -> "1 month $remainingDays days ($days days) until"
                    remainingDays == 0L -> "$months months ($days days) until"
                    else -> "$months months $remainingDays days ($days days) until"
                }
            }
        }
    }
    
    private fun formatPast(days: Long): String {
        return when {
            days == 1L -> "1 day ago"
            days < 30 -> "$days days ago"
            else -> {
                val months = days / 30
                val remainingDays = days % 30
                when {
                    months == 1L && remainingDays == 0L -> "1 month ago ($days days)"
                    months == 1L -> "1 month $remainingDays days ago ($days days)"
                    remainingDays == 0L -> "$months months ago ($days days)"
                    else -> "$months months $remainingDays days ago ($days days)"
                }
            }
        }
    }

    fun String.toTitleCase(): String =
        split(" ", "_", "-").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
} 