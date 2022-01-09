package ru.dimsuz.yolk

import kotlinx.coroutines.flow.Flow

public class LazyKeyCache<K : Any, V>(
  private val keyFactory: suspend () -> K,
  expirePolicy: ExpirePolicy<K>,
  fetch: suspend (key: K) -> V,
  keyStore: KeyStore<K>,
  valueStore: ValueStore<K, V>,
  ticker: Ticker = Ticker.system(),
) {
  private val _cache = Cache(expirePolicy, fetch, keyStore, valueStore, ticker)

  public suspend fun load(forceRefresh: Boolean = false): V = _cache.load(keyFactory(), forceRefresh)
  public suspend fun hasExpired(): Boolean = _cache.hasExpired(keyFactory())
  public suspend fun invalidate(): Unit = _cache.invalidate(keyFactory())

  public val events: Flow<CacheEvent<K>> = _cache.events
}
