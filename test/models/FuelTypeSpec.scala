package models

import org.scalatestplus.play.PlaySpec
import play.api.libs.json._

class FuelTypeSpec extends PlaySpec {

  "FuelType objects" must {

    "be serializable to JSON strings" in {
      Json.toJson(Gasoline) must equal(JsString("Gasoline"))
      Json.toJson(Diesel) must equal(JsString("Diesel"))
    }

    "be deserializable form JSON strings" in {
      Json.fromJson[FuelType](JsString("Gasoline")) must equal(JsSuccess(Gasoline))
      Json.fromJson[FuelType](JsString("Diesel")) must equal(JsSuccess(Diesel))

      val msg = "Unknown fuel type 'NotAFuelType'"
      Json.fromJson[FuelType](JsString("NotAFuelType")) must equal(JsError(msg))
    }
  }

}
