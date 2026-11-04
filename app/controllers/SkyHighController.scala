package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import play.api.libs.json.Json
import services.SearchService

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class SkyHighController @Inject()(
  cc: ControllerComponents,
  searchService: SearchService
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def get(origin: String, destination: String, date: String): Action[AnyContent] = Action.async {
       Try(LocalDate.parse(date)).toEither match {
         case Right(_) =>
           searchService.offers(origin, destination, date).map {
             case (skyHighOfferings, _) => Ok(Json.toJson(skyHighOfferings))
           }.recover{
             case ex =>
               BadGateway(Json.obj("error" -> s"Dependent service failed: ${ex.getMessage}"))
           }
         case Left(_) => Future.successful(BadRequest(Json.obj("error" -> s"Incorrect date format: $date should be in 'yyyy-MM-dd'"))) // so we must return a future here too
       }


  }

}
