package connectors

import models.BlueJetResponse
import play.api.Configuration
import play.api.libs.ws.WSClient
import scala.concurrent.{ExecutionContext, Future, TimeoutException}

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.util.Try
import scala.util.control.NonFatal

@Singleton
class BlueJetConnector @Inject()(ws: WSClient, config: Configuration)(implicit ec: ExecutionContext) {

  private val baseUrl = config.get[String]("blueJet.baseUrl")

  def search(origin: String, destination:String, date: String): Future[Either[String, List[BlueJetResponse]]] = {

    ws.url(s"$baseUrl/search")
      .withQueryStringParameters("origin" -> origin, "destination" -> destination, "day" -> date)
      .withRequestTimeout(2.seconds)
      .get()
      .map{
        response =>
          response.status match {
            case 200 =>
              for{
                json <- Try(response.json).toEither.left.map(_ => "BlueJet returned a response that isn't JSON") // wrapped the json in a wrapper so for comprehension allows us to access the value inside
                parsed <- json.validate[List[BlueJetResponse]].asEither.left.map(errors => s"Unable to parse JSON into BlueJetResponse Error $errors")
              } yield parsed
            case other => Left(s"BlueJet returned status code: $other") // must return Future[Either[String or BlueJetResponse]
          }
      }
      .recover {
        case _: TimeoutException => Left("BlueJet timeout after 2s")
        case _: ConnectException => Left("BlueJet is unreachable")
        case NonFatal(e) => Left(s"BlueJet call failed ${e.getMessage}")
      }
  }

}
