package mappers

import models.{Flight, Money, Offer}

import java.time.{Instant}
import scala.util.Try

object SkyHighMapper {


    def toOffer(flight: Flight): Either[String, Offer] = {


      for{

        price         <- parseToBigDecimal(flight.price.amount, flight.flightNumber)
        departureTime      <- parseDateTime(flight.departureTime, flight.flightNumber)
        arrivalTime       <- parseDateTime(flight.arrivalTime, flight.flightNumber)

      } yield {

        Offer("SkyHigh", flight.flightNumber, flight.route.origin, flight.route.destination, departureTime, arrivalTime, Money(price, flight.price.currency), Some(flight.seatsAvailable))
      }
    }

  private def parseDateTime(flightTime: String, code: String): Either[String, Instant] = {
    Try(Instant.parse(flightTime)).toEither.left.map(_ => s"Failed to parse $flightTime to Instant for flight $code")
  }

    private def parseToBigDecimal(amount: String, code: String ): Either[String, BigDecimal] = {
      Try(BigDecimal.apply(amount)).toEither.left.map(_ => s"Failed to parse ${amount} to BigDecimal for flight $code")
      // Have to implement this method as the data type of price is a string unlike an Int with BlueJet
    }


}

