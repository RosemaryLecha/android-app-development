// ─────────────────────────────────────────────────────────────────────────────
// Student.kt  —  Data class + two required functions on it
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Core data class representing a single student and their exam results.
 * Computed properties (average, grade, gpa, status) are derived automatically.
 */
data class Student(
    val name:   String,
    val id:     String,
    val scores: List<Double>
) {
    /** Arithmetic mean of all scores (0.0 if no scores). */
    val average: Double
        get() = if (scores.isEmpty()) 0.0 else scores.sum() / scores.size

    /** Sum of all scores. */
    val total: Double
        get() = scores.sum()

    /** Letter grade based on the average. */
    val letterGrade: String
        get() = assignLetterGrade(average)

    /** GPA points (4.0 scale). */
    val gpa: Double
        get() = letterGradeToGPA(letterGrade)

    /** Simple pass / fail string. */
    val status: String
        get() = if (average >= 60.0) "PASS" else "FAIL"
}

// ── Function 1: Validate a student record ────────────────────────────────────
/**
 * Returns true only when all fields are sensible:
 *  - name and id must be non-blank
 *  - at least one score must exist
 *  - every score must lie in [0, 100]
 *
 * Prints a console warning for each failure so the user knows what was skipped.
 */
fun validateStudent(student: Student): Boolean = when {
    student.name.isBlank() -> {
        println("  ⚠  Validation failed — blank student name, record skipped")
        false
    }
    student.id.isBlank() -> {
        println("  ⚠  Validation failed — '${student.name}' has no student ID, skipped")
        false
    }
    student.scores.isEmpty() -> {
        println("  ⚠  Validation failed — '${student.name}' has no scores, skipped")
        false
    }
    student.scores.any { it < 0.0 || it > 100.0 } -> {
        println("  ⚠  Validation failed — '${student.name}' has a score outside [0-100], skipped")
        false
    }
    else -> true
}

// ── Function 2: Format a student result as a terminal-friendly string ─────────
/**
 * Produces a single aligned line like:
 *
 *   Alice Johnson        | S001       | 92.0, 88.0, 95.0 | Avg:  91.25 | Grade: A | GPA: 4.0 | PASS ★ BEST STUDENT
 */
fun formatStudentResult(student: Student, isBest: Boolean = false): String {
    val star   = if (isBest) "  ★ BEST STUDENT" else ""
    val scores = student.scores.joinToString(", ") { "%.1f".format(it) }
    return "%-22s | %-10s | %-28s | Avg: %6.2f | Grade: %-2s | GPA: %.1f | %-4s%s".format(
        student.name,
        student.id,
        scores,
        student.average,
        student.letterGrade,
        student.gpa,
        student.status,
        star
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Map a numeric average to the standard A–F letter scale. */
fun assignLetterGrade(avg: Double): String = when {
    avg >= 90.0 -> "A"
    avg >= 80.0 -> "B"
    avg >= 70.0 -> "C"
    avg >= 60.0 -> "D"
    else        -> "F"
}

/** Convert a letter grade to the 4.0 GPA scale. */
fun letterGradeToGPA(grade: String): Double = when (grade) {
    "A"  -> 4.0
    "B"  -> 3.0
    "C"  -> 2.0
    "D"  -> 1.0
    else -> 0.0
}
