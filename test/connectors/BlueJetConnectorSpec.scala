package connectors

import models.BlueJetResponse
import org.apache.pekko.stream.ConnectionException
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when

import java.util.concurrent.TimeoutException
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}

import java.lang
import java.net.ConnectException
import scala.concurrent.{ExecutionContext, Future}

class BlueJetConnectorSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  // mocks and values needed for setup
  implicit val ec = ExecutionContext.global
  val ws = mock[WSClient]
  val request = mock[WSRequest]
  val response = mock[WSResponse]
  val config: Configuration = Configuration.from(Map("blueJet.baseUrl" -> "localhost:4002"))

  // default data
  val origin = "LHR"
  val destination = "LOS"
  val date = "2026-03-03"

  // BlueJet Model class
  val blueJetResponse =
    BlueJetResponse("BJ267", "LHR", "CDG", "03/11/2026 12:30", "+00:00", 75, 16546, "GBP", Some(7))

  // Connector (code being tested)
  val blueJetConnector = new BlueJetConnector(ws, config)

  // helper method
  def mockConnection(statusCode: Int, call: Boolean = true, error: String = ""): Unit = {
    when(ws.url(any())).thenReturn(request)
    when(request.withQueryStringParameters(any())).thenReturn(request)
    when(request.withRequestTimeout(any[Duration])).thenReturn(request)
    if (call) {
      when(request.get()).thenReturn(Future.successful(response))
      when(response.status).thenReturn(statusCode)
      when(response.json).thenReturn(Json.toJson(List(blueJetResponse)))
    }
    else {
      case error: String if error.contains("timeout") => when(request.get()).thenReturn(Future.failed(new TimeoutException))
      case error: String if error.contains("connection") => when(request.get()).thenReturn(Future.failed(new ConnectException()))
      case error: String if error.contains("NonFatal") => when(request.get()).thenReturn(Future.failed(RuntimeException))
      case other => throw new IllegalArgumentException(s"Unhandled error stub: $other")
    }

  }

  "blueJetConnector" should {
    "return list of blueJetResponses (happy path)" in {
      mockConnection(200)

      val result = blueJetConnector.search(origin, destination, date).futureValue

      result shouldBe Right(List(blueJetResponse))
    }

    "return Left(string) when status code is not 200" in {
      mockConnection(404)

      val result = blueJetConnector.search(origin, destination, date).futureValue

      result shouldBe Left(s"BlueJet returned status code: 404")
    }

    "return TimeoutException when call fails to return a response in two seconds" in {
      mockConnection(500, call = false, "timeout")

      val result = blueJetConnector.search(origin, destination, date).futureValue

      result shouldBe Left("BlueJet timeout after 2s")
    }

    "return ConnectException when call fails to connect to API " in {
      mockConnection(500, call = false, "connection")

      val result = blueJetConnector.search(origin, destination, date).futureValue

      result shouldBe Left("BlueJet timeout after 2s")
    }

    "return NonFatal for all non-fatal exceptions" in {
      mockConnection(500, call = false, "NonFatal")

      val result = blueJetConnector.search(origin, destination, date).futureValue

      result shouldBe Left("BlueJet timeout after 2s")
    }
  }


}
