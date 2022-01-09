package ru.dimsuz.yolk.fake

import ru.dimsuz.yolk.Cache
import ru.dimsuz.yolk.ExpirePolicy
import ru.dimsuz.yolk.KeyStore
import ru.dimsuz.yolk.MemoryKeyStore
import ru.dimsuz.yolk.MemoryValueStore
import ru.dimsuz.yolk.Ticker
import ru.dimsuz.yolk.ValueStore
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

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
  expireAfterWrite: Duration = 3.minutes,
  fetch: suspend (key: String) -> Int = { 42 },
  valueStore: ValueStore<String, Int> = MemoryValueStore(),
  ticker: Ticker = Ticker.fake(),
  keyStore: KeyStore<String> = MemoryKeyStore(ticker),
): Cache<String, Int> {
  return Cache(
    expirePolicy = ExpirePolicy.afterWrite(expireAfterWrite, ticker),
    fetch = fetch,
    valueStore = valueStore,
    keyStore = keyStore,
    ticker = ticker,
  )
}
