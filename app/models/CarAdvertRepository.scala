package models

import java.sql.Date
import java.time.LocalDate
import java.util.UUID
import javax.inject.{Inject, Singleton}

import models.CarAdvertRepository._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

object CarAdvertRepository {
  sealed trait SortBy

  case object IdAsc extends SortBy
  case object IdDesc extends SortBy
  case object TitleAsc extends SortBy
  case object TitleDesc extends SortBy
  case object FuelTypeAsc extends SortBy
  case object FuelTypeDesc extends SortBy
  case object NewAsc extends SortBy
  case object NewDesc extends SortBy
  case object MileageAsc extends SortBy
  case object MileageDesc extends SortBy
  case object FirstRegistrationAsc extends SortBy
  case object FirstRegistrationDesc extends SortBy

  object SortBy {
    def apply(s: String): SortBy = s match {
      case "idAsc" => IdAsc
      case "idDesc" => IdDesc
      case "titleAsc" => TitleAsc
      case "titleDesc" => TitleDesc
      case "fuelTypeAsc" => FuelTypeAsc
      case "fuelTypeDesc" => FuelTypeDesc
      case "newAsc" => NewAsc
      case "newDesc" => NewDesc
      case "mileageAsc" => MileageAsc
      case "mileageDesc" => MileageDesc
      case "firstRegistrationAsc" => FirstRegistrationAsc
      case "firstRegistrationDesc" => FirstRegistrationDesc
      case _ => throw new IllegalArgumentException(s"Can't sort by $s")
    }
  }
}

@Singleton
class CarAdvertRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private implicit val localDateToDate: BaseColumnType[LocalDate] = MappedColumnType.base[LocalDate, Date](
    ld => Date.valueOf(ld),
    d => d.toLocalDate
  )

  private implicit val fuelTypeToString: BaseColumnType[FuelType] = MappedColumnType.base[FuelType, String](
    ft => FuelType.unapply(ft),
    s => FuelType(s)
  )

  private class CarAdvertTable(tag: Tag) extends Table[CarAdvert](tag, "T_CAR_ADVERT") {

    def id = column[UUID]("CAR_ADVERT_ID", O.PrimaryKey)

    def title = column[String]("TITLE")

    def fuel = column[FuelType]("FUEL")

    def price = column[Int]("PRICE")

    def `new` = column[Boolean]("NEW")

    def mileage = column[Option[Int]]("MILEAGE")

    def firstRegistration = column[Option[LocalDate]]("FIRST_REGISTRATION")

    override def * = (id, title, fuel, price, `new`, mileage, firstRegistration) <> ((CarAdvert.apply _).tupled, CarAdvert.unapply)
  }

  private def sortByFunc(sortBy: SortBy) = sortBy match {
    case IdAsc => (t: CarAdvertTable) => t.id.asc
    case IdDesc => (t: CarAdvertTable) => t.id.desc
    case TitleAsc => (t: CarAdvertTable) => t.title.asc
    case TitleDesc => (t: CarAdvertTable) => t.title.desc
    case FuelTypeAsc => (t: CarAdvertTable) => t.fuel.asc
    case FuelTypeDesc => (t: CarAdvertTable) => t.fuel.desc
    case NewAsc => (t: CarAdvertTable) => t.`new`.asc
    case NewDesc => (t: CarAdvertTable) => t.`new`.desc
    case MileageAsc => (t: CarAdvertTable) => t.mileage.asc
    case MileageDesc => (t: CarAdvertTable) => t.mileage.desc
    case FirstRegistrationAsc => (t: CarAdvertTable) => t.firstRegistration.asc
    case FirstRegistrationDesc => (t: CarAdvertTable) => t.firstRegistration.desc
  }

  private val carAdverts = TableQuery[CarAdvertTable]

  def create(c: CarAdvert): Future[Int] = db.run {
    carAdverts += c
  }

  def update(u: CarAdvert): Future[Int] = db.run {
    val q = for(c <- carAdverts if c.id === u.id) yield c
    q.update(u)
  }

  def delete(id: UUID): Future[Int] = db.run {
    carAdverts.filter(_.id === id).delete
  }

  def findById(id: UUID): Future[Option[CarAdvert]] = db.run {
    carAdverts.filter(_.id === id).result.map(_.headOption)
  }

  def findAll[T](sortBy: SortBy = IdAsc): Future[Seq[CarAdvert]] = db.run {
    carAdverts.sortBy(sortByFunc(sortBy)).result
  }
}
