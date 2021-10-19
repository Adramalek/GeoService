package model

import conf.Database
import slick.jdbc.PostgresProfile

trait WebCellTable {
  this: Database[PostgresProfile] =>
  import config.profile.api._

  class GeoWeb(tag: Tag) extends Table[WebCell](tag, "geo_web") {
    def tileX = column[Int]("tile_x")
    def tileY = column[Int]("tile_y")
    def distanceError = column[Double]("distance_error")

    def geoWebPk = primaryKey("geo_web_pk", (tileX, tileY))

    override def * = (tileX, tileY, distanceError) <> (WebCell.tupled, WebCell.unapply)
  }

  val cells = TableQuery[GeoWeb]
}
