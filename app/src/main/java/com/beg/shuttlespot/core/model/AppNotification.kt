package com.beg.shuttlespot.core.model

// Overview: Defines app notification for the core model area of ShuttleSpot.

enum class NotificationTarget { LIVE_TRIP, ANNOUNCEMENT, NONE }

// Defines the app notification type.
data class AppNotification(
    val id: String,
    val profileId: String,
    val title: String,
    val body: String,
    val target: NotificationTarget,
    val createdAtEpochMillis: Long,
    val isRead: Boolean,
)
