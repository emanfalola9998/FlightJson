package services

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.mockito.ArgumentMatchers.any
import scala.concurrent.ExecutionContext
import connectors.{BlueJetConnector, SkyHighConnector}
import models.{Money, Offer}

import java.time.Instant


class SearchServiceSpec extends AnyWordSpec with Matchers {
  implicit val ec: ExecutionContext = ExecutionContext.global

  val origin = "LHR"
  val destination = "LGS"
  val date = "2025-03-03"

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
        "mangled airline"
      )
    )
  )

  val resultAirlineUnhappyPath: Either[String, List[Either[String, Offer]]] = Left(
    "SkyHigh"
  )

  val resultAirlineBothPaths: Either[String, List[Either[String, Offer]]] = Right(
    List(
      Right(
        skyOfferHappyPath
      )
    )
  )

  val mangledAirline = List("mangledAirline")


  "SearchService.collect" should {
    "collect method should take airline and the result from the connector and return successful Flights and warnings" in {
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
      warnings shouldBe List("mangled airline")
    }

    "collect method should message airline is unavailable if no flights are present" in {
      val product = SearchService.collect("SkyHigh", resultAirlineUnhappyPath)

      product shouldBe (List.empty[Offer], List("SkyHigh is unavailable"))
    }

    "collect keeps valid offers and reports mangled ones" in {
      val mangledMessage = "Failed to parse abc to BigDecimal for flight SH999"

      val input: Either[String, List[Either[String, Offer]]] =
        Right(List(Right(skyOfferHappyPath), Left(mangledMessage)))

      val (offers, warnings) = SearchService.collect("SkyHigh", input)

      offers shouldBe List(skyOfferHappyPath)
      warnings shouldBe List("Failed to parse abc to BigDecimal for flight SH999")

    }

  }

}
