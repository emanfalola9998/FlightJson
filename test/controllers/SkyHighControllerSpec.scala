package controllers

import models.{Money, Offer, SearchResult}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SearchService

import java.time.Instant
import java.util.concurrent.TimeoutException
import scala.concurrent.{ExecutionContext, Future}

class SkyHighControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  // setup
  implicit val ec = ExecutionContext.global
  val service = mock[SearchService]
  val cc = stubControllerComponents()


  // controller (code being tested)
  val skyHighController = new SkyHighController(cc, service)

  // parameters for controller
  val origin = "LHR"
  val destination = "LOS"
  val date = "2026-03-03"

  // searchResult model
  val skyHighOffering = SearchResult(
    offers = List(
      Offer("SkyHigh", "XD432", "LGW", "BCN", Instant.parse("2026-11-03T16:30:00Z"), Instant.parse("2026-11-03T17:45:00Z"), Money(BigDecimal("173.79"), "GBP"), Some(6))
    ),
    warnings = List()
  )

  val blueJetOffering = SearchResult (
    offers = List.empty[Offer],
    warnings = List()
  )


  "skyHighController" should {
    "return an Action with status code 'Ok' when presented with valid data from SkyHigh's API" in {

      // can only use any() inside when block
      // must use concrete or mock value inside .thenReturn block
      when(service.offers(any(), any(), any()))
        .thenReturn(Future.successful((skyHighOffering, blueJetOffering)))

      val resultFuture = skyHighController.get(origin, destination, date).apply(FakeRequest())

      status(resultFuture) shouldBe OK
      contentAsJson(resultFuture) shouldBe Json.toJson(skyHighOffering)
    }

    "return an Action with status code 'BadGateway' when presented with invalid data" in {

      when(service.offers(any(), any(), any()))
        .thenReturn(Future.failed(new TimeoutException("Service timedout")))

      val resultFuture = skyHighController.get(origin, destination, date).apply(FakeRequest())

      status(resultFuture) shouldBe BAD_GATEWAY

    }

    "return an Action with status BadRequest when an invalid date is provided" in {

      val resultFuture = skyHighController.get(origin, destination, "2026-003-03").apply(FakeRequest())

      status(resultFuture) shouldBe BAD_REQUEST
    }
  }

}
