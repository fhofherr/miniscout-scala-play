package controllers

import java.time.LocalDate

import models.FuelType
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

object CreateCarAdvertData {
  private def expectMissing[A](attrName: String): Reads[A] = Reads[A] { _ =>
    JsError(s"Expected '$attrName' to be missing")
  }

  implicit val createCarAdvertRequestWrites: Writes[CreateCarAdvertData] =
    Json.writes[CreateCarAdvertData]

  implicit val createCarAdvertRequestReads: Reads[CreateCarAdvertData] = { json: JsValue =>
    val reads = Json.reads[CreateCarAdvertData]
    Try(json.as[CreateCarAdvertData](reads)) match {
      case Success(cad) =>
        val errors = cad.validationErrors()
        if (errors.isEmpty) {
          JsSuccess(cad)
        } else {
          errors
            .map(e => JsError(e))
            .reduce((e1: JsError, e2: JsError) => JsError.merge(e1, e2))
        }
      case Failure(e) => JsError(e.getMessage)
    }

  }
}

case class CreateCarAdvertData(title: String,
                               fuel: FuelType,
                               price: Int,
                               `new`: Boolean,
                               mileage: Option[Int] = Option.empty,
                               firstRegistration: Option[LocalDate] = Option.empty) {
  def validationErrors(): Seq[String] = if (`new`) {
    val mileageError = mileage
      .map(_ => Seq("No 'mileage' for new cars"))
      .getOrElse(Seq.empty)
    val firstRegistrationError = firstRegistration
      .map(_ => Seq("No 'firstRegistration' for new cars,"))
      .getOrElse(Seq.empty)
    mileageError ++ firstRegistrationError
  } else {
    val mileageError = mileage
      .map(_ => Seq.empty[String])
      .getOrElse(Seq("Missing 'mileage' for used car"))
    val firstRegistrationError = firstRegistration
      .map(_ => Seq.empty[String])
      .getOrElse(Seq("Missing 'firstRegistration' for used car"))
    mileageError ++ firstRegistrationError
  }
}