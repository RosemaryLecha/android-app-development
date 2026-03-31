# android-app-development
Android Application Development coursework — Kotlin projects &amp; assignments


Course: Android Application Development
Language: Kotlin  
IDE: IntelliJ IDEA / Android Studio  
Student:Rose Mary Lecha Ngwa Mbenoh
Institution: The ICT University

---

##  Assignments

| # | Assignment | Topics Covered | Status |
|---|-----------|----------------|--------|
| 01 | [Grading Calculator](./Assignment-01-GradingCalculator) | Data classes, Higher-order functions, File I/O, Swing GUI | ✅ Done |
| 02 | ICTAttend | - | In Process |

---

## 🛠️ Technologies Used
- Kotlin 1.9
- Java 17
- Apache POI (Excel)
- OpenCSV
- PDFBox
- Swing GUI

---

## 🚀 How to Run Any Assignment
1. Clone this repo
2. Open the assignment folder in IntelliJ
3. Let Gradle sync
4. Run `Main.kt`


# ICTU Attend 🎓
**The Ultimate Fraud-Proof Attendance Ecosystem for The ICT University**

ICT Attend is a high-security, management solution designed specifically for the academic rigors of **The ICT University**. It replaces outdated, cheat-prone paper registers with a sophisticated "Zero-Trust" verification engine.

---

## 🚀 The Value Proposition (Selling ICT Attend)

In an environment where academic integrity is paramount, **ICT Attend** stands as the definitive barrier against attendance fraud. We have engineered a "Two-Factor" verification process that ensures every "Present" mark is earned.

### 🛡️ Unmatched Security Infrastructure
*   **Dynamic QR Sync:** Unlike static codes that can be photographed and shared, ICT Attend generates **Dynamic QR Tokens** that refresh every **30 seconds**. By the time a student tries to send a photo to a friend, the code is already obsolete.
*   **Precision Geofencing:** We leverage high-accuracy GPS providers to ensure students are physically within a **70-meter radius** of the classroom. If you aren't in the room, the scan is rejected—period.

### 👨‍🏫 Administrative Excellence for Lecturers
*   **Live Command Center:** Monitor your classroom's density in real-time with a live-updating dashboard showing Present vs. Absent counts.
*   **Professional Report Generation:** Export full attendance sheets into industry-standard **Excel (.xlsx)** and **CSV** formats with a single tap. Reports are pre-formatted with bold headers and auto-sized columns, ready for immediate submission.
*   **Manual Override:** Full control remains with the lecturer, allowing for manual attendance marking in exceptional cases.

### 🎓 Student Success & Transparency
*   **75% Exam Eligibility Tracker:** Students get a real-time visualization of their attendance percentage. The app triggers a red warning if they fall below the 75% threshold required for exams.
*   **Persistent Notifications:** A smart notification tab ensures students never miss a "Class is starting" alert or an enrollment confirmation. Records stay saved until the user chooses to remove them.

---

## 🛠️ Professional Tooling & Tech Stack

ICT Attend is built using the latest industry-standard tools to ensure performance, scalability, and security:

| Category | Tools & Technologies |
| :--- | :--- |
| **Language** | **Kotlin** (Modern, type-safe, and concise) |
| **UI Framework** | **Jetpack Compose** (Declarative UI for smooth, modern experiences) |
| **Backend** | **Firebase** (Auth for identity, Firestore for real-time sync, Storage for media) |
| **Dependency Injection** | **Hilt / Dagger** (Clean Architecture and modularity) |
| **QR Engine** | **Google ML Kit** (Scanning) & **ZXing** (Dynamic Generation) |
| **Location Services** | **Google Play Services Location** (High-precision Geofencing) |
| **Reporting Engine** | **Apache POI 5.2.5** (Professional Excel/OOXML processing) |
| **Image Loading** | **Coil** (Optimized asynchronous image loading) |

---

## 🏗️ Technical Architecture

The project follows the **Clean Architecture** pattern combined with **MVVM (Model-View-ViewModel)** to ensure a separation of concerns:
*   **Data Layer:** Handles Firestore repositories, Firebase Auth, and local DataStore.
*   **Domain Layer:** Contains business logic (Use Cases) for attendance validation and GPS calculations.
*   **UI Layer:** Reactive UI components built with Jetpack Compose that observe StateFlows from ViewModels.

---

## 📊 Deployment & Setup

1.  **Firebase:** Add `google-services.json` to the `app/` folder.
2.  **Permissions:** The app requires `CAMERA` and `ACCESS_FINE_LOCATION` to function.
3.  **Environment:** Built for Android 8.0 (API 26) and above.

---

## 🤝 Developed By
**The ICT Attend Development Team**  
*Optimizing Academic Integrity at The ICT University.*

---
*Distributed under the MIT License. Created as part of the Level 3 Android Application Development Course.*

```

---

