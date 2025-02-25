package models

case class Kecamatan(
    id: Long,
    nama: String,
    idKabupaten: Long,
    kode: String
)

case class Kabupaten(
    id: Long,
    idProvinsi: Long,
    nama: String
)

case class KunjunganRequest(
    id_kabupaten: Long,
    tgl_awal: String,
    tgl_akhir: String
)

case class SubArea(
    nama: String,
    total: Int
)

case class Area(
    area: String,
    sub_area: List[SubArea],
    total: Int
)

case class ResponseParameter(
    tipe: String,
    waktu: String,
    kategori: String,
    area: String
)

case class PersentaseWilayah(
    value: String,
    label: String
)

case class KunjunganResponse(
    status: String,
    parameter: ResponseParameter,
    persentase_wilayah: List[PersentaseWilayah],
    data: List[Area]
)
