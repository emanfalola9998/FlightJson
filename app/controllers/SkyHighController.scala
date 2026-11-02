package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import connectors.SkyHighConnector
import play.api.libs.json.Json
import services.SearchService

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class SkyHighController @Inject()(
  cc: ControllerComponents,
  skyHighConnector: SkyHighConnector,
  searchService: SearchService
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def get(origin: String, destination: String, date: String): Action[AnyContent] = Action.async {
       Try(LocalDate.parse(date)).toEither match {
         case Right(_) =>
//           searchService.offerings(origin, destination, date)
           skyHighConnector.search(origin, destination, date).map {
             case Right(response) => Ok(Json.toJson(response)) // async call so it's a future
             case Left(error) => BadGateway(Json.obj("error" -> error )) // async call so it's a future
           }
         case Left(_) => Future.successful(BadRequest(Json.obj("error" -> s"Incorrect date format: $date should be in 'yyyy-MM-dd'"))) // so we must return a future here too
       }


  }

}
