package services.repository

import conf.Database
import model.{UserMark, UserMarkTable, WebCellTable}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class UserMarkRepository @Inject()(val configProvider: DatabaseConfigProvider)
  extends Database[PostgresProfile] with UserMarkTable with WebCellTable {
  import config.profile.api._

  import scala.concurrent.ExecutionContext.Implicits.global

  val logger: Logger = Logger(this.getClass)

  def find(userId: Long): Future[Option[UserMark]] = {
    db.run(findByIdQuery(userId).map(mark => mark).result.headOption)
  }

  def saveAction(userMark: UserMark): DBIO[UserMark] = {
    val insertAction = (mark: UserMark) => {
      logger.info(s"Inserting new mark: $mark")
      val action = (userMarks returning userMarks.map(_.userId)
                                   into ((m, id) => m.copy(userId = Some(id)))) += mark
      action
    }

    userMark.userId.map(id => {
      val query = findByIdQuery(id)
      for {
        exists <- query.exists.result
        result <- if (exists) {
          logger.info(s"Updating mark: $userMark")
          query.update(userMark).flatMap { _ => findByIdQuery(userMark.userId.get).result.head }
        } else {
          insertAction(userMark)
        }
      } yield result
    }) getOrElse insertAction(userMark)
  }

  def deleteAction(userId: Long): DBIO[Boolean] = {
    logger.info(s"Trying to delete user: $userId")
    findByIdQuery(userId).delete map { _ > 0 }
  }

  def findAllInCell(lon: Double, lat: Double): Future[Int] = {
    val query = userMarks.join(cells).on((mark, cell) => cell.tileX === mark.lon.floor.asColumnOf[Int] &&
                                                         cell.tileY === mark.lat.floor.asColumnOf[Int])
                         .filter(tuple => tuple._2.tileX === lon.floor.toInt && tuple._2.tileY === lat.floor.toInt)
                         .map(tuple => tuple._1)
    db.run(query.length.result)
  }

  private def findByIdQuery(userId: Long) = {
    userMarks.filter(_.userId === userId)
  }
}
