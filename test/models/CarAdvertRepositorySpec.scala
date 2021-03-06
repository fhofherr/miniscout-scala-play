package models

import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.TimeUnit

import models.CarAdvertRepository._
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

    "sort ascending and descending by id" in withEvolutions {
      mustSortAscendingAndDescendingBy(_.id, IdAsc, IdDesc)
    }

    "sort ascending and descending by title" in withEvolutions {
      mustSortAscendingAndDescendingBy(_.title, TitleAsc, TitleDesc)
    }

    "sort ascending and descending by fuel type" in withEvolutions {
      mustSortAscendingAndDescendingBy(_.fuel, FuelTypeAsc, FuelTypeDesc)
    }

    "sort ascending and descending by the new attribute" in withEvolutions {
      mustSortAscendingAndDescendingBy(_.`new`, NewAsc, NewDesc)
    }

    "sort ascending and descending by mileage" in withEvolutions {
      mustSortAscendingAndDescendingBy(_.mileage, MileageAsc, MileageDesc)
    }

    "sort ascending and descending by first registration date" in withEvolutions {
      import CarAdvert._
      mustSortAscendingAndDescendingBy(_.firstRegistration, FirstRegistrationAsc, FirstRegistrationDesc)
    }
  }

  "CarAdvertRepository#delete" must {

    "delete an existing car advert" in withEvolutions {
      val carAdvertRepository = inject[CarAdvertRepository]
      await(carAdvertRepository.create(carAdvert))

      val nDeleted = await(carAdvertRepository.delete(carAdvert.id))
      nDeleted must equal(1)

      val retrievedCarAdvert = await(carAdvertRepository.findById(carAdvert.id))
      retrievedCarAdvert mustBe None
    }

    "return 0 if no car advert was deleted" in withEvolutions {
      val carAdvertRepository = inject[CarAdvertRepository]
      val id = UUID.randomUUID()
      val nDeleted = await(carAdvertRepository.delete(id))
      nDeleted must equal(0)
    }
  }

  "CarAdvertRepository#update" must {

    "update an existing car advert" in withEvolutions {
      val carAdvertRepository = inject[CarAdvertRepository]
      await(carAdvertRepository.create(carAdvert))

      val updatedCarAdvert = carAdvert.copy(
        title = "Updated title",
        fuel = Gasoline,
        price = 238947,
        `new` = false,
        mileage = Option(34985),
        firstRegistration = Option(LocalDate.of(2015, 1, 23)))

      val nUpdated = await(carAdvertRepository.update(updatedCarAdvert))

      nUpdated must equal(1)

      val Some(storedCarAdvert) =
        await(carAdvertRepository.findById(carAdvert.id))

      storedCarAdvert must equal(updatedCarAdvert)
      storedCarAdvert must not equal carAdvert
    }

    "return 0 if no car advert was updated" in withEvolutions {
      val carAdvertRepository = inject[CarAdvertRepository]
      val nUpdated = await(carAdvertRepository.update(carAdvert))
      nUpdated must equal(0)
    }
  }

  val duration = Duration(100, TimeUnit.MILLISECONDS)

  def await[T](future: Future[T]): T = Await.result(future, duration)

  def withAllCarAdverts[T](carAdvertRepository: CarAdvertRepository)(block: =>T): T = {
    await(carAdvertRepository.create(carAdvert))
    await(carAdvertRepository.create(anotherCarAdvert))
    block
  }

  def mustSortAscendingAndDescendingBy[T](f: CarAdvert => T, asc: SortBy, desc: SortBy)(implicit ord: Ordering[T]): Unit = {
    val carAdvertRepository = inject[CarAdvertRepository]
    withAllCarAdverts(carAdvertRepository) {
      val advertsAsc = await(carAdvertRepository.findAll(asc))
      val advertsDesc = await(carAdvertRepository.findAll(desc))

      val expected = Seq(carAdvert, anotherCarAdvert).sortBy(f)
      advertsAsc must have size  expected.size
      advertsDesc must have size  expected.size

      advertsAsc must contain theSameElementsInOrderAs expected
      advertsDesc must contain theSameElementsInOrderAs expected.reverse
    }
  }

  val carAdvert = CarAdvert(
    id = UUID.randomUUID(),
    title = "some car advert",
    fuel = Diesel,
    price = 23094,
    `new` = true
  )

  val anotherCarAdvert = CarAdvert(
    id = UUID.randomUUID(),
    title = "another car advert",
    fuel = Gasoline,
    price = 13523,
    `new` = false,
    mileage = Option(10000),
    firstRegistration = Option(LocalDate.of(2017, 1, 1))
  )

}
