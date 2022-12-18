package calculator

import java.util.Scanner
// by https://hyperskill.org/profile/2121956
fun main() {
    val scanner = Scanner(System.`in`)

    loop@ while (true)
        when (val input = scanner.nextLine()) {
            "" -> continue@loop
            "/exit" -> break@loop
            "/help" -> println("This adds numbers and stuff")
            else -> println(input
                    .replace("+", "")
                    .replace("--", "")
                    .replace(Regex("- +"), "-")
                    .split(Regex(" +"))
                    .sumBy { it.toInt() })
        }
    println("Bye!")
}
