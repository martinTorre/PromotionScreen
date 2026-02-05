package com.dermy.pharma.promotionscreen.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ProcessLifecycleOwner
import com.dermy.pharma.promotionscreen.PromoApplication
import com.dermy.pharma.promotionscreen.data.model.MediaItem
import com.dermy.pharma.promotionscreen.data.model.MediaType
import com.dermy.pharma.promotionscreen.data.repository.PromoRepository
import com.dermy.pharma.promotionscreen.util.CacheHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val mediaItems: List<MediaItem> = emptyList(),
    val currentIndex: Int = 0,
    val slideTimeSeconds: Long = 30L,
    val accessToken: String? = null,
    val isLoading: Boolean = true,
    val needsSignIn: Boolean = false,
    val errorMessage: String? = null
)

private const val REFRESH_INTERVAL_MS = 24L * 60 * 60 * 1000

class MainViewModel(
    application: Application
) : ViewModel() {

    private val applicationContext: Application = application
    private val repository: PromoRepository = PromoApplication.getPromoRepository()
    private val authDataSource = PromoApplication.getAuthDataSource()
    private val localSettings = PromoApplication.getLocalSettings(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var slideJob: Job? = null
    private var dailyRefreshJob: Job? = null

    private val processLifecycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_START && authDataSource.getLastSignedInAccount(applicationContext) != null) {
            loadInitialData(backgroundRefresh = true)
        }
    }

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(processLifecycleObserver)
        if (authDataSource.getLastSignedInAccount(applicationContext) == null) {
            _uiState.update { it.copy(needsSignIn = true, isLoading = false) }
        } else {
            loadInitialData()
        }
    }

    fun getSignInIntent(): Intent {
        return authDataSource.getSignInIntent(applicationContext)
    }

    fun onSignInSuccess() {
        _uiState.update { it.copy(needsSignIn = false) }
        loadInitialData()
    }

    fun loadInitialData(backgroundRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    mediaItems = emptyList(),
                    currentIndex = 0,
                    isLoading = true,
                    errorMessage = null
                )
            }
            val lastFetch = localSettings.getLastMediaFetchTime()
            val now = System.currentTimeMillis()
            val shouldClearCache = lastFetch == null || (now - lastFetch >= REFRESH_INTERVAL_MS)
            runCatching {
                if (shouldClearCache) {
                    CacheHelper.clearMediaCache(applicationContext)
                }
                repository.refreshConfig()
                val config = repository.getConfig()
                val items = repository.getMediaItems(applicationContext)
                localSettings.setLastMediaFetchTime(System.currentTimeMillis())
                val account = authDataSource.getLastSignedInAccount(applicationContext)
                val token = account?.let { authDataSource.getAccessToken(applicationContext, it) }
                _uiState.update {
                    it.copy(
                        mediaItems = items,
                        slideTimeSeconds = config.slideTimeSeconds,
                        currentIndex = 0,
                        accessToken = token,
                        isLoading = false,
                        errorMessage = if (items.isEmpty()) "No hay medios en la carpeta" else null
                    )
                }
                if (items.isNotEmpty()) {
                    startSlideTimer()
                }
                startDailyRefreshJob()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        mediaItems = emptyList(),
                        isLoading = false,
                        errorMessage = e.message ?: "Error al cargar"
                    )
                }
            }
        }
    }

    private fun startDailyRefreshJob() {
        dailyRefreshJob?.cancel()
        dailyRefreshJob = viewModelScope.launch {
            delay(REFRESH_INTERVAL_MS)
            loadInitialData()
        }
    }

    fun onVideoEnded() {
        advanceToNext()
    }

    fun startSlideTimer() {
        slideJob?.cancel()
        val items = _uiState.value.mediaItems
        val current = _uiState.value.currentIndex
        if (items.isEmpty()) return
        val currentItem = items.getOrNull(current) ?: return
        if (currentItem.type != MediaType.IMAGE) return
        val slideTimeMs = _uiState.value.slideTimeSeconds * 1000
        slideJob = viewModelScope.launch {
            delay(slideTimeMs)
            advanceToNext()
        }
    }

    fun stopSlideTimer() {
        slideJob?.cancel()
        slideJob = null
    }

    private fun advanceToNext() {
        val items = _uiState.value.mediaItems
        if (items.isEmpty()) return
        val nextIndex = (_uiState.value.currentIndex + 1) % items.size
        _uiState.update { it.copy(currentIndex = nextIndex) }
        val nextItem = items[nextIndex]
        if (nextItem.type == MediaType.IMAGE) {
            startSlideTimer()
        }
    }

    override fun onCleared() {
        super.onCleared()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(processLifecycleObserver)
        stopSlideTimer()
        dailyRefreshJob?.cancel()
        dailyRefreshJob = null
    }
}
