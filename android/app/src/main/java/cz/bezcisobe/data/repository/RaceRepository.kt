package cz.bezcisobe.data.repository

import cz.bezcisobe.data.local.RaceDao
import cz.bezcisobe.data.mapper.toDomain
import cz.bezcisobe.data.mapper.toEntity
import cz.bezcisobe.data.remote.ApiService
import cz.bezcisobe.data.remote.dto.RaceDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Supplies the bundled demo races (from assets) used as an offline fallback when the
 * backend is unreachable and the cache is empty. Abstracted so it can be faked in tests
 * without an Android Context.
 */
fun interface SampleRaceSource {
    suspend fun load(): List<RaceDto>
}

class RaceRepository @Inject constructor(
    private val api: ApiService,
    private val dao: RaceDao,
    private val sampleRaceSource: SampleRaceSource,
) {
    fun observeRaces(): Flow<List<Race>> = dao.observeRaces().map { list -> list.map { it.toDomain() } }

    suspend fun refresh() {
        try {
            val remote = api.getRaces().map { it.toEntity() }
            dao.upsertAll(remote)
        } catch (e: Exception) {
            // Offline-first: if the backend is unreachable AND we have nothing cached,
            // seed the demo data bundled in assets so the app is usable standalone.
            if (dao.count() == 0) {
                dao.upsertAll(sampleRaceSource.load().map { it.toEntity() })
            } else {
                throw e
            }
        }
    }

    suspend fun getRace(id: String): Race? = dao.getById(id)?.toDomain()
}
