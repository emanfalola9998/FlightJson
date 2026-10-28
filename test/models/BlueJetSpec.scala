package models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsSuccess, Json}

class BlueJetSpec extends AnyWordSpec with Matchers {


  val blueJet = BlueJet("BJ267", "LHR", "CDG", "03/11/2026 12:30", "+00:00", 75, 16546, "GBP", Some(7))

  val blueJetRespones = """[
      |  {
      |    "code": "BJ267",
      |    "from": "LHR",
      |    "to": "CDG",
      |    "dep_local": "03/11/2026 12:30",
      |    "dep_utc_offset": "+00:00",
      |    "duration_mins": 75,
      |    "fare_pence": 16546,
      |    "cur": "GBP",
      |    "seats": 7
      |  },
      |  {
      |    "code": "BJ278",
      |    "from": "LHR",
      |    "to": "CDG",
      |    "dep_local": "03/11/2026 19:15",
      |    "dep_utc_offset": "+00:00",
      |    "duration_mins": 75,
      |    "fare_pence": 28551,
      |    "cur": "GBP",
      |    "seats": null
      |  }
      |]""".stripMargin

  val wrongDatatypeRespones = """[
                          |  {
                          |    "code": 0,
                          |    "from": "LHR",
                          |    "to": "CDG",
                          |    "dep_local": "03/11/2026 12:30",
                          |    "dep_utc_offset": "+00:00",
                          |    "duration_mins": 75,
                          |    "fare_pence": 16546,
                          |    "cur": "GBP",
                          |    "seats": 7
                          |  },
                          |  {
                          |    "code": "BJ278",
                          |    "from": "LHR",
                          |    "to": "CDG",
                          |    "dep_local": "03/11/2026 19:15",
                          |    "dep_utc_offset": "+00:00",
                          |    "duration_mins": 75,
                          |    "fare_pence": 28551,
                          |    "cur": "GBP",
                          |    "seats": null
                          |  }
                          |]""".stripMargin

  val missingFieldResponse = """[
                          |  {
                          |    "code": "BJ267",
                          |    "from": "LHR",
                          |    "dep_local": "03/11/2026 12:30",
                          |    "dep_utc_offset": "+00:00",
                          |    "duration_mins": 75,
                          |    "fare_pence": 16546,
                          |    "cur": "GBP",
                          |    "seats": 7
                          |  },
                          |  {
                          |    "code": "BJ278",
                          |    "from": "LHR",
                          |    "to": "CDG",
                          |    "dep_local": "03/11/2026 19:15",
                          |    "dep_utc_offset": "+00:00",
                          |    "duration_mins": 75,
                          |    "fare_pence": 28551,
                          |    "cur": "GBP",
                          |    "seats": null
                          |  }
                          |]""".stripMargin

  def parseJsonHelper(response: String) = {
      Json.parse(response).validate[List[BlueJet]] match {
        case JsSuccess(caseClass, _) => caseClass
        case JsError(errors) => fail(s"failed to read blueJet, Error: $errors")
      }
    }




  "BlueJet Json formats" should {

    "parse the Json to a case class BlueJet" in {
      val convertedBlueJet = parseJsonHelper(blueJetRespones)

      convertedBlueJet.headOption.get shouldBe blueJet
    }

    "return None when seats unknown in BlueJetResponses" in {
      val convertedBlueJet = parseJsonHelper(blueJetRespones)

      convertedBlueJet.lift(1).get.seats shouldBe None
    }

    "throw JsError when wrong data type is present in Json response" in {
      val brokenBlueJet = Json.parse(wrongDatatypeRespones).validate[BlueJet]

      brokenBlueJet shouldBe a[JsError]
    }

    "throw JsError when missing fields are present in Json response" in {
      val missingFieldBlueJet = Json.parse(missingFieldResponse).validate[BlueJet]

      missingFieldBlueJet shouldBe a[JsError]
    }

  }

}
