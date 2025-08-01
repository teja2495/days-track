package com.tk.daystrack

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.Period
import kotlin.math.abs
import androidx.compose.runtime.mutableStateOf

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
    
    // Enhanced caching for better performance when called frequently
    private val todayCache = mutableStateOf<LocalDate?>(null)
    private val todayCacheTime = mutableStateOf<Long>(0)
    private const val CACHE_DURATION_MS = 30000L // 30 seconds cache for more frequent updates
    
    // Cache for formatted time differences to avoid repeated calculations
    private val timeDifferenceCache = mutableMapOf<LocalDate, String>()
    private const val MAX_CACHE_SIZE = 100 // Limit cache size to prevent memory issues
    
    fun formatTimeDifferenceCached(eventDate: LocalDate): String {
        // Check cache first
        timeDifferenceCache[eventDate]?.let { return it }
        
        val currentTime = System.currentTimeMillis()
        val cachedToday = todayCache.value
        val cachedTime = todayCacheTime.value
        
        val today = if (cachedToday != null && (currentTime - cachedTime) < CACHE_DURATION_MS) {
            cachedToday
        } else {
            val newToday = LocalDate.now()
            todayCache.value = newToday
            todayCacheTime.value = currentTime
            newToday
        }
        
        val daysDifference = ChronoUnit.DAYS.between(today, eventDate)
        
        val result = when {
            daysDifference == 0L -> "today"
            daysDifference > 0 -> formatFuture(eventDate, today)
            else -> formatPast(eventDate, today)
        }
        
        // Cache the result
        if (timeDifferenceCache.size < MAX_CACHE_SIZE) {
            timeDifferenceCache[eventDate] = result
        }
        
        return result
    }
    
    // Clear cache when needed (call this periodically or when memory pressure is detected)
    fun clearCache() {
        timeDifferenceCache.clear()
        todayCache.value = null
        todayCacheTime.value = 0
    }
    
    private fun formatFuture(eventDate: LocalDate, today: LocalDate): String {
        val period = Period.between(today, eventDate)
        val years = period.years
        val months = period.months
        val days = period.days
        
        // If it's a year or more, show in years and months format
        if (years > 0 || (years == 0 && months >= 12)) {
            return when {
                years == 0 -> "in $months months"
                years == 1 && months == 0 -> "in 1 year"
                years == 1 -> "in 1 year $months months"
                months == 0 -> "in $years years"
                else -> "in $years years $months months"
            }
        }
        
        // For periods less than a year, show detailed breakdown
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
        val years = period.years
        val months = period.months
        val days = period.days
        
        // If it's a year or more, show in years and months format
        if (years > 0 || (years == 0 && months >= 12)) {
            return when {
                years == 0 -> "$months months ago"
                years == 1 && months == 0 -> "1 year ago"
                years == 1 -> "1 year $months months ago"
                months == 0 -> "$years years ago"
                else -> "$years years $months months ago"
            }
        }
        
        // For periods less than a year, show detailed breakdown
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

    // Cached version of getDaysDifference for better performance
    private val daysDifferenceCache = mutableMapOf<LocalDate, Long>()
    
    fun getDaysDifference(eventDate: LocalDate): Long {
        // Check cache first
        daysDifferenceCache[eventDate]?.let { return it }
        
        val today = LocalDate.now()
        val difference = abs(ChronoUnit.DAYS.between(today, eventDate))
        
        // Cache the result
        if (daysDifferenceCache.size < MAX_CACHE_SIZE) {
            daysDifferenceCache[eventDate] = difference
        }
        
        return difference
    }

    fun averageFrequency(dates: List<LocalDate>): Double? {
        if (dates.size < 2) return null
        val sorted = dates.sorted()
        val intervals = sorted.zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b).toDouble() }
        return if (intervals.isNotEmpty()) intervals.average() else null
    }

    // Cached version of isAtLeastOneMonth for better performance
    private val isAtLeastOneMonthCache = mutableMapOf<LocalDate, Boolean>()
    
    fun isAtLeastOneMonth(eventDate: LocalDate): Boolean {
        // Check cache first
        isAtLeastOneMonthCache[eventDate]?.let { return it }
        
        val today = LocalDate.now()
        val daysDifference = abs(ChronoUnit.DAYS.between(today, eventDate))
        val result = daysDifference >= 30
        
        // Cache the result
        if (isAtLeastOneMonthCache.size < MAX_CACHE_SIZE) {
            isAtLeastOneMonthCache[eventDate] = result
        }
        
        return result
    }
} 