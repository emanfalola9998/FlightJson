package services

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import models.{BlueJetResponse, Data, Flight, Money, Offer, Price, RouteFlight, SkyHighResponse}
import connectors.{BlueJetConnector, SkyHighConnector}
import org.mockito.ArgumentMatchers.any
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.when

import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant


class SearchServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with MockitoSugar {

  // initialised globally to be received by any code that needs it
  implicit val ec: ExecutionContext = ExecutionContext.global
  val origin = "LHR"
  val destination = "LOS"
  val date = "2026-03-03"
  val blueJetMapperDate = "03/03/2026"

  // response models
  val skyHighResponse = SkyHighResponse(Data(List(Flight("SH176", RouteFlight(origin, destination), s"${date}T16:30:00.000Z", s"${date}T17:45:00.000Z", Price("173.79", "GBP"), 6))))
  val blueJetResponse = BlueJetResponse("BJ276", origin, destination, s"${blueJetMapperDate} 12:30", "+00:00", 120, 16546, "GBP", Some(7))

  // mock connectors
  val mockBlueJet: BlueJetConnector = mock[BlueJetConnector] // mock as we are not testing this
  val mockSkyHigh: SkyHighConnector = mock[SkyHighConnector]

  // service (being tested)
  val searchService = new SearchService(mockSkyHigh, mockBlueJet)

  // Offers
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

  // Offer Models
  val skyHighOffer = Offer("SkyHigh", "SH176", origin, destination, Instant.parse(s"${date}T16:30:00Z"), Instant.parse(s"${date}T17:45:00Z"), Money(BigDecimal("173.79"), "GBP"), Some(6))
  val blueJetOffer = Offer("BlueJet", "BJ276", origin, destination, Instant.parse(s"${date}T12:30:00Z"), Instant.parse(s"${date}T14:30:00Z"), Money(BigDecimal("165.46"), "GBP"), Some(7))


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

      product shouldBe(List.empty[Offer], List("SkyHigh is unavailable"))
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

  "SearchService" should {

    "return offers on the happy path for SkyHighToOffer" in {
      when(mockSkyHigh.search(any(), any(), any()))
        .thenReturn(Future.successful(Right(skyHighResponse)))

      val result = searchService.skyHighToOffer(origin, destination, date).futureValue

      result shouldBe Right(List(Right(skyHighOffer)))
    }

    "return offers on the happy path for BlueJetToOffer" in {
      when(mockBlueJet.search(any(), any(), any()))
        .thenReturn(Future.successful(Right(List(blueJetResponse))))


      val result = searchService.blueJetToOffer(origin, destination, date).futureValue

      result shouldBe Right(List(Right(blueJetOffer)))
    }

  }
}
