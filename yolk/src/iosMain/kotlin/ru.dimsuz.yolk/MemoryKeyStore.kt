package ru.dimsuz.yolk

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

public actual class MemoryKeyStore<K : Any> actual constructor(
  private val ticker: Ticker,
) : KeyStore<K> {
  private val store = HashMap<K, KeyTimestamps>()
  private val mutex = Mutex()

  override suspend fun read(key: K): KeyTimestamps? {
    return mutex.withLock {
      val value = store[key]?.copy(accessAt = ticker.now)
      if (value != null) {
        store[key] = value
      }
      value
    }
  }

  override suspend fun write(key: K, timestamps: KeyTimestamps) {
    mutex.withLock {
      val value = store[key]
      if (value != null) {
        store[key] = value.copy(updatedAt = ticker.now)
      }
    }
  }

  override suspend fun update(key: K, transform: (KeyTimestamps?) -> KeyTimestamps?) {
    mutex.withLock {
      val value = transform(store[key])
      if (value != null) {
        store[key] = value
      }
    }
  }

  override suspend fun remove(key: K) {
    mutex.withLock {
      store.remove(key)
    }
  }
}
