# TaskFlow — Görev & Proje Yönetimi REST API

TaskFlow, ekiplerin projeler oluşturup bu projeler altında görev (task) yönetebildiği; rol tabanlı yetkilendirme (RBAC), yorum, checklist, dosya eki ve bildirim gibi özellikleri barındıran bir Spring Boot REST API projesidir. 8 haftalık staj programı kapsamında geliştirilmiştir.

## İçindekiler
- [Teknolojiler](#teknolojiler)
- [Özellikler](#özellikler)
- [Mimari & ER Diagram](#mimari--er-diagram)
- [Kurulum](#kurulum)
- [Ortam Değişkenleri](#ortam-değişkenleri)
- [API Dokümantasyonu (Swagger)](#api-dokümantasyonu-swagger)
- [Postman Collection](#postman-collection)
- [Testler](#testler)
- [Bilinen Sınırlamalar](#bilinen-sınırlamalar)

## Teknolojiler

| Katman | Teknoloji |
|---|---|
| Dil / Platform | Java 21 |
| Framework | Spring Boot 3.5.16 |
| Güvenlik | Spring Security + JWT (jjwt 0.12.7) |
| Veritabanı | PostgreSQL 15 |
| ORM | Spring Data JPA / Hibernate |
| Dokümantasyon | springdoc-openapi (Swagger UI) |
| Test | JUnit 5, Mockito |
| Konteynerleştirme | Docker, Docker Compose |
| E-posta | Spring Mail (Mailtrap sandbox — password reset) |

## Özellikler

- **Auth**: Kayıt, giriş, JWT üretimi, refresh token, logout, şifre sıfırlama (e-posta ile), başarısız giriş denemesi sonrası hesap kilitleme.
- **Project**: CRUD, sahiplik (owner), üyelik sistemi, isme göre arama, duruma göre filtreleme, oluşturulma tarihine göre sıralama, proje istatistikleri (task sayıları).
- **Task**: CRUD, alt görev (subtask) desteği, görev kopyalama (duplicate), status/priority güncelleme, atama (assignee), status geçmişi, soft delete.
- **RBAC**: Proje bazlı OWNER / EDITOR / VIEWER rolleri. VIEWER; task, comment, checklist ve attachment üzerinde yazma işlemi yapamaz.
- **Comment**: Görevlere yorum ekleme/listeleme, sadece yazar veya proje sahibi silebilir.
- **Checklist (CheckItem)**: Görev altında alt madde (checklist) oluşturma/güncelleme/tamamlama/silme.
- **Attachment**: Görevlere dosya yükleme (uzantı + content-type whitelist, 10 MB üst sınır), indirme, silme (sahibi veya yükleyen).
- **Label**: Etiket oluşturma/listeleme, görevlere etiket ekleme/çıkarma, etikete göre filtreleme.
- **Filtreleme / Sayfalama**: Status, priority, assignee, label, tarih (tekil eşleşme), arama; `Pageable` ile sayfalama ve sıralama.
- **Notification**: Bildirim listeleme, okunmamış sayısı, tümünü okundu işaretleme.
- **Swagger / OpenAPI**: Tüm endpointler `bearerAuth` ile işaretli, `/swagger-ui.html` üzerinden interaktif dokümantasyon.

## Mimari & ER Diagram

Varlıklar arası ilişkiler için bkz. [`ER_DIAGRAM.md`](./ER_DIAGRAM.md) (Mermaid formatında, GitHub üzerinde otomatik render edilir).

Kısaca:
- Bir `User`, birden çok `Project`'e sahip olabilir veya üye olabilir (`ProjectMember` ile rol bilgisi tutulur: OWNER/EDITOR/VIEWER).
- Bir `Project`, birden çok `Task` içerir; her `Task`'ın `Comment`, `CheckItem`, `Attachment` ve `Label` ilişkileri vardır.
- `Task` kendi kendine referans verir (`parentTask`) — subtask ve duplicate task için kullanılır.

## Kurulum

### 1) Docker Compose ile (önerilen)

```bash
docker compose up --build
```

Bu komut PostgreSQL ve uygulamayı ayağa kaldırır. Uygulama `http://localhost:8080` üzerinden erişilebilir olur.

### 2) Yerel geliştirme ortamında

Gereksinimler: Java 21, Maven, çalışan bir PostgreSQL instance'ı.

```bash
# .env veya application-local.properties içinde gerekli değişkenleri tanımlayın (bkz. Ortam Değişkenleri)
./mvnw clean install
./mvnw spring-boot:run
```

Testleri çalıştırmak için:

```bash
./mvnw test
```

## Ortam Değişkenleri

| Değişken | Açıklama | Örnek |
|---|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL bağlantı adresi | `jdbc:postgresql://localhost:5432/taskflow` |
| `SPRING_DATASOURCE_USERNAME` | DB kullanıcı adı | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | DB şifresi | `postgrespassword` |
| `JWT_SECRET` | JWT imzalama anahtarı (Base64/hex, yeterince uzun) | — |
| `FILE_UPLOAD_DIR` | Dosya eklerinin saklanacağı klasör | `/app/uploads` |
| `spring.mail.*` | Şifre sıfırlama e-postaları için SMTP ayarları (varsayılan: Mailtrap sandbox) | — |

> **Not:** `JWT_SECRET` ve mail bilgileri gibi hassas değerleri repoya committ etmeyin; `application-local.properties` veya ortam değişkenleri üzerinden yönetin.

## API Dokümantasyonu (Swagger)

Uygulama ayağa kalktıktan sonra:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Tüm endpointler JWT (`bearerAuth`) ile korunur; Swagger UI üzerinden "Authorize" butonuna `Bearer <token>` girerek test edebilirsiniz.

## Postman Collection

Proje kök dizinindeki [`TaskFlow.postman_collection.json`](./TaskFlow.postman_collection.json) dosyasını Postman'e import edin. Koleksiyon haftalara göre klasörlenmiştir (Project CRUD, Task, Auth, Ownership/RBAC, Assignment/Comments, Filter/Sort/Pagination, Label/Swagger, Extension/Polish).

Kullanmadan önce koleksiyon değişkenlerini (`baseUrl`, token değişkenleri vb.) ortamınıza göre güncelleyin. Login/Register isteklerini çalıştırarak token değişkenlerinin otomatik doldurulmasını sağlayabilirsiniz.

## Testler

61 adet JUnit/Mockito birim testi mevcuttur; servis katmanındaki iş kurallarını (auth, ownership, RBAC, soft delete, password reset, refresh token, label, notification vb.) kapsar.

```bash
./mvnw test
```

## Bilinen Sınırlamalar

Aşağıdaki noktalar staj kılavuzunda "genişletme / opsiyonel" olarak değerlendirilmiş, zorunlu teslim kriterlerini etkilememektedir:

- Proje arama şu an yalnızca isme göre çalışır; `tag` alanına göre filtreleme desteklenmemektedir.
- Görev tarih filtresi tekil (`dueDate=YYYY-MM-DD`) eşleşme şeklindedir; `startDate`/`endDate` aralık filtresi henüz desteklenmemektedir.
- Proje sıralaması yalnızca `createdAt` alanına göre yapılabilmektedir.

---

**Geliştirici:** Emel Ece Gizli
**Staj Programı:** 8 Haftalık Backend Staj Projesi — TaskFlow
