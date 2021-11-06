package ru.dimsuz.yolk

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class Cache<K : Any, V>(
  private val key: suspend () -> K,
  private val expirePolicy: ExpirePolicy<K>,
  private val fetch: suspend (key: K) -> V,
  private val write: suspend (key: K, value: V) -> Unit,
  private val keyStore: KeyStore<K>,
  private val ticker: Ticker,
  private val log: (() -> String) -> Unit = {}
) {
  suspend fun refresh() { TODO() }

  suspend fun load() {
    val key = key()
    log { "loading key $key" }
    val timestamps = keyStore.read(key)
    if (expirePolicy.hasExpired(key, timestamps)) {
      log {
        val updatedAt = timestamps?.updatedAt?.let { LocalDateTime.ofInstant(it, ZoneOffset.UTC) }
        val now = LocalDateTime.ofInstant(ticker.now, ZoneOffset.UTC)
        "  key has expired. Fetching. (updatedAt: $updatedAt, now: $now)"
      }
      fetch(key)
      keyStore.update(key) { ts ->
        ts?.copy(updatedAt = ticker.now) ?: KeyTimestamps(updatedAt = ticker.now, accessAt = null)
      }
    } else {
      log {
        val updatedAt = timestamps?.updatedAt?.let { LocalDateTime.ofInstant(it, ZoneOffset.UTC) }
        val now = LocalDateTime.ofInstant(ticker.now, ZoneOffset.UTC)
        "  key has not expired. (updatedAt: $updatedAt, now: $now)"
      }
    }
  }

  suspend fun get(): V? { TODO() }

  suspend fun hasExpired(): Boolean {
    val key = key()
    return expirePolicy.hasExpired(key, keyStore.read(key))
  }

  suspend fun invalidate() {
    keyStore.remove(key())
  }
}

interface ExpirePolicy<K : Any> {
  fun hasExpired(key: K, timestamps: KeyTimestamps?): Boolean

  companion object {
    fun <K : Any> afterWrite(duration: Duration, ticker: Ticker = Ticker.system()): ExpirePolicy<K> {
      return object : ExpirePolicy<K> {
        override fun hasExpired(key: K, timestamps: KeyTimestamps?): Boolean {
          return timestamps?.updatedAt == null || Duration.between(timestamps.updatedAt, ticker.now) > duration
        }
      }
    }
  }
}

data class KeyTimestamps(
  val accessAt: Instant?,
  val updatedAt: Instant?,
)

fun <T> Flow<T>.filterCached(cache: Cache<*, T>): Flow<T> {
  return this.filter { !cache.hasExpired() }
}

interface Ticker {
  val now: Instant

  companion object {
    fun system() = object : Ticker {
      override val now: Instant
        get() = Instant.now()
    }
  }
}
