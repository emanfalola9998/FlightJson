package models

import org.scalatest.matchers.must.Matchers.matchPattern
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsPath, JsSuccess, Json}

class SkyHighSpec extends AnyWordSpec with Matchers {

  val skyHigh = SkyHigh(Data(List(Flight("SH176", RouteFlight("LHR", "CDG"), "2026-11-03T09:00:00.000Z", "2026-11-03T10:15:00.000Z", Price("319.94","GBP" ), 1))))

  val skyHighJsonResponse =  """{
               |  "data": {
               |    "flights": [
               |      {
               |        "flight_number": "SH176",
               |        "route": {
               |          "origin": "LHR",
               |          "destination": "CDG"
               |        },
               |        "departure_time": "2026-11-03T09:00:00.000Z",
               |        "arrival_time": "2026-11-03T10:15:00.000Z",
               |        "price": {
               |          "amount": "319.94",
               |          "currency": "GBP"
               |        },
               |        "seats_available": 1
               |      }
               |    ]
               |  }
               |}""".stripMargin

  val wrongFieldTypeResponse =  """{
                               |  "data": {
                               |    "flights": [
                               |       {
                               |        "flight_number": "SH176",
                               |        "route": {
                               |          "origin": 1,
                               |          "destination": "CDG"
                               |        },
                               |        "departure_time": "2026-11-03T09:00:00.000Z",
                               |        "arrival_time": "2026-11-03T10:15:00.000Z",
                               |        "price": {
                               |          "amount": "319.94",
                               |          "currency": "GBP"
                               |        },
                               |        "seats_available": 1
                               |      }
                               |    ]
                               |  }
                               |}""".stripMargin

  val missingFieldResponse =  """{
                                  |  "data": {
                                  |    "flights": [
                                  |       {
                                  |        "route": {
                                  |          "origin": "LHR",
                                  |          "destination": "CDG"
                                  |        },
                                  |        "departure_time": "2026-11-03T09:00:00.000Z",
                                  |        "arrival_time": "2026-11-03T10:15:00.000Z",
                                  |        "price": {
                                  |          "amount": "319.94",
                                  |          "currency": "GBP"
                                  |        },
                                  |        "seats_available": 1
                                  |      }
                                  |    ]
                                  |  }
                                  |}""".stripMargin


  def parseJsonHelper(response: String) ={
    Json.parse(response).validate[SkyHigh] match{
      case JsSuccess(skyHigh, _) => skyHigh
      case JsError(errors) => fail(s"failed to read jsValue correctly, Error: $errors")
    }
  }


  "SkyHigh Json formats" should {
    "parse the Json to a case class SkyHigh" in {
      val convertedSkyHigh = parseJsonHelper(skyHighJsonResponse)

      convertedSkyHigh.data.flights.head shouldBe skyHigh.data.flights.head
    }

    "Return failure when the wrong type in Json is present" in {
      val convertedBrokenSkyHigh = Json.parse(wrongFieldTypeResponse).validate[SkyHigh]

      convertedBrokenSkyHigh match {
        case JsError(errors) if errors.exists(_._1 == JsPath \"data"\ "flights" \ 0 \ "route" \ "origin") =>
      }


    }

    "Return failure when a required field is missing in the JSON" in {
      val convertedBrokenSkyHigh = Json.parse(missingFieldResponse).validate[SkyHigh]

      convertedBrokenSkyHigh should matchPattern {
        case JsError(errors) if errors.exists { case (path, validationErrors) =>
          // 1. Check if the error happened at the right nested path
          path == JsPath \ "data" \ "flights" \ 0 \ "flight_number"  &&
            // 2. Check if the error reason is exactly due to a missing field
            validationErrors.exists(_.message == "error.path.missing")
        } =>
      }
    }

  }
}

//Here is the exact structural path breakdown for your wrongFieldTypeResponse:
//1. Start at the root object (data)
//2. Go into the flights array
//3. Target the first flight in that array (index 0)
//4. Go into the route object
//5. Target the broken origin key
