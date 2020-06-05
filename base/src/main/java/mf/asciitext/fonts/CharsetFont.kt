package mf.asciitext.fonts

open class CharsetFont internal constructor(
    id: String,
    name: String,
    private val charset: Array<String>?,
    reverse: Boolean
) : AppFont(id, name, reverse) {

    override fun encode(text: String?, sequence: CharSequence?): String? {
        if (!isEmpty(text)) {
            val firstChar = ' '.toInt()
            val cs = text!!.toCharArray()
            val temp = StringBuilder()
            if (charset != null && charset.isNotEmpty()) {
                for (c in cs) {
                    val num = c.toInt() - firstChar
                    temp.append(if (num >= 0 && num < charset.size) charset[num] else c)
                }
            } else {
                temp.append(text)
            }
            return (if (this.reverse) temp.reverse() else temp).toString()
        }
        return text
    }
}