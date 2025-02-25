package repositories

import javax.inject.{Inject, Singleton}
import anorm._
import anorm.SqlParser._
import play.api.db.DBApi
import scala.concurrent.{ExecutionContext, Future}
import models.Kabupaten

@Singleton
class KabupatenRepository @Inject()(dbapi: DBApi)(implicit ec: ExecutionContext) {
  private val db = dbapi.database("default")

  val kabupatenParser: RowParser[Kabupaten] = {
      get[Long]("id") ~
      get[Long]("id_provinsi") ~
      get[String]("nama") map {
      case id ~ idProvinsi ~ nama => Kabupaten(id, idProvinsi, nama)
    }
  }

  def findAll(): Future[List[Kabupaten]] = Future {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM dc_kabupaten").as(kabupatenParser.*)
    }
  }

   def findById(id: Long): Future[Kabupaten] = Future {
    db.withConnection { implicit connection =>
      SQL("SELECT id, id_provinsi, nama FROM dc_kabupaten WHERE id = {id}")
        .on("id" -> id)
        .as(kabupatenParser.single)
    }
  }

}
