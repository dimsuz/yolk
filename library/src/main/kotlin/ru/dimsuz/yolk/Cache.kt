package ru.dimsuz.yolk

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

class Cache<K : Any, V>(
  private val expirePolicy: ExpirePolicy<K>,
  private val fetch: suspend (key: K) -> V,
  private val keyStore: KeyStore<K>,
  private val valueStore: ValueStore<K, V>,
  private val ticker: Ticker = Ticker.system(),
  private val log: (() -> String) -> Unit = {}
) {

  suspend fun load(key: K, forceRefresh: Boolean = false): V {
    log { "loading key $key" }
    val timestamps = keyStore.read(key)
    return if (forceRefresh || expirePolicy.hasExpired(key, timestamps)) {
      logMiss(timestamps, forceRefresh)
      val value = fetch(key)
      valueStore.write(key, value)
      keyStore.update(key) { ts ->
        ts?.copy(updatedAt = ticker.now) ?: KeyTimestamps(updatedAt = ticker.now, accessAt = null)
      }
      value
    } else {
      logHit(timestamps)
      valueStore.read(key) ?: error("no value for \"$key\" in value store, while the key is not expired")
    }
  }

  private fun logHit(timestamps: KeyTimestamps?) {
    log {
      val updatedAt = timestamps?.updatedAt?.let { LocalDateTime.ofInstant(it, ZoneOffset.UTC) }
      val now = LocalDateTime.ofInstant(ticker.now, ZoneOffset.UTC)
      "  key has not expired. (updatedAt: $updatedAt, now: $now)"
    }
  }

  private fun logMiss(timestamps: KeyTimestamps?, forceRefresh: Boolean) {
    log {
      val updatedAt = timestamps?.updatedAt?.let { LocalDateTime.ofInstant(it, ZoneOffset.UTC) }
      val now = LocalDateTime.ofInstant(ticker.now, ZoneOffset.UTC)
      buildString {
        if (forceRefresh) {
          append("  refresh forced.")
        } else {
          append("  key has expired.")
        }
        append(" Fetching. (updatedAt: $updatedAt, now: $now)")
      }
    }
  }

  suspend fun hasExpired(key: K): Boolean {
    return expirePolicy.hasExpired(key, keyStore.read(key))
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
