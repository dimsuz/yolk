package ru.dimsuz.yolk.extension.sqldelight

import app.cash.turbine.test
import com.squareup.sqldelight.Query
import com.squareup.sqldelight.db.SqlCursor
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Instant
import ru.dimsuz.yolk.Cache
import ru.dimsuz.yolk.ExpirePolicy
import ru.dimsuz.yolk.MemoryKeyStore
import ru.dimsuz.yolk.MemoryValueStore
import ru.dimsuz.yolk.Ticker
import kotlin.time.Duration

class FlowExtensionsTest : ShouldSpec({
  should("emit new value after cache is invalidated second time") {

    val tableData = MutableStateFlow(0L)

    val query = object : Query<Int>(mutableListOf(), { it.getLong(0)!!.toInt() }) {
      override fun execute(): SqlCursor {
        return object : SqlCursor {
          override fun close() {
          }

          override fun getBytes(index: Int): ByteArray? {
            return null
          }

          override fun getDouble(index: Int): Double? {
            return null
          }

          override fun getLong(index: Int): Long {
            return tableData.value
          }

          override fun getString(index: Int): String? {
            return null
          }

          var count = 0
          override fun next(): Boolean {
            return count++ == 0
          }
        }
      }
    }

    val ticker = Ticker.fake()
    val cache = Cache<Int, Unit>(
      expirePolicy = ExpirePolicy.afterWrite(Duration.hours(1), ticker),
      fetch = { key ->
        println("fetching key")
        tableData.update { it + 1 }
        ticker.advanceBy(Duration.milliseconds(200))
        query.notifyDataChanged()
        ticker.advanceBy(Duration.milliseconds(200))
      },
      keyStore = MemoryKeyStore(ticker),
      valueStore = MemoryValueStore(),
      ticker = ticker,
    )

    query
      .asCachedFlow(cache, keyFactory = { 3 })
      .map { it.executeAsOne() }
      .test {
        cache.load(key = 3)
        awaitItem() shouldBe 1
        ticker.advanceBy(Duration.hours(2))
        cache.load(key = 3)
        awaitItem() shouldBe 2
        cancelAndIgnoreRemainingEvents()
      }
  }
})

fun Ticker.Companion.fake(): FakeTicker {
  return FakeTicker()
}

class FakeTicker(override var now: Instant = Instant.fromEpochMilliseconds(0)) : Ticker {
  fun advanceBy(duration: Duration) {
    now += duration
  }
}
