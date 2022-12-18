package calculator

private fun correct(expr: String): List<String> { // replace all type of "+-.." or "-+.." with '+' or '-'
    return expr.split(" ").map {
        if (it.length > 1 && it[0] in "-+" && it[1] in "-+")
            if (it.count { it1 -> it1 == '-' } % 2 == 1) "-" else "+" else it
    }
}

private fun eval(exprList: List<String>): Double? {
    try {
        if (exprList.size == 1) return exprList[0].toDoubleOrNull()
        when (exprList[1]) {
            "*" -> return try {                                                 // high priority operations
                val result = exprList[0].toDouble() * exprList[2].toDouble()
                if (exprList.size == 3) result else eval(listOf(result.toString()).plus(exprList.drop(3)))
            } catch (err: NumberFormatException) {
                exprList[0].toDouble() * eval(exprList.drop(2))!!
            }
            "/" -> return try {                                                 // high priority operations
                val result = exprList[0].toDouble() / exprList[2].toDouble()
                if (exprList.size == 3) result else eval(listOf(result.toString()).plus(exprList.drop(3)))
            } catch (err: NumberFormatException) {
                exprList[0].toDouble() / eval(exprList.drop(2))!!
            }
            "+" -> return exprList[0].toDouble() + eval(exprList.drop(2))!!  // low priority operations
            "-" -> return exprList[0].toDouble() - eval(exprList                // low priority operations
                    .drop(2)
                    .map {                         // inverse sign example "a - b - c" == "a - (b + c)"
                        when (it) {
                            "-" -> "+"
                            "+" -> "-"
                            else -> it
                        }
                    }
                    .toList())!!
        }
    } catch (e: NumberFormatException) {
        return null
    }
    return null
}

fun main() {
    do {
        val expr = readLine()!!
        when {
            expr == "" -> continue
            expr == "/help" -> {
                println("The program calculates priority operations with recursive algorithm ")
                continue
            }
            expr == "/exit" -> break
            expr[0] == '/' -> {
                println("Unknown command")
                continue
            }
            else -> try {
                println(eval(correct(expr))!!.toInt())
            } catch (e: Exception) {
                println("Invalid expression")
            }
        }
    } while (true)
    println("Bye!")
}

