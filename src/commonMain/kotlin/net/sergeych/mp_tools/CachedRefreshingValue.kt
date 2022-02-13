@file:OptIn(DelicateCoroutinesApi::class)

package net.sergeych.mp_tools

import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.sergeych.mp_logger.LogTag
import net.sergeych.mp_logger.exception
import net.sergeych.mp_logger.info

/**
 * The value that is periodically refreshed with a provided lambda and return last successful result. Unlike
 * [CachedExpression] it will be refreshed even if not used, but there will be a fresh (or best attempt) value
 * any time.
 *
 * __Important! Lambda is called periodically and asynchronously, so do not use any disposable data
 * frin a calling closure!__ This is especially important with resources like database connections: if yo
 * need one, request and release it inside the refresher block!
 */
@Suppress("unused")
class CachedRefreshingValue<T>(
    /**
     * Timeout between successful refreshes
     */
    val refreshInMilli: Long ,
    /**
     * Timeout on first and second errors
     */
    val errorTimeout1Milli: Long = refreshInMilli,
    /**
     * Timeout after third and all further errors
     */
    val errorTimeout2Milli: Long = errorTimeout1Milli*3,
    /**
     * The lambda to calculate the value to cache
     */
    refresher: suspend () -> T
): LogTag("CRVAL") {

    /**
     * Number of error retries after last successful refresh. ) if the current value is refreshed successfully
     */
    var errorRetry: Int = 0
        private set

    /**
     * When the value was refreshed for the last time
     */
    var lastUpdatedAt: Instant? = null
        private set

    private var completableDeferred = CompletableDeferred<T>()

    /**
     * Get the current state. If will not suspend unless there wos not a single successful refresh at all. Otherwise
     * it suspends until first siccessful refresh (maybe indefinitely)
     */
    suspend fun get(): T = completableDeferred.await()

    init {
        GlobalScope.launch {
            while( true ) {
                try {
                    val result = refresher()
                    if( completableDeferred.isActive )
                        completableDeferred.complete(result)
                    else
                        completableDeferred = CompletableDeferred(result)
                    lastUpdatedAt = Clock.System.now()
                    errorRetry = 0
                    info { "updated successfully" }
                    delay(refreshInMilli)
                }
                catch(x: Exception) {
                    exception { "while recalculating cached value" to x }
                    delay( if( errorRetry++ < 2) errorTimeout1Milli else errorTimeout2Milli )
                }
            }
        }
    }

}