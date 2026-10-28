package mappers

import models._

import java.time.Instant
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec


class SkyHighMapperSpec extends AnyWordSpec with Matchers {

  "SkyHighMapper.toOffer" should {

    "convert time to Instant, convert flight price to Money case class and compute arrival time" in {
      val input = Flight("XD432", RouteFlight("LGW", "BCN"), "2026-11-03T16:30:00.000Z", "2026-11-03T17:45:00.000Z", Price("173.79","GBP"), 6)

      val expected = Offer("SkyHigh", "XD432", "LGW", "BCN", Instant.parse("2026-11-03T16:30:00Z"), Instant.parse("2026-11-03T17:45:00Z"), Money(BigDecimal("173.79"), "GBP"), Some(6))

      SkyHighMapper.toOffer(input: Flight) shouldBe Right(expected)
    }

    "Produce a Left from a malformed departureTime error message" in {
      val input = Flight("FD987", RouteFlight("LN", "QWE"), "2026-0221-03T11:30:00.000Z", "2026-01-03T14:45:00.000Z", Price("45.79","EUR"), 3)

      SkyHighMapper.toOffer(input: Flight) shouldBe Left("Failed to parse 2026-0221-03T11:30:00.000Z to Instant for flight FD987")

    }

    "Produce a Left from a bad price and receive correct error message" in {
      val input = Flight("KL863", RouteFlight("LOG", "ZXS"), "2026-03-03T11:30:00.000Z", "2026-03-03T19:25:00.000Z", Price("AR","GBP"), 6)

      SkyHighMapper.toOffer(input: Flight) shouldBe Left("Failed to parse AR to BigDecimal for flight KL863")

    }
  }

}

