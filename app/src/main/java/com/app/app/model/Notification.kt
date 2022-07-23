package com.app.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.app.dto.NotificationBody
import java.util.*

@Entity
data class Notification(
    @PrimaryKey var id: Int?,
    @ColumnInfo(name = "RECEPTION_TIME") var receptionTime: Date,
    @ColumnInfo(name = "BODY") var body: NotificationBody?,
    // FROM is sender's FCM token
    @ColumnInfo(name = "FROM") var from: String?
) {

    constructor(): this(
        null,
        Date(),
        null,
        null
    )
}