package calculator
//import kotlin.math.*
// behavior for some operation
class Operation
{
    enum class type(val ch: Char, val Additional: List<Char> = listOf<Char>())
    {
        plus('+', listOf(' ')),
        minus('-'), exp('*'), div('/'),
        mod('%');
        fun doOperation(a: Number, b: Number, t: type): Number? {
	    val operandFirst = java.math.BigDecimal(a.toString())
	    val operandSecond = java.math.BigDecimal(b.toString())
            try {
                return when(t)
                {
                    plus -> operandFirst + operandSecond
                    minus -> operandFirst - operandSecond
                    exp -> operandFirst * operandSecond
                    div -> operandFirst / operandSecond
                    mod -> operandFirst % operandSecond
                }
            } catch(e: Exception) {
                println("${e.toString()}")
                TODO("CHECK there on Null divide exception")
                return null
            }
        }
    }
}
// TODO
// LR parser?
class Parser
{
    companion object {
        fun doParse(line: String): Number
        {
            //val withoutSpace = line.replace(' ', "")
            val numbers = line.split(' ')
            return Operation.type.plus.doOperation(numbers[0].toDouble(), numbers[1].toDouble(), Operation.type.plus)!! // toFloat not exists?
        }
    }
}
fun main() {
    println(Parser.doParse(readln()).toInt())
}
