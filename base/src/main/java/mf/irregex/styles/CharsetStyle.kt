package mf.irregex.styles

open class CharsetStyle internal constructor(
    id: String,
    name: String,
    priority: Int,
    enabled: Boolean,
    private val charset: Array<String>?,
    reverse: Boolean
) : AppTextStyle(id, name, enabled, priority, reverse) {

    companion object {
        // all character sets start from first printing char
        private const val firstChar = 32
    }

    init {
        encodedName = encode(name)!!
    }

    private fun encodeAllChars(cs: CharArray): java.lang.StringBuilder {
        val temp = StringBuilder()
        for (c in cs) {
            val num = c.code - firstChar
            temp.append(if (num >= 0 && num < charset!!.size) charset[num] else c)
        }
        return temp
    }

    final override fun encode(text: String?, sequence: CharSequence?): String? {
        if (!isEmpty(text)) {
            val cs = text!!.toCharArray()
            val temp = if (charset != null && charset.isNotEmpty())
                encodeAllChars(cs) else StringBuilder().append(text)
            return (if (this.reverse) temp.reverse() else temp).toString()
        }
        return text
    }
}