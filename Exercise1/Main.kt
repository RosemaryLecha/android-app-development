data class User(val name: String, val email: String?)

val users = listOf(
    User("Alex", "alex@example.com"),
    User("Blake", null),
    User("Casey", "casey@work.com")
)

fun main() {
    // Requirements 1 & 2: Print email in uppercase or "has no email"
    for (user in users) {
        if (user.email != null) {
            println(user.email.uppercase())
        } else {
            println("${user.name} has no email")
        }
    }

    // Requirement 3: Count users with valid emails
    val validEmailCount = users.count { it.email != null }
    println("Total users with valid emails: $validEmailCount")
}
