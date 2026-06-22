package cz.bezcisobe.data.remote

import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Captures the request that the interceptor forwards via chain.proceed(). */
private class CapturingChain(private val original: Request) : Interceptor.Chain {
    var proceeded: Request? = null
    override fun request(): Request = original
    override fun proceed(request: Request): Response {
        proceeded = request
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("".toResponseBody(null))
            .build()
    }
    override fun connection(): Connection? = null
    override fun call(): Call = throw UnsupportedOperationException()
    override fun connectTimeoutMillis(): Int = 0
    override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
    override fun readTimeoutMillis(): Int = 0
    override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
    override fun writeTimeoutMillis(): Int = 0
    override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
}

class AuthInterceptorTest {
    private val request = Request.Builder().url("http://10.0.2.2:8080/api/races").build()

    @Test
    fun `attaches bearer header when token present`() {
        val interceptor = AuthInterceptor { "abc123" }
        val chain = CapturingChain(request)
        interceptor.intercept(chain)
        assertEquals("Bearer abc123", chain.proceeded?.header("Authorization"))
    }

    @Test
    fun `no header when token null`() {
        val interceptor = AuthInterceptor { null }
        val chain = CapturingChain(request)
        interceptor.intercept(chain)
        assertNull(chain.proceeded?.header("Authorization"))
    }

    @Test
    fun `no header when token blank`() {
        val interceptor = AuthInterceptor { "   " }
        val chain = CapturingChain(request)
        interceptor.intercept(chain)
        assertNull(chain.proceeded?.header("Authorization"))
    }
}
