// https://hyperskill.org/profile/296726569
package calculator

fun main() {
    Calculator().run()
}

class Calculator {
    private val variables: MutableMap<String, Int> = mutableMapOf()

    fun run() {
        var terminate = false
        readln().trim().let {
            try {
                if (it.isCommand()) terminate = it.command()
                else if (it.isAssignment()) it.assignment()
                else it.expression()
            } catch (e : Exception) {
                println(e.message)
            }
        }

        if (!terminate) run()
    }

    fun String.command(): Boolean {
        when (this) {
            "/help" -> println("The program adds and subtracts numbers")
            "/exit" -> println("Bye!").run { return true }
            else -> throw Exception("Unknown command")
        }

        return false
    }

    fun String.assignment() {
        val terms = split("\\s*=\\s*".toRegex())
        if (!terms[0].isValidIdentifier()) throw Exception("Invalid identifier")
        else if (terms.size != 2 || !terms[1].isValidTerm()) throw Exception("Invalid assignment")
        else variables[terms[0]] = terms[1].getValue()
    }

    fun String.expression() {
        if (trim().isEmpty()) {}
        else if (isValidExpression()) {
            var operation: (Int, Int) -> Int = ::add
            println(split(Regex("\\s+")).fold(0) { acc, s ->
                if (s.isOperator()) {
                    operation = s.parseOperator()
                    acc
                } else {
                    operation(acc, s.getValue())
                }
            })
        }
        else throw Exception("Invalid expression")
    }

    fun String.parseOperator(): (Int, Int) -> Int {
        return when (replace("--|\\+".toRegex(), "")) {
            "-" -> ::subtract
            "" -> ::add
            else -> throw Exception("Unknown operator")
        }
    }

    fun String.getValue() = variables.getOrElse(this) { this.toIntOrNull() ?: throw Exception("Unknown variable") }
    fun String.isValidIdentifier() = "[a-zA-Z]+".toRegex().matches(this)
    fun String.isValidTerm() = isValidIdentifier() || toIntOrNull() != null
    fun String.isOperator() = "[+-]+".toRegex().matches(this)
    fun String.isAssignment() = contains("=")
    fun String.isCommand() = startsWith("/")
    fun String.isValidExpression(): Boolean {
        val validTerm = "(([a-zA-Z]+)|([+-]?\\d+))"
        return "($validTerm)(\\s+[+-]+\\s+$validTerm)*".toRegex().matches(this)
    }

    private fun add(x: Int, y: Int): Int = x + y
    private fun subtract(x: Int, y: Int): Int = x - y
}

