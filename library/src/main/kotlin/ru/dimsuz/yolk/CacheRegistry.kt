package ru.dimsuz.yolk

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import java.time.Duration

class Cache<K, V>(
  key: suspend () -> K,
  expireAfterWrite: Duration,
  fetch: suspend (key: K) -> V,
  write: suspend (key: K, value: V) -> Unit,
  keyStore: KeyStore<K>,
  ticker: Ticker,
) {
  suspend fun refresh() { TODO() }
  suspend fun load() { TODO() }
  suspend fun get(): V? { TODO() }
  suspend fun hasExpired(): Boolean { TODO() }
  suspend fun invalidate() {}
}

fun <T> Flow<T>.filterCached(cache: Cache<*, T>): Flow<T> {
  return this.filter { !cache.hasExpired() }
}

interface Ticker {
  fun read(): Long

  companion object {
    fun system() = object : Ticker {
      override fun read() = System.currentTimeMillis()
    }
  }
}
