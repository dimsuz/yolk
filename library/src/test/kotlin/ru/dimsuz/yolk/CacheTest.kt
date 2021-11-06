package ru.dimsuz.yolk

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.atomic.shouldBeTrue
import io.kotest.matchers.shouldBe
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

    sut.load()

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
    sut.load()

    ticker.advanceBy(Duration.ofMinutes(2))
    sut.load()

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
    sut.load()

    ticker.advanceBy(Duration.ofMinutes(3).plusNanos(10))
    sut.load()

    fetchCount.get() shouldBe 2
  }
})
