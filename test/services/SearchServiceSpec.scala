package services

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import models.{Money, Offer}

import java.time.Instant


class SearchServiceSpec extends AnyWordSpec with Matchers {


  val skyOfferHappyPath = Offer(
    airline = "SkyHigh",
    flightNumber = "XD432",
    origin = "LGW",
    destination = "BCN",
    departsAt = Instant.parse("2026-11-03T16:30:00Z"),
    arrivesAt = Instant.parse("2026-11-03T17:45:00Z"),
    price = Money(BigDecimal("173.79"), "GBP"),
    seatsAvailable = Some(6)
  )


  val resultHappyPath: Either[String, List[Either[String, Offer]]] = Right(
    List(
      Right(
        skyOfferHappyPath
      )
    )
  )

  val resultUnhappyPath: Either[String, List[Either[String, Offer]]] = Right(
    List(
      Left(
        "Mangled Airline data"
      )
    )
  )

  val resultAirlineUnhappyPath: Either[String, List[Either[String, Offer]]] = Left(
    "SkyHigh is unreachable" // error obtained from the connector that is then overriden in the collect method
  )


  "SearchService.collect" should {
    "take airline and the result from the connector and return successful Flights and warnings" in {
      val product = SearchService.collect("SkyHigh", resultHappyPath)

      val successfulFlights =
        List(
          skyOfferHappyPath
        )

      val warnings = List.empty[String]

      val expected = (successfulFlights, warnings)

      product shouldBe expected
    }

    "mangled Offer should return warnings list" in {
      val (offers, warnings) = SearchService.collect("SkyHigh", resultUnhappyPath)

      offers shouldBe List.empty[Offer]
      warnings shouldBe List("Mangled Airline data")
    }

    "message airline is unavailable when the airline call fails" in {
      val product = SearchService.collect("SkyHigh", resultAirlineUnhappyPath)

      product shouldBe (List.empty[Offer], List("SkyHigh is unavailable"))
    }

    "collect keeps valid offers and reports mangled ones" in {
      val mangledMessage = "Failed to parse abc to BigDecimal for flight SH999"

      val input: Either[String, List[Either[String, Offer]]] =
        Right(List(Right(skyOfferHappyPath), Left(mangledMessage)))

      val (offers, warnings) = SearchService.collect("SkyHigh", input)

      offers shouldBe List(skyOfferHappyPath)
      warnings shouldBe List(mangledMessage)

    }

    "collect should return empty lists when no flights are present in input" in {
      val input: Either[String, List[Either[String, Offer]]] = Right(List())

      val (offers, warnings) = SearchService.collect("SkyHigh", input)

      offers shouldBe List.empty[Offer]
      warnings shouldBe List.empty[String]
    }

  }

}
