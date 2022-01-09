package ru.dimsuz.yolk

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

public suspend fun <K> Flow<CacheEvent<K>>.collectToLog(
  log: (() -> String) -> Unit,
  ticker: Ticker = Ticker.system()
) {
  this.collect { event ->
    when (event) {
      is CacheEvent.LoadStarted -> {
        log { "  loading key ${event.key}" }
      }
      is CacheEvent.Hit -> {
        log {
          val updatedAt = event.timestamps?.updatedAt?.toLocalDateTime(TimeZone.UTC)
          val now = ticker.now.toLocalDateTime(TimeZone.UTC)
          "  key \"${event.key}\" has not expired. (updatedAt: $updatedAt, now: $now)"
        }
      }
      is CacheEvent.Miss -> {
        log {
          val updatedAt = event.timestamps?.updatedAt?.toLocalDateTime(TimeZone.UTC)
          val now = ticker.now.toLocalDateTime(TimeZone.UTC)
          buildString {
            if (event.forceRefresh) {
              append("  \"${event.key}\" refresh forced.")
            } else {
              append("  \"${event.key}\" key has expired.")
            }
            append(" Fetching \"${event.key}\". (updatedAt: $updatedAt, now: $now)")
          }
        }
      }
      is CacheEvent.FetchSuccess -> {
        log {
          "  key \"${event.key}\" fetched successfully"
        }
      }
    }
  }
}
