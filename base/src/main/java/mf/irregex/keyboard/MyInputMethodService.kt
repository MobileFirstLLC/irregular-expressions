@file:Suppress("DEPRECATION")

package mf.irregex.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.Keyboard.KEYCODE_DONE
import android.inputmethodservice.KeyboardView.GONE
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.preference.PreferenceManager
import android.text.InputType
import android.text.TextUtils
import android.text.method.MetaKeyKeyListener
import android.util.Log
import android.view.*
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mf.irregex.R
import mf.irregex.keyboard.Constants.DEFAULT_HEIGHT
import mf.irregex.keyboard.Constants.DEFAULT_VIBRATIONS
import mf.irregex.keyboard.Constants.DOUBLETAP_MAX_DELAY_MS
import mf.irregex.keyboard.Constants.KEYCODE_SPACE
import mf.irregex.keyboard.Constants.LONG_PRESS
import mf.irregex.keyboard.Constants.PROCESS_HARD_KEYS
import mf.irregex.keyboard.Constants.REGULAR_STYLE_INDEX
import mf.irregex.keyboard.Constants.VIBRATION_DURATION_MS
import mf.irregex.settings.SettingsActivity
import mf.irregex.styles.AppTextStyle
import mf.irregex.styles.AvailableStyles.getEnabledStyles
import mf.irregex.styles.StylePickerAdapter
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 * This class sets up and handles virtual keyboard events
 */
class MyInputMethodService : InputMethodService(), OnKeyboardActionListener {

    // All available text styles
    private var styles = getEnabledStyles()

    // UI Elements
    private var keyboardView: IrregularKeyboardView? = null
    private var keyboard: IrregularKeyboard? = null
    private var styleToggle: AppCompatImageView? = null
    private var settingsButton: AppCompatImageView? = null
    private var stylePicker: RecyclerView? = null
    private var adapter: StylePickerAdapter? = null
    private var keyboardExtras: View? = null

    // keyboard default state
    private val mComposing = StringBuilder()
    private var keyboardChoice = Constants.ALPHA_KBD
    private var styleIndex = REGULAR_STYLE_INDEX
    private var lastSelectedStyleIndex = REGULAR_STYLE_INDEX
    private var reverseCursorDirection = false
    private var mMetaState: Long = 0

    // SHIFT key related variables
    private var uppercaseNextKeyOnly = false
    private var lastShift: Long = 0

    // SPACE bar variables
    private var spaceDown: Long = 0
    private var spaceIsPressed = false
    private var pickerInflated: Boolean = false

    // ENTER key variables
    private var mEnterKeyIndex: Int = -1

    // user preferences
    private var keyVibrations = DEFAULT_VIBRATIONS
    private var keyboardLayout = Constants.DEFAULT_LAYOUT
    private var keyHeight = DEFAULT_HEIGHT
    private var appearance = Constants.DEFAULT_THEME
    private var sysDarkMode: Boolean? = null

    override fun onCreate() {
        super.onCreate()
        initPreferences()
    }

    // called initially when inflating keyboard
    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    override fun onCreateInputView(): View {

        val theme = when (appearance) {
            Constants.DARK -> R.style.KeyboardThemeDark
            Constants.LIGHT -> R.style.KeyboardThemeLight
            else -> R.style.KeyboardTheme
        }
        val contextThemeWrapper = ContextThemeWrapper(applicationContext, theme)
        val layout = LayoutInflater.from(contextThemeWrapper)
            .inflate(R.layout.keyboard_view, null)

        /* initialize keyboard */
        keyboardView = layout.findViewById(R.id.keyboard_view)

        keyboardView?.setOnKeyboardActionListener(this)
        if (keyboardChoice == Constants.NUMBER_KBD) {
            enableSymbolicKeyboard()
        } else {
            enableAlphaKeyboard()
        }
        for (i in 0 until (keyboard!!.keys).size) {
            if (keyboard!!.keys[i].codes.contains(KEYCODE_DONE)) {
                mEnterKeyIndex = i
                break
            }
        }
        keyboardView?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP)
                if (spaceIsPressed) {
                    spaceIsPressed = false
                    onSpaceKeyRelease()
                }
            false
        }

        /* setup style picker recyclerView */

        stylePicker = layout.findViewById(R.id.stylePicker)
        keyboardExtras = layout.findViewById(R.id.keyboard_extras)
        adapter = StylePickerAdapter(styles, onFontSelection())
        val layoutManager = GridLayoutManager(
            applicationContext, 1, LinearLayoutManager.HORIZONTAL, false
        )
        stylePicker?.layoutManager = layoutManager
        stylePicker?.adapter = adapter
        adapter!!.setSelectedFont(styleIndex)

        /* setup UI icon buttons */

        styleToggle = layout.findViewById(R.id.style_button)
        styleToggle?.setOnClickListener(onFontButtonClick())
        setFontStyleIcon(styleIndex == REGULAR_STYLE_INDEX)
        settingsButton = layout.findViewById(R.id.settings_button)
        settingsButton?.setOnClickListener(onSettingsClick())
        return layout
    }

    /**
     * determine which keyboard view to show initially based on input type
     */
    private fun chooseKeyboardFromInputType(attribute: EditorInfo?) {
        when (attribute?.inputType?.and(InputType.TYPE_MASK_CLASS)) {
            InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_DATETIME -> {
                // Numbers and dates default to the symbols keyboard, with
                // no extra features.
                keyboardChoice = Constants.NUMBER_KBD
            }
            InputType.TYPE_CLASS_PHONE -> {
                // Phones will also default to the symbols keyboard, though
                // often you will want to have a dedicated phone keyboard.
                keyboardChoice = Constants.PHONE_KBD
            }
            InputType.TYPE_CLASS_TEXT -> {
                // This is general text editing.  We will default to the
                // normal alphabetic keyboard, and assume that we should
                // be doing predictive text (showing candidates as the
                // user types).
                keyboardChoice = Constants.ALPHA_KBD
            }
            else -> {
                // For all unknown input types, default to the alphabetic
                // keyboard with no special features.
                keyboardChoice = Constants.ALPHA_KBD
            }
        }
        when (keyboardChoice) {
            Constants.NUMBER_KBD -> enableSymbolicKeyboard()
            Constants.PHONE_KBD -> enablePhoneKeyboard()
            else -> enableAlphaKeyboard()
        }
    }

    /**
     * Reload user preferences every time keyboard is inflated
     * as these preferences may have changed
     */
    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (initPreferences()) {
            // when preferences have changed appearance, need to
            // restart the input method service to apply theme change
            setInputView(onCreateInputView())
            return
        }
        keyboardExtras?.visibility = VISIBLE
        styles = getEnabledStyles()
        adapter!!.updateStyles(styles)
        chooseKeyboardFromInputType(info)
    }

    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point keyboard is
     * bound to client, and is now receiving all of the detailed information
     * about the target of edits.
     */
    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        // Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed anyway.
        mComposing.setLength(0)
        if (!restarting) mMetaState = 0
        chooseKeyboardFromInputType(attribute)
        setImeOptions(attribute.imeOptions)
    }

    /**
     * Handle keyboard key presses; if key is held down this fires multiple times
     */
    override fun onKey(primaryCode: Int, keyCodes: IntArray) {
        if (currentInputConnection != null) {
            when (primaryCode) {
                Constants.SECONDARY_KBD_KEYCODE -> toggleExtendedKeyboardView()
                Constants.ALPHA_KEYBOARD_KEYCODE -> enableAlphaKeyboard()
                Keyboard.KEYCODE_DELETE -> handleDeleteKeyPress()
                Keyboard.KEYCODE_SHIFT -> handleShiftKeyPress()
                KEYCODE_DONE -> handleDoneKeyPress()
                KEYCODE_SPACE -> return
                else -> encodeCharacter(primaryCode)
            }
        }
    }

    /**
     * Fired each time key is pressed; if key is held down this fires only once
     */
    override fun onPress(i: Int) {
        vibrate(this)
        if (i == KEYCODE_SPACE) onSpaceKeyDown()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            // The InputMethodService already takes care of the back
            // key for us, to dismiss the input method if it is shown.
            KeyEvent.KEYCODE_BACK ->
                if (event?.repeatCount == 0 && keyboardView != null) {
                    if (keyboardView!!.handleBack()) {
                        return true
                    }
                }
            else -> {
                if (PROCESS_HARD_KEYS && event != null) {
                    return translateKeyDown(keyCode, event)
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        // If we want to do transformations on text being entered with a hard
        // keyboard, we need to process the up events to update the meta key
        // state we are tracking.
        if (PROCESS_HARD_KEYS) {
            mMetaState = MetaKeyKeyListener.handleKeyUp(mMetaState, keyCode, event)
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onRelease(i: Int) {
        if (i == KEYCODE_SPACE) onSpaceKeyRelease()
    }

    /**
     * This translates incoming hard key events in to edit operations on an
     * InputConnection.  It is only needed when using the
     * PROCESS_HARD_KEYS option.
     */
    private fun translateKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState, keyCode, event)
        val c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState))
        mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState)
        if (c == 0 || currentInputConnection == null) {
            return false
        }
        if (c == Constants.KEYCODE_SPACE) {
            encodeCharacter(Constants.KEYCODE_SPACE)
            return true
        }
        onKey(c, IntArray(0))
        return true
    }

    /**
     * Switch between numeric and symbolic keyboard
     */
    private fun toggleExtendedKeyboardView() {
        if (keyboardChoice == Constants.NUMBER_KBD) {
            keyboard = IrregularKeyboard(this, R.xml.keyboard_math, keyHeight)
            keyboardChoice = Constants.MATH_KBD
        } else {
            keyboard = IrregularKeyboard(this, R.xml.keyboard_extended, keyHeight)
            keyboardChoice = Constants.NUMBER_KBD
        }
        keyboardView!!.keyboard = keyboard
        keyboardView!!.invalidateAllKeys()
    }

    private  fun enableSymbolicKeyboard() {
        keyboard = IrregularKeyboard(this, R.xml.keyboard_extended, keyHeight)
        keyboardChoice = Constants.NUMBER_KBD
        keyboardView!!.keyboard = keyboard
        keyboardView!!.invalidateAllKeys()
    }

    private fun enablePhoneKeyboard() {
        keyboard = IrregularKeyboard(this, R.xml.keyboard_phone, keyHeight)
        keyboardExtras?.visibility = GONE
        keyboardChoice = Constants.PHONE_KBD
        keyboardView?.keyboard = keyboard
        keyboardView?.invalidateAllKeys()
    }

    /**
     * Change view to alphabetic keyboard
     */
    private fun enableAlphaKeyboard() {
        val keyLayout: Int = when (keyboardLayout) {
            Constants.AZERTY -> R.xml.keyboard_azerty
            Constants.QWERTZ -> R.xml.keyboard_qwertz
            Constants.DVORAK -> R.xml.keyboard_dvorak
            else -> R.xml.keyboard_qwerty
        }
        keyboard = IrregularKeyboard(this, keyLayout, keyHeight)
        keyboardChoice = Constants.ALPHA_KBD
        keyboard!!.isShifted = false
        uppercaseNextKeyOnly = false
        setShiftKeyIcon()
        keyboardView?.keyboard = keyboard
        keyboardView?.invalidateAllKeys()
    }

    /**
     * Special handler for space down
     */
    private fun onSpaceKeyDown() {
        spaceDown = System.nanoTime()
        pickerInflated = false
        spaceIsPressed = true
    }

    /**
     * Short press on space bar results in space key press
     * Long press inflates keyboard picker
     */
    private fun onSpaceKeyRelease() {
        val spaceUp = System.nanoTime()
        val diff = spaceUp - spaceDown
        if (!spaceIsPressed && diff < LONG_PRESS) {
            encodeCharacter(KEYCODE_SPACE)
        }
        else if (!pickerInflated && diff >= LONG_PRESS) {
            showKeyboardPicker()
            // prevent continuously inflating this menu
            pickerInflated = true
        }
    }

    /**
     * When user clicks shift key
     */
    private fun handleShiftKeyPress() {
        val now = SystemClock.uptimeMillis()
        val quickPress = now - lastShift < DOUBLETAP_MAX_DELAY_MS

        if (keyboard!!.isShifted && !quickPress) {
            uppercaseNextKeyOnly = false
            keyboard!!.isShifted = false
        } else {
            uppercaseNextKeyOnly = !quickPress
            keyboard!!.isShifted = true
        }
        setShiftKeyIcon()
        keyboardView!!.invalidateAllKeys()
        lastShift = now
    }

    /**
     * When shift is supposed to uppercase next letter only,
     * this method will reset to keyboard lowercase. Call it
     * after key press has occurred.
     */
    private fun unsetShift() {
        keyboard!!.isShifted = false
        uppercaseNextKeyOnly = false
        lastShift = 0L
        setShiftKeyIcon()
        keyboardView!!.invalidateAllKeys()
    }

    /**
     * Handle keyboard delete key press
     */
    private fun handleDeleteKeyPress() {
        val inputConnection = currentInputConnection
        val selectedText = inputConnection.getSelectedText(0)
        if (TextUtils.isEmpty(selectedText)) {
            if (reverseCursorDirection) {
                inputConnection.deleteSurroundingText(0, 1)
            } else
                inputConnection.deleteSurroundingText(1, 0)
        } else {
            inputConnection.commitText("", 1)
        }
    }

    /**
     * Clicking on done key inserts a line break
     */
    private fun handleDoneKeyPress() {
        val inputConnection = currentInputConnection
        inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    /**
     * Default keyboard key press handler
     *
     * @param primaryCode pressed key code
     */
    private fun encodeCharacter(primaryCode: Int) {
        val inputConnection = currentInputConnection
        var code = primaryCode.toChar()
        if (Character.isLetter(code) && keyboard!!.isShifted) {
            code = Character.toUpperCase(code)
        }
        var text = code.toString()
        if (styleIndex >= 0 && styleIndex < styles.size) {
            val style = styles[styleIndex]
            text = if (style.isSequenceAware) {
                val seq = inputConnection.getTextBeforeCursor(5, 0)
                style.encode(text, seq).toString()
            } else {
                style.encode(text).toString()
            }
        }
        inputConnection.commitText(text, 1)

        // adjust cursor position when typing right to left
        if (reverseCursorDirection)
            inputConnection.commitText("", -text.length)

        if (uppercaseNextKeyOnly) unsetShift()
    }

    /**
     * Handle style change
     * Keep track of previous selection before current
     * to enable style on-off toggle;
     */
    private fun onFontSelection(): StylePickerAdapter.OnItemClickListener {
        return object : StylePickerAdapter.OnItemClickListener {
            override fun onItemClick(item: AppTextStyle?, index: Int) {
                val previous = lastSelectedStyleIndex
                styleIndex = index
                lastSelectedStyleIndex = index
                setFontStyleIcon(false)
                setCursorDirection(styleIndex)
                adapter!!.setSelectedFont(styleIndex)
                adapter!!.notifyItemChanged(styleIndex)
                adapter!!.notifyItemChanged(previous)
            }
        }
    }

    /**
     * Determine if cursor should move left -> right
     * or left <- right following keyboard input
     */
    private fun setCursorDirection(index: Int) {
        reverseCursorDirection = if (index >= 0 && index < styles.size) {
            val style = styles[index]
            style.isReversed
        } else false
    }

    /**
     * Settings button click launches settings activity
     */
    private fun onSettingsClick(): View.OnClickListener {
        return View.OnClickListener {
            val intent = Intent(applicationContext, SettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    /**
     * Clicking f-icon toggles style on and off.
     * OFF: type in regular letter case
     * ON: restore previous style (if selection exists)
     */
    private fun onFontButtonClick(): View.OnClickListener {
        return View.OnClickListener {
            val disable = styleIndex != REGULAR_STYLE_INDEX
            styleIndex = if (disable) REGULAR_STYLE_INDEX else lastSelectedStyleIndex
            setCursorDirection(styleIndex)
            setFontStyleIcon(disable)
            adapter!!.setSelectedFont(styleIndex)
        }
    }

    /**
     * Update f-icon based on if custom styles are enabled/disabled
     *
     * @param disable - custom styles are disabled
     */
    private fun setFontStyleIcon(disable: Boolean) {
        styleToggle!!.setImageResource(
            if (disable) R.drawable.kbd_ic_style_off
            else R.drawable.kbd_ic_style_on
        )
        stylePicker!!.alpha = if (disable) 0.65f else 1.0f
    }

    /**
     * Update keyboard shift key icon to match caps lock state
     */
    private fun setShiftKeyIcon() {
        val keys = keyboard!!.keys
        val shiftIndex = keyboard!!.shiftKeyIndex
        if (shiftIndex >= 0 && shiftIndex < keys.size) {
            val icon = when {
                uppercaseNextKeyOnly -> R.drawable.kbd_ic_arrow_up_bold
                keyboard!!.isShifted -> R.drawable.kbd_ic_keyboard_caps_filled
                else -> R.drawable.kbd_ic_arrow_up_bold_outline
            }
            keyboardView?.setShiftIcon(icon)
        }
    }

    /**
     * Generate key vibration effect
     **/
    private fun vibrate(ctx: Context) {
        if (!keyVibrations) return
        val vibrator = ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createOneShot(
                VIBRATION_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE
            )
            vibrator.vibrate(vibrationEffect)
        } else {
            vibrator.vibrate(TimeUnit.MILLISECONDS.toMillis(VIBRATION_DURATION_MS))
        }
    }

    /**
     * Initialize user preference variables
     *
     * Return boolean to indicate if preference changes should result in recreating the
     * keyboard view
     */
    private fun initPreferences(): Boolean {
        val window = getSystemService(WINDOW_SERVICE) as WindowManager
        val orientation = resources.configuration.orientation
        val heightMultiplier = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 1.7f else 1f
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        keyVibrations = prefs.getBoolean("key_vibrations", DEFAULT_VIBRATIONS)
        keyboardLayout = prefs.getString("kbd_layout", Constants.DEFAULT_LAYOUT.toString())?.toInt()
            ?: Constants.DEFAULT_LAYOUT
        keyHeight = (.15f.coerceAtMost(
            heightMultiplier * (prefs.getInt("kdb_key_height", DEFAULT_HEIGHT) / 100f)
        ) * window.defaultDisplay.height).roundToInt()


        val previousSystemMode = sysDarkMode
        val previousAppearance = appearance

        appearance = prefs.getString("kbd_appearance", Constants.DEFAULT_THEME.toString())?.toInt()
            ?: Constants.DEFAULT_THEME
        if (appearance == Constants.DEFAULT_THEME) {
            sysDarkMode = when (resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> true
                Configuration.UI_MODE_NIGHT_NO -> false
                else -> null
            }
        }
        return previousAppearance != appearance || previousSystemMode != sysDarkMode
    }

    /**
     * Show keyboard switcher
     */
    private fun showKeyboardPicker() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()
    }

    /**
     * This looks at the ime options given by the current editor, to set the
     * appropriate label on the keyboard's enter key (if it has one).
     */
    private fun setImeOptions(options: Int) {

        // skip this for now until it works with theming
        return

        val mEnterKey =
            (if (keyboard != null && mEnterKeyIndex >= 0 && mEnterKeyIndex < keyboard!!.keys.size)
                keyboard!!.keys[mEnterKeyIndex] else null)
                ?: return

        when (options and (EditorInfo.IME_MASK_ACTION or EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
            EditorInfo.IME_ACTION_GO -> {
                mEnterKey.iconPreview = null
                mEnterKey.icon = null
                mEnterKey.label = resources.getText(R.string.label_go_key)
            }
            EditorInfo.IME_ACTION_NEXT -> {
                mEnterKey.iconPreview = null
                mEnterKey.icon = null
                mEnterKey.label = resources.getText(R.string.label_next_key)
            }
            EditorInfo.IME_ACTION_SEARCH -> {
                mEnterKey.icon = resources.getDrawable(R.drawable.kbd_ic_keyboard_search)
                mEnterKey.label = null
            }
            EditorInfo.IME_ACTION_SEND -> {
                mEnterKey.iconPreview = null
                mEnterKey.icon = null
                mEnterKey.label = resources.getText(R.string.label_send_key)
            }
            else -> {
                mEnterKey.icon = resources.getDrawable(R.drawable.kbd_ic_keyboard_return)
                mEnterKey.label = null
            }
        }
        if (keyboardView != null) keyboardView!!.invalidateKey(mEnterKeyIndex)
    }

    override fun onText(charSequence: CharSequence) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}