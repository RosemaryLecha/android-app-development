// ─────────────────────────────────────────────────────────────────────────────
// GradeUI.kt  —  Swing GUI with dark-terminal aesthetic
// Progress messages appear in both the on-screen terminal pane AND System.out
// ─────────────────────────────────────────────────────────────────────────────

import java.awt.*
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

class GradeUI : JFrame("🎓  Kotlin Grading Calculator") {

    // ── State ─────────────────────────────────────────────────────────────────
    private val calculator  = GradeCalculator()
    private var students    = listOf<Student>()
    private var bestStudent: Student? = null
    private var stats       = GradeStatistics()
    private var loadedFile: File? = null

    // ── Colour palette ─────────────────────────────────────────────────────────
    private val BG_DEEP    = Color(10, 14, 26)
    private val BG_PANEL   = Color(18, 24, 40)
    private val BG_SIDEBAR = Color(22, 30, 50)
    private val ACCENT     = Color(251, 191, 36)         // gold
    private val C_GREEN    = Color(52, 211, 153)
    private val C_BLUE     = Color(99, 179, 237)
    private val C_RED      = Color(248, 113, 113)
    private val C_ORANGE   = Color(251, 146, 60)
    private val C_SLATE    = Color(148, 163, 184)
    private val C_WHITE    = Color(226, 232, 240)

    // ── Components ────────────────────────────────────────────────────────────
    private val terminalPane = JTextPane().apply {
        background  = BG_DEEP
        foreground  = C_GREEN
        font        = Font("Consolas", Font.PLAIN, 13)
        isEditable  = false
        border      = EmptyBorder(12, 16, 12, 16)
    }
    private val statusLabel  = JLabel("  Ready — upload a file to begin")
    private val progressBar  = JProgressBar(0, 100)

    private val btnUpload    = mkBtn("📂  Upload File",       Color(37, 99, 235))
    private val btnCalc      = mkBtn("⚙️   Calculate Grades",  Color(5, 150, 105)).also { it.isEnabled = false }
    private val btnXlsx      = mkBtn("📊  Export to Excel",   Color(180, 120, 10)).also { it.isEnabled = false }
    private val btnCsv       = mkBtn("📄  Export to CSV",     Color(109, 40, 217)).also { it.isEnabled = false }
    private val btnClear     = mkBtn("🗑️   Clear",              Color(71, 85, 105))

    // ── Init ──────────────────────────────────────────────────────────────────
    init {
        buildUI()
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(1060, 740)
        minimumSize = Dimension(860, 600)
        setLocationRelativeTo(null)
        isVisible = true
        log("SYS", "Grading Calculator ready.  Upload a CSV, Excel, or PDF file to begin.")
    }

    // ── Layout ─────────────────────────────────────────────────────────────────
    private fun buildUI() {
        contentPane.background = BG_DEEP
        layout = BorderLayout(0, 0)

        // ── Title bar ─────────────────────────────────────────────────────
        val titleBar = JPanel(FlowLayout(FlowLayout.LEFT, 20, 0)).apply {
            background   = BG_PANEL
            preferredSize = Dimension(0, 60)
            border       = BorderFactory.createMatteBorder(0, 0, 1, 0, Color(40, 55, 80))
            add(JLabel("●").apply { foreground = Color(255, 90, 90);  font = Font("Dialog", Font.BOLD, 18) })
            add(JLabel("●").apply { foreground = Color(255, 200, 50); font = Font("Dialog", Font.BOLD, 18) })
            add(JLabel("●").apply { foreground = C_GREEN;             font = Font("Dialog", Font.BOLD, 18) })
            add(Box.createHorizontalStrut(12))
            add(JLabel("🎓  KOTLIN GRADING CALCULATOR").apply {
                foreground = ACCENT
                font       = Font("Consolas", Font.BOLD, 20)
            })
            add(Box.createHorizontalGlue())
            add(JLabel("IntelliJ  ·  Kotlin/JVM").apply {
                foreground = C_SLATE
                font       = Font("Consolas", Font.PLAIN, 12)
                border     = EmptyBorder(0, 0, 0, 20)
            })
        }
        add(titleBar, BorderLayout.NORTH)

        // ── Terminal scroll pane ──────────────────────────────────────────
        val scroll = JScrollPane(terminalPane).apply {
            border            = BorderFactory.createMatteBorder(0, 0, 0, 1, Color(35, 50, 75))
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        }
        add(scroll, BorderLayout.CENTER)

        // ── Right sidebar ─────────────────────────────────────────────────
        val sidebar = JPanel().apply {
            layout     = BoxLayout(this, BoxLayout.Y_AXIS)
            background = BG_SIDEBAR
            border     = EmptyBorder(24, 16, 24, 16)
            preferredSize = Dimension(220, 0)
        }

        sidebar.add(sidebarLabel("FILE"))
        sidebar.add(Box.createVerticalStrut(8))
        sidebar.add(btnUpload)
        sidebar.add(Box.createVerticalStrut(4))
        sidebar.add(btnCalc)

        sidebar.add(Box.createVerticalStrut(24))
        sidebar.add(sidebarLabel("EXPORT"))
        sidebar.add(Box.createVerticalStrut(8))
        sidebar.add(btnXlsx)
        sidebar.add(Box.createVerticalStrut(4))
        sidebar.add(btnCsv)

        sidebar.add(Box.createVerticalStrut(24))
        sidebar.add(sidebarLabel("SESSION"))
        sidebar.add(Box.createVerticalStrut(8))
        sidebar.add(btnClear)

        sidebar.add(Box.createVerticalGlue())

        // Tiny help text at the bottom of sidebar
        sidebar.add(JLabel("<html><div style='text-align:center;color:#94a3b8;font-size:10px;'>" +
            "Supported input:<br>CSV · XLSX · XLS · PDF</div></html>").apply {
            alignmentX = Component.CENTER_ALIGNMENT
        })
        add(sidebar, BorderLayout.EAST)

        // ── Status bar ────────────────────────────────────────────────────
        val statusBar = JPanel(BorderLayout(8, 0)).apply {
            background    = BG_PANEL
            border        = CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color(40, 55, 80)),
                EmptyBorder(6, 14, 6, 14)
            )
            preferredSize = Dimension(0, 36)
        }
        statusLabel.apply {
            foreground = C_SLATE
            font       = Font("Consolas", Font.PLAIN, 12)
        }
        progressBar.apply {
            isStringPainted   = true
            background        = Color(30, 42, 60)
            foreground        = C_GREEN
            string            = ""
            preferredSize     = Dimension(200, 18)
            border            = EmptyBorder(0, 0, 0, 0)
        }
        statusBar.add(statusLabel, BorderLayout.WEST)
        statusBar.add(progressBar, BorderLayout.EAST)
        add(statusBar, BorderLayout.SOUTH)

        // ── Wire listeners ────────────────────────────────────────────────
        btnUpload.addActionListener { handleUpload()        }
        btnCalc.addActionListener   { handleCalculate()     }
        btnXlsx.addActionListener   { handleExportExcel()   }
        btnCsv.addActionListener    { handleExportCSV()     }
        btnClear.addActionListener  { handleClear()         }
    }

    // ── Logging ───────────────────────────────────────────────────────────────
    /**
     * Writes a timestamped line to BOTH the GUI terminal pane and System.out.
     * [level] controls colour: SYS | INFO | SUCCESS | WARN | ERROR | HEADER
     */
    fun log(level: String, message: String) {
        val color = when (level.uppercase()) {
            "SUCCESS" -> C_GREEN
            "HEADER"  -> C_BLUE
            "WARN"    -> C_ORANGE
            "ERROR"   -> C_RED
            "SYS"     -> ACCENT
            else      -> C_WHITE
        }
        val ts   = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val line = "[$ts][$level] $message\n"

        // ── Console output (visible in IntelliJ Run terminal) ────────────
        println(line.trimEnd())

        // ── GUI text pane ─────────────────────────────────────────────────
        SwingUtilities.invokeLater {
            val doc   = terminalPane.styledDocument
            val attrs = SimpleAttributeSet().also { StyleConstants.setForeground(it, color) }
            try { doc.insertString(doc.length, line, attrs) } catch (_: Exception) {}
            terminalPane.caretPosition = doc.length
        }
        SwingUtilities.invokeLater { statusLabel.text = "  $message" }
    }

    private fun setProgress(pct: Int, label: String = "") = SwingUtilities.invokeLater {
        progressBar.value  = pct
        progressBar.string = label
    }

    // ── Handlers ──────────────────────────────────────────────────────────────
    private fun handleUpload() {
        val fc = JFileChooser().apply {
            dialogTitle = "Select Student Data File"
            fileFilter  = FileNameExtensionFilter(
                "Student files (CSV, Excel, PDF)",
                "csv", "xlsx", "xls", "pdf"
            )
        }
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return
        loadedFile = fc.selectedFile

        Thread {
            try {
                log("SYS",  "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                log("SYS",  "  FILE UPLOAD")
                log("SYS",  "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                setProgress(20, "Reading…")

                val raw = FileProcessor.readFile(loadedFile!!) { log("INFO", it) }
                setProgress(60, "Validating…")

                students = raw.filter { validateStudent(it) }

                val skipped = raw.size - students.size
                log("SUCCESS", "✓ Loaded ${students.size} valid record(s) from '${loadedFile!!.name}'")
                if (skipped > 0) log("WARN", "  $skipped record(s) skipped (failed validation)")

                setProgress(100, "Loaded")
                SwingUtilities.invokeLater { btnCalc.isEnabled = students.isNotEmpty() }

            } catch (e: Exception) {
                log("ERROR", "Upload failed: ${e.message}")
                setProgress(0, "Error")
            }
        }.start()
    }

    private fun handleCalculate() {
        if (students.isEmpty()) { log("WARN", "No students loaded — upload a file first"); return }

        Thread {
            log("HEADER", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            log("HEADER", "  GRADE CALCULATION")
            log("HEADER", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            setProgress(10, "Calculating…")

            // ── Use custom higher-order function with a lambda ────────────
            val best    = calculator.getBestStudent(students)
            val lines   = calculator.processStudents(students) { s ->
                formatStudentResult(s, s == best)   // lambda
            }
            lines.forEach { log("INFO", it) }

            setProgress(50, "Computing statistics…")

            bestStudent = best
            stats       = calculator.getStatistics(students)
            val ranked  = calculator.rankStudents(students)

            log("HEADER", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            log("HEADER", "  CLASS STATISTICS")
            log("HEADER", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            log("INFO",    "  Students      : ${stats.totalStudents}")
            log("INFO",    "  Class average : ${"%.2f".format(stats.classAverage)}")
            log("INFO",    "  Highest avg   : ${"%.2f".format(stats.highestScore)}")
            log("INFO",    "  Lowest avg    : ${"%.2f".format(stats.lowestScore)}")
            log("INFO",    "  Passing       : ${stats.passingCount}")
            log("INFO",    "  Failing       : ${stats.failingCount}")
            log("HEADER",  "  Grade distribution:")
            stats.gradeDistribution.entries.sortedBy { it.key }
                .forEach { (g, c) -> log("INFO", "    Grade $g : $c student(s)") }

            bestStudent?.let {
                log("SUCCESS",
                    "  ★ Best student: ${it.name}  (ID: ${it.id})  " +
                    "Avg: ${"%.2f".format(it.average)}  Grade: ${it.letterGrade}")
            }

            // ── Print failing students ────────────────────────────────────
            val failing = calculator.getFailingStudents(students)
            if (failing.isNotEmpty()) {
                log("WARN", "  Students at risk (failing):")
                failing.forEach { s ->
                    log("WARN", "    ✗  ${s.name} (${s.id})  Avg: ${"%.2f".format(s.average)}")
                }
            }

            log("HEADER", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            log("HEADER", "  TOP 3 STUDENTS")
            log("HEADER", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            ranked.take(3).forEachIndexed { i, s ->
                log("SUCCESS", "  ${i + 1}. ${s.name} — ${"%.2f".format(s.average)} (${s.letterGrade})")
            }

            setProgress(100, "Done")
            log("SUCCESS", "✓ Calculation complete — ready to export")
            SwingUtilities.invokeLater {
                btnXlsx.isEnabled = true
                btnCsv.isEnabled  = true
            }
        }.start()
    }

    private fun handleExportExcel() {
        val fc = JFileChooser().apply {
            dialogTitle  = "Save Excel Report"
            selectedFile = File("GradeReport.xlsx")
            fileFilter   = FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx")
        }
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return
        val out = fc.selectedFile.let {
            if (it.name.endsWith(".xlsx")) it else File("${it.absolutePath}.xlsx")
        }
        Thread {
            setProgress(20, "Exporting…")
            try {
                FileProcessor.exportToExcel(students, bestStudent, stats, out) { log("INFO", it) }
                setProgress(100, "Exported")
                log("SUCCESS", "✓ Excel report saved → ${out.absolutePath}")
            } catch (e: Exception) {
                log("ERROR", "Export failed: ${e.message}")
                setProgress(0, "Error")
            }
        }.start()
    }

    private fun handleExportCSV() {
        val fc = JFileChooser().apply {
            dialogTitle  = "Save CSV Report"
            selectedFile = File("GradeReport.csv")
            fileFilter   = FileNameExtensionFilter("CSV file (*.csv)", "csv")
        }
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return
        val out = fc.selectedFile.let {
            if (it.name.endsWith(".csv")) it else File("${it.absolutePath}.csv")
        }
        Thread {
            setProgress(20, "Exporting…")
            try {
                FileProcessor.exportToCSV(students, bestStudent, stats, out) { log("INFO", it) }
                setProgress(100, "Exported")
                log("SUCCESS", "✓ CSV report saved → ${out.absolutePath}")
            } catch (e: Exception) {
                log("ERROR", "Export failed: ${e.message}")
                setProgress(0, "Error")
            }
        }.start()
    }

    private fun handleClear() {
        students    = emptyList()
        bestStudent = null
        stats       = GradeStatistics()
        loadedFile  = null
        terminalPane.text = ""
        btnCalc.isEnabled = false
        btnXlsx.isEnabled = false
        btnCsv.isEnabled  = false
        setProgress(0, "")
        log("SYS", "Session cleared — ready for a new file upload.")
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private fun mkBtn(text: String, bg: Color): JButton =
        JButton(text).apply {
            background    = bg
            foreground    = Color.WHITE
            font          = Font("Consolas", Font.BOLD, 13)
            isFocusPainted  = false
            isBorderPainted = false
            isOpaque      = true
            preferredSize = Dimension(188, 40)
            maximumSize   = Dimension(188, 40)
            alignmentX    = Component.CENTER_ALIGNMENT
            cursor        = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

            // Hover effect
            addMouseListener(object : java.awt.event.MouseAdapter() {
                private val normal  = bg
                private val hovered = bg.brighter()
                override fun mouseEntered(e: java.awt.event.MouseEvent?) {
                    if (isEnabled) background = hovered
                }
                override fun mouseExited(e: java.awt.event.MouseEvent?) {
                    background = normal
                }
            })
        }

    private fun sidebarLabel(text: String) = JLabel(text).apply {
        foreground  = C_SLATE
        font        = Font("Consolas", Font.BOLD, 10)
        alignmentX  = Component.LEFT_ALIGNMENT
        border      = EmptyBorder(0, 4, 0, 0)
    }

    // Compound border helper (avoid importing external class)
    private fun CompoundBorder(outer: javax.swing.border.Border,
                                inner: javax.swing.border.Border) =
        BorderFactory.createCompoundBorder(outer, inner)
}
