package services.repository

import conf.Database
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class TransactionHelper @Inject()(val configProvider: DatabaseConfigProvider) extends Database[PostgresProfile] {
	import config.profile.api._

	def runWithTransaction(actions: DBIO[_]*): Future[Unit] = {
		db.run(DBIO.seq(actions:_*).transactionally)
	}

	def runWithTransaction[R](action: DBIO[R]): Future[R] = {
		db.run(action.transactionally)
	}
}
