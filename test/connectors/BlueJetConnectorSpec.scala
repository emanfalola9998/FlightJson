package connectors

import models.BlueJetResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Configuration
import play.api.libs.ws.{WSClient, WSRequest}

import scala.concurrent.{ExecutionContext, Future}

class BlueJetConnectorSpec extends AnyWordSpec with Matchers {


  implicit val ec = ExecutionContext.global
  val ws = mock[WSClient]
  val config = mock[Configuration]

  val origin = "LHR"
  val destination = "LOS"
  val date = "2026-03-03"

  val blueJetResponse =  BlueJetResponse("BJ267", "LHR", "CDG", "03/11/2026 12:30", "+00:00", 75, 16546, "GBP", Some(7))


  val blueJetConnector = new BlueJetConnector(ws, config)

  "blueJetConnector" should {
    "return list of blueJetResponses" in {
      when(ws.url(any()))
        .thenReturn(Future.successful(any[WSRequest]))

      val result = blueJetConnector.search(origin, destination, date).futureValue

      result shouldBe List(blueJetResponse)
    }
  }

}
