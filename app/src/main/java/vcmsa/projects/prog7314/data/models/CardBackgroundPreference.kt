package vcmsa.projects.prog7314.data.models

enum class CardBackground(val displayName: String, val drawableRes: String) {
    DEFAULT("Default (Light Blue)", "card_background_default"),
    BLUE("Navy Blue", "card_background_blue"),
    GREEN("Green", "card_background_green"),
    PINK("Pink", "card_background_pink"),
    PURPLE("Purple", "card_background_purple"),
    RED("Red", "card_background_red"),
    YELLOW("Yellow", "card_background_yellow");

    companion object {
        fun fromString(value: String): CardBackground {
            return values().find { it.name == value } ?: DEFAULT
        }
    }
}