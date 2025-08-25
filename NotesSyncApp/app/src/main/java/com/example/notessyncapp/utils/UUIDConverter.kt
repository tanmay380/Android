package com.example.notessyncapp.utils

import androidx.room.TypeConverter
import java.util.UUID

class UUIDConverter {
    @TypeConverter
    fun fromUUID(uuid: UUID): String? {
        return uuid.toString()
    }

    @TypeConverter
    fun uuidFromString(string: String?): UUID? {
        return UUID.fromString(string)
    }

    companion object {
        fun uuidFromString(id: String): UUID {
            return uuidFromString(id)
        }
    }
}