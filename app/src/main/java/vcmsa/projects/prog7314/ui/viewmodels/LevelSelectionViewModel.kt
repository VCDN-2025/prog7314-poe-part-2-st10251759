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

/*
    Code Attribution for: Creating ViewModels
    ===================================================
    Android Developers, 2019b. ViewModel Overview | Android Developers (Version unknown) [Source code].
    Available at: <https://developer.android.com/topic/libraries/architecture/viewmodel>
    [Accessed 18 November 2025].
*/

/**
 * ViewModel that manages the level selection screen.
 * Handles loading and displaying progress for all available levels.
 */
class LevelSelectionViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "LevelSelectionViewModel"

    // Repository for accessing level data
    private val levelRepository: LevelRepository

    // List of all levels with their progress information
    private val _levelsProgress = MutableStateFlow<List<LevelProgressEntity>>(emptyList())
    val levelsProgress: StateFlow<List<LevelProgressEntity>> = _levelsProgress.asStateFlow()

    // Count of how many levels the player has completed
    private val _completedCount = MutableStateFlow(0)
    val completedCount: StateFlow<Int> = _completedCount.asStateFlow()

    // Whether data is currently being loaded
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Initialize the repository provider before accessing any repositories
        RepositoryProvider.initialize(application)

        // Get the level repository instance
        levelRepository = RepositoryProvider.getLevelRepository()

        // Load all levels for the current user
        loadLevelsProgress()
    }

    /**
     * Loads level progress data for the currently logged in user.
     * Initializes levels if this is the first time the user is playing.
     * Sets up continuous observation of level progress and completion count.
     */
    private fun loadLevelsProgress() {
        val userId = AuthManager.getCurrentUser()?.uid

        // Exit early if no user is logged in
        if (userId == null) {
            Log.e(TAG, "No user ID found")
            _levelsProgress.value = emptyList()
            _completedCount.value = 0
            return
        }

        // Initialize level data if needed
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Loading levels for user: $userId")

                // Check if levels exist for this user, create them if not
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

        // Set up continuous observation of level progress
        // This flow updates automatically when level data changes in the database
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

        // Set up continuous observation of completion count in a separate coroutine
        // This prevents blocking the levels flow collection
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
     * Manually triggers a reload of level data.
     * Useful for pull-to-refresh or when returning from a completed level.
     */
    fun refreshLevels() {
        loadLevelsProgress()
    }
}