fun describeTemperature(temp: Int?): String {
    return when {
        temp == null -> "No data"
        temp <= 0 -> "Freezing"
        temp in 1..15 -> "Cold"
        temp in 16..25 -> "Mild"
        temp in 26..35 -> "Warm"
        temp in 36..45 -> "Hot"
        else -> "Extreme"
    }
}

fun main() {
    val temperatures: List<Int?> = listOf(0, 10, null, 22, 30, 40, 50, null, -5)

    for (temp in temperatures) {
        println("$temp -> ${describeTemperature(temp)}")
    }
}
