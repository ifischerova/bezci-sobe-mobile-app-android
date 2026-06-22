package cz.bezcisobe.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/** Supplies the current auth token without blocking (reads an in-memory cache). */
fun interface TokenProvider {
    fun currentToken(): String?
}

class AuthInterceptor(private val tokenProvider: TokenProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider.currentToken()
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
