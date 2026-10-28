package mappers

import models._

import java.time.{Duration, Instant, LocalDateTime, ZoneOffset}
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import scala.util.Try


object BlueJetMapper {

  private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")


  def toOffer(flight: BlueJet): Either[String, Offer] = {

    for {
      localDateTime  <- parseLocalDateTime(flight.depLocal,flight.code, formatter) // calls that can fail
      zoneOffset     <- toZoneOffset(flight.depUtcOffset, flight.code)    //  calls that can fail

    } yield {
      val departureTime = localDateTime.toInstant(zoneOffset) // calls that can't fail
      val arrivalTime = departureTime.plus(Duration.ofMinutes(flight.durationMins))
      val price = BigDecimal.apply(flight.farePence) / 100

      Offer("BlueJet", flight.code, flight.from, flight.to, departureTime, arrivalTime, Money(price, flight.cur), flight.seats )

    }

  }

  private def parseLocalDateTime(flightDepLocal: String, code: String, formatter: DateTimeFormatter): Either[String, LocalDateTime] = {
    Try(LocalDateTime.parse(flightDepLocal, formatter)).toEither.left.map(_ => s"Error parsing to LocalDateTime for flight code: ${code},  ${flightDepLocal} ")
    // Use try block for codes that can fail
  }

  private def toZoneOffset(flightZoneOffset: String, code: String): Either[String, ZoneOffset] = {
    Try(ZoneOffset.of(flightZoneOffset)).toEither.left.map(_ => s"Error parsing flight offset to ZoneOffset for flight code: ${code}, ${flightZoneOffset}")
  }

}

