package controllers

import java.time.LocalDate

import models.Gasoline
import org.scalatestplus.play.PlaySpec

class CreateCarAdvertDataSpec extends PlaySpec {

  "#validationErrors" must {
    "return an empty sequence if it is a valid new car" in {
      validNewCar.validationErrors mustBe empty
    }

    "return an empty sequence if it is a valid used car" in {
      validUsedCar.validationErrors mustBe empty
    }

    "return a non-empty sequence if a new car is invalid" in {
      validNewCar.copy(mileage = Option(1)).validationErrors() must not be empty
      validNewCar.copy(firstRegistration = Option(LocalDate.now())).validationErrors() must not be empty
    }

    "return a non-empty sequence if a used car is invalid" in {
      validUsedCar.copy(mileage = Option.empty).validationErrors() must not be empty
      validUsedCar.copy(firstRegistration = Option.empty).validationErrors() must not be empty
    }
  }

  val validNewCar = CreateCarAdvertData(
    title = "new car",
    fuel = Gasoline,
    price = 20349,
    `new` = true
  )

  val validUsedCar = validNewCar.copy(
    title = "used car",
    `new` = false,
    mileage = Option(3234),
    firstRegistration = Option(LocalDate.of(2017, 1, 2))
  )

}
