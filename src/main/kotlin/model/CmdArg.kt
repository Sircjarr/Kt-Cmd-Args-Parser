package lib.lib_args_parse.model

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
    initializer: () -> T?
) : SynchronizedLazyImpl<T?>(initializer)