package com.example.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object DateTimeUtils {
    // API dates are typically ISO 8601: 2026-05-29T04:17:58Z
    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val fullDateFormatter = SimpleDateFormat("EEEE, MMM d, yyyy • hh:mm a", Locale.US)
    private val olderDateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.US)
    private val headerDateFormatter = SimpleDateFormat("EEEE, MMMM d", Locale.US)
    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.US)
    private val istTimeFormatter = SimpleDateFormat("hh:mm a 'IST'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("Asia/Kolkata")
    }

    fun parseIsoDate(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            isoFormatter.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    fun getRelativeTime(dateStr: String?): String {
        val date = parseIsoDate(dateStr) ?: return ""
        val now = Date()
        val diffInMillis = now.time - date.time

        if (diffInMillis < 0) return "Just now"

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes minute${if (minutes == 1L) "" else "s"} ago"
            hours < 24 -> "$hours hour${if (hours == 1L) "" else "s"} ago"
            days == 1L -> "Yesterday"
            days <= 7 -> "$days days ago"
            else -> olderDateFormatter.format(date)
        }
    }

    fun getFullDateTime(dateStr: String?): String {
        val date = parseIsoDate(dateStr) ?: return ""
        return fullDateFormatter.format(date)
    }

    fun getReadingTime(text: String?): String {
        if (text.isNullOrBlank()) return "1 min read"
        // assuming 200 words per minute average reading speed
        val wordCount = text.split("\\s+".toRegex()).size
        val minutes = maxOf(1, Math.ceil(wordCount / 200.0).toInt())
        return "$minutes min read"
    }

    fun getTodayHeaderDate(): String {
        return headerDateFormatter.format(Date())
    }

    fun getGreeting(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> "Good Morning, here's your briefing"
            in 12..16 -> "Good Afternoon, here's your briefing"
            else -> "Good Evening, here's your briefing"
        }
    }

    // New York Stock Exchange hours are 9:30 AM to 4:00 PM EST, Monday - Friday
    fun getMarketStatusAndCountdown(): Pair<Boolean, String> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"))
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        val timeInMinutes = hour * 60 + minute
        val openTime = 9 * 60 + 30 // 9:30 AM
        val closeTime = 16 * 60 // 4:00 PM

        val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
        val isOpen = !isWeekend && timeInMinutes in openTime until closeTime

        if (isOpen) {
            return Pair(true, "")
        }

        // Calculate next opening
        var daysToAdd = 0
        var nextOpenTimeInMinutes = openTime
        
        if (isWeekend) {
            daysToAdd = if (dayOfWeek == Calendar.SATURDAY) 2 else 1
        } else if (timeInMinutes >= closeTime) {
            daysToAdd = if (dayOfWeek == Calendar.FRIDAY) 3 else 1
        } else if (timeInMinutes < openTime) {
            daysToAdd = 0
        }

        val currentTotalMinutes = timeInMinutes
        val nextTotalMinutes = daysToAdd * 24 * 60 + nextOpenTimeInMinutes

        val diffMinutes = nextTotalMinutes - currentTotalMinutes
        val diffHours = diffMinutes / 60
        val remainingMins = diffMinutes % 60

        val countdownStr = if (daysToAdd > 1) {
            "Opens on Monday"
        } else if (diffHours > 0) {
            "Opens in ${diffHours}h ${remainingMins}m"
        } else {
            "Opens in ${remainingMins}m"
        }

        return Pair(false, countdownStr)
    }
    
    fun getCurrentIstTime(): String {
        return istTimeFormatter.format(Date())
    }
}
