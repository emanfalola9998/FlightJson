package connectors

import models.{Data, Flight, Price, RouteFlight, SkyHighResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}

import java.net.ConnectException
import java.util.concurrent.TimeoutException
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

class SkyHighConnectorSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures{

  // mocks and values needed for setup
  implicit val ec = ExecutionContext.global
  val ws = mock[WSClient]
  val request = mock[WSRequest]
  val response = mock[WSResponse]
  val config: Configuration = Configuration.from(Map("skyHigh.baseUrl" -> "localhost:4001"))

// default data
  val origin = "LHR"
  val destination = "LOS"
  val date = "2026-03-03"

  // SkyHigh model class
  val skyHighResponse = SkyHighResponse(Data(List(Flight("SH176", RouteFlight("LHR", "CDG"), "2026-11-03T09:00:00.000Z", "2026-11-03T10:15:00.000Z", Price("319.94","GBP"), 1))))

  // Connector (code being tested)
  val skyHighConnector = new SkyHighConnector(ws, config)

  // helper method
  def mockConnection(statusCode: Int, call: Boolean = true, error: String = ""): Unit = {
    when(ws.url(any())).thenReturn(request)
    when(request.withQueryStringParameters(any())).thenReturn(request)
    when(request.withRequestTimeout(any[Duration])).thenReturn(request)
    if (call){
      when(request.get()).thenReturn(Future.successful(response))
      when(response.status).thenReturn(statusCode)
      when(response.json).thenReturn(Json.toJson(skyHighResponse))
    }
    else {
      case error: String if error.contains("timeout") => when(request.get()).thenReturn(Future.failed(new TimeoutException))
      case error: String if error.contains("connection") => when(request.get()).thenReturn(Future.failed(new ConnectException()))
      case error: String  if error.contains("NonFatal") => when(request.get()).thenReturn(Future.failed(RuntimeException))
      case other =>  throw new IllegalArgumentException(s"Unhandled error stub: $other")
    }

  }

  "skyHighConnector" should {
    "return SkyHighResponse (happy path)" in {
      mockConnection(200)

      val result = skyHighConnector.search(origin, destination, date).futureValue

      result shouldBe Right(skyHighResponse)
    }

    "return Left(string) when status code is not 200" in {
      mockConnection(404)

      val result = skyHighConnector.search(origin, destination, date).futureValue

      result shouldBe Left(s"SkyHigh returned status code: 404")
    }

    "return Timeout Exception when call fails to return a response in two seconds" in {
      mockConnection(500, call = false, "timeout")

      val result = skyHighConnector.search(origin, destination, date).futureValue

      result shouldBe Left("SkyHigh timeout after 2s")
    }

    "return Connection Exception when call fails to return a response in two seconds" in {
      mockConnection(500, call = false, "connection")

      val result = skyHighConnector.search(origin, destination, date).futureValue

      result shouldBe Left("SkyHigh is unreachable")
    }

    "return NonFatal Exception when call fails to return a response in two seconds" in {
      mockConnection(500, call = false, "NonFatal")

      val result = skyHighConnector.search(origin, destination, date).futureValue

      result shouldBe Left("SkyHigh call failed")
    }
  }
}
