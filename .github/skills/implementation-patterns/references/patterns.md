# Song Swipe Implementation Patterns Reference

## Pattern 1: Use Case

Use cases live in `domain/usecase/` organized by feature. Each has a single responsibility.

```kotlin
// domain/usecase/feature/MyNewUseCase.kt
package org.ilerna.song_swipe_frontend.domain.usecase.feature

class MyNewUseCase(
    private val repository: MyRepository
) {
    suspend operator fun invoke(param: String): NetworkResult<MyModel> {
        if (param.isBlank()) {
            return NetworkResult.Error("Parameter cannot be blank")
        }
        return repository.getData(param)
    }
}
```

Rules:
- Single `operator fun invoke()` method (or named methods for complex cases like `LoginUseCase`)
- Accept repository interfaces (from domain layer), not implementations
- Return `NetworkResult<T>` for all operations
- Validate inputs before delegating to repository
- No Android/framework imports

## Pattern 2: Repository Interface (Domain)

Contracts live in `domain/repository/`. Define what, not how.

```kotlin
// domain/repository/MyRepository.kt
package org.ilerna.song_swipe_frontend.domain.repository

interface MyRepository {
    suspend fun getData(id: String): NetworkResult<MyModel>
    suspend fun saveData(item: MyModel): NetworkResult<Unit>
}
```

Rules:
- All methods are `suspend`
- Return `NetworkResult<T>`, never raw types or Response objects
- Use domain models in signatures, never DTOs
- Located in domain layer (no framework dependencies)

## Pattern 3: DTO

DTOs live in `data/datasource/remote/dto/`. One per API response shape.

```kotlin
// data/datasource/remote/dto/MyApiItemDto.kt
package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

data class MyApiItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("image_url") val imageUrl: String?
)
```

Rules:
- Prefix with API name + "Dto" suffix (e.g., `SpotifyTrackDto`, `DeezerTrackDto`)
- Use `@SerializedName` for JSON field mapping
- All fields nullable where API might omit them
- No domain logic — pure data containers

## Pattern 4: Mapper

Mappers live in `data/repository/mapper/`. Convert DTOs to domain models.

```kotlin
// data/repository/mapper/MyMapper.kt
package org.ilerna.song_swipe_frontend.data.repository.mapper

object MyMapper {
    fun toDomain(dto: MyApiItemDto): MyModel {
        return MyModel(
            id = dto.id,
            name = dto.displayName ?: dto.id, // Fallback
            imageUrl = dto.imageUrl
        )
    }
}
```

Rules:
- Use `object` for stateless mappers
- Provide sensible defaults/fallbacks for nullable DTO fields
- Only convert DTO → Domain (one direction)
- Named `toDomain()` by convention

## Pattern 5: Retrofit API Interface

API interfaces live in `data/datasource/remote/api/`.

```kotlin
// data/datasource/remote/api/MyApi.kt
package org.ilerna.song_swipe_frontend.data.datasource.remote.api

interface MyApi {
    @GET("v1/endpoint/{id}")
    suspend fun getItem(@Path("id") id: String): Response<MyApiItemDto>

    @POST("v1/endpoint")
    suspend fun createItem(@Body request: MyCreateRequestDto): Response<MyApiResponseDto>
}
```

Rules:
- Return `Response<T>` (Retrofit), not raw types
- Auth header added automatically by `SpotifyAuthInterceptor` (for Spotify API)
- Deezer API has its own Retrofit instance (no auth needed)
- Use `@SerializedName` in request DTOs for JSON body fields

## Pattern 6: DataSource Implementation

DataSources live in `data/datasource/remote/impl/`. Wrap API calls with `ApiResponse`.

```kotlin
// data/datasource/remote/impl/MyDataSourceImpl.kt
package org.ilerna.song_swipe_frontend.data.datasource.remote.impl

class MyDataSourceImpl(private val api: MyApi) {
    suspend fun getItem(id: String): ApiResponse<MyApiItemDto> {
        return try {
            ApiResponse.create(api.getItem(id))
        } catch (e: Exception) {
            ApiResponse.create(e)
        }
    }
}
```

Rules:
- Wrap all API calls in try-catch
- Convert `Response<T>` to `ApiResponse<T>` using factory methods
- No business logic — only API call wrapping

## Pattern 7: Repository Implementation

Implementations live in `data/repository/impl/`. Convert `ApiResponse` to `NetworkResult` and map DTOs.

```kotlin
// data/repository/impl/MyRepositoryImpl.kt
package org.ilerna.song_swipe_frontend.data.repository.impl

class MyRepositoryImpl(
    private val dataSource: MyDataSourceImpl
) : MyRepository {

    override suspend fun getData(id: String): NetworkResult<MyModel> {
        return when (val response = dataSource.getItem(id)) {
            is ApiResponse.Success -> {
                val domain = MyMapper.toDomain(response.data)
                NetworkResult.Success(domain)
            }
            is ApiResponse.Error -> {
                AnalyticsManager.logException(Exception("API error: ${response.message}"))
                NetworkResult.Error(response.message, response.code)
            }
        }
    }
}
```

Rules:
- Convert `ApiResponse` → `NetworkResult` with when expression
- Map DTOs to domain models via Mappers
- Log errors to Crashlytics via `AnalyticsManager`
- Catch unexpected exceptions and wrap in `NetworkResult.Error`

## Pattern 8: ViewModel

ViewModels live in `presentation/screen/<feature>/`. Expose StateFlow for UI.

```kotlin
// presentation/screen/feature/MyViewModel.kt
package org.ilerna.song_swipe_frontend.presentation.screen.feature

class MyViewModel(
    private val myUseCase: MyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<MyModel>>(UiState.Idle)
    val state: StateFlow<UiState<MyModel>> = _state.asStateFlow()

    fun loadData(id: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            when (val result = myUseCase(id)) {
                is NetworkResult.Success -> _state.value = UiState.Success(result.data)
                is NetworkResult.Error -> _state.value = UiState.Error(result.message)
                is NetworkResult.Loading -> {} // Not typically returned by use cases
            }
        }
    }
}
```

Rules:
- Use `MutableStateFlow` (private) + `StateFlow` (public, via `asStateFlow()`)
- Use `UiState<T>` sealed class for screen state
- Launch coroutines in `viewModelScope`
- Convert `NetworkResult` to `UiState` in the ViewModel
- Never expose MutableStateFlow publicly

## Pattern 9: Compose Screen

Screens live in `presentation/screen/<feature>/`. Observe ViewModel state.

```kotlin
// presentation/screen/feature/MyScreen.kt
package org.ilerna.song_swipe_frontend.presentation.screen.feature

@Composable
fun MyScreen(
    viewModel: MyViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    when (val currentState = state) {
        is UiState.Idle -> { /* Initial state */ }
        is UiState.Loading -> LoadingIndicator()
        is UiState.Success -> MyContent(data = currentState.data)
        is UiState.Error -> ErrorMessage(message = currentState.message)
    }
}
```

Rules:
- Observe StateFlow with `collectAsState()`
- Handle all UiState variants exhaustively
- Use `LoadingIndicator` from components/animation/ for loading states
- Pass ViewModel as parameter (manual DI, no hiltViewModel())
- Use `modifier` parameter for layout flexibility

## Pattern 10: Navigation Route

Add new screens to the navigation system:

1. Add screen to `Screen` sealed class in `presentation/navigation/Screen.kt`:
```kotlin
data object MyFeature : Screen("my_feature")
```

2. Add composable to NavHost in `AppNavigation.kt`:
```kotlin
composable(Screen.MyFeature.route) {
    MyScreen(viewModel = myViewModel)
}
```

3. Optionally add to bottom navigation in `BottomNavItem`:
```kotlin
data object MyFeature : BottomNavItem(Screen.MyFeature, Icons.Default.Star, "My Feature")
```

## Full Feature Checklist

When adding a new feature end-to-end:

- [ ] Domain model in `domain/model/`
- [ ] Repository interface in `domain/repository/`
- [ ] Use case in `domain/usecase/<feature>/`
- [ ] DTO in `data/datasource/remote/dto/`
- [ ] API endpoint in `data/datasource/remote/api/`
- [ ] DataSource in `data/datasource/remote/impl/`
- [ ] Mapper in `data/repository/mapper/`
- [ ] Repository impl in `data/repository/impl/`
- [ ] ViewModel in `presentation/screen/<feature>/`
- [ ] Screen composable in `presentation/screen/<feature>/`
- [ ] Navigation route in `presentation/navigation/`
- [ ] DI wiring in `MainActivity`
