package models

import java.time.LocalDate
import java.util.UUID

import play.api.libs.json.{Json, Reads, Writes}

object CarAdvert {
  implicit val carAdvertWrites: Writes[CarAdvert] = Json.writes[CarAdvert]
  implicit val carAdvertReads: Reads[CarAdvert] = Json.reads[CarAdvert]
}

case class CarAdvert(id: UUID,
                     title: String,
                     fuel: FuelType,
                     price: Int,
                     `new`: Boolean,
                     mileage: Option[Int] = Option.empty,
                     firstRegistration: Option[LocalDate] = Option.empty)
