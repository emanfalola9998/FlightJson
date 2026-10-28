package mappers


import models._

import java.time.{Instant}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec


class SkyHighMapperSpec extends AnyWordSpec with Matchers {

  "SkyHighMapperSpec.toOffer" should {

    "convert local time + offset to an Instant, convert flight price to Money case class and compute arrival time" {
      val input = SkyHigh(Data(List(Flight("XD432", RouteFlight("LGW", "BCN"), "2026-11-03T16:30:00.000Z", "2026-11-03T17:45:00.000Z", Price( "amount": "173.79", "currency": "GBP"), 6))))

      val expected = Offer("SkyHigh", "XD432", "LGW", "BCM", Instant.parse("2026-11-03T16:30:00Z"), Instant.parse("2026-11-03T17:45:00Z"), Money(BigDecimal.apply("173.79".toInt), "GBP"), Some(6))

      skyHighMapper.toOffer(input) shouldBe Right(expected)
    }

    "Produce a Left from a malformed departureTime error message" {
      val input = SkyHigh(Data(List(Flight("FD987", RouteFlight("LN", "QWE"), "2026-01-03T11:30:00.000Z", "2026-01-03T14:45:00.000Z", Price( "amount": "45.79", "currency": "EUR"), 3))))

      SkyHighMapper.toOffer(input) shouldBe Left("Error parsing to LocalDateTime for flight code: ZC200,  03/111/2026 14:15 ")

    }
  }

}

