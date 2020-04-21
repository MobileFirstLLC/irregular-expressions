package mf.asciitext.gallery

import android.content.res.Resources
import android.content.res.TypedArray
import androidx.annotation.StyleableRes
import mf.asciitext.R
import java.util.*

object AvailableArt {

    private val categories = LinkedList<ArtCategory>()
    private val pieces = LinkedList<ArtPiece>()

    fun getCategories(): List<ArtCategory> {
        return categories
    }

    fun getArtPieces(): List<ArtPiece> {
        return pieces
    }

    /**
     * Call this method once on application start to initialize application art categories
     *
     * @param res - Reference to android resources
     */
    fun init(res: Resources) {
        initCategories(res.getStringArray(R.array.art_categories))
        initArtPieces(res, res.obtainTypedArray(R.array.art_pieces))
    }

    /**
     * Initialize ASCII art categories
     *
     * @param res - Android resources reference
     * @param ta  - TypedArray of all art categories
     */
    private fun initCategories(ta: Array<String>) {
        categories.clear()
        for (i in ta.indices) {
            val artId = ta[i]
            val name = ta[i]
            categories.add(ArtCategory(artId, name))
        }
    }

    /**
     * Initialize individual ASCII art pieces
     *
     * @param res - Android resources reference
     * @param ta  - TypedArray of all ASCII art pieces
     */
    private fun initArtPieces(res: Resources, ta: TypedArray) {

        @StyleableRes val categoryIndex = res.getInteger(R.integer.ArtCategoryIndex)
        @StyleableRes val nameIndex = res.getInteger(R.integer.ArtNameIndex)
        @StyleableRes val textIndex = res.getInteger(R.integer.ArtTextIndex)
        @StyleableRes val premiumIndex = res.getInteger(R.integer.ArtPremiumIndex)
        val fullWidthCategories = arrayOf(res.getString(R.string.lines))

        pieces.clear()
        for (i in 0 until ta.length()) {
            val id = ta.getResourceId(i, 0)
            if (id > 0) {
                val catDef = res.obtainTypedArray(id)
                val category = catDef.getString(categoryIndex)
                val name = catDef.getString(nameIndex)
                val text = catDef.getString(textIndex)
                val fullWidth = fullWidthCategories.indexOf(category) > -1
                val premium = true and
                        if (catDef.length() > premiumIndex) {
                            catDef.getInteger(premiumIndex, 0) == 1
                        } else false

                if (category != null && name != null && text != null)
                    pieces.add(ArtPiece(id.toString(), name, text, category, fullWidth, premium))
                catDef.recycle()
            }
        }
        ta.recycle()
    }
}
