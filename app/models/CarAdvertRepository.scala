package models

import java.sql.Date
import java.time.LocalDate
import java.util.UUID
import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CarAdvertRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private implicit val localDateToDate = MappedColumnType.base[LocalDate, Date](
    ld => Date.valueOf(ld),
    d => d.toLocalDate
  )

  private implicit val fuelTypeToString = MappedColumnType.base[FuelType, String](
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

  private val carAdverts = TableQuery[CarAdvertTable]

  def create(c: CarAdvert): Future[Int] = db.run {
    (carAdverts += c)
  }

  def findById(id: UUID): Future[Option[CarAdvert]] = db.run {
    carAdverts.filter(_.id === id).result.map(_.headOption)
  }
}
