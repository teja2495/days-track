package com.tk.daystrack

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.Period
import kotlin.math.abs

object DateUtils {
    
    fun formatTimeDifference(eventDate: LocalDate): String {
        val today = LocalDate.now()
        val daysDifference = ChronoUnit.DAYS.between(today, eventDate)
        
        return when {
            daysDifference == 0L -> "today"
            daysDifference > 0 -> formatFuture(eventDate, today)
            else -> formatPast(eventDate, today)
        }
    }
    
    private fun formatFuture(eventDate: LocalDate, today: LocalDate): String {
        val period = Period.between(today, eventDate)
        val months = period.months
        val days = period.days
        
        return when {
            months == 0 && days == 0 -> "today"
            months == 0 && days == 1 -> "in 1 day"
            months == 0 -> "in $days days"
            months == 1 && days == 0 -> "in 1 month"
            months == 1 -> "in 1 month $days days"
            days == 0 -> "in $months months"
            else -> "in $months months $days days"
        }
    }
    
    private fun formatPast(eventDate: LocalDate, today: LocalDate): String {
        val period = Period.between(eventDate, today)
        val months = period.months
        val days = period.days
        
        return when {
            months == 0 && days == 0 -> "today"
            months == 0 && days == 1 -> "1 day ago"
            months == 0 -> "$days days ago"
            months == 1 && days == 0 -> "1 month ago"
            months == 1 -> "1 month $days days ago"
            days == 0 -> "$months months ago"
            else -> "$months months $days days ago"
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