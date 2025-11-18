package vcmsa.projects.prog7314.data.models

/*
    Code Attribution for: Enum Classes
    ===================================================
    Kotlin, 2025b. Enum classes | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://kotlinlang.org/docs/enum-classes.html>
    [Accessed 18 November 2025].
*/

/**
 * Enum class representing the different card background themes available in the app.
 *
 * @property displayName A user-friendly name to show in the UI for selection.
 * @property drawableRes The string reference to the drawable resource for the background.
 */
enum class CardBackground(val displayName: String, val drawableRes: String) {
    // Default light blue background
    DEFAULT("Default (Light Blue)", "card_background_default"),

    // Navy blue background
    BLUE("Navy Blue", "card_background_blue"),

    // Green background
    GREEN("Green", "card_background_green"),

    // Pink background
    PINK("Pink", "card_background_pink"),

    // Purple background
    PURPLE("Purple", "card_background_purple"),

    // Red background
    RED("Red", "card_background_red"),

    // Yellow background
    YELLOW("Yellow", "card_background_yellow");

    companion object {
        /**
         * Converts a string value to the corresponding [CardBackground] enum.
         * Returns [DEFAULT] if the value does not match any enum name.
         *
         * @param value The string representation of the enum name.
         * @return The matching [CardBackground] or [DEFAULT].
         */
        fun fromString(value: String): CardBackground {
            return values().find { it.name == value } ?: DEFAULT
        }
    }
}
