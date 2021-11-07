package ru.dimsuz.yolk

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

class Cache<K : Any, V>(
  private val expirePolicy: ExpirePolicy<K>,
  private val fetch: suspend (key: K) -> V,
  private val keyStore: KeyStore<K>,
  private val valueStore: ValueStore<K, V>,
  private val ticker: Ticker,
  private val log: (() -> String) -> Unit = {}
) {

  suspend fun load(key: K) {
    log { "loading key $key" }
    val timestamps = keyStore.read(key)
    if (expirePolicy.hasExpired(key, timestamps)) {
      log {
        val updatedAt = timestamps?.updatedAt?.let { LocalDateTime.ofInstant(it, ZoneOffset.UTC) }
        val now = LocalDateTime.ofInstant(ticker.now, ZoneOffset.UTC)
        "  key has expired. Fetching. (updatedAt: $updatedAt, now: $now)"
      }
      val value = fetch(key)
      valueStore.write(key, value)
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

  suspend fun get(key: K): V? {
    return valueStore.read(key)
  }

  suspend fun hasExpired(key: K): Boolean {
    return expirePolicy.hasExpired(key, keyStore.read(key))
  }

  suspend fun refresh(key: K) {
    invalidate(key)
    load(key)
  }

  suspend fun invalidate(key: K) {
    keyStore.remove(key)
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
