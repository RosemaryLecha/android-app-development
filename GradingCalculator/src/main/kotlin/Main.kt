// ─────────────────────────────────────────────────────────────────────────────
// Main.kt  —  Entry point
//
//  Demonstrates:
//  ① Lambda passed to a custom higher-order function (processStudents)
//  ② Collection operations: filter, map, forEach, groupBy
//  ③ Data class with two functions (validateStudent, formatStudentResult)
//  Then launches the Swing GUI.
// ─────────────────────────────────────────────────────────────────────────────

fun main() {
    println("═══════════════════════════════════════════════════════════════")
    println("   KOTLIN GRADING CALCULATOR  —  CONSOLE DEMONSTRATION")
    println("═══════════════════════════════════════════════════════════════\n")

    // ── Demo dataset ──────────────────────────────────────────────────────────
    val rawStudents = listOf(
        Student("Alice Johnson",  "S001", listOf(92.0, 88.0, 95.0, 91.0)),
        Student("Bob Smith",      "S002", listOf(74.0, 68.0, 72.0, 77.0)),
        Student("Carol Williams", "S003", listOf(55.0, 60.0, 48.0, 52.0)),
        Student("David Brown",    "S004", listOf(85.0, 90.0, 82.0, 88.0)),
        Student("Eva Martinez",   "S005", listOf(98.0, 97.0, 99.0, 96.0)),
        Student("Frank Lee",      "S006", listOf(63.0, 70.0, 66.0, 61.0)),
        // ── Intentionally invalid records ────────────────────────────────────
        Student("",               "S007", listOf(80.0, 85.0)),         // blank name
        Student("Ghost User",     "",     listOf(75.0, 80.0)),         // blank id
        Student("Bad Scores",     "S009", listOf(-5.0, 105.0)),        // out of range
    )

    // =========================================================================
    // 1.  FUNCTION ON DATA CLASS — validateStudent()
    // =========================================================================
    println("── 1. VALIDATION (validateStudent) ─────────────────────────────")
    val students = rawStudents.filter { validateStudent(it) }
    println("Result: ${students.size} valid / ${rawStudents.size} total\n")

    // =========================================================================
    // 2.  CUSTOM HIGHER-ORDER FUNCTION + LAMBDA
    // =========================================================================
    println("── 2. CUSTOM HIGHER-ORDER FUNCTION (processStudents + lambda) ──")
    val calculator = GradeCalculator()

    // ↓  Lambda is passed to the custom higher-order function
    val best      = calculator.getBestStudent(students)
    val formatted = calculator.processStudents(students) { student ->
        formatStudentResult(student, student == best)   // ← lambda body
    }
    formatted.forEach { println(it) }
    println()

    // =========================================================================
    // 3.  COLLECTION OPERATIONS
    // =========================================================================
    println("── 3. COLLECTION OPERATIONS ─────────────────────────────────────")

    // filter — passing students (avg ≥ 60)
    val passing = students.filter { it.average >= 60.0 }
    println("filter  → Passing students (${passing.size}):")
    passing.forEach { println("    ${it.name}  avg=${"%.2f".format(it.average)}") }
    println()

    // filter — failing
    val failing = students.filter { it.average < 60.0 }
    println("filter  → Failing students (${failing.size}):")
    if (failing.isEmpty()) println("    (none)")
    failing.forEach { println("    ${it.name}  avg=${"%.2f".format(it.average)}") }
    println()

    // map — build a "Name → Grade" summary list
    val gradeSummary = students.map { "${it.name}  →  ${it.letterGrade}" }
    println("map     → Grade summary:")
    gradeSummary.forEach { println("    $it") }
    println()

    // forEach — list students needing attention (grade D or F)
    println("forEach → At-risk students (Grade D or F):")
    students.filter { it.letterGrade in listOf("D", "F") }
        .forEach { println("    ⚠  ${it.name} (${it.letterGrade})  avg=${"%.2f".format(it.average)}") }
    println()

    // =========================================================================
    // 4.  FUNCTION ON DATA CLASS — formatStudentResult()
    // =========================================================================
    println("── 4. FORMATTING FUNCTION (formatStudentResult) ─────────────────")
    best?.let { println(formatStudentResult(it, isBest = true)) }
    println()

    // =========================================================================
    // 5.  STATISTICS
    // =========================================================================
    println("── 5. CLASS STATISTICS ───────────────────────────────────────────")
    val stats = calculator.getStatistics(students)
    println("  Total students : ${stats.totalStudents}")
    println("  Class average  : ${"%.2f".format(stats.classAverage)}")
    println("  Highest average: ${"%.2f".format(stats.highestScore)}")
    println("  Lowest average : ${"%.2f".format(stats.lowestScore)}")
    println("  Passing        : ${stats.passingCount}")
    println("  Failing        : ${stats.failingCount}")
    println("  Grade distribution:")
    stats.gradeDistribution.entries.sortedBy { it.key }
        .forEach { (g, c) -> println("    Grade $g  →  $c student(s)") }
    best?.let {
        println("\n  ★ Best student : ${it.name}  " +
                "(Avg: ${"%.2f".format(it.average)}, Grade: ${it.letterGrade})")
    }

    println("\n═══════════════════════════════════════════════════════════════")
    println("   Launching GUI…")
    println("═══════════════════════════════════════════════════════════════\n")

    // ── Launch Swing GUI on the Event Dispatch Thread ─────────────────────────
    javax.swing.SwingUtilities.invokeLater {
        javax.swing.UIManager.setLookAndFeel(
            javax.swing.UIManager.getSystemLookAndFeelClassName()
        )
        GradeUI()
    }
}
