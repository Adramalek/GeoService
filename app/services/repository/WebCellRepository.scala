package services.repository

import conf.Database
import model.{WebCell, WebCellTable}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class WebCellRepository @Inject()(val configProvider: DatabaseConfigProvider)
	extends Database[PostgresProfile] with WebCellTable {

	import config.profile.api._

	import scala.concurrent.ExecutionContext.Implicits.global

	val logger: Logger = Logger(this
		.getClass)

	def saveAction(webCell: WebCell): DBIO[WebCell] = {
		val query = findByIdQuery(webCell.tileX, webCell.tileY)
		for {
			exists <- query.exists.result
			result <- if (exists) {
				logger.info(s"Updating cell $webCell")
				query.update(webCell).flatMap { _ =>findByIdQuery(webCell.tileX, webCell.tileY).result.head }
			} else {
				logger.info(s"Inserting new cell: $webCell")
				(cells returning cells.map(cell => cell)) += webCell
			}
		} yield result
	}

	def find(tileX: Int, tileY: Int): Future[Option[WebCell]] = {
		db.run(findByIdQuery(tileX, tileY).map(cell => cell).result.headOption)
	}

	private def findByIdQuery(tileX: Int, tileY: Int) = {
		cells.filter(cell => cell.tileX === tileX && cell.tileY === tileY)
	}
}
