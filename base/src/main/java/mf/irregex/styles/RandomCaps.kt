package mf.irregex.styles

class RandomCaps internal constructor(
    name: String,
    priority: Int,
    enabled: Boolean
) : AppTextStyle(id, name, enabled, priority, false, true) {

    init {
        encodedName = encode(name)!!
    }

    companion object {
        const val id = "spongemock"
    }


    override fun encode(text: String?, sequence: CharSequence?): String? {
        if (!isEmpty(text)) {
            var lowercase = false
            if (sequence != null && sequence.lastIndex >= 0) {
                val trimmedSeq = sequence.trim()
                if (trimmedSeq.isNotEmpty()) {
                    val lastChar = trimmedSeq[trimmedSeq.lastIndex].toInt()
                    lowercase = lastChar >= 'A'.toInt() && lastChar <= 'Z'.toInt()
                }
            }
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