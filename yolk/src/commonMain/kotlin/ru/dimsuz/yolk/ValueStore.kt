package ru.dimsuz.yolk

public interface ValueStore<K : Any, V> {
  public suspend fun read(key: K): V?
  public suspend fun write(key: K, value: V)
  public suspend fun remove(key: K)
}
