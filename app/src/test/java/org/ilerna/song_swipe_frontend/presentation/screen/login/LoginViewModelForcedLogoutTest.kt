package org.ilerna.song_swipe_frontend.presentation.screen.login

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.domain.usecase.LoginUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelForcedLogoutTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `null token while authenticated triggers forced sign-out and clears session stores`() =
        runTest {
            val tokenFlow = MutableStateFlow<String?>("valid-token")
            var clearCalls = 0
            val repository = FakeAuthRepository(
                hasSession = true,
                user = User(id = "u1", email = "a@b.c", displayName = "A")
            )
            val viewModel = LoginViewModel(
                loginUseCase = LoginUseCase(repository),
                analyticsManager = null,
                spotifyAccessTokenFlow = tokenFlow,
                clearSessionDataStores = { clearCalls++ }
            )
            advanceUntilIdle()
            assertTrue(viewModel.authState.value is AuthState.Success)

            // Token expires / authenticator clears it
            tokenFlow.value = null
            advanceUntilIdle()

            assertTrue(viewModel.authState.value is AuthState.Idle)
            assertEquals(1, clearCalls)
        }

    @Test
    fun `null token while idle does not trigger session-store clear`() = runTest {
        val tokenFlow = MutableStateFlow<String?>(null)
        var clearCalls = 0
        val repository = FakeAuthRepository(hasSession = false)
        val viewModel = LoginViewModel(
            loginUseCase = LoginUseCase(repository),
            analyticsManager = null,
            spotifyAccessTokenFlow = tokenFlow,
            clearSessionDataStores = { clearCalls++ }
        )
        advanceUntilIdle()
        assertTrue(viewModel.authState.value is AuthState.Idle)

        // Still null — no transition while authenticated
        tokenFlow.value = null
        advanceUntilIdle()

        assertEquals(0, clearCalls)
    }

    @Test
    fun `manual signOut also clears session stores`() = runTest {
        val tokenFlow = MutableStateFlow<String?>("valid-token")
        var clearCalls = 0
        val repository = FakeAuthRepository(
            hasSession = true,
            user = User(id = "u1", email = "a@b.c", displayName = "A")
        )
        val viewModel = LoginViewModel(
            loginUseCase = LoginUseCase(repository),
            analyticsManager = null,
            spotifyAccessTokenFlow = tokenFlow,
            clearSessionDataStores = { clearCalls++ }
        )
        advanceUntilIdle()

        viewModel.signOut()
        advanceUntilIdle()

        assertTrue(viewModel.authState.value is AuthState.Idle)
        assertEquals(1, clearCalls)
    }
}
