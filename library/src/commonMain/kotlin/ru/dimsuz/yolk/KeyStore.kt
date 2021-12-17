package ru.dimsuz.yolk

interface KeyStore<K : Any> {
  suspend fun read(key: K): KeyTimestamps?
  suspend fun write(key: K, timestamps: KeyTimestamps)
  suspend fun update(key: K, transform: (KeyTimestamps?) -> KeyTimestamps?)
  suspend fun remove(key: K)
}
