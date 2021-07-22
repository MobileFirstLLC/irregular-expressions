package mf.irregex.styles

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.content.res.TypedArray
import androidx.annotation.StyleableRes
import androidx.preference.PreferenceManager
import mf.irregex.R
import java.util.*
import kotlin.collections.HashSet

object AvailableStyles {

    private const val disabled_key = "pref_disabled_fonts"
    private const val order_key = "pref_font_order"
    private val styles = LinkedList<AppTextStyle>()
    private val disabledList = HashSet<String>()
    private val styleOrder = LinkedList<String>()
    private lateinit var prefs: SharedPreferences

    class SortComparator : Comparator<AppTextStyle> {
        override fun compare(style1: AppTextStyle, style2: AppTextStyle): Int {
            val a = style1.priority
            val b = style2.priority
            return if (a < b) -1 else if (a > b) 1 else 0
        }
    }

    /**
     * This method returns a list of all known styles
     */
    fun getStyles(): List<AppTextStyle> {
        return styles.sortedWith(SortComparator())
    }

    /**
     * This method returns a list of enabled text styles only.
     * All styles are enabled by default but user may
     * choose to disable some
     */
    fun getEnabledStyles(): List<AppTextStyle> {
        val enableCondition: ((AppTextStyle)) -> Boolean = { it.isEnabled }
        return styles.filter(enableCondition).sortedWith(SortComparator())
    }

    /**
     * Call this method once on application start to initialize application text styles
     *
     * @param ctx - Context reference
     */
    fun init(ctx: Context) {
        styles.clear()
        val res = ctx.resources
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        restoreDisabledList()
        restoreOrder()
        initCharsetFonts(res, res.obtainTypedArray(R.array.charset_styles))
        initRandomCaps(res.getString(R.string.name_spongemock))
        initZalgoText(res.getString(R.string.name_zalgo))
        initAccentStyles(res, res.obtainTypedArray(R.array.accent_styles))
    }

    private fun persistDisabledList() {
        val arr = disabledList.toSet()
        val editor = prefs.edit()
        editor.putStringSet(disabled_key, arr)
        editor.apply()
    }

    private fun restoreDisabledList() {
        val prefList = prefs.getStringSet(disabled_key, null)
        if (prefList != null) disabledList.addAll(prefList)
    }

    private fun persistOrder() {
        val arr = styleOrder.joinToString(",")
        val editor = prefs.edit()
        editor.putString(order_key, arr)
        editor.apply()
    }

    private fun restoreOrder() {
        val prefList = prefs.getString(order_key, null)
        if (prefList != null) {
            val temp = prefList.split(",")
            styleOrder.addAll(temp)
        }
    }

    private fun getOrder(id: String): Int {
        if (styleOrder.contains(id))
            return styleOrder.indexOf(id)
        return styles.size
    }

    fun setNewPosition(id: String, position: Int) {
        styleOrder.clear()
        val sorted = getStyles()
        for (i in sorted.indices) {
            val fId = sorted[i].styleId
            if (id != fId) styleOrder.add(fId)
        }
        styleOrder.add(position, id)
        for (i in styles.indices) {
            styles[i].priority = styleOrder.indexOf(styles[i].styleId)
        }
        persistOrder()
    }

    fun toggleEnabled(id: String) {
        styles.forEach {
            if (it.styleId == id) {
                val nextState = !it.isEnabled
                it.isEnabled = nextState
                if (!nextState) disabledList.add(id)
                else disabledList.remove(id)
                persistDisabledList()
                return
            }
        }
    }

    /**
     * Initialize character set styles
     *
     * @param res - Android resources reference
     * @param ta  - TypedArray of all styles with custom character set
     */
    private fun initCharsetFonts(res: Resources, ta: TypedArray) {

        @StyleableRes val nameIndex = res.getInteger(R.integer.CharsetNameIndex)
        @StyleableRes val reverseIndex = res.getInteger(R.integer.CharsetReverseIndex)
        @StyleableRes val charsetIndex = res.getInteger(R.integer.CharsetIndex)

        for (i in 0 until ta.length()) {
            val id = ta.getResourceId(i, 0)
            if (id > 0) {
                val styleDef = res.obtainTypedArray(id)
                val styleId = styleDef.getString(nameIndex) + i.toShort();
                val name = styleDef.getString(nameIndex)
                val charset = res.getStringArray(styleDef.getResourceId(charsetIndex, 0))
                val reversed = styleDef.getBoolean(reverseIndex, false)
                val enabled = !disabledList.contains(styleId)
                if (name != null)
                    styles.add(
                        CharsetStyle(
                            styleId,
                            name,
                            getOrder(styleId),
                            enabled,
                            charset,
                            reversed
                        )
                    )

                styleDef.recycle()
            }
        }
        ta.recycle()
    }

    /**
     * Initialize accented styles
     *
     * @param res - Android resources reference
     * @param ta  - TypedArray of all accented styles
     */
    private fun initAccentStyles(res: Resources, ta: TypedArray) {

        // Initialize accented styles
        @StyleableRes val accentIdIndex = res.getInteger(R.integer.AccentIdIndex)
        @StyleableRes val accentNameIndex = res.getInteger(R.integer.AccentNameIndex)
        @StyleableRes val accentCharIndex = res.getInteger(R.integer.AccentValue)

        for (i in 0 until ta.length()) {
            val id = ta.getResourceId(i, 0)
            if (id > 0) {
                val styleDef = res.obtainTypedArray(id)
                val styleId = styleDef.getString(accentIdIndex)
                val name = styleDef.getString(accentNameIndex)
                val accentChar = styleDef.getString(accentCharIndex)
                val enabled = !disabledList.contains(styleId)
                if (styleId != null && name != null && accentChar != null)
                    styles.add(
                        AccentStyle(
                            styleId,
                            name,
                            getOrder(styleId),
                            enabled,
                            accentChar
                        )
                    )
                styleDef.recycle()
            }
        }
        ta.recycle()
    }

    private fun initRandomCaps(name: String) {
        val id = "spongemock"
        val enabled = !disabledList.contains(id)
        styles.add(RandomCaps(id, name, getOrder(id), enabled))
    }

    private fun initZalgoText(name: String) {
        val id = "zalgo"
        val enabled = !disabledList.contains(id)
        styles.add(Zalgo(id, name, getOrder(id), enabled))
    }
}
