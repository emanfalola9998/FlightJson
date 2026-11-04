package controllers

import models.{Money, Offer, SearchResult}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SearchService

import java.util.concurrent.TimeoutException
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class BlueJetControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  // setup
  implicit val ec = ExecutionContext.global
  val service = mock[SearchService]
  val cc = stubControllerComponents()

  // controller (code being tested)
  val blueJetController = new BlueJetController(cc, service)

  // parameters for controller
  val origin = "LHR"
  val destination = "LOS"
  val date = "2026-03-03"

  // searchResult model
  val skyHighOffering = SearchResult(
    offers = List.empty[Offer],
    warnings = List()
  )

  val blueJetOffering = SearchResult(
    offers = List(
      Offer("BlueJet", "XD432", "LGW", "BCN", Instant.parse("2026-11-03T16:30:00Z"), Instant.parse("2026-11-03T17:45:00Z"), Money(BigDecimal("173.79"), "GBP"), Some(6))
    ),
    warnings = List()
  )

  "blueJetController" should {
    "return an Action with status code 'OK' when presented with valid data from blueJet's API" in {

      when(service.offers(any(), any(), any()))
        .thenReturn(Future.successful((skyHighOffering, blueJetOffering)))

      val resultFuture = blueJetController.get(origin, destination, date).apply(FakeRequest())

      status(resultFuture) shouldBe OK
      contentAsJson(resultFuture) shouldBe Json.toJson(blueJetOffering)
    }
  }

    "return an Action with status code 'BadGateway' when presented with invalid data" in {
      when(service.offers(any(), any(), any()))
        .thenReturn(Future.failed(new TimeoutException()))

      val resultFuture = blueJetController.get(origin, destination, date).apply(FakeRequest())

      status(resultFuture) shouldBe BAD_GATEWAY
    }

    "return an Action with status code 'BadRequest' when invalid date is provided" in {

      val resultFuture = blueJetController.get(origin, destination, "20225-04-01").apply(FakeRequest())

      status(resultFuture) shouldBe BAD_REQUEST
    }

}
