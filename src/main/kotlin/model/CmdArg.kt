package com.github.sircjarr.cmdargsparser.model

import com.github.sircjarr.cmdargsparser.lib.kotlinstdlib.SynchronizedLazyImpl

/**
 * Nullable [SynchronizedLazyImpl] command argument for optionals.
 */
sealed class CmdArgNullable<T>(
    val help: String,
    val keys: Array<out String>,
    val valueLabel: String,
    initializer: () -> T?,
) : SynchronizedLazyImpl<T?>(initializer, help) {

    /**
     * Nullable [SynchronizedLazyImpl] command argument for optionals with a key-value syntax.
     */
    sealed class KeyValue<T>(
        initializer: () -> T?,
        help: String,
        valueLabel: String,
        keys: Array<out String>,
    ) : CmdArgNullable<T?>(help, keys, valueLabel, initializer) {
        /**
         * Nullable [SynchronizedLazyImpl] command argument for optionals with a key-value syntax. Values are not
         * restricted.
         *
         * @see [com.github.sircjarr.cmdargsparser.CmdArgsParser.optionalArg]
         */
        class Single<T>(
            help: String,
            keys: Array<out String>,
            valueLabel: String,
            initializer: () -> T?,
        ) : KeyValue<T?>(initializer, help, valueLabel, keys)

        /**
         * Nullable [SynchronizedLazyImpl] command argument for optionals with a key-value syntax. Values are restricted
         * to the keys in [map].
         *
         * @see [com.github.sircjarr.cmdargsparser.CmdArgsParser.optionalMappedArg]
         */
        class Mapped<T>(
            help: String,
            keys: Array<out String>,
            valueLabel: String,
            initializer: () -> T?,
            val map: Map<String, T>
        ) : KeyValue<T?>(initializer, help, valueLabel, keys)
    }
}

/**
 * Non-null [SynchronizedLazyImpl] command argument for required or optional with default args.
 */
sealed class CmdArgNonNull<T>(
    initializer: () -> T,
    val help: String
) : SynchronizedLazyImpl<T>(initializer, help) {

    /**
     * Non-null [SynchronizedLazyImpl] key-value command argument for required or optional with default args.
     */
    sealed class KeyValue<T>(
        initializer: () -> T,
        help: String,
        val valueLabel: String,
        val keys: Array<out String>,
        val default: T?,
    ) : CmdArgNonNull<T>(initializer = {
        initializer() ?: checkNotNull(default)
    }, help) {

        /**
         * Non-null [SynchronizedLazyImpl] key-value command argument for required or optional with default args. Values
         * are not restricted.
         *
         * @see [com.github.sircjarr.cmdargsparser.CmdArgsParser.requiredArg]
         */
        class Single<T>(
            help: String,
            keys: Array<out String>,
            valueLabel: String,
            default: T?,
            initializer: () -> T,
        ) : KeyValue<T>(initializer, help, valueLabel, keys, default)

        /**
         * Non-null [SynchronizedLazyImpl] key-value command argument for required or optional with default args. Values
         * are restricted to the keys in [map].
         *
         * @see [com.github.sircjarr.cmdargsparser.CmdArgsParser.requiredMapArg]
         */
        class Mapped<T>(
            help: String,
            keys: Array<out String>,
            valueLabel: String,
            default: T?,
            initializer: () -> T,
            val map: Map<String, T>
        ) : KeyValue<T>(initializer, help, valueLabel, keys, default)
    }

    /**
     * Non-null [SynchronizedLazyImpl] optional command argument that maps to a Boolean.
     *
     * @see [com.github.sircjarr.cmdargsparser.CmdArgsParser.flagArg]
     */
    class Flag(
        val keys: Array<out String>,
        val default: Boolean,
        help: String,
        initializer: () -> Boolean,
    ) : CmdArgNonNull<Boolean>(initializer, help)


    /**
     * Non-null [SynchronizedLazyImpl] required positional command argument.
     *
     * @see [com.github.sircjarr.cmdargsparser.CmdArgsParser.positionalArg]
     */
    class Positional<T>(
        help: String,
        val valueLabel: String,
        initializer: () -> T
    ) : CmdArgNonNull<T>(initializer, help)
}

/**
 * Nullable [SynchronizedLazyImpl] for subcommands.
 *
 * @see [com.github.sircjarr.cmdargsparser.CmdArgsParser.subparser]
 */
class Subcommand<T>(
    val help: String,
    initializer: () -> T?
) : SynchronizedLazyImpl<T?>(initializer)