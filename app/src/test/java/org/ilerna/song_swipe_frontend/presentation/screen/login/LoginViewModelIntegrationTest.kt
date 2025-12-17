package org.ilerna.song_swipe_frontend.presentation.screen.login


import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.domain.usecase.LoginUseCase
import org.junit.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelIntegrationTest {

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
    fun `init without active session should set state to Idle`() = runTest {
        // Given
        val repository = FakeAuthRepository(hasSession = false)
        val useCase = LoginUseCase(repository)

        // When
        val viewModel = LoginViewModel(useCase)

        advanceUntilIdle()

        // Then
        assertTrue(viewModel.authState.value is AuthState.Idle)
    }

    @Test
    fun `init with active session and user should set state to Success`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User"
        )
        val repository = FakeAuthRepository(
            hasSession = true,
            user = user
        )
        val useCase = LoginUseCase(repository)

        // When
        val viewModel = LoginViewModel(useCase)

        advanceUntilIdle()

        // Then
        assertTrue(viewModel.authState.value is AuthState.Success)
    }

    @Test
    fun `handleAuthCallback success should update state to Success`() = runTest {
        // Given
        val repository = FakeAuthRepository(
            authResult = AuthState.Success("user123")
        )
        val useCase = LoginUseCase(repository)
        val viewModel = LoginViewModel(useCase)

        advanceUntilIdle()

        // When
        viewModel.handleAuthCallback("songswipe://callback#success")

        advanceUntilIdle()

        // Then
        assertTrue(viewModel.authState.value is AuthState.Success)
    }

    @Test
    fun `handleAuthCallback error should update state to Error`() = runTest {
        // Given
        val repository = FakeAuthRepository(throwOnCallback = true)
        val useCase = LoginUseCase(repository)
        val viewModel = LoginViewModel(useCase)

        advanceUntilIdle()

        // When
        viewModel.handleAuthCallback("songswipe://callback#error")

        advanceUntilIdle()

        // Then
        assertTrue(viewModel.authState.value is AuthState.Error)
    }
}