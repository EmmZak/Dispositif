package com.app.app.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Utils {

    companion object {
        fun getFormattedDateTime(): String {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy - HH:mm")
            return current.format(formatter)
        }
    }

}