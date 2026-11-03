package models

import play.api.libs.json.{Json, OFormat}

case class SearchResult(offers: List[Offer], warnings: List[String])

object SearchResult {
  implicit val formats: OFormat[SearchResult] = Json.format[SearchResult]
}

