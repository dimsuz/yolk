package ru.dimsuz.yolk

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

interface Ticker {
  val now: Instant

  companion object {
    fun system() = object : Ticker {
      override val now: Instant
        get() = Clock.System.now()
    }
  }
}
