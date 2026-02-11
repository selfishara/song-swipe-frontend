package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO that represents the response from Spotify API when fetching browse categories.
 * Used to retrieve available Spotify categories (used as genres).
 */
data class SpotifyCategoriesResponseDto(
    @SerializedName("categories")
    val categories: SpotifyCategoriesPagingDto
)

/**
 * DTO that represents paginated categories data from Spotify.
 */
data class SpotifyCategoriesPagingDto(
    @SerializedName("items")
    val items: List<SpotifyCategoryDto>
)