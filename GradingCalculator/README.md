# 🎓 Kotlin Grading Calculator

A Kotlin/JVM desktop application that reads student score files (CSV, Excel, PDF),
calculates grades, and exports a coloured Excel or CSV report — with the best
student highlighted in gold.

---

## Project Structure

```
GradingCalculator/
├── build.gradle.kts          ← Gradle build + dependencies
├── settings.gradle.kts
├── sample_data/
│   └── students.csv          ← Test file (10 students, 5 subjects)
└── src/main/kotlin/
    ├── Student.kt            ← Data class + validateStudent() + formatStudentResult()
    ├── GradeCalculator.kt    ← Higher-order processStudents(), statistics
    ├── FileProcessor.kt      ← Read CSV/Excel/PDF · Export Excel/CSV
    ├── GradeUI.kt            ← Swing GUI (dark terminal aesthetic)
    └── Main.kt               ← Entry point + all required demonstrations
```

---

## Setup in IntelliJ IDEA

1. **Open IntelliJ** → `File` → `New` → `Project from Existing Sources`
   - OR: `File` → `Open` → select this folder
2. Choose **Gradle** as the build system when prompted
3. Wait for Gradle to sync and download dependencies (needs internet first time)
4. Open `src/main/kotlin/Main.kt` and click the ▶ green run button

> **Alternative:** Run → Edit Configurations → Application → Main class: `MainKt`

---

## How to Use the App

| Step | Action |
|------|--------|
| 1 | Click **📂 Upload File** and select a CSV, Excel (.xlsx/.xls), or PDF |
| 2 | Click **⚙️ Calculate Grades** to process and display results |
| 3 | Click **📊 Export to Excel** or **📄 Export to CSV** to save the report |
| Clear | Resets the session for a new file |

Watch the **terminal pane** (GUI) and the **IntelliJ Run console** simultaneously —
all progress messages appear in both places.

---

## Input File Format

Every supported format must follow this column order:

| Col 0       | Col 1      | Col 2+          |
|-------------|------------|-----------------|
| Student Name | Student ID | Score 1, Score 2, … |

A header row is **automatically detected and skipped** (CSV & Excel).
Scores must be numeric values between **0 and 100**.

---

## Output (Excel Report)

- Title row
- Students **ranked** from highest to lowest average
- ★ Best student row highlighted in **gold**
- PASS rows in **green**, FAIL rows in **red**
- Alternating row shading for readability
- Class statistics & grade distribution table at the bottom

---

## Assignment Requirements — Where to Find Them

| Requirement | Location |
|---|---|
| Data class | `Student.kt` — `data class Student(…)` |
| Function 1 on data class — validation | `Student.kt` — `fun validateStudent(…)` |
| Function 2 on data class — formatting | `Student.kt` — `fun formatStudentResult(…)` |
| Custom higher-order function | `GradeCalculator.kt` — `fun <T> processStudents(…, operation: (Student) -> T)` |
| `map`, `filter`, `forEach`, `groupBy` | `GradeCalculator.kt`, `Main.kt` |
| Lambda passed to higher-order function | `Main.kt` — `calculator.processStudents(students) { student -> … }` |
| Collection filter demo | `Main.kt` — sections 3 |
| GUI | `GradeUI.kt` |
| Terminal output while running | `GradeUI.log()` + `println()` in every handler |

---

## Dependencies (auto-downloaded by Gradle)

| Library | Purpose |
|---|---|
| `org.apache.poi:poi-ooxml:5.2.3` | Read & write `.xlsx` / `.xls` |
| `com.opencsv:opencsv:5.7.1` | Read & write `.csv` |
| `org.apache.pdfbox:pdfbox:2.0.29` | Extract text from `.pdf` |

---

## Grade Scale

| Average | Grade | GPA |
|---------|-------|-----|
| 90 – 100 | A | 4.0 |
| 80 – 89 | B | 3.0 |
| 70 – 79 | C | 2.0 |
| 60 – 69 | D | 1.0 |
| 0 – 59 | F | 0.0 |
