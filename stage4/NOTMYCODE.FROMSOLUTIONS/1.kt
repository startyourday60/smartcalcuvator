package calculator

// by https://hyperskill.org/profile/49824682
fun main() {
    do {
        val line = readLine()!!
        when (line) {
            "/help" -> println(
                    "The program calculates the sum of numbers,\n" +
                    "handling addition and (binary and unary) subtraction,\n" +
                    " ignoring whitespace.")
            "/exit" -> println("Bye!")
            else -> if (!line.isEmpty()) {
                // regex replace in order to account for unary and binary
                // operations as well as clean up spacing...then the process
                // is the same as in the previous stage -> conversion; reduce
                line.replace("(--|[+]+( )+)".toRegex(), "")
                    .replace("(-( )+)+".toRegex(), "-")
                    .replace("( )+(-|)".toRegex(), " $2")
                    .split(" ").map(String::toInt).reduce {
                    a, b -> a + b
                }.let(::println)
            }
        }
    } while(line != "/exit")
}
