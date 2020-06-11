package mf.asciitext.fonts

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.content.res.TypedArray
import android.util.Log
import androidx.annotation.StyleableRes
import androidx.preference.PreferenceManager
import mf.asciitext.R
import java.util.*
import kotlin.collections.HashSet

object AvailableFonts {

    val disabled_key = "pref_disabled_fonts"
    val order_key = "pref_font_order"

    private val fonts = LinkedList<AppFont>()
    private val disabledList = HashSet<String>()
    private val fontOrder = LinkedList<String>()
    private lateinit var prefs: SharedPreferences

    class SortComparator : Comparator<AppFont> {
        val indexVal: (String) -> Int = {
            if (fontOrder.contains(it))
                fontOrder.indexOf(it)
            else fontOrder.size
        }

        override fun compare(font1: AppFont, font2: AppFont): Int {
            val a = indexVal(font1.fontId)
            val b = indexVal(font2.fontId)
            return if (a < b) -1 else if (a > b) 1 else 0
        }
    }

    /**
     * This method returns a list of all known fonts
     */
    fun getFonts(): List<AppFont> {
        return fonts.sortedWith(SortComparator())
    }

    /**
     * This method returns a list of enabled fonts only.
     * All fonts are enabled by default but user may
     * choose to disable some
     */
    fun getEnabledFonts(): List<AppFont> {
        val enableCondition: ((AppFont)) -> Boolean = { it.isEnabled }
        return fonts.filter(enableCondition).sortedWith(SortComparator())
    }

    /**
     * Call this method once on application start to initialize application fonts
     *
     * @param res - Reference to android resources
     */
    fun init(ctx: Context) {
        fonts.clear()
        val res = ctx.resources
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        initPreferences()
        initCharsetFonts(res, res.obtainTypedArray(R.array.charset_fonts))
        initRandomCaps(res.getString(R.string.font_name_spongemock))
        initAccentFonts(res, res.obtainTypedArray(R.array.accent_fonts))
    }

    private fun initPreferences() {
        restoreDisabledList()
        restoreOrder()
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
        val arr = fontOrder.joinToString(",")
        val editor = prefs.edit()
        editor.putString(order_key, arr)
        editor.apply()
    }

    private fun restoreOrder() {
        val prefList = prefs.getString(order_key, null) ?: return
        fontOrder.addAll(prefList.split(","))
    }

    fun setFirst(index: Int) {
        if (index < 0 || index > fonts.size) return
        val newFirst = fonts.removeAt(index)
        fonts.add(0, newFirst)
        persistOrder()
    }

    fun toggleEnabled(index: Int) {
        if (index < 0 || index > fonts.size) return
        val nextState = !fonts[index].isEnabled
        val fontID = fonts[index].fontId

        fonts[index].isEnabled = nextState
        if (!nextState) disabledList.add(fontID)
        else disabledList.remove(fontID)

        persistDisabledList()
    }

    /**
     * Initialize character set fonts
     *
     * @param res - Android resources reference
     * @param ta  - TypedArray of all fonts with custom character set
     */
    private fun initCharsetFonts(res: Resources, ta: TypedArray) {

        @StyleableRes val idIndex = res.getInteger(R.integer.CharsetIdIndex)
        @StyleableRes val nameIndex = res.getInteger(R.integer.CharsetNameIndex)
        @StyleableRes val reverseIndex = res.getInteger(R.integer.CharsetReverseIndex)
        @StyleableRes val charsetIndex = res.getInteger(R.integer.CharsetIndex)

        for (i in 0 until ta.length()) {
            val id = ta.getResourceId(i, 0)
            if (id > 0) {
                val fontDef = res.obtainTypedArray(id)
                val fontId = fontDef.getString(nameIndex) + i.toShort();
                val name = fontDef.getString(nameIndex)
                val charset = res.getStringArray(fontDef.getResourceId(charsetIndex, 0))
                val reversed = fontDef.getBoolean(reverseIndex, false)
                val enabled = !disabledList.contains(fontId)
                if (name != null)
                    fonts.add(CharsetFont(fontId, name, enabled, charset, reversed))

                fontDef.recycle()
            } else {
                Log.d("INIT FONTS", "Invalid resource Id!")
            }
        }
        ta.recycle()
    }

    /**
     * Initialize accented fonts
     *
     * @param res - Android resources reference
     * @param ta  - TypedArray of all accented Fonts
     */
    private fun initAccentFonts(res: Resources, ta: TypedArray) {

        // Initialize Accented fonts
        @StyleableRes val accentIdIndex = res.getInteger(R.integer.AccentIdIndex)
        @StyleableRes val accentNameIndex = res.getInteger(R.integer.AccentNameIndex)
        @StyleableRes val accentCharIndex = res.getInteger(R.integer.AccentValue)

        for (i in 0 until ta.length()) {
            val id = ta.getResourceId(i, 0)
            if (id > 0) {
                val fontDef = res.obtainTypedArray(id)
                val fontId = fontDef.getString(accentIdIndex)
                val name = fontDef.getString(accentNameIndex)
                val accentChar = fontDef.getString(accentCharIndex)
                val enabled = !disabledList.contains(fontId)
                if (fontId != null && name != null && accentChar != null)
                    fonts.add(AccentFont(fontId, name, enabled, accentChar))
                fontDef.recycle()
            } else {
                Log.d("INIT FONTS", "Invalid resource Id!")
            }
        }
        ta.recycle()
    }

    private fun initRandomCaps(name: String) {
        val id = "spongemock"
        val enabled = !disabledList.contains(id)
        fonts.add(RandomCaps(id, name, enabled))
    }
}
