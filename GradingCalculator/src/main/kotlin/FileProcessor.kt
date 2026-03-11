// ─────────────────────────────────────────────────────────────────────────────
// FileProcessor.kt  —  Read CSV / Excel / PDF  ·  Export Excel / CSV reports
// ─────────────────────────────────────────────────────────────────────────────

import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileReader
import java.io.FileWriter

/**
 * Stateless object that handles all file I/O.
 * The [log] callback lets callers (GUI or console) receive progress messages.
 *
 * Input format expected for every file type:
 *   Column 0 : Student Name
 *   Column 1 : Student ID
 *   Column 2+ : Numeric scores (as many as needed)
 */
object FileProcessor {

    // ── Public entry point ────────────────────────────────────────────────────

    /**
     * Dispatches to the correct reader based on file extension.
     * Returns an empty list if the extension is not supported.
     */
    fun readFile(file: File, log: (String) -> Unit): List<Student> =
        when (file.extension.lowercase()) {
            "csv"         -> readCSV(file,   log)
            "xlsx", "xls" -> readExcel(file, log)
            "pdf"         -> readPDF(file,   log)
            else -> {
                log("✗ Unsupported file type: .${file.extension}  (use csv, xlsx, xls, or pdf)")
                emptyList()
            }
        }

    // ── CSV reader ────────────────────────────────────────────────────────────
    private fun readCSV(file: File, log: (String) -> Unit): List<Student> {
        log("  Reading CSV: ${file.name}")
        val students = mutableListOf<Student>()

        CSVReader(FileReader(file)).use { reader ->
            val rows = reader.readAll()

            // Auto-detect header: skip first row when cell-0 is not a number
            val dataRows = if (rows.isNotEmpty() &&
                rows[0].firstOrNull()?.trim()?.toDoubleOrNull() == null
            ) {
                log("  → Header row detected, skipping it")
                rows.drop(1)
            } else rows

            dataRows.forEachIndexed { idx, row ->
                if (row.size < 3) {
                    log("  ⚠  Row ${idx + 2}: fewer than 3 columns — skipped")
                    return@forEachIndexed
                }
                val name   = row[0].trim()
                val id     = row[1].trim()
                val scores = row.drop(2).mapNotNull { it.trim().toDoubleOrNull() }
                students.add(Student(name, id, scores))
            }
        }
        log("  → Parsed ${students.size} record(s) from CSV")
        return students
    }

    // ── Excel reader ──────────────────────────────────────────────────────────
    private fun readExcel(file: File, log: (String) -> Unit): List<Student> {
        log("  Reading Excel: ${file.name}")
        val students = mutableListOf<Student>()

        WorkbookFactory.create(file).use { wb ->
            val sheet = wb.getSheetAt(0)

            // Auto-detect header row
            val firstDataRow = run {
                val r = sheet.getRow(0)
                val c = r?.getCell(0)
                if (c != null && c.cellType == CellType.STRING &&
                    c.stringCellValue.trim().toDoubleOrNull() == null
                ) {
                    log("  → Header row detected, skipping it")
                    1
                } else 0
            }

            for (i in firstDataRow..sheet.lastRowNum) {
                val row  = sheet.getRow(i) ?: continue
                val name = cellToString(row.getCell(0))
                val id   = cellToString(row.getCell(1))
                if (name.isBlank() && id.isBlank()) continue

                val scores = mutableListOf<Double>()
                for (j in 2 until row.lastCellNum) {
                    val cell = row.getCell(j) ?: continue
                    when (cell.cellType) {
                        CellType.NUMERIC -> scores.add(cell.numericCellValue)
                        CellType.STRING  -> cell.stringCellValue.trim()
                            .toDoubleOrNull()?.let { scores.add(it) }
                        else -> { /* ignore blanks / formulas */ }
                    }
                }
                students.add(Student(name, id, scores))
            }
        }
        log("  → Parsed ${students.size} record(s) from Excel")
        return students
    }

    /** Safe cell-to-string for numeric or text cells. */
    private fun cellToString(cell: Cell?): String = when (cell?.cellType) {
        CellType.STRING  -> cell.stringCellValue.trim()
        CellType.NUMERIC -> cell.numericCellValue.toLong().toString()
        else             -> ""
    }

    // ── PDF reader ────────────────────────────────────────────────────────────
    /**
     * Strips plain text from the PDF, then treats each non-blank line as a row
     * delimited by comma, semicolon, or tab.  Works well for structured reports.
     */
    private fun readPDF(file: File, log: (String) -> Unit): List<Student> {
        log("  Reading PDF: ${file.name}")
        val students = mutableListOf<Student>()

        PDDocument.load(file).use { doc ->
            val text = PDFTextStripper().getText(doc)
            text.lines()
                .map    { it.trim() }
                .filter { it.isNotBlank() }
                .forEach { line ->
                    val parts = line.split(Regex("[,;\\t]+")).map { it.trim() }
                    if (parts.size < 3) return@forEach
                    val name   = parts[0]
                    val id     = parts[1]
                    val scores = parts.drop(2).mapNotNull { it.toDoubleOrNull() }
                    if (scores.isNotEmpty()) students.add(Student(name, id, scores))
                }
        }
        log("  → Parsed ${students.size} record(s) from PDF")
        return students
    }

    // ── Excel export (with best-student highlight) ────────────────────────────
    /**
     * Generates a polished .xlsx report:
     * • Title row
     * • Ranked table with colour-coded rows
     * • Best student row highlighted in GOLD
     * • Pass = light green, Fail = rose
     * • Class statistics and grade distribution at the bottom
     */
    fun exportToExcel(
        students:    List<Student>,
        bestStudent: Student?,
        stats:       GradeStatistics,
        outputFile:  File,
        log:         (String) -> Unit
    ) {
        log("  Building Excel report…")
        val wb    = XSSFWorkbook()
        val sheet = wb.createSheet("Grade Report")

        // ── Cell styles ───────────────────────────────────────────────────
        fun boldFont(color: Short = IndexedColors.BLACK.index, size: Short = 11) =
            wb.createFont().apply {
                bold               = true
                this.color         = color
                fontHeightInPoints = size
            }

        val headerStyle = wb.createCellStyle().apply {
            fillForegroundColor = IndexedColors.DARK_BLUE.index
            fillPattern         = FillPatternType.SOLID_FOREGROUND
            alignment           = HorizontalAlignment.CENTER
            setFont(boldFont(IndexedColors.WHITE.index, 12))
        }
        val titleStyle = wb.createCellStyle().apply {
            alignment = HorizontalAlignment.CENTER
            setFont(boldFont(IndexedColors.DARK_BLUE.index, 16))
        }
        val bestStyle = wb.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GOLD.index
            fillPattern         = FillPatternType.SOLID_FOREGROUND
            setFont(boldFont())
        }
        val altStyle = wb.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.index
            fillPattern         = FillPatternType.SOLID_FOREGROUND
        }
        val passStyle = wb.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_GREEN.index
            fillPattern         = FillPatternType.SOLID_FOREGROUND
            setFont(boldFont())
        }
        val failStyle = wb.createCellStyle().apply {
            fillForegroundColor = IndexedColors.ROSE.index
            fillPattern         = FillPatternType.SOLID_FOREGROUND
            setFont(boldFont())
        }
        val statLabelStyle = wb.createCellStyle().apply { setFont(boldFont()) }

        var rowIdx = 0
        val maxScores = students.maxOfOrNull { it.scores.size } ?: 0
        val totalCols = maxScores + 7   // Rank, Name, ID, scores…, Avg, Grade, GPA, Status

        // ── Title ─────────────────────────────────────────────────────────
        sheet.createRow(rowIdx++).also { r ->
            r.height = 600
            r.createCell(0).apply {
                setCellValue("🎓  STUDENT GRADE REPORT")
                cellStyle = titleStyle
            }
            sheet.addMergedRegion(CellRangeAddress(0, 0, 0, totalCols - 1))
        }
        rowIdx++   // blank spacer row

        // ── Column headers ────────────────────────────────────────────────
        val headers = mutableListOf("Rank", "Name", "Student ID")
        repeat(maxScores) { headers.add("Score ${it + 1}") }
        headers.addAll(listOf("Average", "Grade", "GPA", "Status"))

        sheet.createRow(rowIdx++).also { r ->
            headers.forEachIndexed { i, h ->
                r.createCell(i).apply { setCellValue(h); cellStyle = headerStyle }
            }
        }

        // ── Student data rows ─────────────────────────────────────────────
        val ranked = students.sortedByDescending { it.average }
        ranked.forEachIndexed { idx, student ->
            val isBest = (student == bestStudent)
            val rowStyle: XSSFCellStyle = when {
                isBest        -> bestStyle
                idx % 2 == 1  -> altStyle
                else          -> wb.createCellStyle()  // plain white
            }

            sheet.createRow(rowIdx++).also { r ->
                var col = 0
                fun cell(value: String) = r.createCell(col++).apply {
                    setCellValue(value); cellStyle = rowStyle
                }
                fun cell(value: Double) = r.createCell(col++).apply {
                    setCellValue(value); cellStyle = rowStyle
                }

                cell((idx + 1).toDouble())
                cell(if (isBest) "★  ${student.name}" else student.name)
                cell(student.id)
                student.scores.forEach { cell(it) }
                repeat(maxScores - student.scores.size) { cell("-") }
                cell(student.average)
                cell(student.letterGrade)
                cell(student.gpa)

                // Status cell gets its own pass/fail colour regardless of row
                r.createCell(col).apply {
                    setCellValue(student.status)
                    cellStyle = if (student.status == "PASS") passStyle else failStyle
                }
            }
        }

        rowIdx++   // blank spacer

        // ── Statistics section ────────────────────────────────────────────
        sheet.createRow(rowIdx++).also { r ->
            r.createCell(0).apply {
                setCellValue("CLASS STATISTICS")
                cellStyle = headerStyle
            }
            sheet.addMergedRegion(CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 3))
        }

        fun statRow(label: String, value: String) {
            sheet.createRow(rowIdx++).also { r ->
                r.createCell(0).apply { setCellValue(label); cellStyle = statLabelStyle }
                r.createCell(1).setCellValue(value)
            }
        }
        statRow("Total Students",  stats.totalStudents.toString())
        statRow("Class Average",   "%.2f".format(stats.classAverage))
        statRow("Highest Average", "%.2f".format(stats.highestScore))
        statRow("Lowest Average",  "%.2f".format(stats.lowestScore))
        statRow("Passing",         "${stats.passingCount} student(s)")
        statRow("Failing",         "${stats.failingCount} student(s)")
        bestStudent?.let {
            statRow("★ Best Student",
                "${it.name}  (ID: ${it.id})  —  Avg: ${"%.2f".format(it.average)}, Grade: ${it.letterGrade}")
        }

        rowIdx++
        sheet.createRow(rowIdx++).also { r ->
            r.createCell(0).apply {
                setCellValue("GRADE DISTRIBUTION")
                cellStyle = headerStyle
            }
            sheet.addMergedRegion(CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 1))
        }
        stats.gradeDistribution.entries
            .sortedBy { it.key }
            .forEach { (grade, count) ->
                sheet.createRow(rowIdx++).also { r ->
                    r.createCell(0).apply { setCellValue("Grade $grade"); cellStyle = statLabelStyle }
                    r.createCell(1).setCellValue(count.toDouble())
                }
            }

        // Auto-size every column
        repeat(totalCols) { sheet.autoSizeColumn(it) }

        outputFile.outputStream().use { wb.write(it) }
        wb.close()
        log("  ✓ Saved Excel report → ${outputFile.absolutePath}")
    }

    // ── CSV export ────────────────────────────────────────────────────────────
    fun exportToCSV(
        students:    List<Student>,
        bestStudent: Student?,
        stats:       GradeStatistics,
        outputFile:  File,
        log:         (String) -> Unit
    ) {
        log("  Building CSV report…")
        val maxScores = students.maxOfOrNull { it.scores.size } ?: 0

        CSVWriter(FileWriter(outputFile)).use { writer ->
            // Header
            val header = mutableListOf("Rank", "Name", "Student ID")
            repeat(maxScores) { header.add("Score ${it + 1}") }
            header.addAll(listOf("Average", "Grade", "GPA", "Status", "Note"))
            writer.writeNext(header.toTypedArray())

            // Student rows (ranked)
            students.sortedByDescending { it.average }
                .forEachIndexed { idx, student ->
                    val row = mutableListOf(
                        (idx + 1).toString(),
                        student.name,
                        student.id
                    )
                    student.scores.forEach { row.add("%.1f".format(it)) }
                    repeat(maxScores - student.scores.size) { row.add("") }
                    row.add("%.2f".format(student.average))
                    row.add(student.letterGrade)
                    row.add("%.1f".format(student.gpa))
                    row.add(student.status)
                    row.add(if (student == bestStudent) "★ BEST STUDENT" else "")
                    writer.writeNext(row.toTypedArray())
                }

            // Statistics footer
            writer.writeNext(arrayOf())
            writer.writeNext(arrayOf("--- CLASS STATISTICS ---"))
            writer.writeNext(arrayOf("Total Students",  stats.totalStudents.toString()))
            writer.writeNext(arrayOf("Class Average",   "%.2f".format(stats.classAverage)))
            writer.writeNext(arrayOf("Highest Average", "%.2f".format(stats.highestScore)))
            writer.writeNext(arrayOf("Lowest Average",  "%.2f".format(stats.lowestScore)))
            writer.writeNext(arrayOf("Passing",         stats.passingCount.toString()))
            writer.writeNext(arrayOf("Failing",         stats.failingCount.toString()))
            bestStudent?.let {
                writer.writeNext(arrayOf(
                    "Best Student",
                    "${it.name} (${it.id}) — Avg: ${"%.2f".format(it.average)}, Grade: ${it.letterGrade}"
                ))
            }
        }
        log("  ✓ Saved CSV report → ${outputFile.absolutePath}")
    }
}