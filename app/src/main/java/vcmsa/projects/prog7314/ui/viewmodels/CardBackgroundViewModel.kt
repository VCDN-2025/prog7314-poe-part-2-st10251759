package vcmsa.projects.prog7314.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.data.models.CardBackground
import vcmsa.projects.prog7314.data.repository.CardBackgroundRepository

class CardBackgroundViewModel(application: Application) : AndroidViewModel(application) {

    private val _selectedCardBackground = MutableStateFlow(CardBackground.DEFAULT)
    val selectedCardBackground: StateFlow<CardBackground> = _selectedCardBackground

    private val _cardBackgroundDrawable = MutableStateFlow(0)
    val cardBackgroundDrawable: StateFlow<Int> = _cardBackgroundDrawable

    init {
        loadCardBackground()
    }

    fun loadCardBackground() {
        viewModelScope.launch {
            val background = CardBackgroundRepository.loadCardBackground(getApplication())
            _selectedCardBackground.value = background
            updateDrawable(background)
        }
    }

    fun setCardBackground(cardBackground: CardBackground) {
        viewModelScope.launch {
            CardBackgroundRepository.saveCardBackground(getApplication(), cardBackground)
            _selectedCardBackground.value = cardBackground
            updateDrawable(cardBackground)
        }
    }

    private fun updateDrawable(cardBackground: CardBackground) {
        val drawableId = CardBackgroundRepository.getCardBackgroundDrawable(
            getApplication(),
            cardBackground
        )
        _cardBackgroundDrawable.value = drawableId
    }

    fun getCardBackgroundDrawableId(): Int {
        return _cardBackgroundDrawable.value
    }
}