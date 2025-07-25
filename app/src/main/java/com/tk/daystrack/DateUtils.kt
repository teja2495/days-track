package com.tk.daystrack

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

object DateUtils {
    
    fun formatTimeDifference(eventDate: LocalDate): String {
        val today = LocalDate.now()
        val daysDifference = ChronoUnit.DAYS.between(today, eventDate)
        
        return when {
            daysDifference == 0L -> "today"
            daysDifference > 0 -> formatFuture(daysDifference)
            else -> formatPast(abs(daysDifference))
        }
    }
    
    private fun formatFuture(days: Long): String {
        return when {
            days == 1L -> "in 1 day"
            days < 30 -> "in $days days"
            else -> {
                val months = days / 30
                val remainingDays = days % 30
                when {
                    months == 1L && remainingDays == 0L -> "in 1 month"
                    months == 1L -> "in 1 month $remainingDays days"
                    remainingDays == 0L -> "in $months months"
                    else -> "in $months months $remainingDays days"
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
                    months == 1L && remainingDays == 0L -> "1 month ago"
                    months == 1L -> "1 month $remainingDays days ago"
                    remainingDays == 0L -> "$months months ago"
                    else -> "$months months $remainingDays days ago"
                }
            }
        }
    }

    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

    fun getDaysDifference(eventDate: LocalDate): Long {
        val today = LocalDate.now()
        return kotlin.math.abs(java.time.temporal.ChronoUnit.DAYS.between(today, eventDate))
    }

    fun averageFrequency(dates: List<LocalDate>): Double? {
        if (dates.size < 2) return null
        val sorted = dates.sorted()
        val intervals = sorted.zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b).toDouble() }
        return if (intervals.isNotEmpty()) intervals.average() else null
    }

    fun isAtLeastOneMonth(eventDate: LocalDate): Boolean {
        val today = LocalDate.now()
        val daysDifference = kotlin.math.abs(ChronoUnit.DAYS.between(today, eventDate))
        return daysDifference >= 30
    }
} 