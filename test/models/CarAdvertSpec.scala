package models

import java.time.LocalDate
import java.util.UUID

import org.scalatestplus.play.PlaySpec
import play.api.libs.json._

class CarAdvertSpec extends PlaySpec {

  "A CarAdvert" must {

    val carAdvert = CarAdvert(
      id = UUID.randomUUID(),
      title = "Sample Advert",
      fuel = Gasoline,
      price = 1,
      `new` = false,
      mileage = Option(1003),
      firstRegistration = Option(LocalDate.now())
    )

    "be serializable to JSON" in {
      val expected = JsObject(
        Seq("id" -> JsString(carAdvert.id.toString),
          "title" -> JsString(carAdvert.title),
          "fuel" -> JsString(FuelType.unapply(carAdvert.fuel)),
          "price" -> JsNumber(carAdvert.price),
          "new" -> JsBoolean(carAdvert.`new`),
          "mileage" -> JsNumber(carAdvert.mileage.get),
          "firstRegistration" -> JsString(carAdvert.firstRegistration.get.toString)))

      Json.toJson(carAdvert) must equal(expected)
    }

    "be deserializable from JSON" in {
      val jsonCarAdvert = Json.toJson(carAdvert)
      val deserialized = Json.fromJson[CarAdvert](jsonCarAdvert)
      deserialized must equal(JsSuccess(carAdvert))
    }
  }
}
