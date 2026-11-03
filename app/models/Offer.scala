package models

import play.api.libs.json.{Json, OFormat}
import java.time.Instant


case class Money(amount: BigDecimal, currency: String)

case class Offer(
                airline: String,
                flightNumber: String,
                origin: String,
                destination: String,
                departsAt: Instant,
                arrivesAt: Instant,
                price: Money,
                seatsAvailable: Option[Int]
                )

object Offer{
  implicit val formats: OFormat[Offer] = Json.format[Offer]
}

object Money {
  implicit val formats: OFormat[Money] = Json.format[Money]
}

