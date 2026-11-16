package com.beg.shuttlespot.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteProfileDto(
    val id: String,
    val role: String,

    @SerialName("display_name")
    val displayName: String,

    val email: String? = null,

    @SerialName("phone_e164")
    val phoneE164: String? = null,
)