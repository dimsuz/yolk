package ru.dimsuz.yolk.extension.sqldelight

import com.squareup.sqldelight.Query
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import ru.dimsuz.yolk.Cache
import ru.dimsuz.yolk.CacheEvent
import ru.dimsuz.yolk.LazyKeyCache

public fun <T : Any, K : Any> Query<T>.asCachedFlow(
  cache: Cache<K, Unit>,
  keyFactory: suspend () -> K,
): Flow<Query<T>> {
  return merge(
    this.resultChangeEvents(),
    cache.events.filterIsInstance<CacheEvent.FetchSuccess<*>>()
  )
    .filter { !cache.hasExpired(keyFactory()) }
    .map { this@asCachedFlow }
}

public fun <T : Any, K : Any> Query<T>.asCachedFlow(
  cache: LazyKeyCache<K, Unit>,
): Flow<Query<T>> {
  return merge(
    this.resultChangeEvents(),
    cache.events.filterIsInstance<CacheEvent.FetchSuccess<*>>()
  )
    .filter { !cache.hasExpired() }
    .map { this@asCachedFlow }
}

private fun Query<*>.resultChangeEvents(): Flow<Unit> {
  return flow {
    // NOTE @dz this implementation is copied from SqlDelight "FlowExtensions.kt" and extended to support cache
    val channel = Channel<Unit>(Channel.CONFLATED)
    channel.trySend(Unit)

    val listener = object : Query.Listener {
      override fun queryResultsChanged() {
        channel.trySend(Unit)
      }
    }

    addListener(listener)
    try {
      for (item in channel) {
        emit(Unit)
      }
    } finally {
      removeListener(listener)
    }
  }
}
