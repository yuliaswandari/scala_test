# scala_test

## Task
Membuat Web Service API dengan output JSON yang berisi laporan kunjungan pasien dari **1 Januari 2022 - 31 Oktober 2022** untuk Kabupaten **Banjarnegara, Jawa Tengah**, dengan ketentuan:  
- **Menampilkan data kelurahan** yang dikelompokkan berdasarkan kecamatannya.  
- **Menampilkan persentase jumlah kunjungan** tiap-tiap kecamatan.  

Dikerjakan menggunakan **Play Framework**, **Anorm**, dan **MySQL**.

---

## Langkah-langkah Pengerjaan

### 1. Setup MySQL
- Menggunakan **MySQL 5.7** dalam **Docker Container**.
- Menghubungkan database MySQL dan melakukan import data.

### 2. Query SQL yang Digunakan

#### **List Kunjungan per Kelurahan**
```sql
SELECT
    kec.nama AS kecamatan,
    kel.nama AS kelurahan,
    COUNT(*) AS jumlah_kunjungan
FROM dc_pendaftaran dp
JOIN dc_pasien p ON p.id = dp.id_pasien
JOIN dc_kelurahan kel ON kel.id = p.id_kelurahan
JOIN dc_kecamatan kec ON kec.id = kel.id_kecamatan
JOIN dc_kabupaten kab ON kab.id = kec.id_kabupaten
WHERE dp.waktu_daftar BETWEEN '2022-01-01 00:00:00' AND '2022-10-31 23:59:59'
AND kab.id = 10
GROUP BY kecamatan, kelurahan
ORDER BY kecamatan, jumlah_kunjungan DESC;
```

#### **Persentase Kunjungan per Kecamatan**
```sql
SELECT
    kec.nama AS value,
    ROUND((COUNT(*) * 100.0 / (
        SELECT COUNT(*)
        FROM dc_pendaftaran dp2
        JOIN dc_pasien p2 ON p2.id = dp2.id_pasien
        JOIN dc_kelurahan kel2 ON kel2.id = p2.id_kelurahan
        JOIN dc_kecamatan kec2 ON kec2.id = kel2.id_kecamatan
        JOIN dc_kabupaten kab2 ON kab2.id = kec2.id_kabupaten
        WHERE dp2.waktu_daftar BETWEEN '2022-01-01 00:00:00' AND '2022-10-31 23:59:59'
        AND kab2.id = 10
    )), 2) AS label
FROM dc_pendaftaran dp
JOIN dc_pasien p ON p.id = dp.id_pasien
JOIN dc_kelurahan kel ON kel.id = p.id_kelurahan
JOIN dc_kecamatan kec ON kec.id = kel.id_kecamatan
JOIN dc_kabupaten kab ON kab.id = kec.id_kabupaten
WHERE dp.waktu_daftar BETWEEN '2022-01-01 00:00:00' AND '2022-10-31 23:59:59'
AND kab.id = 10
GROUP BY kec.nama, kab.nama
ORDER BY label DESC;
```

### 3. Setup Project Play Framework  
- Membuat project Play Framework menggunakan **Anorm**.  
- Membuat endpoint API untuk **Kabupaten** dan **Kecamatan** sebagai percobaan.  
- Membuat endpoint API **Kunjungan** sesuai format JSON yang diberikan.  
- Melakukan pengujian API menggunakan **Postman**.

---

## Langkah-langkah Menjalankan Aplikasi

### **1. Menjalankan MySQL 5.7 di Docker**
- Jika belum ada, jalankan:
  ```sh
  docker run --name mysql57 -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql:5.7
  ```
- Jika sudah ada, jalankan:
  ```sh
  docker start mysql57
  ```

### **2. Import Database**
- Jalankan **SQL file** yang ada di folder `database`.

### **3. Menjalankan Aplikasi**
```sh
sbt run
```

### **4. Menggunakan Postman untuk Testing API**
- **Import Postman Collection** dari folder `postman`.  
- **Test API "Kunjungan Tiap Kelurahan"** dengan body JSON berikut:
```json
{
  "id_kabupaten": 10,
  "tgl_awal": "2022-01-01 00:00:00",
  "tgl_akhir": "2022-10-31 23:59:59"
}
```

---

## **Author**
👩‍💻 **Yulia Swandari**  
📌 **Tech Stack:** Scala, Play Framework, Anorm, MySQL, Docker  

---

