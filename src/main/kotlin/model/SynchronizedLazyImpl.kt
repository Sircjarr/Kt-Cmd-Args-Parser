/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package com.github.sircjarr.cmdargsparser.model

/**
 * Borrowed from [kotlin.SynchronizedLazyImpl].
 */
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