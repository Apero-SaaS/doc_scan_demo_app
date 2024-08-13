package com.apero.app.poc_ml_docscan.image_processing.model

import java.io.Serializable
import java.lang.ref.WeakReference

internal fun <V> weakRefLazy(
    lock: Any? = null,
    initializer: () -> V,
): Lazy<V> = WeakRefLazyImpl(initializer, lock)

private object UNINITIALIZED_VALUE

// Forked from SynchronizedLazyImpl 
private class WeakRefLazyImpl<out T>(
    private val initializer: () -> T,
    lock: Any? = null,
) : Lazy<T>,
    Serializable {

    @Volatile
    private var _value = WeakReference<Any?>(UNINITIALIZED_VALUE)

    // final field is required to enable safe publication of constructed instance
    private val lock = lock ?: this

    override val value: T
        get() {
            val v1 = _value.get()
            if (v1 !== UNINITIALIZED_VALUE) {
                @Suppress("UNCHECKED_CAST")
                return v1 as T
            }

            return synchronized(lock) {
                val v2 = _value.get()
                if (v2 !== UNINITIALIZED_VALUE) {
                    @Suppress("UNCHECKED_CAST") (v2 as T)
                } else {
                    val typedValue = initializer()
                    _value = WeakReference(typedValue)
                    typedValue
                }
            }
        }

    override fun isInitialized(): Boolean = _value.get() !== UNINITIALIZED_VALUE

    override fun toString(): String =
        if (isInitialized()) value.toString() else "Lazy value not initialized yet."
}
