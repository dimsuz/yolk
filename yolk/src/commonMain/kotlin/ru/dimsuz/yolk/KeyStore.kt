package ru.dimsuz.yolk

public interface KeyStore<K : Any> {
  public suspend fun read(key: K): KeyTimestamps?
  public suspend fun write(key: K, timestamps: KeyTimestamps)
  public suspend fun update(key: K, transform: (KeyTimestamps?) -> KeyTimestamps?)
  public suspend fun remove(key: K)
}
