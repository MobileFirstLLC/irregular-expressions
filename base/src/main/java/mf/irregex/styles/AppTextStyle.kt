package mf.irregex.styles

abstract class AppTextStyle(
    protected var id: String,
    protected var name: String,
    private var _enabled: Boolean,
    private var _priority: Int,
    /**
     * This property should be set true for styles
     * that typed in RIGHT -> LEFT direction
     */
    protected var reverse: Boolean = false,
    /**
     * This property should be set true for styles where
     * encoding depends on a sequence of preceding characters
     */
    private var sequenceAware: Boolean = false
) {
    val styleId: String
        get() = id

    open val styledName: String
        get() = encode(name)!!

    val isReversed: Boolean
        get() = reverse

    var isEnabled: Boolean
        get() = _enabled
        set(value) {
            _enabled = value
        }
    var priority: Int
        get() = _priority
        set(value) {
            _priority = value
        }

    val isSequenceAware: Boolean
        get() = sequenceAware

    fun isEmpty(text: String?): Boolean {
        return text == null || text.isEmpty()
    }

    abstract fun encode(text: String?, sequence: CharSequence? = null): String?
}