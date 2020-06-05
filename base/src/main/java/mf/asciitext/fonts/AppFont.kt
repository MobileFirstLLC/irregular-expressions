package mf.asciitext.fonts

abstract class AppFont(
    protected var id: String,
    protected var name: String,

    /**
     * This property should be set true for font styles
     * that typed in RIGHT -> LEFT direction
     */
    protected var reverse: Boolean = false,

    /**
     * This property should be set true for fonts where
     * encoding depends on a sequence of preceding characters
     */
    protected var sequenceAware: Boolean = false
) {
    val styledName: String
        get() = encode(name)!!

    val isReversed : Boolean
        get() = reverse

    val isSequenceAware : Boolean
        get() = sequenceAware

    fun isEmpty(text: String?): Boolean {
        return text == null || text.isEmpty()
    }

    abstract fun encode(text: String?, sequence: CharSequence? = null): String?

}