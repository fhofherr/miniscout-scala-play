package controllers


import akka.stream.Materializer
import controllers.CarAdvertController.CreateCarAdvertData
import models.{CarAdvert, Gasoline}
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test._
import testhelpers.DbEvolutions

import scala.concurrent.Future


class CarAdvertControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with DbEvolutions {
  implicit lazy val materializer: Materializer = app.materializer

  "CarAdvertController POST /" should {
    "create a new car advert" in withEvolutions {
      val controller = inject[CarAdvertController]
      val response = postCarAdvert(controller, carAdvertData)

      status(response) mustBe CREATED
      contentType(response) mustBe Option(JSON)
      contentAsJson(response).validate[CarAdvert].asOpt mustBe defined
    }
  }

  "CarAdvertController GET /" should {
    "get a list of all available car adverts" in withEvolutions {
      val controller = inject[CarAdvertController]
      val carAdvertPorsche = saveNewCarAdvert(controller, carAdvertData)
      val carAdvertBMW = saveNewCarAdvert(
        controller, carAdvertData.copy(title = "BMW X3", price = 40324))

      val listCarAdvertsRequest = FakeRequest("GET", "/?sortBy=idAsc")
      val response = controller.index(Option("idAsc")).apply(listCarAdvertsRequest)

      status(response) mustBe OK
      contentType(response) mustBe Option(JSON)

      val retrievedAdverts = contentAsJson(response)
        .validate[Seq[CarAdvert]]
        .asOpt
        .getOrElse(fail("Error while retrieving car adverts"))

      val expected = Seq(carAdvertPorsche, carAdvertBMW).sortBy(_.id)
      retrievedAdverts must have size expected.length
      retrievedAdverts must contain theSameElementsInOrderAs expected
    }
  }

  "CarAdvertController GET /<uuid>" should {
    "obtain an existing car advert using its id" in withEvolutions {
      val controller = inject[CarAdvertController]
      val carAdvert = saveNewCarAdvert(controller, carAdvertData)

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

  def saveNewCarAdvert(controller: CarAdvertController, data: CreateCarAdvertData): CarAdvert = {
    val response = postCarAdvert(controller, data)
    contentAsJson(response)
      .validate[CarAdvert]
      .asOpt
      .getOrElse(fail("Car advert not created"))
  }
}