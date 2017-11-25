package controllers

import java.time.LocalDate
import java.util.UUID
import javax.inject._

import controllers.CarAdvertController.CreateCarAdvertData
import models.{CarAdvert, CarAdvertRepository, FuelType}
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}


object CarAdvertController {

  object CreateCarAdvertData {
    implicit val createCarAdvertRequestWrites: Writes[CreateCarAdvertData] = Json.writes[CreateCarAdvertData]
    implicit val createCarAdvertRequestReads: Reads[CreateCarAdvertData] = Json.reads[CreateCarAdvertData]
  }

  case class CreateCarAdvertData(title: String,
                                 fuel: FuelType,
                                 price: Int,
                                 `new`: Boolean,
                                 mileage: Option[Int] = Option.empty,
                                 firstRegistration: Option[LocalDate] = Option.empty)

}

@Singleton
class CarAdvertController @Inject()(cc: ControllerComponents, repo: CarAdvertRepository)
                                   (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def validateJson[A: Reads]: BodyParser[A] = parse.json.validate(
    _.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  def index(sortBy: Option[String] = Option.empty): Action[AnyContent] = Action.async { request =>
    val sortByType = sortBy
      .map(s => CarAdvertRepository.SortBy(s))
      .getOrElse(CarAdvertRepository.IdAsc)
    repo.findAll(sortByType).map(as => Ok(Json.toJson(as)))
  }

  def createNew: Action[CreateCarAdvertData] = Action(validateJson[CreateCarAdvertData]).async { request =>
    val data = request.body
    val carAdvert = CarAdvert(
      id = UUID.randomUUID(),
      title = data.title,
      fuel = data.fuel,
      price = data.price,
      `new` = data.`new`,
      mileage = data.mileage,
      firstRegistration = data.firstRegistration)
    repo
      .create(carAdvert)
      .map(_ => Created(Json.toJson(carAdvert)))
  }

  def findById(id: String): Action[AnyContent] = Action.async { _ =>
    val uuid = UUID.fromString(id)
    repo
      .findById(uuid)
      .map(carAdvert => Ok(Json.toJson(carAdvert)))
  }

  def update(id: String): Action[CreateCarAdvertData] = Action(validateJson[CreateCarAdvertData]).async { request =>
    def update(ca: CarAdvert): Future[Result] = repo
        .update(ca)
        .map(n => if (n > 0) { Ok(Json.toJson(ca)) } else { NotFound })

    val uuid = UUID.fromString(id)
    val data = request.body
    val currentCarAdvert: Future[Option[CarAdvert]] = repo.findById(uuid)

    currentCarAdvert
      .flatMap {
        case Some(ca) => update(ca.copy(
            title = data.title,
            fuel = data.fuel,
            price = data.price,
            `new` = data.`new`,
            mileage = data.mileage,
            firstRegistration = data.firstRegistration))
        case None => Future.successful(NotFound)
      }

  }

  def delete(id: String): Action[AnyContent] = Action.async { _ =>
    val uuid = UUID.fromString(id)
    repo
      .delete(uuid)
      .map(nDeleted => if (nDeleted > 0) {
        Ok
      } else {
        NotFound
      })
  }
}
