package ru.dimsuz.yolk.fake

import ru.dimsuz.yolk.Ticker
import java.time.Duration
import java.time.Instant

fun Ticker.Companion.fake(): FakeTicker {
  return FakeTicker()
}

class FakeTicker(override var now: Instant = Instant.ofEpochMilli(0)) : Ticker {
  fun advanceBy(duration: Duration) {
    now += duration
  }
}
