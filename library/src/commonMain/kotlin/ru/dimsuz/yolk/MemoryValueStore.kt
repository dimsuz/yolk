package ru.dimsuz.yolk

import java.util.concurrent.ConcurrentHashMap

class MemoryValueStore<K : Any, V> : ValueStore<K, V> {
  private val store = ConcurrentHashMap<K, V>()

  override suspend fun read(key: K): V? {
    return store[key]
  }

  override suspend fun write(key: K, value: V) {
    store[key] = value
  }

  override suspend fun remove(key: K) {
    store.remove(key)
  }
}
