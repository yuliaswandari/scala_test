package repositories

import javax.inject.{Inject, Singleton}
import anorm._
import anorm.SqlParser._
import play.api.db.DBApi
import scala.concurrent.{ExecutionContext, Future}
import models.{Area, SubArea, PersentaseWilayah}

@Singleton
class KunjunganRepository @Inject()(dbApi: DBApi)(implicit ec: ExecutionContext) {
  private val db = dbApi.database("default")

  // Parser untuk membaca hasil query ke dalam Tuple
  val kunjunganParser: RowParser[(String, String, Int)] = {
    get[String]("kecamatan") ~
    get[String]("kelurahan") ~
    get[Int]("jumlah_kunjungan") map {
      case kecamatan ~ kelurahan ~ jumlahKunjungan =>
        (kecamatan, kelurahan, jumlahKunjungan)
    }
  }

  // Parser untuk hasil query persentase wilayah
  val persentaseWilayahParser: RowParser[PersentaseWilayah] = {
    get[String]("value") ~
    get[BigDecimal]("label") map {
      case value ~ label =>
        PersentaseWilayah(value, s"${label.toString} %")
    }
  }

  // Ambil persentase wilayah berdasarkan kabupaten
  def getPersentaseWilayah(id_kabupaten: Long, tgl_awal: String, tgl_akhir: String): List[PersentaseWilayah] = {
    db.withConnection { implicit connection =>
      SQL(
        """SELECT
            kec.nama AS value,
            ROUND((COUNT(*) * 100.0 / (
              SELECT COUNT(*)
              FROM dc_pendaftaran dp2
              JOIN dc_pasien p2 ON p2.id = dp2.id_pasien
              JOIN dc_kelurahan kel2 ON kel2.id = p2.id_kelurahan
              JOIN dc_kecamatan kec2 ON kec2.id = kel2.id_kecamatan
              JOIN dc_kabupaten kab2 ON kab2.id = kec2.id_kabupaten
              WHERE dp2.waktu_daftar BETWEEN {tgl_awal} AND {tgl_akhir}
              AND kab2.id = {id_kabupaten}
            )), 2) AS label
          FROM dc_pendaftaran dp
          JOIN dc_pasien p ON p.id = dp.id_pasien
          JOIN dc_kelurahan kel ON kel.id = p.id_kelurahan
          JOIN dc_kecamatan kec ON kec.id = kel.id_kecamatan
          JOIN dc_kabupaten kab ON kab.id = kec.id_kabupaten
          WHERE dp.waktu_daftar BETWEEN {tgl_awal} AND {tgl_akhir}
          AND kab.id = {id_kabupaten}
          GROUP BY kec.nama, kab.nama
          ORDER BY label DESC"""
      ).on(
        "id_kabupaten" -> id_kabupaten,
        "tgl_awal" -> tgl_awal,
        "tgl_akhir" -> tgl_akhir
      ).as(persentaseWilayahParser.*)
    }
  }

  // Ambil daftar kunjungan dan kelompokkan berdasarkan kecamatan
  def getKunjunganByKabupaten(id_kabupaten: Long, tgl_awal: String, tgl_akhir: String): List[Area] = {
    db.withConnection { implicit connection =>
      val rawData = SQL(
        """SELECT
            kec.nama AS kecamatan,
            kel.nama AS kelurahan,
            COUNT(*) AS jumlah_kunjungan
          FROM dc_pendaftaran dp
          JOIN dc_pasien p ON p.id = dp.id_pasien
          JOIN dc_kelurahan kel ON kel.id = p.id_kelurahan
          JOIN dc_kecamatan kec ON kec.id = kel.id_kecamatan
          JOIN dc_kabupaten kab ON kab.id = kec.id_kabupaten
          WHERE dp.waktu_daftar BETWEEN {tgl_awal} AND {tgl_akhir}
          AND kab.id = {id_kabupaten}
          GROUP BY kecamatan, kelurahan
          ORDER BY kecamatan, jumlah_kunjungan DESC"""
      ).on(
        "id_kabupaten" -> id_kabupaten,
        "tgl_awal" -> tgl_awal,
        "tgl_akhir" -> tgl_akhir
      ).as(kunjunganParser.*)

      // Kelompokkan berdasarkan kecamatan
      rawData.groupBy(_._1).map { case (kecamatan, list) =>
        val subAreas = list.map { case (_, kelurahan, jumlah) => SubArea(kelurahan, jumlah) }
        val totalKunjungan = subAreas.map(_.total).sum
        Area(kecamatan, subAreas, totalKunjungan)
      }.toList
    }
  }

}
