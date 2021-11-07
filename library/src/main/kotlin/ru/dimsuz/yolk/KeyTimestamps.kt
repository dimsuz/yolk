package ru.dimsuz.yolk

import java.time.Instant

data class KeyTimestamps(
  val accessAt: Instant?,
  val updatedAt: Instant?,
)
