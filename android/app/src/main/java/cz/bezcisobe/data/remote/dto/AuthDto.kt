package cz.bezcisobe.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(val username: String, val password: String)

@Serializable
data class AuthResponseDto(
    val token: String,
    val userId: String,
    val username: String,
    val roles: List<String> = emptyList(),
)

@Serializable
data class RegisterRequestDto(
    val username: String,
    val email: String,
    val password: String,
    val language: String? = null,
)

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val roles: List<String> = emptyList(),
)
