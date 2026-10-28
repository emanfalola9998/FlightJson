package models

import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{Json, JsonConfiguration, OFormat}


case class BlueJet(
                  code: String,
                  from: String,
                  to: String,
                  depLocal: String,
                  depUtcOffset: String,
                  durationMins: Int,
                  farePence: Int,
                  cur: String,
                  seats: Option[Int]
                  )


object BlueJet {
  implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)
  implicit val formats: OFormat[BlueJet] = Json.format[BlueJet]
}
