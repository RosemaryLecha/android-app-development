fun main() {
    val numbers: List<Int?> = listOf(1, null, 3, null, 5, 6, null, 8)

    // Step by step
    val nonNulls = numbers.filterNotNull()
    val doubled = nonNulls.map { it * 2 }
    val total = doubled.sum()
    println("Sum of doubled values: $total")

    // One-liner challenge
    val result = numbers.filterNotNull().map { it * 2 }.sum()
    println("One-liner result: $result")
}
