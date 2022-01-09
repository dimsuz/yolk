package ru.dimsuz.yolk

public sealed class CacheEvent<K> {
  /**
   * Emitted when [key] loading is about to start.
   * Will be followed by either [Hit] or [Miss] and then [FetchSuccess] (if there are no errors)
   */
  public data class LoadStarted<K>(val key: K) : CacheEvent<K>()

  /**
   * Emitted when cache hit occurs when loading the [key].
   */
  public data class Hit<K>(val key: K, val timestamps: KeyTimestamps?) : CacheEvent<K>()

  /**
   * Emitted when cache miss occurs when loading the [key]. Fetching value will start after this event is emitted.
   */
  public data class Miss<K>(val key: K, val timestamps: KeyTimestamps?, val forceRefresh: Boolean) : CacheEvent<K>()

  /**
   * Emitted when fetch operation was successfully completed, value was stored in the value store,
   * and key was marked as fresh
   */
  public data class FetchSuccess<K>(val key: K) : CacheEvent<K>()
}
