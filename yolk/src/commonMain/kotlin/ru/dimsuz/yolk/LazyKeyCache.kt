package ru.dimsuz.yolk

public class LazyKeyCache<K : Any, V>(
  private val keyFactory: suspend () -> K,
  expirePolicy: ExpirePolicy<K>,
  fetch: suspend (key: K) -> V,
  keyStore: KeyStore<K>,
  valueStore: ValueStore<K, V>,
  ticker: Ticker = Ticker.system(),
  log: (() -> String) -> Unit = {}
) {
  private val _cache = Cache(expirePolicy, fetch, keyStore, valueStore, ticker, log)

  public suspend fun load(forceRefresh: Boolean = false): V = _cache.load(keyFactory(), forceRefresh)
  public suspend fun hasExpired(): Boolean = _cache.hasExpired(keyFactory())
  public suspend fun invalidate(): Unit = _cache.invalidate(keyFactory())
}
