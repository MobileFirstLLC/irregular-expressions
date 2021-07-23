package mf.irregex.keyboard

object Constants {

    // Keyboard views
    const val ALPHA_KBD = 0
    const val NUMBER_KBD = 1
    const val MATH_KBD = 2
    const val PHONE_KBD = 3

    // Alpha keyboard layouts
    const val QWERTY = 1
    const val AZERTY = 2
    const val QWERTZ = 3
    const val DVORAK = 4
    const val DEFAULT_LAYOUT = QWERTY

    // themes
    const val LIGHT = 1
    const val DARK = 2
    const val AUTO = 3
    const val DEFAULT_THEME = AUTO

    // key codes
    const val ALPHA_KEYBOARD_KEYCODE = -10
    const val SECONDARY_KBD_KEYCODE = -11
    const val KEYCODE_SPACE = 32

    // other
    const val PROCESS_HARD_KEYS = true
    const val DOUBLETAP_MAX_DELAY_MS = 500L
    const val VIBRATION_DURATION_MS = 25L
    const val LONG_PRESS = 200L
    const val DEFAULT_HEIGHT = 8
    const val DEFAULT_VIBRATIONS = false
    const val REGULAR_STYLE_INDEX = -1 // Font with "no style"
}