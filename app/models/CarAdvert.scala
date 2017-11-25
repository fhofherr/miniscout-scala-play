package models

import java.time.LocalDate
import java.util.UUID

import play.api.libs.json.{Json, Reads, Writes}

object CarAdvert {
  private val JanFirst1900 = "1900-01-01"

  implicit val carAdvertWrites: Writes[CarAdvert] = Json.writes[CarAdvert]
  implicit val carAdvertReads: Reads[CarAdvert] = Json.reads[CarAdvert]

  implicit val firstRegistrationOrdering: Ordering[Option[LocalDate]] =
    Ordering.by(_.map(d => d.toString).getOrElse(JanFirst1900))
}

case class CarAdvert(id: UUID,
                     title: String,
                     fuel: FuelType,
                     price: Int,
                     `new`: Boolean,
                     mileage: Option[Int] = Option.empty,
                     firstRegistration: Option[LocalDate] = Option.empty)
