package mf.asciitext.fonts

abstract class AppFont(
    protected var id: String,
    protected var name: String
) {
    val styledName: String
        get() = encode(name)!!

    fun isEmpty(text: String?): Boolean {
        return text == null || text.isEmpty()
    }

    abstract fun encode(text: String?, sequence: CharSequence? = null): String?

}