package repositories

import javax.inject.{Inject, Singleton}
import anorm._
import anorm.SqlParser._
import play.api.db.DBApi
import scala.concurrent.{ExecutionContext, Future}
import models.Kecamatan

@Singleton
class KecamatanRepository @Inject()(dbApi: DBApi)(implicit ec: ExecutionContext) {
  private val db = dbApi.database("default")

  // Parser untuk mengubah hasil query menjadi objek Kecamatan
  val kecamatanParser: RowParser[Kecamatan] = {
      get[Long]("id") ~
      get[String]("nama") ~
      get[Long]("id_kabupaten") ~
      get[String]("kode") map {
      case id ~ nama ~ idKabupaten ~ kode => Kecamatan(id, nama, idKabupaten, kode)
    }
  }

  def findAll(): Future[List[Kecamatan]] = Future {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM dc_kecamatan").as(kecamatanParser.*)
    }
  }

}
