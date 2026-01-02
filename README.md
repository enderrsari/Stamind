# Stamind 🧠✨

**Stamind**, kullanıcıların zihinsel sağlıklarını takip etmelerini, duygusal farkındalık geliştirmelerini ve yapay zeka destekli kişisel analizler almalarını sağlayan modern bir Android uygulamasıdır.

![Status](https://img.shields.io/badge/Status-Production%20Ready-success) ![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple) ![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-blue) ![AI](https://img.shields.io/badge/AI-Gemini%202.5%20Flash-orange)

<img width="330" height="840" alt="B" src="https://github.com/user-attachments/assets/2c7ea141-ab3b-427e-a883-16a279525f68" />
<img width="330" height="840" alt="C" src="https://github.com/user-attachments/assets/d55b55ce-07fc-473d-b124-2713e6ba1dd8" />
<img width="330" height="840" alt="A" src="https://github.com/user-attachments/assets/201ebc83-bc40-4f45-87da-d42da4fc4521" />

## 🌟 Temel Özellikler

*   **📝 AI Destekli Günlük:** Yazdığınız günlükler **Gemini 2.5 Flash** modeli tarafından analiz edilir. Uygulama size duygusal durumunuzu, mental dayanıklılık puanınızı (0-100) ve kişiye özel tavsiyeler sunar.
*   **Ruh Hali Takibi:** Günlük modunuzu emojilerle takip edin, haftalık değişim grafikleriyle duygu durumunuzu izleyin.
*   **📊 Detaylı Raporlar:**
    *   **Radar Grafiği:** Zihinsel sağlığınızın 5 boyutunu (Enerji, Sosyal, Odak, Sakinlik, Üretkenlik) görselleştirin.
    *   **Kelime Bulutu:** Günlüklerinizde en sık kullandığınız kelimeleri görün.
    *   **Tema Analizi:** Hayatınızda hangi konuların (İş, Aile, İlişkiler vb.) baskın olduğunu keşfedin.
*   **🔒 Premium Sistem:** Google Play Billing entegrasyonu ile gelişmiş raporlara ve haftalık AI içgörülerine erişim (kullanım snaryolarına göre farklı UI).
*   **🎨 Modern Tasarım:** Jetpack Compose ile geliştirilmiş, akıcı animasyonlar ve özel tipografi (Lexend & Nunito) kullanan şık arayüz.

## 🛠 Teknoloji Yığını (Tech Stack)

Bu proje, modern Android geliştirme standartlarına uygun olarak inşa edilmiştir:

*   **Dil:** Kotlin
*   **Kullanıcı Arayüzü (UI):** Jetpack Compose (Material3)
*   **Mimari:** MVVM (Model-View-ViewModel) + Repository Pattern
*   **Backend & Veritabanı:** Firebase Firestore (Cloud-first)
*   **Kimlik Doğrulama:** Firebase Auth (Google Sign-In)
*   **Yapay Zeka (AI):** Firebase AI SDK (Gemini 2.5 Flash entegrasyonu)
*   **Ödeme Sistemi:** Google Play Billing Library 7.0

## 📂 Proje Yapısı

```
app/src/main/java/com/stamindapp/stamind/
├── auth/            # Firebase Auth & Abonelik yönetimi
├── billing/         # Google Play ödeme işlemleri
├── database/        # Veri modelleri (JournalEntry, MoodEntry)
├── engine/          # AI Servisleri (GeminiService.kt)
├── model/           # ViewModel katmanı (State yönetimi)
├── repository/      # Veri erişim katmanı (Firestore işlemleri)
├── screens/         # Jetpack Compose ekranları
│   ├── HomeActivity.kt
│   ├── JournalActivity.kt
│   ├── ReportsActivity.kt
│   └── ProfileScreen.kt
├── ui/              # Tema, Renkler, Fontlar ve Ortak Bileşenler
└── util/            # Yardımcı sınıflar
```

## 🚀 Kurulum ve Çalıştırma

Bu projeyi yerel makinenizde çalıştırmak için aşağıdaki adımları izleyin:

1.  **Repoyu Klonlayın:**
    ```bash
    git clone https://github.com/enderrsari/stamind.git
    ```

2.  **Firebase Kurulumu 🔥:**
    *   Bu proje Firebase servislerini kullanır.
    *   Kendi Firebase konsolunuzda yeni bir proje oluşturun.
    *   `google-services.json` dosyasını indirip `app/` klasörünün içine yapıştırın.
    *   Firebase konsolunda **Authentication** (Google Sign-In), **Firestore** ve **Vertex AI in Firebase** servislerini etkinleştirin.

3.  **Android Studio ile Açın:**
    *   Projeyi Android Studio'da açın ve Gradle senkronizasyonunun bitmesini bekleyin.

4.  **Çalıştırın:**
    *   Bir emülatör veya fiziksel cihaz seçerek `Run` butonuna basın.
  
<img width="330" height="840" alt="D" src="https://github.com/user-attachments/assets/6f64e38c-11ec-4e5d-a1a0-f7be4a1aa0fd" />
<img width="330" height="840" alt="E" src="https://github.com/user-attachments/assets/0aee199c-136e-4194-8689-1f8821ea2b40" />
      
---
*Ender Sarı tarafından geliştirilmiştir*
