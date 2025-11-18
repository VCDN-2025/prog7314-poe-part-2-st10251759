package vcmsa.projects.prog7314.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.data.models.CardBackground
import vcmsa.projects.prog7314.data.repository.CardBackgroundRepository

/*
    Code Attribution for: Creating ViewModels
    ===================================================
    Android Developers, 2019b. ViewModel Overview | Android Developers (Version unknown) [Source code].
    Available at: <https://developer.android.com/topic/libraries/architecture/viewmodel>
    [Accessed 18 November 2025].
*/


/**
 * ViewModel that manages the selected card background design for the game.
 * Handles loading, saving, and switching between different card back styles.
 */
class CardBackgroundViewModel(application: Application) : AndroidViewModel(application) {

    // The currently selected card background style (private mutable version)
    private val _selectedCardBackground = MutableStateFlow(CardBackground.DEFAULT)

    // Public read-only version that the UI observes
    val selectedCardBackground: StateFlow<CardBackground> = _selectedCardBackground

    // The actual drawable resource ID for the selected background (private mutable version)
    private val _cardBackgroundDrawable = MutableStateFlow(0)

    // Public read-only version that the UI uses to display the card back
    val cardBackgroundDrawable: StateFlow<Int> = _cardBackgroundDrawable

    init {
        // Load the previously saved background when ViewModel is created
        loadCardBackground()
    }

    /**
     * Loads the saved card background preference from storage.
     * If no preference exists, defaults to the default background.
     */
    fun loadCardBackground() {
        viewModelScope.launch {
            // Retrieve the saved background choice from repository
            val background = CardBackgroundRepository.loadCardBackground(getApplication())

            // Update the state with the loaded background
            _selectedCardBackground.value = background

            // Get the drawable resource ID for this background
            updateDrawable(background)
        }
    }

    /**
     * Changes the card background to a new design and saves the preference.
     * This persists the choice so it's remembered when the app restarts.
     */
    fun setCardBackground(cardBackground: CardBackground) {
        viewModelScope.launch {
            // Save the new background choice to persistent storage
            CardBackgroundRepository.saveCardBackground(getApplication(), cardBackground)

            // Update the current selection
            _selectedCardBackground.value = cardBackground

            // Update the drawable resource for the UI
            updateDrawable(cardBackground)
        }
    }

    /**
     * Converts a CardBackground enum to its corresponding drawable resource ID.
     * This links the background choice to the actual image file.
     */
    private fun updateDrawable(cardBackground: CardBackground) {
        val drawableId = CardBackgroundRepository.getCardBackgroundDrawable(
            getApplication(),
            cardBackground
        )
        _cardBackgroundDrawable.value = drawableId
    }

    /**
     * Returns the drawable resource ID for the currently selected background.
     * Used by the UI to display the correct card back image.
     */
    fun getCardBackgroundDrawableId(): Int {
        return _cardBackgroundDrawable.value
    }
}