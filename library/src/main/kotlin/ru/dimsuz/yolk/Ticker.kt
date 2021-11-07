package ru.dimsuz.yolk

import java.time.Instant

interface Ticker {
  val now: Instant

  companion object {
    fun system() = object : Ticker {
      override val now: Instant
        get() = Instant.now()
    }
  }
}
