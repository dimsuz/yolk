package ru.dimsuz.yolk

class LazyKeyCache<K : Any, V>(
  private val keyFactory: suspend () -> K,
  expirePolicy: ExpirePolicy<K>,
  fetch: suspend (key: K) -> V,
  keyStore: KeyStore<K>,
  valueStore: ValueStore<K, V>,
  ticker: Ticker,
  log: (() -> String) -> Unit = {}
) {
  private val _cache = Cache(expirePolicy, fetch, keyStore, valueStore, ticker, log)

  suspend fun load(forceRefresh: Boolean) = _cache.load(keyFactory(), forceRefresh)
  suspend fun hasExpired() = _cache.hasExpired(keyFactory())
  suspend fun invalidate() = _cache.invalidate(keyFactory())
}
