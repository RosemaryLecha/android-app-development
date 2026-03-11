// ─────────────────────────────────────────────────────────────────────────────
// GradeCalculator.kt  —  Core logic, higher-order functions, statistics
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Snapshot of the class-wide statistics produced after grading.
 */
data class GradeStatistics(
    val classAverage:       Double           = 0.0,
    val highestScore:       Double           = 0.0,
    val lowestScore:        Double           = 0.0,
    val totalStudents:      Int              = 0,
    val passingCount:       Int              = 0,
    val failingCount:       Int              = 0,
    val gradeDistribution:  Map<String, Int> = emptyMap()
)

class GradeCalculator {

    // ── Higher-order function ─────────────────────────────────────────────────
    /**
     * Applies an arbitrary [operation] (lambda) to every student and collects
     * the results.  This is the "custom higher-order function" requirement.
     *
     * Example usage:
     *   val lines = calculator.processStudents(students) { formatStudentResult(it) }
     */
    fun <T> processStudents(
        students:  List<Student>,
        operation: (Student) -> T
    ): List<T> = students.map(operation)

    // ── Filtering helpers ─────────────────────────────────────────────────────

    /** Returns only the students whose average is ≥ 60. */
    fun getPassingStudents(students: List<Student>): List<Student> =
        students.filter { it.average >= 60.0 }

    /** Returns only the students whose average is < 60. */
    fun getFailingStudents(students: List<Student>): List<Student> =
        students.filter { it.average < 60.0 }

    // ── Best student ──────────────────────────────────────────────────────────

    /** Returns the student with the highest average, or null if list is empty. */
    fun getBestStudent(students: List<Student>): Student? =
        students.maxByOrNull { it.average }

    // ── Ranked list ───────────────────────────────────────────────────────────

    /** Returns a new list sorted from highest to lowest average. */
    fun rankStudents(students: List<Student>): List<Student> =
        students.sortedByDescending { it.average }

    // ── Class-wide statistics ─────────────────────────────────────────────────

    /**
     * Computes a [GradeStatistics] snapshot for a given list of students.
     * Uses map / groupBy / count internally (collection operations).
     */
    fun getStatistics(students: List<Student>): GradeStatistics {
        if (students.isEmpty()) return GradeStatistics()
        val averages = students.map { it.average }          // map
        return GradeStatistics(
            classAverage      = averages.average(),
            highestScore      = averages.max(),
            lowestScore       = averages.min(),
            totalStudents     = students.size,
            passingCount      = students.count { it.average >= 60.0 },  // filter+count
            failingCount      = students.count { it.average <  60.0 },
            gradeDistribution = students
                .groupBy { it.letterGrade }                 // groupBy
                .mapValues { it.value.size }
        )
    }
}
