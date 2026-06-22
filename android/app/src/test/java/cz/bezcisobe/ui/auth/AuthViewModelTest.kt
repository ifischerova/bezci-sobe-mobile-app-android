package cz.bezcisobe.ui.auth

import cz.bezcisobe.data.repository.AuthRepositoryContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeAuthRepo(var failLogin: Boolean = false) : AuthRepositoryContract {
    val loggedIn = MutableStateFlow(false)
    override val isLoggedIn = loggedIn
    override val currentUserId = MutableStateFlow<String?>(null)
    override val currentUsername = MutableStateFlow<String?>(null)
    override suspend fun login(username: String, password: String) {
        if (failLogin) throw RuntimeException("Bad credentials"); loggedIn.value = true
    }
    override suspend fun register(username: String, email: String, password: String, language: String) {}
    override suspend fun logout() { loggedIn.value = false }
}

class AuthViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun s() = Dispatchers.setMain(dispatcher)
    @After fun t() = Dispatchers.resetMain()

    @Test fun `successful login sets Success`() = runTest {
        val vm = AuthViewModel(FakeAuthRepo())
        vm.login("ivka", "ivka123")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is AuthUiState.Success)
    }

    @Test fun `failed login sets Error`() = runTest {
        val vm = AuthViewModel(FakeAuthRepo(failLogin = true))
        vm.login("x", "y")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is AuthUiState.Error)
    }
}
