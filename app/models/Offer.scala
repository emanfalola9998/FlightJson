package models

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
