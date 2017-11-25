package controllers


import akka.stream.Materializer
import controllers.CarAdvertController.CreateCarAdvertData
import models.{CarAdvert, Gasoline}
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test._
import testhelpers.DbEvolutions

import scala.concurrent.Future


class CarAdvertControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with DbEvolutions {
  implicit lazy val materializer: Materializer = app.materializer

  "CarAdvertController POST /" should {
    "create a new car advert" in withEvolutions {
      val response = postCarAdvert(carAdvertDataPorsche)

      status(response) mustBe CREATED
      contentType(response) mustBe Option(JSON)
      contentAsJson(response).validate[CarAdvert].asOpt mustBe defined
    }
  }

  "CarAdvertController GET /" should {

    "get a list of all available car adverts sorted by id" in withEvolutions {
      val carAdvertPorsche = saveNewCarAdvert(carAdvertDataPorsche)
      val carAdvertBMW = saveNewCarAdvert(
        carAdvertDataBMW)

      val listCarAdvertsRequest = FakeRequest(GET, "/")
      val Some(response) = route(app, listCarAdvertsRequest)

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

    "allow to specify a different ordering as query parameter" in withEvolutions {
      val carAdvertPorsche = saveNewCarAdvert(carAdvertDataPorsche)
      val carAdvertBMW = saveNewCarAdvert(carAdvertDataBMW)
      val request = FakeRequest(GET, "/?sortBy=titleAsc")
      val Some(response) = route(app, request)

      status(response) mustBe OK
      contentType(response) mustBe Option(JSON)

      val Some(retrievedAdverts) = contentAsJson(response)
        .validate[Seq[CarAdvert]]
        .asOpt

      val expected = Seq(carAdvertPorsche, carAdvertBMW).sortBy(_.title)
      retrievedAdverts must have size expected.length
      retrievedAdverts must contain theSameElementsInOrderAs expected
    }

  }

  "CarAdvertController GET /<uuid>" should {
    "obtain an existing car advert using its id" in withEvolutions {
      val carAdvert = saveNewCarAdvert(carAdvertDataPorsche)

      val request = FakeRequest("GET", s"/${carAdvert.id}")
      val Some(response) = route(app, request)

      val Some(retrievedAdvert) = contentAsJson(response)
        .validate[CarAdvert]
        .asOpt

      retrievedAdvert mustEqual carAdvert
    }
  }

  val carAdvertDataPorsche = CreateCarAdvertData(
    title = "Porsche 918",
    fuel = Gasoline,
    price = 847000,
    `new` = true)

  val carAdvertDataBMW = carAdvertDataPorsche
    .copy(title = "BMW X3", price = 40324)

  def postCarAdvert(data: CreateCarAdvertData): Future[Result] = {
    val createCarAdvertRequest = FakeRequest(POST, "/")
      .withJsonBody(Json.toJson(carAdvertDataPorsche))
      .withHeaders(CONTENT_TYPE -> JSON)

    route(app, createCarAdvertRequest)
      .getOrElse(fail("Could not post to /"))
  }

  def saveNewCarAdvert(data: CreateCarAdvertData): CarAdvert = {
    val response = postCarAdvert(data)
    contentAsJson(response)
      .validate[CarAdvert]
      .asOpt
      .getOrElse(fail("Car advert not created"))
  }
}