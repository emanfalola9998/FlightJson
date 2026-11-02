package connectors

import models.SkyHighResponse
import play.api.Configuration
import play.api.libs.ws.WSClient

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future, TimeoutException}
import scala.util.Try
import scala.util.control.NonFatal

@Singleton
class SkyHighConnector @Inject()(ws: WSClient, config: Configuration)(implicit ec: ExecutionContext) {

  private val baseUrl = config.get[String]("skyHigh.baseUrl")

  def search(origin: String, destination: String, date: String): Future[Either[String, SkyHighResponse]] = {

    ws.url(s"$baseUrl/v1/flights") // http call
      .withQueryStringParameters("from" -> origin, "to" -> destination, "date" -> date) // with parameters
      .withRequestTimeout(2.seconds) // timeout after 2 seconds
      .get()
      .map { // unwrap the future
        response =>
          response.status match { // match on status code
            case 200 =>
              for {
                json <- Try(response.json).toEither.left.map(_ => "SkyHigh returned a body that isn't JSON") // code that can throw (transform to a throwable) so wrapped in a Try block
                parsed <- json.validate[SkyHighResponse].asEither.left.map(errs => s"SkyHigh JSON didn't match our model: $errs")
              } yield parsed
            case other => Left(s"SkyHigh returned status code: $other") // deal with other status codes
          }
      }
      .recover {
        case _: TimeoutException => Left("SkyHigh timeout after 2s") // recover: the future failed (timeout) we handle the error
        case _: ConnectException => Left("SkyHigh is unreachable")
        case NonFatal(e) => Left(s"SkyHigh call failed ${e.getMessage}") // handle all other errors that are nonfatal
      }
  }


}
