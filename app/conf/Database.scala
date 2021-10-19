package conf

import play.api.db.slick.DatabaseConfigProvider
import slick.basic.{BasicProfile, DatabaseConfig}

trait Database[P <: BasicProfile] {
  val configProvider: DatabaseConfigProvider
  val config: DatabaseConfig[P] = configProvider.get[P]
  val db: P#Backend#Database = config.db
}
