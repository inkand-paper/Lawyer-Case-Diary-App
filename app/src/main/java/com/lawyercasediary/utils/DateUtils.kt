package com.lawyercasediary.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    /**
     * Extremely resilient date formatter that handles UTC to Local conversion.
     */
    fun formatIsoToLocal(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "N/A"
        
        // Expected formats from backend
        val formats = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss"
        )

        for (format in formats) {
            try {
                val parser = SimpleDateFormat(format, Locale.US)
                if (format.endsWith("'Z'")) {
                    parser.timeZone = TimeZone.getTimeZone("UTC")
                }
                
                val date = parser.parse(isoString)
                if (date != null) {
                    val formatter = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                    formatter.timeZone = TimeZone.getDefault()
                    return formatter.format(date)
                }
            } catch (e: Exception) {
                // Try next format
            }
        }

        // Ultimate fallback: Just clean up the string a bit
        return isoString.replace("T", " ").take(16)
    }
    
    fun toIsoString(calendar: Calendar): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(calendar.time)
    }
}
