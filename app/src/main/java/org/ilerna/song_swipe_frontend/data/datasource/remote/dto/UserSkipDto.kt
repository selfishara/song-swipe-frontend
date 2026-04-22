package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSkipDto(
    val id: Int? = null,

    @SerialName("user_id")
    val userId: String,

    @SerialName("track_id")
    val trackId: String,

    @SerialName("created_at")
    val createdAt: String? = null
)