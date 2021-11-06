package ru.dimsuz.yolk.fake

import ru.dimsuz.yolk.Cache
import ru.dimsuz.yolk.ExpirePolicy
import ru.dimsuz.yolk.KeyStore
import ru.dimsuz.yolk.MemoryKeyStore
import ru.dimsuz.yolk.Ticker
import java.time.Duration

fun <K : Any, V> createCache(
  key: suspend () -> K,
  expirePolicy: ExpirePolicy<K>,
  fetch: suspend (key: K) -> V,
  write: suspend (key: K, value: V) -> Unit = { _, _ -> },
  ticker: Ticker = Ticker.fake(),
  keyStore: KeyStore<K> = MemoryKeyStore(ticker),
): Cache<K, V> {
  return Cache(key, expirePolicy, fetch, write, keyStore, ticker)
}

fun createStringIntCache(
  key: String = "a",
  expireAfterWrite: Duration = Duration.ofMinutes(3),
  fetch: suspend (key: String) -> Int = { 42 },
  write: suspend (key: String, value: Int) -> Unit = { _, _ -> },
  ticker: Ticker = Ticker.fake(),
  keyStore: KeyStore<String> = MemoryKeyStore(ticker),
  enableLogging: Boolean = false,
): Cache<String, Int> {
  return Cache(
    key = { key },
    expirePolicy = ExpirePolicy.afterWrite(expireAfterWrite, ticker),
    fetch = fetch,
    write = write,
    keyStore = keyStore,
    ticker = ticker,
    log = { lazyValue -> if (enableLogging) println(lazyValue()) }
  )
}
