package ru.dimsuz.yolk

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

public interface Ticker {
  public val now: Instant

  public companion object {
    public fun system(): Ticker = object : Ticker {
      override val now: Instant
        get() = Clock.System.now()
    }
  }
}
