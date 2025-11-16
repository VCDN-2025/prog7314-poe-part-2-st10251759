package vcmsa.projects.prog7314.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.data.AppDatabase
import vcmsa.projects.prog7314.data.entities.LevelProgressEntity
import vcmsa.projects.prog7314.data.repository.LevelRepository
import vcmsa.projects.prog7314.utils.AuthManager
import vcmsa.projects.prog7314.data.repository.RepositoryProvider

class LevelSelectionViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "LevelSelectionViewModel"
    private val levelRepository: LevelRepository

    private val _levelsProgress = MutableStateFlow<List<LevelProgressEntity>>(emptyList())
    val levelsProgress: StateFlow<List<LevelProgressEntity>> = _levelsProgress.asStateFlow()

    private val _completedCount = MutableStateFlow(0)
    val completedCount: StateFlow<Int> = _completedCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        RepositoryProvider.initialize(application)  // 🔥 INITIALIZE FIRST
        levelRepository = RepositoryProvider.getLevelRepository()  // ✅ HAS CONTEXT
        loadLevelsProgress()
    }

    /**
     * Load levels progress for current user
     */
    private fun loadLevelsProgress() {
        val userId = AuthManager.getCurrentUser()?.uid

        if (userId == null) {
            Log.e(TAG, "No user ID found")
            _levelsProgress.value = emptyList()
            _completedCount.value = 0
            return
        }

        // Initialize levels in a separate coroutine
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Loading levels for user: $userId")

                // Initialize levels if not exists
                val existingLevels = levelRepository.getAllLevelsProgress(userId)
                if (existingLevels.isEmpty()) {
                    Log.d(TAG, "No levels found, initializing...")
                    levelRepository.initializeLevelsForUser(userId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing levels: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }

        // FIXED: Launch separate coroutines for each Flow collection
        // Collect levels progress
        viewModelScope.launch {
            try {
                levelRepository.getAllLevelsProgressFlow(userId).collect { levels ->
                    _levelsProgress.value = levels
                    Log.d(TAG, "✅ Loaded ${levels.size} levels")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error collecting levels flow: ${e.message}", e)
            }
        }

        // Collect completed count in a separate coroutine
        viewModelScope.launch {
            try {
                levelRepository.getCompletedLevelsCountFlow(userId).collect { count ->
                    _completedCount.value = count
                    Log.d(TAG, "✅ Completed levels updated: $count")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error collecting completed count flow: ${e.message}", e)
            }
        }
    }

    /**
     * Refresh levels data
     */
    fun refreshLevels() {
        loadLevelsProgress()
    }
}