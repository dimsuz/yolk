package ru.dimsuz.yolk

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.atomic.shouldBeTrue
import io.kotest.matchers.shouldBe
import ru.dimsuz.yolk.fake.FakeValueStore
import ru.dimsuz.yolk.fake.createStringIntCache
import ru.dimsuz.yolk.fake.fake
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class CacheTest : ShouldSpec({
  should("fetch on first load") {
    val fetched = AtomicBoolean()
    val sut = createStringIntCache(
      fetch = { fetched.set(true); 42 }
    )

    sut.load("a")

    fetched.shouldBeTrue()
  }

  should("not fetch on fresh key") {
    val fetchCount = AtomicInteger()
    val ticker = Ticker.fake()
    val sut = createStringIntCache(
      ticker = ticker,
      expireAfterWrite = Duration.ofMinutes(3),
      fetch = { fetchCount.incrementAndGet(); 42 },
      enableLogging = true
    )
    sut.load("a")

    ticker.advanceBy(Duration.ofMinutes(2))
    sut.load("a")

    fetchCount.get() shouldBe 1
  }

  should("fetch on expired key") {
    val fetchCount = AtomicInteger()
    val ticker = Ticker.fake()
    val sut = createStringIntCache(
      ticker = ticker,
      expireAfterWrite = Duration.ofMinutes(3),
      fetch = { fetchCount.incrementAndGet(); 42 },
      enableLogging = true
    )
    sut.load("a")

    ticker.advanceBy(Duration.ofMinutes(3).plusNanos(10))
    sut.load("a")

    fetchCount.get() shouldBe 2
  }

  should("write value to value store on cache miss") {
    val store = MemoryValueStore<String, Int>()
    val sut = createStringIntCache(
      fetch = { 42 },
      valueStore = store
    )

    sut.load("a")

    sut.get("a") shouldBe 42
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
    val fetchCount = AtomicInteger()
    val sut = createStringIntCache(
      fetch = { if (fetchCount.getAndIncrement() == 0) 42 else 24 },
    )
    sut.load("a")
    check(!sut.hasExpired("a"))

    sut.refresh("a")

    sut.get("a") shouldBe 24
  }
})
