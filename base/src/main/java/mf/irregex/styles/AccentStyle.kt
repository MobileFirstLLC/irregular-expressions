package mf.irregex.styles

class AccentStyle internal constructor(
    id: String,
    name: String,
    priority: Int,
    enabled: Boolean,
    private val uChar: String
) : AppTextStyle(id, name, enabled, priority) {

    init {
        encodedName = encode(name)!!
    }

    override fun encode(text: String?, sequence: CharSequence?): String? {
        if (!isEmpty(text)) {
            val cs = text!!.toCharArray()
            val temp = StringBuilder()
            for (c in cs) {
                temp.append(c)
                if (!Character.isWhitespace(c))
                    temp.append(uChar)
            }
            return temp.toString()
        }
        return text;
    }
}
