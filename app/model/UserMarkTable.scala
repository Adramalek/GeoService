package model

import conf.Database
import slick.jdbc.PostgresProfile

trait UserMarkTable {
  this: Database[PostgresProfile] =>
  import config.profile.api._

  class UserMarks(tag: Tag) extends Table[UserMark](tag, Some("public"),"user_marks") {
    def userId = column[Long]("user_id", O.PrimaryKey, O.AutoInc)
    def lon = column[Double]("lon")
    def lat = column[Double]("lat")

    def coordinatesIndex = index("user_marks__coordinates_nui", (lon, lat), unique = false)

    override def * = (userId.?, lon, lat) <> (UserMark.tupled, UserMark.unapply)
  }

  val userMarks = TableQuery[UserMarks]
}
