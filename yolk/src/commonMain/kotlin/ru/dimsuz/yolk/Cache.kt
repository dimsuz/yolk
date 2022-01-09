package ru.dimsuz.yolk

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.time.Duration

public class Cache<K : Any, V>(
  private val expirePolicy: ExpirePolicy<K>,
  private val fetch: suspend (key: K) -> V,
  private val keyStore: KeyStore<K>,
  private val valueStore: ValueStore<K, V>,
  private val ticker: Ticker = Ticker.system(),
) {

  public suspend fun load(key: K, forceRefresh: Boolean = false): V {
    _events.emit(CacheEvent.LoadStarted(key))
    val timestamps = keyStore.read(key)
    return if (forceRefresh || expirePolicy.hasExpired(key, timestamps)) {
      _events.emit(CacheEvent.Miss(key, timestamps, forceRefresh))
      val value = fetch(key)
      valueStore.write(key, value)
      keyStore.update(key) { ts ->
        ts?.copy(updatedAt = ticker.now) ?: KeyTimestamps(updatedAt = ticker.now, accessAt = null)
      }
      _events.emit(CacheEvent.FetchSuccess(key))
      value
    } else {
      _events.emit(CacheEvent.Hit(key, timestamps))
      valueStore.read(key) ?: error("no value for \"$key\" in value store, while the key is not expired")
    }
  }

  private val _events = MutableSharedFlow<CacheEvent<K>>(
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )

  /**
   * Emits cache events. If backpressure occurs, older unprocessed items will be dropped.
   *
   * You can use built-in logger helper to log these events, see [collectToLog] extension function.
   */
  public val events: Flow<CacheEvent<K>> = _events

  public suspend fun hasExpired(key: K): Boolean {
    return expirePolicy.hasExpired(key, keyStore.read(key))
  }

  public suspend fun invalidate(key: K) {
    keyStore.remove(key)
  }
}

public interface ExpirePolicy<K : Any> {
  public fun hasExpired(key: K, timestamps: KeyTimestamps?): Boolean

  public companion object {
    public fun <K : Any> afterWrite(duration: Duration, ticker: Ticker = Ticker.system()): ExpirePolicy<K> {
      return object : ExpirePolicy<K> {
        override fun hasExpired(key: K, timestamps: KeyTimestamps?): Boolean {
          return timestamps?.updatedAt == null || ticker.now.minus(timestamps.updatedAt) > duration
        }
      }
    }
  }
}
