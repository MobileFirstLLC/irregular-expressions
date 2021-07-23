@file:Suppress("DEPRECATION")

package mf.irregex.keyboard

import android.content.Context
import android.content.res.Resources.Theme
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import mf.irregex.R

class IrregularKeyboardView : KeyboardView {

    private val NUMERIC = resources.getInteger(R.integer.kbd_numeric_switch)
    private val ALPHA = resources.getInteger(R.integer.kbd_alpha_switch)
    private val ACTION_KEY = resources.getInteger(R.integer.kbd_enter_return_key)
    private val BACKSPACE = resources.getInteger(R.integer.kbd_backspace_delete)
    private val SHIFT = resources.getInteger(R.integer.kbd_shift_key)
    private val SPACE = resources.getInteger(R.integer.kbd_space_key)
    private val MINUS = resources.getInteger(R.integer.kbd_minus_key)
    private val LIGHT_APPEARANCE = "1"
    private val DARK_APPEARANCE = "2"
    private val DEFAULT_APPEARANCE = "3"
    private var themeId = R.style.KeyboardTheme
    private var iconRatio = .75f
    private var shiftIcon = R.drawable.kbd_ic_arrow_up_bold_outline
    private val actionIcon = R.drawable.kbd_ic_keyboard_return

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        determineTheme()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        determineTheme()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes) {
        determineTheme()
    }

    private fun determineTheme() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        when (prefs.getString("kbd_appearance", DEFAULT_APPEARANCE)) {
            DARK_APPEARANCE -> themeId = R.style.KeyboardThemeDark
            LIGHT_APPEARANCE -> themeId = R.style.KeyboardThemeLight
        }
        val outValue = TypedValue()
        resources.getValue(R.dimen.key_icon_ratio, outValue, true)
        iconRatio = outValue.float
    }

    fun setShiftIcon(resId: Int) {
        shiftIcon = resId
    }

    private fun drawIt(canvas: Canvas, key: Keyboard.Key, iconResId: Int, theme: Theme) {
        val drawable: Drawable? = VectorDrawableCompat.create(resources, iconResId, theme)
        val minDimension = Math.min(key.height, key.width)
        val iconSize = (minDimension * iconRatio).toInt()
        val paddingX = (key.width - iconSize) / 2
        val paddingY = (key.height - iconSize) / 2

        val left = key.x + paddingX
        val right = left + iconSize
        val top = key.y + paddingY
        val bottom = top + iconSize

        drawable!!.setBounds(left, top, right, bottom)
        drawable.draw(canvas)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val theme = resources.newTheme()
        theme.applyStyle(themeId, false)
        val keys = keyboard.keys
        for (key in keys) {
            if (key.codes.isNotEmpty() && key.label == null) {
                when (key.codes[0]) {
                    ACTION_KEY -> drawIt(canvas, key, actionIcon, theme)
                    NUMERIC -> drawIt(canvas, key, R.drawable.kbd_ic_numeric, theme)
                    BACKSPACE -> drawIt(canvas, key, R.drawable.kbd_ic_backspace_outline, theme)
                    SHIFT -> drawIt(canvas, key, shiftIcon, theme)
                    SPACE -> drawIt(canvas, key, R.drawable.kbd_ic_keyboard_space, theme)
                    ALPHA -> drawIt(canvas, key, R.drawable.kbd_ic_alphabetical_variant, theme)
                    MINUS -> drawIt(canvas, key, R.drawable.kbd_ic_minus, theme)
                }
            }
        }
    }
}