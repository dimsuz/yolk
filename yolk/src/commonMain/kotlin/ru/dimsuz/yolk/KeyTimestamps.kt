package ru.dimsuz.yolk

import kotlinx.datetime.Instant

public data class KeyTimestamps(
  val accessAt: Instant?,
  val updatedAt: Instant?,
)
