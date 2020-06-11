package mf.asciitext.fonts

class AccentFont internal constructor(
    id: String,
    name: String,
    enabled: Boolean,
    private val uChar: String
) : AppFont(id, name, enabled) {

    override fun encode(text: String?, sequence: CharSequence?): String? {
        if (!isEmpty(text)) {
            val cs = text!!.toCharArray()
            val temp = StringBuilder()
            for (c in cs) {
                if (!Character.isWhitespace(c))
                    temp.append(uChar)
                temp.append(c)
            }
            temp.append(uChar)
            return temp.toString()
        }
        return text;
    }
}
