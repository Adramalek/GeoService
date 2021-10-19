package controllers

import exceptions.{UserMarkNotFoundException, WebCellNotFoundException}
import model.{UserMark, WebCell}
import play.api.libs.json.{Json, OFormat}
import play.api.mvc._
import services.GeoService

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class GeoController @Inject()(private val geoService: GeoService,
                              private val cc: ControllerComponents)(implicit exec: ExecutionContext)
	extends AbstractController(cc) {

	implicit private val userMarkJson: OFormat[UserMark] = Json.format[UserMark]
	implicit private val coordinatesDtoJson: OFormat[CoordinatesDto] = Json.format[CoordinatesDto]
	implicit private val webCellJson: OFormat[WebCell] = Json.format[WebCell]

	private val api: Map[String, Seq[String]] = Map.newBuilder
	                                               .addOne(("GET", Seq("/","/geo/marks/$userId<\\d+>/near", "/geo/marks/at")))
	                                               .addOne(("POST", Seq("/geo/marks")))
	                                               .addOne(("PUT", Seq("/geo/marks/$userId<\\d+>", "/geo/web/cells/$tileX<-?\\d+>/$tileY<-?\\d+>")))
	                                               .addOne(("DELETE", Seq("/geo/marks/$userId<\\d+>")))
	                                               .result()

	def index(): Action[AnyContent] = Action {
		Ok(Json.toJson(api))
	}

	def isNear(userId: Long, lon: Double, lat: Double): Action[AnyContent] = Action.async {
		geoService.isNear(userId, lon, lat).transform {
			case Failure(exception) =>
				val result = exception match {
					case userMarkNotFound: UserMarkNotFoundException => NotFound(userMarkNotFound.getMessage)
					case cellNotFound: WebCellNotFoundException => NotFound(cellNotFound.getMessage)
					case e => InternalServerError(e.getMessage)
				}
				Try(result)
			case Success(value) =>
				val message = if (value) "Is near the mark" else "Away from the mark"
				Try(Ok(message))
		}
	}

	def statistics(lon: Double, lat: Double): Action[AnyContent] = Action.async {
		geoService.getStatistics(lon, lat).transform {
			case Success(users) => Try(Ok(users.toString))
			case Failure(exception) => Try(InternalServerError(exception.getMessage))
		}
	}

	def saveMark(): Action[AnyContent] = Action.async { implicit request => processUserMark(request) }

	def updateMark(userId: Long): Action[AnyContent] = Action.async { implicit request =>
		processUserMark(request, Some(userId))
	}

	def deleteMark(userId: Long): Action[AnyContent] = Action.async {
		geoService.deleteMark(userId).transform {
			case Success(deleted) => Try(if (deleted) Ok else NotFound)
			case Failure(exception) => Try(InternalServerError(exception.getMessage))
		}
	}

	def putCell(tileX: Int, tileY: Int, distanceError: Double): Action[AnyContent] = Action.async {
		val cell = WebCell(tileX, tileY, distanceError)
		if (distanceError < 0) {
			Future(BadRequest(s"Distance error must be positive or zero"))
		} else {
			geoService.saveCell(cell).transform {
				case Success(newCell) => Try(Created(Json.toJson(newCell)))
				case Failure(exception) => Try(InternalServerError(exception.getMessage))
			}
		}
	}

	private def processUserMark(request: Request[AnyContent], userId: Option[Long] = None) = {
		val userMark = request.body.asJson.flatMap(Json.fromJson[CoordinatesDto](_).asOpt)
		                      .map(coors => UserMark(userId, coors.lon, coors.lat))
		userMark match {
			case Some(mark) => geoService.saveMark(mark).transform {
				case Success(saved) => Try(Created(Json.toJson(saved)))
				case Failure(exception) => Try(InternalServerError(exception.getMessage))
			}
			case None => Future(BadRequest)
		}
	}
}
