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

    private var iconRatio = .75f
    private var themeId = R.style.KeyboardTheme
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
        themeId =
            when (prefs.getString("kbd_appearance", Constants.DEFAULT_THEME.toString())?.toInt()
                ?: Constants.DEFAULT_THEME) {
                Constants.DARK -> R.style.KeyboardThemeDark
                Constants.LIGHT -> R.style.KeyboardThemeLight
                else -> R.style.KeyboardTheme
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
                val iconRes: Int? = when (key.codes[0]) {
                    Keyboard.KEYCODE_DONE -> actionIcon
                    Keyboard.KEYCODE_DELETE -> R.drawable.kbd_ic_backspace_outline
                    Keyboard.KEYCODE_SHIFT -> shiftIcon
                    Constants.SECONDARY_KBD_KEYCODE -> R.drawable.kbd_ic_numeric
                    Constants.KEYCODE_SPACE -> R.drawable.kbd_ic_keyboard_space
                    Constants.ALPHA_KEYBOARD_KEYCODE -> R.drawable.kbd_ic_alphabetical_variant
                    Constants.KEYCODE_MINUS -> R.drawable.kbd_ic_minus
                    else -> null
                }
                if (iconRes != null) drawIt(canvas, key, iconRes, theme)
            }
        }
    }
}