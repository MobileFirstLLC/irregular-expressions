package mf.asciitext.fonts

abstract class AppFont(
    protected var id: String,
    protected var name: String,
    protected var reverse: Boolean = false
) {
    val styledName: String
        get() = encode(name)!!

    val isReversed : Boolean
        get() = reverse

    fun isEmpty(text: String?): Boolean {
        return text == null || text.isEmpty()
    }

    abstract fun encode(text: String?, sequence: CharSequence? = null): String?

}