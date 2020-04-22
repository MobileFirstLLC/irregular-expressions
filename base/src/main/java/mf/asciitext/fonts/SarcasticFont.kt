package mf.asciitext.fonts

// TODO: add back this font style
// the challenge is this: you need a longer sequence to determine casing
// but when encoding keypresses, the input is a single char

class SarcasticFont internal constructor(
    id: String,
    name: String,
    premium: Boolean
) : AppFont(id, name, premium) {

    override fun encode(text: String?): String? {
        if (!isEmpty(text)) {
            var lowercase = true
            val cs = text!!.toCharArray()
            val temp = StringBuilder()
            for (c in cs) {
                temp.append(if (lowercase) c.toLowerCase() else c.toUpperCase())
                lowercase = !lowercase
            }
            return temp.toString()
        }
        return text
    }
}