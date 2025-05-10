package lib.lib_args_parse.model

sealed class CmdArgNullable<T>(
    val help: String,
    val keys: Array<out String>,
    val valueLabel: String,
    initializer: () -> T?,
) : SynchronizedLazyImpl<T?>(initializer, help) {

    fun validate() {
        initializer?.invoke()
    }

    sealed class KeyValue<T>(
        initializer: () -> T?,
        help: String,
        valueLabel: String,
        keys: Array<out String>,
    ) : CmdArgNullable<T?>(help, keys, valueLabel, initializer) {
        class Single<T>(
            help: String,
            keys: Array<out String>,
            valueLabel: String,
            initializer: () -> T?,
        ) : KeyValue<T?>(initializer, help, valueLabel, keys)

        class Mapped<T>(
            help: String,
            keys: Array<out String>,
            valueLabel: String,
            initializer: () -> T?,
            val map: Map<String, T>
        ) : KeyValue<T?>(initializer, help, valueLabel, keys)
    }
}

sealed class CmdArgNonNull<T>(
    initializer: () -> T,
    val help: String
) : SynchronizedLazyImpl<T>(initializer, help) {

    sealed class KeyValue<T>(
        initializer: () -> T,
        help: String,
        val valueLabel: String,
        val keys: Array<out String>,
        val default: T?,
    ) : CmdArgNonNull<T>(initializer = {
        initializer() ?: checkNotNull(default)
    }, help) {

        fun validate() {
            initializer?.invoke()
        }

        class Single<T>(
            help: String,
            keys: Array<out String>,
            valueLabel: String,
            default: T?,
            initializer: () -> T,
        ) : KeyValue<T>(initializer, help, valueLabel, keys, default)

        class Mapped<T>(
            help: String,
            keys: Array<out String>,
            valueLabel: String,
            default: T?,
            initializer: () -> T,
            val map: Map<String, T>
        ) : KeyValue<T>(initializer, help, valueLabel, keys, default)
    }

    class Flag(
        val keys: Array<out String>,
        val default: Boolean,
        help: String,
        initializer: () -> Boolean,
    ) : CmdArgNonNull<Boolean>(initializer, help)

    class Positional<T>(
        help: String,
        val valueLabel: String,
        initializer: () -> T
    ) : CmdArgNonNull<T>(initializer, help)
}

class Subcommand<T>(
    val help: String,
    initializer: () -> T?
) : SynchronizedLazyImpl<T?>(initializer)