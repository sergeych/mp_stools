package net.sergeych.tools

import kotlinx.coroutines.sync.Mutex
import kotlin.time.Clock
import kotlin.time.Instant
import net.sergeych.mptools.withReentrantLock
import kotlin.time.Duration

/**
 * Like [CachedExpression] but working in sync mode, no coroutines required, hence only for JVM target.
 */
class CachedSyncExpression<T>(
    /**
     * If not null, recalculated cached expression is automaticallu invalidated after this time. Set it to null
     * to keep it indefinitely until [clearCache] is called
     */
    var expiresIn: Duration? = null,
    initialValue: T? = null,
) {

    private var cachedValue: T? = initialValue
    private var cacheSetAt: Instant? = initialValue?.let { Clock.System.now() }
    private val access = Object()

    /**
     * If there is a cached value it will be dropped
     */
    fun clearCache() {
        synchronized(access) { cachedValue = null }
    }

    /**
     * The cached value is overriden or set to a specified value. It meands, next [get] call will not
     * calculate expression but return the value provided.
     */
    @Suppress("unused")
    fun overrideCacheWith(value: T) {
        synchronized(access) { cachedValue = value; cacheSetAt = Clock.System.now() }
    }

    /**
     * Return cache value, if presented and not expired. See [expiresIn].
     */
    fun cachedOrNull(): T? = synchronized(access) {
        if (cachedValue != null) {
            expiresIn?.let { d ->
                val setAt = cacheSetAt ?: throw IllegalStateException("cached value is set but cacheSetAt is null")
                if (setAt + d < Clock.System.now())
                    cachedValue = null
            }
        }
        cachedValue
    }


    /**
     * Return cached value if exists and not expired, or recalculates new one and caches it. [producer] will either
     * be called _before_ return, or not be called at all, so it is safe to use dusposable resource in it.
     * @param producer lambda expresson to calculate actual value, that will be cached for subsequent calls
     */
    fun get(producer: () -> T) = synchronized(access) {
        cachedOrNull() ?: producer().also {
            cacheSetAt = Clock.System.now()
            cachedValue = it
        }
    }

    /**
     * Try to get the expression value from the block if it is not already cached. If the block returns
     * null, just return it.
     */
    fun optGet(producer: () -> T?) = synchronized(access) {
        cachedOrNull() ?: producer().also {
            if (it != null) {
                cacheSetAt = Clock.System.now()
                cachedValue = it
            }
        }
    }
}
