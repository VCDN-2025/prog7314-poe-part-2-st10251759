package vcmsa.projects.prog7314

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import vcmsa.projects.prog7314.data.models.GameCard
import vcmsa.projects.prog7314.data.models.GameTheme
import vcmsa.projects.prog7314.data.models.PlayerColor
import vcmsa.projects.prog7314.game.GameConfig
import vcmsa.projects.prog7314.game.GameEngine
import vcmsa.projects.prog7314.game.MultiplayerGameEngine

/*
    Code Attribution for: Unit testing
    ===================================================
    Android Developers, 2025. Build local unit tests (Version unknown) [Source code].
    Available at: <https://developer.android.com/training/testing/local-tests>
    [Accessed 18 November 2025].
*/


/**
 * Comprehensive Unit Tests for Game Engines
 * Tests both GameEngine and MultiplayerGameEngine functionality
 */
class GameEngineTests {

    private lateinit var gameEngine: GameEngine
    private lateinit var multiplayerEngine: MultiplayerGameEngine
    private lateinit var testTheme: GameTheme
    private lateinit var testConfig: GameConfig.LevelConfig

    @Before
    fun setup() {
        // Setup test theme with mock data
        testTheme = GameTheme.ANIMALS

        // Setup test configuration for a beginner level
        testConfig = GameConfig.LevelConfig(
            levelNumber = 1,
            difficulty = vcmsa.projects.prog7314.data.models.DifficultyLevel.BEGINNER,
            gridRows = 3,
            gridColumns = 2,
            maxMoves = 40,
            timeLimit = 0,
            matchScore = 100,
            timeBonusPerSecond = 0
        )

        gameEngine = GameEngine(testTheme, testConfig)
        multiplayerEngine = MultiplayerGameEngine(testTheme)
    }

    // TEST 1: Card Initialization
    @Test
    fun testGameEngineInitializeCards_CreatesCorrectNumberOfCards() {
        val cards = gameEngine.initializeCards()

        // Should create 6 cards (3 pairs for 3x2 grid)
        assertEquals(6, cards.size)

        // Verify all cards are face down initially
        cards.forEach { card ->
            assertFalse("Card should not be flipped initially", card.isFlipped)
            assertFalse("Card should not be matched initially", card.isMatched)
        }
    }

    // TEST 2: Card Pairing
    @Test
    fun testGameEngineInitializeCards_CreatesPairsCorrectly() {
        val cards = gameEngine.initializeCards()

        // Group cards by pairId and verify pairs
        val pairGroups = cards.groupBy { it.pairId }

        pairGroups.forEach { (pairId, pairCards) ->
            assertEquals("Each pair should have exactly 2 cards", 2, pairCards.size)
            assertEquals("Cards in pair should have same imageResId",
                pairCards[0].imageResId,
                pairCards[1].imageResId
            )
        }
    }

    // TEST 3: Flip Single Card
    @Test
    fun testGameEngineFlipCard_SingleCardFlipsSuccessfully() {
        val cards = gameEngine.initializeCards()
        val firstCard = cards[0]

        val (success, result) = gameEngine.flipCard(firstCard.id)

        assertTrue("Card flip should succeed", success)
        assertEquals("Result should be CARD_FLIPPED",
            GameEngine.FlipResult.CARD_FLIPPED,
            result
        )

        val gameState = gameEngine.getGameState()
        val flippedCard = gameState.cards.find { it.id == firstCard.id }
        assertTrue("Card should be flipped", flippedCard?.isFlipped == true)
    }

    // TEST 4: Match Detection
    @Test
    fun testGameEngineFlipCard_DetectsMatchCorrectly() {
        val cards = gameEngine.initializeCards()

        // Find a pair
        val firstPairCards = cards.filter { it.pairId == 0 }
        assertEquals("Should have a pair", 2, firstPairCards.size)

        // Flip first card
        gameEngine.flipCard(firstPairCards[0].id)

        // Flip matching card
        val (success, result) = gameEngine.flipCard(firstPairCards[1].id)

        assertTrue("Match should succeed", success)
        assertEquals("Should detect match",
            GameEngine.FlipResult.MATCH,
            result
        )

        // Clear matched cards
        gameEngine.clearMatchedCards()

        val gameState = gameEngine.getGameState()
        assertEquals("Should have 1 matched pair", 1, gameState.matchedPairs)
        assertEquals("Score should increase", 100, gameState.score)
    }

    // TEST 5: Mismatch Detection
    @Test
    fun testGameEngineFlipCard_DetectsMismatchCorrectly() {
        val cards = gameEngine.initializeCards()

        // Find two cards that are NOT a pair
        val firstCard = cards.first { it.pairId == 0 }
        val secondCard = cards.first { it.pairId == 1 }

        // Flip first card
        gameEngine.flipCard(firstCard.id)

        // Flip non-matching card
        val (success, result) = gameEngine.flipCard(secondCard.id)

        assertTrue("Flip should succeed", success)
        assertEquals("Should detect no match",
            GameEngine.FlipResult.NO_MATCH,
            result
        )

        // Reset cards
        gameEngine.resetFlippedCards()

        val gameState = gameEngine.getGameState()
        assertEquals("Should have 0 matched pairs", 0, gameState.matchedPairs)
        assertEquals("Score should be 0", 0, gameState.score)
    }

    // TEST 6: Game Completion
    @Test
    fun testGameEngine_CompletesGameWhenAllPairsMatched() {
        val cards = gameEngine.initializeCards()
        assertFalse("Game should not be complete initially", gameEngine.isGameComplete)

        // Match all pairs
        val pairs = cards.groupBy { it.pairId }
        pairs.forEach { (_, pairCards) ->
            gameEngine.flipCard(pairCards[0].id)
            gameEngine.flipCard(pairCards[1].id)
            gameEngine.clearMatchedCards()
        }

        assertTrue("Game should be complete", gameEngine.isGameComplete)

        val gameState = gameEngine.getGameState()
        assertEquals("All pairs should be matched",
            testConfig.totalPairs,
            gameState.matchedPairs
        )
    }

    // TEST 7: Score Calculation
    @Test
    fun testGameEngine_CalculatesFinalScoreCorrectly() {
        gameEngine.initializeCards()

        // Complete the game by matching all pairs manually
        val cards = gameEngine.getGameState().cards
        val pairs = cards.groupBy { it.pairId }

        pairs.forEach { (_, pairCards) ->
            gameEngine.flipCard(pairCards[0].id)
            gameEngine.flipCard(pairCards[1].id)
            gameEngine.clearMatchedCards()
        }

        // Calculate final score with time remaining
        val timeRemaining = 60
        val finalScore = gameEngine.calculateFinalScore(timeRemaining)

        assertEquals("Base score should match",
            testConfig.totalPairs * 100,
            finalScore.baseScore
        )
        assertTrue("Final score should include base score",
            finalScore.finalScore >= finalScore.baseScore
        )
    }

    // TEST 8: Multiplayer Card Initialization
    @Test
    fun testMultiplayerEngine_InitializesCorrectNumberOfCards() {
        val cards = multiplayerEngine.initializeCards()

        // Multiplayer uses fixed 6x4 grid = 24 cards
        assertEquals("Should create 24 cards", 24, cards.size)

        // Verify 12 pairs
        val pairs = cards.groupBy { it.pairId }
        assertEquals("Should have 12 pairs", 12, pairs.size)
    }

    // TEST 9: Multiplayer Turn Switching
    @Test
    fun testMultiplayerEngine_SwitchesTurnsOnMismatch() {
        val cards = multiplayerEngine.initializeCards()

        val gameState1 = multiplayerEngine.getGameState()
        val initialPlayer = if (gameState1.player1.isCurrentTurn) PlayerColor.RED else PlayerColor.BLUE

        // Find two non-matching cards
        val card1 = cards.first { it.pairId == 0 }
        val card2 = cards.first { it.pairId == 1 }

        // Flip and mismatch
        multiplayerEngine.flipCard(card1.id)
        multiplayerEngine.flipCard(card2.id)
        multiplayerEngine.resetFlippedCards()

        val gameState2 = multiplayerEngine.getGameState()
        val newPlayer = if (gameState2.player1.isCurrentTurn) PlayerColor.RED else PlayerColor.BLUE

        assertNotEquals("Turn should switch after mismatch", initialPlayer, newPlayer)
    }

    // TEST 10: Multiplayer Score Tracking
    @Test
    fun testMultiplayerEngine_TracksPlayerScoresCorrectly() {
        val cards = multiplayerEngine.initializeCards()

        // Player 1 makes a match
        val firstPair = cards.filter { it.pairId == 0 }
        multiplayerEngine.flipCard(firstPair[0].id)
        multiplayerEngine.flipCard(firstPair[1].id)
        multiplayerEngine.clearMatchedCards()

        val gameState1 = multiplayerEngine.getGameState()
        val player1Score = gameState1.player1.score
        val player2Score = gameState1.player2.score

        // One player should have 1 point
        assertTrue("One player should have scored",
            player1Score == 1 || player2Score == 1
        )

        // Total score should be 1
        assertEquals("Total score should be 1",
            1,
            player1Score + player2Score
        )
    }
}