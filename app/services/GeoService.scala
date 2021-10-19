package services

import exceptions.{UserMarkNotFoundException, WebCellNotFoundException}
import model.{UserMark, WebCell}
import services.repository.{TransactionHelper, UserMarkRepository, WebCellRepository}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class GeoService @Inject()(private val transactionHelper: TransactionHelper,
                           private val userMarkRepository: UserMarkRepository,
                           private val webCellRepository: WebCellRepository) {
	private val AVERAGE_RADIUS_OF_EARTH_M = 6_371_000

	def isNear(userId: Long, userLon: Double, userLat: Double): Future[Boolean] = {
		val curriedIsNear: (UserMark, Double) => Boolean = isNear(userLon, userLat)
		userMarkRepository.find(userId)
		                  .flatMap {
			                  case Some(mark) =>
				                  webCellRepository.find(mark.lon.floor.toInt, mark.lat.floor.toInt)
				                                   .map(cell => (mark, cell))
			                  case None => throw new UserMarkNotFoundException(s"User $userId not found")
		                  }
		                  .map(tuple => {
			                  val (mark, maybeCell) = tuple
			                  maybeCell match {
				                  case Some(cell) => curriedIsNear(mark, cell.distanceError)
				                  case None => throw new WebCellNotFoundException(s"No cell found")
			                  }
		                  })
	}

	def saveMark(userMark: UserMark): Future[UserMark] = {
		val markSaveAction = userMarkRepository.saveAction(userMark)
		transactionHelper.runWithTransaction(markSaveAction)
	}

	def deleteMark(userId: Long): Future[Boolean] = {
		val deleteAction = userMarkRepository.deleteAction(userId)
		transactionHelper.runWithTransaction(deleteAction)
	}

	def saveCell(webCell: WebCell): Future[WebCell] = {
		val insertAction = webCellRepository.saveAction(webCell)
		transactionHelper.runWithTransaction(insertAction)
	}

	def getStatistics(pointLon: Double, pointLat: Double): Future[Int] =
		userMarkRepository.findAllInCell(pointLon, pointLat)

	private def isNear(userLon: Double, userLat: Double)(userMark: UserMark, distanceError: Double) = {
		val latDistance = (userLat - userMark.lat).toRadians
		val lonDistance = (userLon - userMark.lon).toRadians
		val sinLat = math.sin(latDistance / 2)
		val sinLon = math.sin(lonDistance / 2)
		val haversine = sinLat * sinLat + math.cos(userLat.toRadians) * math.cos(userMark.lat.toRadians) * sinLon * sinLon
		val distance = 2 * math.atan2(math.sqrt(haversine), math.sqrt(1 - haversine))
		(AVERAGE_RADIUS_OF_EARTH_M * distance) <= distanceError
	}
}
