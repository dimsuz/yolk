package ru.dimsuz.yolk.fake

import ru.dimsuz.yolk.MemoryValueStore
import ru.dimsuz.yolk.ValueStore

class FakeValueStore<K : Any, V> : ValueStore<K, V> {
  private val _store = MemoryValueStore<K, V>()
  val stats = Stats()

  data class Stats(
    var reads: Int = 0,
    var writes: Int = 0,
    var removals: Int = 0
  )

  override suspend fun read(key: K): V? {
    stats.reads++
    return _store.read(key)
  }

  override suspend fun write(key: K, value: V) {
    stats.writes++
    _store.write(key, value)
  }

  override suspend fun remove(key: K) {
    stats.removals++
    _store.remove(key)
  }
}
