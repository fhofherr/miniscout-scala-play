package models

import java.util.UUID
import java.util.concurrent.TimeUnit

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Injecting

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class CarAdvertRepositorySpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  val carAdvert = CarAdvert(
    id = UUID.randomUUID(),
    title = "some car advert",
    fuel = Diesel,
    price = 23094,
    `new` = true
  )

  "The CarAdvertRepository" must {
    "be able able to save a new car advert and to obtain a car advert by id" in {
      val carAdvertRepository = inject[CarAdvertRepository]

      carAdvertRepository.create(carAdvert)
      val advertsFuture = carAdvertRepository.findById(carAdvert.id)
      val adverts = Await.result(advertsFuture, Duration(100, TimeUnit.MILLISECONDS))
      adverts must equal(Option(carAdvert))
    }
  }
}
