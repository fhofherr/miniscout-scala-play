package models

import java.util.UUID
import java.util.concurrent.TimeUnit

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Injecting
import testhelpers.DbEvolutions

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class CarAdvertRepositorySpec extends PlaySpec with GuiceOneAppPerTest with Injecting with DbEvolutions {

  "CarAdvertRepository#create" must {
    "store a new car advert to the database" in withEvolutions {
      val carAdvertRepository = inject[CarAdvertRepository]

      await(carAdvertRepository.create(carAdvert))
      val advertsFuture = carAdvertRepository.findById(carAdvert.id)
      val adverts = await(advertsFuture)
      adverts must equal(Option(carAdvert))
    }
  }

  "CarAdvertRepository#findAll" must {
    "return all existing car adverts sorted by id" in withEvolutions {
      val carAdvertRepository = inject[CarAdvertRepository]
      val anotherCarAdvert = carAdvert.copy(
        id = UUID.randomUUID(), title = "another car advert")
      await(carAdvertRepository.create(carAdvert))
      await(carAdvertRepository.create(anotherCarAdvert))

      val adverts = await(carAdvertRepository.findAll())

      val expected = Seq(carAdvert, anotherCarAdvert).sortBy(_.id)
      adverts must contain theSameElementsInOrderAs expected
    }
  }

  val duration = Duration(100, TimeUnit.MILLISECONDS)
  def await[T](future: Future[T]): T = Await.result(future, duration)

  val carAdvert = CarAdvert(
    id = UUID.randomUUID(),
    title = "some car advert",
    fuel = Diesel,
    price = 23094,
    `new` = true
  )

}
