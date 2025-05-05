package lib.lib_args_parse

sealed class CmdArgNullable<T>(
    val hint: String,
    val keys: Array<out String>,
    val valueLabel: String,
    initializer: () -> T?,
) : SynchronizedLazyImpl<T?>(initializer, hint) {

    fun validate() {
        initializer?.invoke()
    }

    sealed class KeyValue<T>(
        initializer: () -> T?,
        hint: String,
        valueLabel: String,
        keys: Array<out String>,
    ) : CmdArgNullable<T?>(hint, keys, valueLabel, initializer) {
        class Single<T>(
            hint: String,
            keys: Array<out String>,
            valueLabel: String,
            initializer: () -> T?,
        ) : KeyValue<T?>(initializer, hint, valueLabel, keys)

        class Mapped<T>(
            hint: String,
            keys: Array<out String>,
            valueLabel: String,
            initializer: () -> T?,
            val map: Map<String, T>
        ) : KeyValue<T?>(initializer, hint, valueLabel, keys)
    }
}

sealed class CmdArgNonNull<T>(
    initializer: () -> T,
    val hint: String
) : SynchronizedLazyImpl<T>(initializer, hint) {

    sealed class KeyValue<T>(
        initializer: () -> T,
        hint: String,
        val valueLabel: String,
        val keys: Array<out String>,
        val default: T?,
    ) : CmdArgNonNull<T>(initializer = {
        initializer() ?: checkNotNull(default)
    }, hint) {

        fun validate() {
            initializer?.invoke()
        }

        class Single<T>(
            hint: String,
            keys: Array<out String>,
            valueLabel: String,
            default: T?,
            initializer: () -> T,
        ) : KeyValue<T>(initializer, hint, valueLabel, keys, default)

        class Mapped<T>(
            hint: String,
            keys: Array<out String>,
            valueLabel: String,
            default: T?,
            initializer: () -> T,
            val map: Map<String, T>
        ) : KeyValue<T>(initializer, hint, valueLabel, keys, default)
    }

    class Flag(
        val keys: Array<out String>,
        val default: Boolean,
        hint: String,
        initializer: () -> Boolean,
    ) : CmdArgNonNull<Boolean>(initializer, hint)

    class Positional<T>(
        hint: String,
        val valueLabel: String,
        initializer: () -> T
    ) : CmdArgNonNull<T>(initializer, hint)
}

class Subcommand<T>(
    val command: String,
    val parser: CmdArgsParser,
    initializer: () -> T?
) : SynchronizedLazyImpl<T?>(initializer)

open class SynchronizedLazyImpl<T>(
    initializer: () -> T,
    lock: Any? = null
) : Lazy<T> {
    object UNINITIALIZED_VALUE

    var initializer: (() -> T)? = initializer

    @Volatile
    private var _value: Any? = UNINITIALIZED_VALUE

    // final field to ensure safe publication of 'SynchronizedLazyImpl' itself through
    // var lazy = lazy() {}
    private val lock = lock ?: this

    override val value: T
        get() {
            val _v1 = _value
            if (_v1 !== UNINITIALIZED_VALUE) {
                @Suppress("UNCHECKED_CAST")
                return _v1 as T
            }

            return synchronized(lock) {
                val _v2 = _value
                if (_v2 !== UNINITIALIZED_VALUE) {
                    @Suppress("UNCHECKED_CAST") (_v2 as T)
                } else {
                    val typedValue = initializer!!()
                    _value = typedValue
                    initializer = null
                    typedValue
                }
            }
        }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."
}