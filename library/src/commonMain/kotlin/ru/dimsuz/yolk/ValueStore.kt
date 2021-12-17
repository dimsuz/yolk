package ru.dimsuz.yolk

interface ValueStore<K : Any, V> {
  suspend fun read(key: K): V?
  suspend fun write(key: K, value: V)
  suspend fun remove(key: K)
}
