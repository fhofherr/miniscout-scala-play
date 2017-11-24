package models

import play.api.libs.json._

import scala.util.{Failure, Success, Try}

object FuelType {

  implicit val fuelTypeWrites: Writes[FuelType] =
    (ft: FuelType) => JsString(unapply(ft))

  implicit val fuelTypeReads: Reads[FuelType] = (json: JsValue) => {
    val t: Try[FuelType] = Try(json.as[String]).map(apply)
    t match {
      case Success(ft) => JsSuccess(ft)
      case Failure(e) => JsError(e.getMessage)
    }
  }

  def apply(s: String): FuelType = s match {
    case "Gasoline" => Gasoline
    case "Diesel" => Diesel
    case _ => throw new IllegalArgumentException(s"Unknown fuel type '$s'")
  }

  def unapply(ft: FuelType): String = ft match {
    case Gasoline => "Gasoline"
    case Diesel => "Diesel"
  }

}
sealed trait FuelType

case object Gasoline extends FuelType
case object Diesel extends FuelType
