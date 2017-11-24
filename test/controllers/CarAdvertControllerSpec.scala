package controllers

import akka.stream.Materializer
import models.{CarAdvert, Gasoline}
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._


class CarAdvertControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  implicit lazy val materializer: Materializer = app.materializer

  "CarAdvertController POST" should {
    "create a new car advert" in {
      val carAdvertRequest = CarAdvertController.CreateCarAdvertData(
        title = "Porsche 918",
        fuel = Gasoline,
        price = 847000,
        `new` = true)
      val controller = inject[CarAdvertController]
      val request = FakeRequest(POST, "/")
        .withBody(carAdvertRequest)
        .withHeaders(CONTENT_TYPE -> JSON)
      val response = controller.index().apply(request)

      status(response) mustBe CREATED
      contentType(response) mustBe Option(JSON)
      Json.fromJson[CarAdvert](contentAsJson(response)).isSuccess mustBe true
    }
  }
}