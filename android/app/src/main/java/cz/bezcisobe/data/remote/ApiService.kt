package cz.bezcisobe.data.remote

import cz.bezcisobe.data.remote.dto.AuthResponseDto
import cz.bezcisobe.data.remote.dto.CreateRideRequestDto
import cz.bezcisobe.data.remote.dto.LoginRequestDto
import cz.bezcisobe.data.remote.dto.RaceDto
import cz.bezcisobe.data.remote.dto.RegisterRequestDto
import cz.bezcisobe.data.remote.dto.RideDto
import cz.bezcisobe.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("races")
    suspend fun getRaces(): List<RaceDto>

    @GET("races/{id}")
    suspend fun getRace(@Path("id") id: String): RaceDto

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): UserDto

    @GET("auth/me")
    suspend fun me(): UserDto

    @GET("rides")
    suspend fun getRides(@Query("raceId") raceId: Long): List<RideDto>

    @POST("rides")
    suspend fun createRide(@Body body: CreateRideRequestDto): RideDto

    @DELETE("rides/{id}")
    suspend fun deleteRide(@Path("id") id: String)

    @POST("rides/{id}/accept")
    suspend fun acceptRide(@Path("id") id: String): RideDto

    @POST("rides/{id}/cancel")
    suspend fun cancelRide(@Path("id") id: String): RideDto
}
