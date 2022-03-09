@file:OptIn(ExperimentalTime::class)

package net.sergeych.mptools

import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * The expression that will be calculated then cached fr specified amount of time. Differs from lazy values
 * as the calculation can use dynamic data passed via closures, so it can be recalculated with time or
 * by request. Optimized to be used with coroutines, e.g. suspending expression like loading values from network,
 * etc.
 *
 *  Usage sample
 *
 * ~~~kotlin
 *   val x = CachedExpression<String>(50.seconds)
 *   //...
 *   val currentValue = x.get {
 *      // this will be called once 50 seconds at maximum:
 *      getValueFromWeb("https://acme.com/api/status")
 *   }
 * ~~~
 *
 * Note that expression closure __is not stored and is only executed where and when it was called__ so it is safe
 * to use any disposable/recycled resources in it, like database connections, etc. Expression producing block
 * will be called before [get] returns, or will not be called at all. Cached is the value, not the producing lambda.
 */
class CachedExpression<T>(
    /**
     * If not null, recalculated cached expression is automaticallu invalidated after this time. Set it to null
     * to keep it indefinitely until [clearCache] is called
     */
    var expiresIn: Duration? = null,
    initialValue: T? = null,
) {

    private var cachedValue: T? = initialValue
    private var cacheSetAt: Instant? = initialValue?.let { Clock.System.now() }
    private val mutex = Mutex()

    /**
     * If there is a cached value it will be dropped
     */
    suspend fun clearCache() {
        mutex.withReentrantLock { cachedValue = null }
    }

    @Suppress("unused")
    suspend fun overrideCacheWith(value: T) {
        mutex.withReentrantLock { cachedValue = value; cacheSetAt = Clock.System.now() }
    }

    /**
     * Return cache value, if presented and not expired. See [expiresIn].
     */
    suspend fun cachedOrNull(): T? = mutex.withReentrantLock {
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
    suspend fun get(producer: suspend () -> T) = mutex.withReentrantLock {
        cachedOrNull() ?: producer().also {
            cacheSetAt = Clock.System.now()
            cachedValue = it
        }
    }

    /**
     * Try to get the expression value from the block if it is not already cached. If the block returns
     * null, just return it.
     */
    suspend fun optGet(producer: suspend () -> T?) = mutex.withReentrantLock {
        cachedOrNull() ?: producer().also {
            if (it != null) {
                cacheSetAt = Clock.System.now()
                cachedValue = it
            }
        }
    }
}
