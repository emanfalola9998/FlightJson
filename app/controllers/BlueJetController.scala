package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import connectors.BlueJetConnector
import play.api.libs.json.Json

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import services.SearchService

@Singleton
class BlueJetController @Inject()(
  cc: ControllerComponents,
  blueJetConnector: BlueJetConnector,
  searchService: SearchService
)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  // Controllers receive requests pass it to the service/connector then receive the response and convert to Json
  // and send back to the client

  def get(origin: String, destination: String, date: String): Action[AnyContent] = Action.async {
    Try(LocalDate.parse(date)).toEither match {
      case Right(_) =>
        searchService.offers(origin, destination, date).map {
          case (_, blueJetOfferResult) => Ok(Json.toJson(blueJetOfferResult))
          case error => BadGateway(Json.obj("error" -> s"$error"))
        }
//        blueJetConnector.search(origin, destination, date).map {
//          case Right(response) => Ok(Json.toJson(response))
//          case Left(error) => BadGateway(Json.obj("error" -> error))
//        }
      case Left(_) => Future.successful(BadRequest(Json.obj("error" -> s"LocalDate unable to convert date provided $date should be in format 'yyyy-MM-dd'")))
    }

  }

}
