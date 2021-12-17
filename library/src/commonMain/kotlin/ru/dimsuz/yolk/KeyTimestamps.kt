package ru.dimsuz.yolk

import kotlinx.datetime.Instant

data class KeyTimestamps(
  val accessAt: Instant?,
  val updatedAt: Instant?,
)
