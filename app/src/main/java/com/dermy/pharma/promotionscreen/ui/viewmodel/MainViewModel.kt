package com.dermy.pharma.promotionscreen.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dermy.pharma.promotionscreen.PromoApplication
import com.dermy.pharma.promotionscreen.data.model.MediaItem
import com.dermy.pharma.promotionscreen.data.model.MediaType
import com.dermy.pharma.promotionscreen.data.repository.PromoRepository
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

class MainViewModel(
    application: Application
) : ViewModel() {

    private val applicationContext: Application = application
    private val repository: PromoRepository = PromoApplication.getPromoRepository()
    private val authDataSource = PromoApplication.getAuthDataSource()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var slideJob: Job? = null

    init {
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

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                repository.refreshConfig()
                val config = repository.getConfig()
                val items = repository.getMediaItems(applicationContext)
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
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al cargar"
                    )
                }
            }
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
        stopSlideTimer()
    }
}
