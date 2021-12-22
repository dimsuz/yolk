package ru.dimsuz.yolk

public expect class MemoryKeyStore<K : Any>(
  ticker: Ticker = Ticker.system(),
) : KeyStore<K>
