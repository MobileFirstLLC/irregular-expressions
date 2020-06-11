package mf.asciitext.fonts

abstract class AppFont(
    protected var id: String,
    protected var name: String,
    protected var _enabled: Boolean,
    /**
     * This property should be set true for font styles
     * that typed in RIGHT -> LEFT direction
     */
    protected var reverse: Boolean = false,

    /**
     * This property should be set true for fonts where
     * encoding depends on a sequence of preceding characters
     */
    private var sequenceAware: Boolean = false

) {
    val fontId: String
        get() = id

    val styledName: String
        get() = encode(name)!!

    val isReversed: Boolean
        get() = reverse

    var isEnabled: Boolean
        get() = _enabled
        set(value) {
            _enabled = value
        }

    val isSequenceAware: Boolean
        get() = sequenceAware

    fun isEmpty(text: String?): Boolean {
        return text == null || text.isEmpty()
    }

    abstract fun encode(text: String?, sequence: CharSequence? = null): String?
}