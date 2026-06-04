package cz.bezcisobe.data.repository

import cz.bezcisobe.data.local.RaceDao
import cz.bezcisobe.data.mapper.toDomain
import cz.bezcisobe.data.mapper.toEntity
import cz.bezcisobe.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RaceRepository @Inject constructor(
    private val api: ApiService,
    private val dao: RaceDao,
) {
    fun observeRaces(): Flow<List<Race>> = dao.observeRaces().map { list -> list.map { it.toDomain() } }

    suspend fun refresh() {
        val remote = api.getRaces().map { it.toEntity() }
        dao.upsertAll(remote)
    }

    suspend fun getRace(id: String): Race? = dao.getById(id)?.toDomain()
}
