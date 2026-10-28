package mappers

import models._

import java.time.Instant
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BlueJetMapperSpec extends AnyWordSpec with Matchers {



  "BlueJetMapper.toOffer" should {
    "convert local time + offset to UTC, compute arrival, and turn pence into money" in {

      val input: BlueJet = BlueJet("BJ276", "CDG", "LHR", "03/11/2026 14:15", "+01:00", 75, 33173, "GBP", Some(7))

      val expected: Offer = Offer("BlueJet", "BJ276", "CDG", "LHR", Instant.parse("2026-11-03T13:15:00Z"), Instant.parse("2026-11-03T14:30:00Z"), Money(BigDecimal("331.73"), "GBP"), Some(7))

      BlueJetMapper.toOffer(input) shouldBe Right(expected)

    }

    "convert local time with no offset, compute arrival and turn pence into money, for flight with unknown seats available" in {

      val input: BlueJet = BlueJet("AX311", "CDG", "LHR", "03/11/2026 14:15", "+00:00", 75, 21345, "GBP", None)

      val expected: Offer = Offer("BlueJet", "AX311", "CDG", "LHR", Instant.parse("2026-11-03T14:15:00Z"), Instant.parse("2026-11-03T15:30:00Z"), Money(BigDecimal("213.45"), "GBP"), None)

      BlueJetMapper.toOffer(input) shouldBe Right(expected)
    }

    "returns a Left naming the flight number and the error generated" in {

      val input: BlueJet = BlueJet("ZC200", "CDG", "LHR", "03/111/2026 14:15", "+00:00", 75, 21345, "GBP", None)

      BlueJetMapper.toOffer(input) shouldBe Left("Error parsing to LocalDateTime for flight code: ZC200,  03/111/2026 14:15")
    }
  }
}
