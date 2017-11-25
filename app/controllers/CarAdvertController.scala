package controllers

import java.time.LocalDate
import java.util.UUID
import javax.inject._

import controllers.CarAdvertController.CreateCarAdvertData
import models.{CarAdvert, CarAdvertRepository, FuelType}
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext


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

  def validateJson[A : Reads]: BodyParser[A] = parse.json.validate(
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

  def findById(id: UUID): Action[AnyContent] = Action.async { request =>
    repo
      .findById(id)
      .map(carAdvert => Ok(Json.toJson(carAdvert)))
  }
}
