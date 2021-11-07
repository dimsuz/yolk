package ru.dimsuz.yolk.fake

import ru.dimsuz.yolk.Cache
import ru.dimsuz.yolk.ExpirePolicy
import ru.dimsuz.yolk.KeyStore
import ru.dimsuz.yolk.MemoryKeyStore
import ru.dimsuz.yolk.MemoryValueStore
import ru.dimsuz.yolk.Ticker
import ru.dimsuz.yolk.ValueStore
import java.time.Duration

fun <K : Any, V> createCache(
  expirePolicy: ExpirePolicy<K>,
  fetch: suspend (key: K) -> V,
  valueStore: ValueStore<K, V> = MemoryValueStore(),
  ticker: Ticker = Ticker.fake(),
  keyStore: KeyStore<K> = MemoryKeyStore(ticker),
): Cache<K, V> {
  return Cache(expirePolicy, fetch, keyStore, valueStore, ticker)
}

fun createStringIntCache(
  expireAfterWrite: Duration = Duration.ofMinutes(3),
  fetch: suspend (key: String) -> Int = { 42 },
  valueStore: ValueStore<String, Int> = MemoryValueStore(),
  ticker: Ticker = Ticker.fake(),
  keyStore: KeyStore<String> = MemoryKeyStore(ticker),
  enableLogging: Boolean = false,
): Cache<String, Int> {
  return Cache(
    expirePolicy = ExpirePolicy.afterWrite(expireAfterWrite, ticker),
    fetch = fetch,
    valueStore = valueStore,
    keyStore = keyStore,
    ticker = ticker,
    log = { lazyValue -> if (enableLogging) println(lazyValue()) }
  )
}
