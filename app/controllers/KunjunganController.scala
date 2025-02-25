package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import repositories.{KunjunganRepository, KecamatanRepository, KabupatenRepository}
import models.{Kecamatan, Kabupaten, KunjunganRequest, KunjunganResponse,
  Area, SubArea, ResponseParameter, PersentaseWilayah}
import java.text.SimpleDateFormat
import java.util.{Date, Locale}

@Singleton
class KunjunganController @Inject()(
    cc: ControllerComponents,
    kunjunganRepository: KunjunganRepository,
    kecamatanRepository: KecamatanRepository,
    kabupatenRepository: KabupatenRepository
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // Format didefinisikan dari tipe yang paling dalam terlebih dahulu
  implicit val subAreaFormat: OFormat[SubArea] = Json.format[SubArea]
  implicit val areaFormat: OFormat[Area] = Json.format[Area]
  implicit val persentaseWilayahFormat: OFormat[PersentaseWilayah] = Json.format[PersentaseWilayah]
  implicit val kecamatanFormat: OFormat[Kecamatan] = Json.format[Kecamatan]
  implicit val kabupatenFormat: OFormat[Kabupaten] = Json.format[Kabupaten]
  implicit val responseParameterFormat: OFormat[ResponseParameter] = Json.format[ResponseParameter]
  implicit val kunjunganrequestFormat: OFormat[KunjunganRequest] = Json.format[KunjunganRequest]
  implicit val kunjunganresponseFormat: OFormat[KunjunganResponse] = Json.format[KunjunganResponse]

  // Endpoint utama untuk debugging
  def index = Action {
    Ok("KunjunganController is working!")
  }

  // Ambil data semua kecamatan
  def getKecamatan = Action.async {
    kabupatenRepository.findAll().map { kabupatens =>
      Ok(Json.toJson(kabupatens))
    }
  }

  // Ambil data semua kabupaten
  def getKabupaten = Action.async {
    kabupatenRepository.findAll().map { kabupatens =>
      Ok(Json.toJson(kabupatens))
    }
  }

  // Helper untuk format tanggal
  private def formatDateRange(tgl_awal: String, tgl_akhir: String): String = {
    try {
      val inputFormat = new SimpleDateFormat("yyyy-MM-dd")
      val outputFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"))

      val tgl_awalObj = inputFormat.parse(tgl_awal)
      val tgl_akhirObj = inputFormat.parse(tgl_akhir)

      s"${outputFormat.format(tgl_awalObj)} s.d ${outputFormat.format(tgl_akhirObj)}"
    } catch {
      case e: Exception => s"$tgl_awal s.d $tgl_akhir"
    }
  }

  // API endpoint untuk mengambil data kunjungan dan mengelompokkan hasilnya
  def getKunjungan = Action.async(parse.json) { request =>
    request.body.validate[KunjunganRequest] match {
      case JsSuccess(params, _) =>
        // Ambil data kunjungan berdasarkan kecamatan
        val kunjunganList = kunjunganRepository.getKunjunganByKabupaten(
          params.id_kabupaten, params.tgl_awal, params.tgl_akhir)

        // Ambil data persentase wilayah
        val persentaseWilayahList = kunjunganRepository.getPersentaseWilayah(
          params.id_kabupaten, params.tgl_awal, params.tgl_akhir)

        if (kunjunganList.isEmpty) {
          Future.successful(NotFound(Json.obj("status" -> "error", "message" -> "No data found")))
        } else {
          // Ambil nama kabupaten
          kabupatenRepository.findById(params.id_kabupaten).map { kabupaten =>
            val kabupatenNama = kabupaten.nama

            // Format tanggal untuk parameter waktu
            val formattedDateRange = formatDateRange(params.tgl_awal, params.tgl_akhir)

            // Buat parameter respons
            val responseParam = ResponseParameter(
              tipe = "harian",
              waktu = formattedDateRange,
              kategori = "kelurahan",
              area = kabupatenNama
            )

            // Buat respons lengkap dengan persentase wilayah
            val response = KunjunganResponse(
              status = "OK",
              parameter = responseParam,
              persentase_wilayah = persentaseWilayahList,
              data = kunjunganList
            )

            Ok(Json.toJson(response))
          }.recover {
            case _ => Ok(Json.toJson(
              KunjunganResponse(
                status = "OK",
                parameter = ResponseParameter(
                  tipe = "harian",
                  waktu = formatDateRange(params.tgl_awal, params.tgl_akhir),
                  kategori = "kelurahan",
                  area = "Unknown"
                ),
                persentase_wilayah = persentaseWilayahList,
                data = kunjunganList
              )
            ))
          }
        }
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "error", "message" -> "Invalid JSON format")))
    }
  }
}
