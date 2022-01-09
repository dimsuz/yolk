package ru.dimsuz.yolk

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import ru.dimsuz.yolk.fake.FakeValueStore
import ru.dimsuz.yolk.fake.createStringIntCache
import ru.dimsuz.yolk.fake.fake
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds

class CacheTest : ShouldSpec({
  should("fetch on first load") {
    val fetched = MutableStateFlow(false)
    val sut = createStringIntCache(
      fetch = { fetched.value = true; 42 }
    )

    sut.load("a")

    fetched.first().shouldBeTrue()
  }

  should("not fetch on fresh key") {
    val fetchCount = MutableStateFlow(0)
    val ticker = Ticker.fake()
    val sut = createStringIntCache(
      ticker = ticker,
      expireAfterWrite = 3.minutes,
      fetch = { fetchCount.update { it + 1 }; 42 },
    )
    sut.load("a")

    ticker.advanceBy(2.minutes)
    sut.load("a")

    fetchCount.value shouldBe 1
  }

  should("fetch on expired key") {
    val fetchCount = MutableStateFlow(0)
    val ticker = Ticker.fake()
    val sut = createStringIntCache(
      ticker = ticker,
      expireAfterWrite = 3.minutes,
      fetch = { fetchCount.update { it + 1 }; 42 },
    )
    sut.load("a")

    ticker.advanceBy(3.minutes.plus(10.nanoseconds))
    sut.load("a")

    fetchCount.value shouldBe 2
  }

  should("return fetched value on cache miss") {
    val sut = createStringIntCache(
      fetch = { if (it == "a") 42 else if (it == "b") 24 else 88 },
    )

    val value1 = sut.load("a")
    val value2 = sut.load("b")

    value1 shouldBe 42
    value2 shouldBe 24
  }

  should("return stored value on cache hit") {
    val fetchCount = MutableStateFlow(0)
    val sut = createStringIntCache(
      fetch = { if (fetchCount.getAndUpdate { it + 1 } == 0) 42 else 24 },
    )

    sut.load("a")
    val value = sut.load("a")

    value shouldBe 42
  }

  should("write value to value store on cache miss") {
    val store = MemoryValueStore<String, Int>()
    val sut = createStringIntCache(
      fetch = { 42 },
      valueStore = store
    )

    val value = sut.load("a")

    value shouldBe 42
  }

  should("write value ONCE to value store on cache miss") {
    val store = FakeValueStore<String, Int>()
    val sut = createStringIntCache(
      fetch = { 42 },
      valueStore = store
    )

    sut.load("a")
    sut.load("a")

    store.stats.writes shouldBe 1
  }

  should("refresh with fetch even if key is not expired") {
    val fetchCount = MutableStateFlow(0)
    val sut = createStringIntCache(
      fetch = { if (fetchCount.getAndUpdate { it + 1 } == 0) 42 else 24 },
    )
    sut.load("a")
    check(!sut.hasExpired("a"))

    val value = sut.load("a", forceRefresh = true)

    value shouldBe 24
  }
})
