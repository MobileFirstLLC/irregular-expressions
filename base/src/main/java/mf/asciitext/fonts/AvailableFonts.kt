package mf.asciitext.fonts

import android.content.res.Resources
import android.content.res.TypedArray
import android.util.Log
import androidx.annotation.StyleableRes
import mf.asciitext.R
import java.util.*

 object AvailableFonts {

    private val fonts = LinkedList<AppFont>()

    fun getFonts(): List<AppFont> {
        return fonts
    }

    /**
     * Call this method once on application start to initialize application fonts
     *
     * @param res - Reference to android resources
     */
    fun init(res: Resources) {
        fonts.clear()
        // fonts.add(SarcasticFont("c000", res.getString(R.string.font_name_sarcastic), false))
        initCharsetFonts(res, res.obtainTypedArray(R.array.charset_fonts))
        initAccentFonts(res, res.obtainTypedArray(R.array.accent_fonts))
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
        @StyleableRes val premiumIndex = res.getInteger(R.integer.CharsetPremiumIndex)
        @StyleableRes val reverseIndex = res.getInteger(R.integer.CharsetReverseIndex)
        @StyleableRes val charsetIndex = res.getInteger(R.integer.CharsetIndex)

        for (i in 0 until ta.length()) {
            val id = ta.getResourceId(i, 0)
            if (id > 0) {
                val fontDef = res.obtainTypedArray(id)
                val fontId = fontDef.getString(nameIndex) + i.toShort();
                val name = fontDef.getString(nameIndex)
                val charset = res.getStringArray(fontDef.getResourceId(charsetIndex, 0))
                val premium = true and fontDef.getBoolean(premiumIndex, false)
                val reversed = fontDef.getBoolean(reverseIndex, false)
                if (name != null)
                    fonts.add(CharsetFont(fontId, name, charset, premium, reversed))

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
        @StyleableRes val accentPremiumIndex = res.getInteger(R.integer.AccentPremiumIndex)
        @StyleableRes val accentCharIndex = res.getInteger(R.integer.AccentValue)

        for (i in 0 until ta.length()) {
            val id = ta.getResourceId(i, 0)
            if (id > 0) {
                val fontDef = res.obtainTypedArray(id)
                val fontId = fontDef.getString(accentIdIndex)
                val name = fontDef.getString(accentNameIndex)
                val accentChar = fontDef.getString(accentCharIndex)
                val premium = fontDef.getBoolean(accentPremiumIndex, false)
                if (fontId != null && name != null && accentChar != null)
                    fonts.add(AccentFont(fontId, name, accentChar, premium))
                fontDef.recycle()
            } else {
                Log.d("INIT FONTS", "Invalid resource Id!")
            }
        }
        ta.recycle()
    }


}
