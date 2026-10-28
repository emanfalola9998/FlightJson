package models

import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{Json, JsonConfiguration, OFormat}

case class SkyHigh(data: Data) {

}

object SkyHigh {
  implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)
  implicit val formats: OFormat[SkyHigh] = Json.format[SkyHigh]
}



case class Data(flights: List[Flight])
object Data {
  implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)
  implicit val formats: OFormat[Data] = Json.format[Data]
}



case class Flight(
                       flightNumber: String,
                       route: RouteFlight,
                       departureTime: String,
                       arrivalTime: String,
                       price: Price,
                       seatsAvailable: Int
                     ) {
}

object Flight {
  implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)
  implicit val formats: OFormat[Flight] = Json.format[Flight]
}




case class RouteFlight(origin: String, destination: String)

object RouteFlight {
  implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)
  implicit val formats: OFormat[RouteFlight] = Json.format[RouteFlight]
}



case class Price(amount: String, currency: String)

object Price {
  implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)
  implicit val formats: OFormat[Price] = Json.format[Price]
}




