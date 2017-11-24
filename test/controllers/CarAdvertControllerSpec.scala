package controllers


import akka.stream.Materializer
import controllers.CarAdvertController.CreateCarAdvertData
import models.{CarAdvert, Gasoline}
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future


class CarAdvertControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  implicit lazy val materializer: Materializer = app.materializer

  "CarAdvertController POST /" should {
    "create a new car advert" in {
      val controller = inject[CarAdvertController]
      val response = postCarAdvert(controller, carAdvertData)

      status(response) mustBe CREATED
      contentType(response) mustBe Option(JSON)
      contentAsJson(response).validate[CarAdvert].asOpt mustBe defined
    }
  }

  "CarAdvertController GET /<uuid>" should {
    "obtain an existing car advert using its id" in {
      val controller = inject[CarAdvertController]
      val response = postCarAdvert(controller, carAdvertData)
      val carAdvert = contentAsJson(response)
        .validate[CarAdvert]
        .asOpt
        .getOrElse(fail("Car advert not created"))

      val getCarAdvertRequest = FakeRequest("GET", s"/${carAdvert.id}")
      val findResponse = controller.findById(carAdvert.id).apply(getCarAdvertRequest)

      val retrievedAdvert = contentAsJson(findResponse)
        .validate[CarAdvert]
        .asOpt
        .getOrElse(fail("Car advert not retrieved"))

      retrievedAdvert mustEqual carAdvert
    }
  }

  val carAdvertData = CreateCarAdvertData(
    title = "Porsche 918",
    fuel = Gasoline,
    price = 847000,
    `new` = true)

  def postCarAdvert(controller: CarAdvertController, data: CreateCarAdvertData): Future[Result] = {
    val createCarAdvertRequest = FakeRequest(POST, "/")
      .withBody(carAdvertData)
      .withHeaders(CONTENT_TYPE -> JSON)

    controller.createNew().apply(createCarAdvertRequest)
  }
}