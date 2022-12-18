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
                println("${e.toString().split(": ")[1]}")
                TODO("CHECK there on Null divide exception")
                //return null
            }
        }
    }
}
// there will be calculvator behavior. 
class calculator {
	object commands {
		fun doHelp() {
			println("The program calculates the sum of numbers")
		}
		fun doExit(): Nothing {
			// https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.system/exit-process.html maybe?
			throw Parser.UserWantToExitException()
		}
		val commandsText = listOf("/help", "/exit")
		val commandsFuns = listOf(::doHelp, ::doExit)
	}
	constructor() {}
	fun run() {
          try {
             while(true) 
             {
         	val res = Parser.doParse(readln())
         	if (res != null) println(res.toInt())
             }
          } catch(e: Parser.UserWantToExitException) {
         	println("Bye!");
          }
	}
}
// TODO
// LR parser?

class Parser
{
    class UserWantToExitException : Exception()
    // class UserNotWroteAnything : Exception()
    // class NotCorrectNumber : Exception()
    companion object {
        fun doParse(line: String): Number?
        {
            val numbers = line.split(' ')
	    if (numbers.size == 1 && numbers.first().toDoubleOrNull() == null)
	    {
		val idx = calculator.commands.commandsText.indexOf(numbers.first())
		if (idx == -1) { }
		else {
			calculator.commands.commandsFuns[idx]()	
		}
		return null
	    } else if (numbers.size == 1) return numbers.first().toDoubleOrNull();
	    var ret = java.math.BigDecimal("0.0")
	    for (idx in 0 until numbers.size)
	    {
			// https://kotlinlang.org/docs/operator-overloading.html#arithmetic-operators (plusAssign is allowed)
			val nextValue = numbers[idx].toDoubleOrNull()
			if (nextValue == null) continue //throw NotCorrectNumber() // Maybe warning?
			ret = java.math.BigDecimal(Operation.type.plus.doOperation(ret, nextValue, Operation.type.plus)!!.toString())
	    }
	    return ret
        }
    }
}
fun main() {
	calculator().run()
}
