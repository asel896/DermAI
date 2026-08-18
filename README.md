# DermAI 🧴✨ | AI-Supported Cosmetic Ingredient & Skin Analysis Assistant

**DermAI**, kozmetik ürünlerin (güneş kremleri, nemlendiriciler, serumlar vb.) arkasında yer alan karmaşık içerik listelerini (**INCI** - *International Nomenclature of Cosmetic Ingredients*) **OCR** (Görsel Metin Okuma) ve **Yapay Zekâ** (Multimodal Vision AI / LLM) teknolojileriyle analiz eden kişiselleştirilmiş bir mobil ve arka uç ekosistemidir.

Kullanıcıların mobil cihaz kamerasıyla çektikleri veya galerilerinden yükledikleri cilt fotoğraflarını güvenli bir şekilde sunucuya iletir. Hibrit yapay zekâ mimarisi sayesinde anlık olarak lezyon türü tahmini, risk skoru ve doğal dilde yazılmış anlaşılır analiz raporları sunar.

Kullanıcıların cilt tiplerine (*yağlı, kuru, karma, hassas*) ve özel durumlarına (*akne, rozasea, hamilelik, alerjen hassasiyetleri*) göre içerikleri çapraz kontrole alarak komedojenik (gözenek tıkama), iritasyon ve alerjen risklerini hesaplar; anlaşılır skorlar ve renk kodlu öneriler sunar.

> ⚠️ **Yasal Uyarı / Disclaimer:** DermAI bir tıbbi teşhis cihazı veya doktor ikamesi değildir. Yalnızca bilgilendirme ve ön farkındalık oluşturma amacıyla geliştirilmiş bir yapay zekâ destekli karar destek aracıdır.

🔄 Detaylı Çalışma Mantığı (Sistem Akışı)
# Cilt Profili Oluşturma: Kullanıcı ilk girişte cilt tipini, hedeflerini, alerjen hassasiyetlerini ve hamilelik/emzirme gibi özel durumlarını tanımlar.
# Kamera / Metin Tarama: CameraX ve Google ML Kit OCR altyapısı ile kozmetik ambalajındaki kimyasal içerik listesi taranır veya elle girilir.
# Ktor Backend İşleme: Taranan metin Ktor sunucusuna iletilir. Sunucu, metni ayrıştırarak INCI kimyasal veritabanı ile eşleştirir.
# AI Uyum Analizi: Yapay zekâ motoru, taranan içerikleri kullanıcının kişisel cilt profiliyle çapraz kontrole alarak risk skorlarını (komedojenik, iritasyon, hassasiyet) hesaplar.
# İnteraktif Sonuç: Ekran üzerinde Custom Canvas ile çizdirilmiş yuvarlatılmış uyum skoru ve renk kodlu (Yeşil/Sarı/Kırmızı) içerik analizi kartları sunulur.
---

## 🛠️ Mimari ve Teknolojik Yapı (Tech Stack)

DermAI, istemci (Android) ve sunucu (Backend) bileşenlerinin tek bir depoda yönetildiği **Monorepo** mimarisine sahiptir:

### 📱 Android Client (İstemci)
* **Dil:** Kotlin
* **Arayüz:** Jetpack Compose & Material Design 3
* **Mimari:** MVVM (Model-View-ViewModel) + Clean Architecture
* **Ağ & API:** Retrofit / OkHttp, Coroutines & Flow
* **Görsel İşleme:** CameraX & Coil

### ⚙️ Backend (Sunucu)
* **Çerçeve:** Kotlin Ktor Framework (Asenkron & Yüksek Performans)
* **Servis Mimarısı:** RESTful API
* **AI & LLM Entegrasyonu:** TensorFlow / PyTorch Model Servisi & Gemini AI (Doğal dil yorumlama)
* **Veritabanı / Cache:** PostgreSQL / Redis (Geliştirme sürecine göre)

---

## 📂 Proje Klasör Hiyerarşisi

```text
DermAI/
├── .gitignore
├── LICENSE
├── README.md
├── android/              # Native Android Mobil Uygulama
│   ├── app/              # Uygulama Modülü
│   │   └── src/main/java/com/dermai/
│   │       ├── data/     # Repository implementations, DTO'lar, Local/Remote Data Sources
│   │       ├── domain/   # Use Cases, Domain Modelleri, Repository Arayüzleri
│   │       └── presentation/ # Compose UI, ViewModels, States, Intents (MVI)
│   ├── build.gradle.kts
│   └── settings.gradle.kts
└── backend/              # Ktor REST API & AI Engine
    ├── src/main/kotlin/com/dermai/
    │   ├── api/          # Route & Controller Tanımları
    │   ├── db/           # Exposed ORM & PostgreSQL Veritabanı Modelleri
    │   ├── services/     # AI Prompt Engine, INCI Chemical Parsing Logic
    │   └── Application.kt # Sunucu Giriş Noktası (Netty Engine)
    └── build.gradle.kts




