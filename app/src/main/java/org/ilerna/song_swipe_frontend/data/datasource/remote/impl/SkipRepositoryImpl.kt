package org.ilerna.song_swipe_frontend.data.repository.impl

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.UserSkipDto
import org.ilerna.song_swipe_frontend.domain.repository.SkipRepository

class SkipRepositoryImpl(
    private val supabase: SupabaseClient
) : SkipRepository {

    override suspend fun saveSkip(trackId: String): NetworkResult<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return NetworkResult.Error("Usuario no autenticado")

            val skip = UserSkipDto(
                userId = userId,
                trackId = trackId
            )

            supabase
                .from("user_skips")
                .insert(skip)

            NetworkResult.Success(Unit)

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Error al guardar skip")
        }
    }

    override suspend fun getSkippedTrackIds(): NetworkResult<List<String>> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return NetworkResult.Error("Usuario no autenticado")

            val result = supabase
                .from("user_skips")
                .select(
                    columns = Columns.list("track_id")
                ) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<UserSkipDto>()

            val trackIds = result.map { it.trackId }

            NetworkResult.Success(trackIds)

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Error al obtener skips")
        }
    }
}