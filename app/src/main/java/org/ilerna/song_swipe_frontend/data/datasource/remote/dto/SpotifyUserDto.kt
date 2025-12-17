package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for Spotify user profile response
 */
data class SpotifyUserDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("display_name")
    val displayName: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("images")
    val images: List<SpotifyImageDto>?,

    @SerializedName("country")
    val country: String?,

    @SerializedName("product")
    val product: String?,

    @SerializedName("followers")
    val followers: SpotifyFollowersDto?,

    @SerializedName("uri")
    val uri: String?,

    @SerializedName("href")
    val href: String?
)

/**
 * DTO for Spotify followers object
 */
data class SpotifyFollowersDto(
    @SerializedName("href")
    val href: String?,

    @SerializedName("total")
    val total: Int?
)