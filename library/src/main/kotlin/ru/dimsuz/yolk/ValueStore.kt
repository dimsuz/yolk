package ru.dimsuz.yolk

interface ValueStore<K : Any, V> {
  suspend fun read(key: K): V?
  suspend fun write(key: K, value: V)
  suspend fun update(key: K, transform: (V?) -> V?)
  suspend fun remove(key: K)
}
