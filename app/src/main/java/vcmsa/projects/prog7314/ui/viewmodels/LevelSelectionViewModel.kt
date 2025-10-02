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
        val database = AppDatabase.getDatabase(application)
        levelRepository = LevelRepository(database.levelProgressDao())

        loadLevelsProgress()
    }

    /**
     * Load levels progress for current user
     */
    private fun loadLevelsProgress() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // FIXED: Get userId properly
                val userId = AuthManager.getCurrentUser()?.uid

                if (userId != null) {
                    Log.d(TAG, "Loading levels for user: $userId")

                    // Initialize levels if not exists
                    val existingLevels = levelRepository.getAllLevelsProgress(userId)
                    if (existingLevels.isEmpty()) {
                        Log.d(TAG, "No levels found, initializing...")
                        levelRepository.initializeLevelsForUser(userId)
                    }

                    // Collect levels progress
                    levelRepository.getAllLevelsProgressFlow(userId).collect { levels ->
                        _levelsProgress.value = levels
                        Log.d(TAG, "✅ Loaded ${levels.size} levels")
                    }

                    // Collect completed count
                    levelRepository.getCompletedLevelsCountFlow(userId).collect { count ->
                        _completedCount.value = count
                        Log.d(TAG, "Completed levels: $count")
                    }
                } else {
                    Log.e(TAG, "No user ID found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading levels: ${e.message}", e)
            } finally {
                _isLoading.value = false
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