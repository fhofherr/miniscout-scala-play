package controllers


import java.util.UUID

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

    "return an empty list if there are no car adverts" in withEvolutions {
      val request = FakeRequest(GET, "/")
      val Some(response) = route(app, request)

      status(response) mustBe OK
      contentType(response) mustBe Option(JSON)

      val Some(retreivedCarAdverts) = contentAsJson(response)
        .validate[Seq[CarAdvert]]
        .asOpt

      retreivedCarAdverts mustBe empty
    }

  }

  "CarAdvertController GET /<uuid>" should {
    "obtain an existing car advert using its id" in withEvolutions {
      val carAdvert = saveNewCarAdvert(carAdvertDataPorsche)

      val request = FakeRequest("GET", s"/${carAdvert.id}")
      val Some(response) = route(app, request)

      status(response) mustBe OK
      contentType(response) mustBe Option(JSON)

      val Some(retrievedAdvert) = contentAsJson(response)
        .validate[CarAdvert]
        .asOpt

      retrievedAdvert mustEqual carAdvert
    }

    "answer with NotFound if the advert does not exist" in withEvolutions {
      val request = FakeRequest(GET, s"/${UUID.randomUUID()}")
      val Some(response) = route(app, request)
      status(response) mustBe NOT_FOUND
    }
  }

  "CarAdvertController DELETE /<uuid>" should {
    "delete the car advert identified by the id" in withEvolutions {
      val carAdvert = saveNewCarAdvert(carAdvertDataPorsche)
      val request = FakeRequest(DELETE, s"/${carAdvert.id}")
      val Some(response) = route(app, request)

      status(response) mustBe OK
    }

    "answer with NotFound if a car advert does not exist" in withEvolutions {
      val request = FakeRequest(DELETE, s"/${UUID.randomUUID()}")
      val Some(response) = route(app, request)

      status(response) mustBe NOT_FOUND
    }
  }

  "CarAdvertController PUT /<uuid>" should {
    "update an existing car advert" in withEvolutions {
      val carAdvert = saveNewCarAdvert(carAdvertDataPorsche)

      val request = FakeRequest(PUT, s"/${carAdvert.id}")
        .withJsonBody(Json.toJson(carAdvertDataBMW))

      val Some(response) = route(app, request)
      status(response) mustBe OK
      contentType(response) mustBe Option(JSON)

      val expectedCarAdvert = carAdvert.copy(
        title = carAdvertDataBMW.title,
        fuel = carAdvertDataBMW.fuel,
        price = carAdvertDataBMW.price,
        `new` = carAdvertDataBMW.`new`,
        mileage = carAdvertDataBMW.mileage,
        firstRegistration = carAdvertDataBMW.firstRegistration)

      val Some(actualCarAdvert) = contentAsJson(response)
        .validate[CarAdvert]
        .asOpt

      actualCarAdvert must equal(expectedCarAdvert)
    }

    "answer with NotFound if the car advert does not exist" in withEvolutions {
      val request = FakeRequest(PUT, s"/${UUID.randomUUID()}")
        .withJsonBody(Json.toJson(carAdvertDataPorsche))
      val Some(response) = route(app, request)

      status(response) mustBe NOT_FOUND
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