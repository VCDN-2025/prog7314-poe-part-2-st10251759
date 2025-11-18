package vcmsa.projects.prog7314.game

import vcmsa.projects.prog7314.data.models.DifficultyLevel

/*
    Code Attribution for: Data Classes
    ===================================================
    Kotlin, 2025a. Data classes | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://kotlinlang.org/docs/data-classes.html>
    [Accessed 18 November 2025].
*/

/**
 * Game configuration and difficulty settings
 */
object GameConfig {

    /**
     * Level configuration based on level number (1-16)
     */
    data class LevelConfig(
        val levelNumber: Int,
        val difficulty: DifficultyLevel,
        val gridRows: Int,
        val gridColumns: Int,
        val maxMoves: Int,
        val timeLimit: Int, // in seconds, 0 = no limit
        val matchScore: Int = 100,
        val timeBonusPerSecond: Int = 10
    ) {
        val totalPairs: Int get() = (gridRows * gridColumns) / 2
        val maxPossibleScore: Int get() = (totalPairs * matchScore) + (timeLimit * timeBonusPerSecond)
    }

    /**
     * Get level configuration based on level number
     */
    fun getLevelConfig(levelNumber: Int): LevelConfig {
        return when (levelNumber) {
            // BEGINNER (Levels 1-4): 3x2 grid
            in 1..4 -> LevelConfig(
                levelNumber = levelNumber,
                difficulty = DifficultyLevel.BEGINNER,
                gridRows = 3,
                gridColumns = 2,
                maxMoves = 40,
                timeLimit = 0, // No time limit for beginners
                matchScore = 100,
                timeBonusPerSecond = 0
            )

            // INTERMEDIATE (Levels 5-8): 4x3 grid
            in 5..8 -> LevelConfig(
                levelNumber = levelNumber,
                difficulty = DifficultyLevel.INTERMEDIATE,
                gridRows = 4,
                gridColumns = 3,
                maxMoves = 30,
                timeLimit = 120, // 2 minutes
                matchScore = 150,
                timeBonusPerSecond = 5
            )

            // HARD (Levels 9-12): 5x4 grid
            in 9..12 -> LevelConfig(
                levelNumber = levelNumber,
                difficulty = DifficultyLevel.HARD,
                gridRows = 5,
                gridColumns = 4,
                maxMoves = 40,
                timeLimit = 180, // 3 minutes
                matchScore = 200,
                timeBonusPerSecond = 8
            )

            // EXPERT (Levels 13-16): 6x4 grid
            in 13..16 -> LevelConfig(
                levelNumber = levelNumber,
                difficulty = DifficultyLevel.EXPERT,
                gridRows = 6,
                gridColumns = 4,
                maxMoves = 50,
                timeLimit = 240, // 4 minutes
                matchScore = 250,
                timeBonusPerSecond = 10
            )

            else -> throw IllegalArgumentException("Invalid level number: $levelNumber. Must be 1-16")
        }
    }

    /**
     * Calculate stars based on score percentage
     */
    fun calculateStars(score: Int, maxPossibleScore: Int): Int {
        if (maxPossibleScore <= 0) return 0

        val percentage = (score.toFloat() / maxPossibleScore.toFloat()) * 100

        return when {
            percentage >= 90 -> 3 // 90%+ = 3 stars
            percentage >= 70 -> 2 // 70-89% = 2 stars
            percentage >= 50 -> 1 // 50-69% = 1 star
            else -> 0 // <50% = 0 stars
        }
    }

    /**
     * Calculate final score with time bonus
     */
    fun calculateFinalScore(
        matchScore: Int,
        pairsMatched: Int,
        timeRemaining: Int,
        timeBonusPerSecond: Int
    ): Int {
        val baseScore = matchScore * pairsMatched
        val timeBonus = timeRemaining * timeBonusPerSecond
        return baseScore + timeBonus
    }

    /**
     * Calculate time bonus
     */
    fun calculateTimeBonus(timeRemaining: Int, timeBonusPerSecond: Int): Int {
        return timeRemaining * timeBonusPerSecond
    }

    /**
     * Check if level is failed
     */
    fun isLevelFailed(moves: Int, maxMoves: Int, timeElapsed: Int, timeLimit: Int): Boolean {
        if (maxMoves > 0 && moves >= maxMoves) return true
        if (timeLimit > 0 && timeElapsed >= timeLimit) return true
        return false
    }

    /**
     * Star rating thresholds
     */
    object StarThresholds {
        const val THREE_STAR_PERCENTAGE = 90
        const val TWO_STAR_PERCENTAGE = 70
        const val ONE_STAR_PERCENTAGE = 50
    }
}