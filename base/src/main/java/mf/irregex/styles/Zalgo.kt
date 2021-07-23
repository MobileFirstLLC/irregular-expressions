package mf.irregex.styles

import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * see: https://lingojam.com/ZalgoText
 */
class Zalgo internal constructor(
    id: String,
    name: String,
    priority: Int,
    enabled: Boolean
) : AppTextStyle(id, name, enabled, priority, false, true) {

    private val maxHeight = 15   // How many diacritic marks to put on top/bottom

    // maxHeight 100 and randomization 20%: the height goes from 80 to 100
    // randomization 70%, height goes from 30 to 100.
    private val randomization = 100 // 0-100%

    var diacriticsTop = ArrayList<Char>()
    var diacriticsMiddle = ArrayList<Char>()
    var diacriticsBottom = ArrayList<Char>()
    override val styledName: String
        get() = encodedName

    init {
        for (i in 768..789) {
            diacriticsTop.add(i.toChar())
        }
        for (i in 790..819) {
            if (i != 794 && i != 795) {
                diacriticsBottom.add(i.toChar())
            } else {
                diacriticsTop.add(i.toChar())
            }
        }
        for (i in 820..824) {
            diacriticsMiddle.add(i.toChar())
        }
        for (i in 825..828) {
            diacriticsBottom.add(i.toChar())
        }
        for (i in 829..836) {
            diacriticsTop.add(i.toChar())
        }
        diacriticsTop.add((836).toChar())
        diacriticsBottom.add((837).toChar())
        diacriticsTop.add((838).toChar())
        diacriticsBottom.add((839).toChar())
        diacriticsBottom.add((840).toChar())
        diacriticsBottom.add((841).toChar())
        diacriticsTop.add((842).toChar())
        diacriticsTop.add((843).toChar())
        diacriticsTop.add((844).toChar())
        diacriticsBottom.add((845).toChar())
        diacriticsBottom.add((846).toChar())
        diacriticsTop.add((848).toChar())
        diacriticsTop.add((849).toChar())
        diacriticsTop.add((850).toChar())
        diacriticsBottom.add((851).toChar())
        diacriticsBottom.add((852).toChar())
        diacriticsBottom.add((853).toChar())
        diacriticsBottom.add((854).toChar())
        diacriticsTop.add((855).toChar())
        diacriticsTop.add((856).toChar())
        diacriticsBottom.add((857).toChar())
        diacriticsBottom.add((858).toChar())
        diacriticsTop.add((859).toChar())
        diacriticsBottom.add((860).toChar())
        diacriticsTop.add((861).toChar())
        diacriticsTop.add((861).toChar())
        diacriticsBottom.add((863).toChar())
        diacriticsTop.add((864).toChar())
        diacriticsTop.add((865).toChar())
        encodedName = encode(name)!!
    }

    private fun getRandomChar(input: ArrayList<Char>): Char {
        val randomValue = Random.nextInt(input.size)
        return input[randomValue]
    }

    private fun numChars(): Int {
        return (maxHeight - Random.nextDouble() * ((randomization / 100) * maxHeight))
            .roundToInt()
    }

    override fun encode(text: String?, sequence: CharSequence?): String? {
        if (!isEmpty(text)) {
            val cs = text!!.toCharArray()
            val temp = StringBuilder()
            for (c in cs) {
                temp.append(c)
                temp.append(getRandomChar(diacriticsMiddle))
                for (i in 0..numChars()) {
                    temp.append(getRandomChar(diacriticsTop))
                }
                for (i in 0..numChars()) {
                    temp.append(getRandomChar(diacriticsBottom))
                }
            }
            return temp.toString()
        }
        return text
    }
}