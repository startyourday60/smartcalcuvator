package calculator
//import kotlin.math.*
// behavior for some operation
const val debugEnabled = false
typealias calculatorDefType = java.math.BigDecimal
const val calculatorDefTypeText = "java.math.BigDecimal"
class Operation
{
    companion object {
	fun getType(ch: Char): type {
		val type_iterator = type.values()
		for (t in type_iterator)
		{
			if (ch == t.ch || ch in t.Additional) return t
		}
		return type.unknownOrVariable

	}
	fun isWType(t: type) = t == type.plus || t == type.minus || t == type.exp || t == type.div || t == type.mod
	
    }
    enum class type(val ch: Char, val Additional: List<Char> = listOf<Char>())
    {
        plus('+', listOf(' ')),
        minus('-'), exp('*'), div('/'),
        mod('%'), oBracket('('), cBrocket(')'), 
	equal('='), command('/'), unknownOrVariable(' ');
	fun type.isBracket(): Boolean =	this.ch == '(' || this.ch == '('
	fun type.isClosedBracket(): Boolean = this.isBracket() && this.ch == ')'
        fun doOperation(a: Number, b: Number, t: type): Number? {
	    val operandFirst = calculatorDefType(a.toString())
	    val operandSecond = calculatorDefType(b.toString())
            try {
                return when(t)
                {
                    plus -> operandFirst + operandSecond
                    minus -> operandFirst - operandSecond
                    exp -> operandFirst * operandSecond
                    div -> operandFirst / operandSecond
                    mod -> operandFirst % operandSecond
		    else -> return null
                }
            } catch(e: Exception) {
                println("${e.toString().split(": ")[1]}")
                TODO("CHECK there on Null divide exception")
                //return null
            }
        }
    }
    interface operator {
	val text: String
	val t: type
    }
    open class anyOperator(val isVariable: Boolean)
    {

    }
//    data class l_operator(override val text: String, override val t: type) : operator, anyOperator(true) 
    data class r_operator(override val text: String, override val t: type) : operator, anyOperator(false) 
    data class variable(val name: String, var value: calculatorDefType = calculatorDefType(0.0)) : anyOperator(true)
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
		try {
         		val res = Parser.doParse(readln())
         		if (res != null) println(res.toInt())
		} catch(e: Parser.badVariableException) {
			val v = e.variable
			if (v.first() != '/') println("Invalid expression") //println("Bad variable $v")
			else {
				val comIDX = calculator.commands.commandsText.indexOf(v)
				if (comIDX == -1) println("Unknown command") //: $v, try /help")
				else calculator.commands.commandsFuns[comIDX]()
			}
		} catch (e: Parser.invalidException) {
			println("Invalid expression")
		} catch(e: java.lang.NumberFormatException) {
			println("Invalid expression")
		}
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
    class PreParserException(val t: String) : Exception(t)
    class badVariableException(val variable: String) : Exception()
    class invalidException : Exception()
    companion object {
//	const val allowedChars = CharArray('
        fun doParse_(line: String): Number? // [deprecated]
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
	    var ret = calculatorDefType("0.0")
	    for (idx in 0 until numbers.size)
	    {
			// https://kotlinlang.org/docs/operator-overloading.html#arithmetic-operators (plusAssign is allowed)
			val nextValue = numbers[idx].toDoubleOrNull()
			if (nextValue == null) continue //throw NotCorrectNumber() // Maybe warning?
			ret = calculatorDefType(Operation.type.plus.doOperation(ret, nextValue, Operation.type.plus)!!.toString())
	    }
	    return ret
        }
	private fun debugParse(msg: String) = if (debugEnabled) println("[PARSER DEBUG] $msg") else {} // 
	private fun pre_parse(line: String): Pair<MutableList<calculatorDefType>, MutableList<Operation.anyOperator>> {
		val pre_parseCharacters = fun(characters: MutableList<Char>, nums: MutableList<calculatorDefType>) {
			//val lC = characters.size
			var closedBracketTimes: Int = 0
			var openedBracketTimes: Int = 0
			var openedBracket = false
			/*preParserCh@*/
			for (idx in 0 until characters.size)
			{
				debugParse("characters pre parse $idx ")//= `${characters[idx]}`")
				// change much -- to +, and much ++ to one +. 
				//val ch = characters[idx] // don't do like it there. is bad idea
				//
				debugParse("Nums size: ${nums.size}; characters size: ${characters.size}")
				/*if (idx + 1 < characters.size && characters.size >= nums.size)
				{
					//val next_ch = characters[idx + 1]
					val isDoubleMinus = characters[idx] == '-' && characters[idx + 1] == '-' 
					val isDoublePlus = characters[idx] == '+' && characters[idx + 1] == '+'
					if (isDoubleMinus || isDoublePlus)
					{
						characters.set(idx, '+')
						characters.removeAt(idx + 1)
						//preParserCh@
						continue
					}
				} //
				^^9 DEPRECATED AND UNSTABLE*/
				if (idx < characters.size) {
					if (characters[idx] == '(') {
						openedBracketTimes++ 
						openedBracket = true
					}
					else if (characters[idx] == ')') {
						closedBracketTimes++
						if (!openedBracket) throw PreParserException("Bad closedBracket")
						openedBracket = false
					}
				}
			}

			debugParse("notClosedBracketTimes = $closedBracketTimes")
			debugParse("openedBracketTimes = $openedBracketTimes")
			if (openedBracketTimes != closedBracketTimes) throw PreParserException("Bad openedBracketTimes")
		} // PRE
		// TODO: NORMAL NAME FOR VARIABLE HERE
		val wSpace = line.split(" ")
		if (wSpace.size > 1) {
			var foundOperater = false
			for (w in wSpace)
			{
				val tmp = Operation.getType(w.firstOrNull()?: ' ')
				if ( Operation.isWType(tmp) ) foundOperater = true
			}
			if (!foundOperater) throw invalidException()
		}
		val withoutSpace = line.filter { it != ' ' }
		val withoutDoubleMinus = withoutSpace.replace(Regex("(\\+\\+*|\\-\\-)"), "+") // there is replacement for ^^9
		debugParse("withoutDoubleMinus $withoutDoubleMinus")
		//var isNumeric = true;
		var lastNum: String? = null
		val numbers = mutableListOf<calculatorDefType>()
		val characters = mutableListOf<Char>()
		for (ch in withoutDoubleMinus) {
			if (ch.isDigit() || ch == '.' || (ch == '-' && '-' !in lastNum?: "" && lastNum == null) || (ch == '+' && '+' !in lastNum?: "" && lastNum == null)) {
				if (lastNum == null) {
					lastNum = ""
				}
				lastNum += ch
				debugParse("Is digit.")
			} else if(!ch.isDigit() && lastNum != null) {
				debugParse("is now not digit!. now is will be saved")
				debugParse("Value for save $lastNum")
				numbers.add(calculatorDefType(lastNum))
				lastNum = null
				characters.add(ch)
			} 
			else {
				characters.add(ch)
			}
		}
		if (lastNum != null)
		{

			numbers.add(calculatorDefType(lastNum))
			lastNum = null
		}
		//System.gc()A
	        pre_parseCharacters(characters, numbers)	
		val parsed_characters = parseCharacters(characters, numbers)
		debugParse("MutableList<Char> = ${characters}")
		debugParse("MutableList<calculatorDefType(${calculatorDefTypeText})> = ${numbers}")
		debugParse("Parsed charactersList: ${parsed_characters}")
		return Pair(numbers, parsed_characters)
	}
	private fun parseCharacters(list: MutableList<Char>, numbers: MutableList<calculatorDefType>): MutableList<Operation.anyOperator>
	{
		val returnValue = mutableListOf<Operation.anyOperator>()
		var variableText: String = ""
		for (IDX in 0 until list.size)
		{
			val element = list[IDX]
			val tmp = Operation.getType(element)
			debugParse("(parseCharacters) ${element} = ${tmp} ($IDX)")
			if (tmp == Operation.type.equal && !variableText.isEmpty())
			{
					debugParse(" (parseCharacters) Add there")
					returnValue.add(Operation.variable(variableText))
					variableText = ""
			}
			else if (tmp != Operation.type.unknownOrVariable && variableText.isEmpty() && !(tmp == Operation.type.div || tmp == Operation.type.equal && IDX == 0)) 
			{
				debugParse("Is not variable")
				if (variableText.isEmpty()) returnValue.add(Operation.r_operator(element.toString(), tmp))
			} 
			else if (tmp == Operation.type.div || tmp == Operation.type.equal && IDX == 0) {
				debugParse("Is command")
				variableText += element
			}
			else if (tmp == Operation.type.unknownOrVariable){
				debugParse("(parseCharacters) variable text")
				variableText += element
			} //else variableText += element
		}
		//TODO("CHECK if variable inited. if yeap not throw")
		if (!variableText.isEmpty()) throw badVariableException(variableText)
		return returnValue
	}
	private fun CharactersToNum(listNums: MutableList<calculatorDefType>, listOperations: MutableList<Operation.anyOperator>): MutableList<Pair<Operation.anyOperator, calculatorDefType>>
	{
		// TODO: check to brackets before
		val ret = mutableListOf<Pair<Operation.anyOperator, calculatorDefType>>()
		debugParse("\n\nCharacters to Num")
		val sListOperations = listOperations.size
		val sListNums = listNums.size

		val listNumsIsOdd = (sListNums % 2 == 1)
		val listOperationsIsOdd = (sListOperations % 2 == 1) 
		val difOfSize = sListOperations - sListNums // if < 0 then is small than sListNums
		if (difOfSize == 0) throw invalidException()
		// there can be bug for -variableName. toDO: fix all logic.
		// for now variables just pre inited. not full supports. its ok for now. but its have to be fixed
		debugParse("sListNums: $sListNums; sListOperations: $sListOperations")
		if (difOfSize == 0 && sListNums == 1 && listOperations[0].isVariable)
		{
			val newValue = listNums[0]
			debugParse("Is variable. the will be new value ${newValue}")
			val oldVariable = listOperations[0]
			oldVariable as Operation.variable
			listOperations[0] as Operation.variable
			val newVariable = Operation.variable(oldVariable.name, newValue)
			ret.add(Pair(newVariable, newValue))
		}
		else {
			debugParse("is not variable. is not command.")
			for (opIdx in 0 until listOperations.size)
			{
				debugParse("opIdx $opIdx") // TODO
				if (opIdx < sListOperations && opIdx < sListNums) ret.add( Pair(listOperations[opIdx], listNums[opIdx]) )
			}
			if (listNums.size > listOperations.size) ret.add(Pair(Operation.r_operator(" ", t=Operation.type.unknownOrVariable), listNums.last()))
			//ret.add(Pair(listOperations))
		}
		debugParse("size of operations = ${sListOperations}; size of nums = ${sListNums}")
		debugParse("listNumsIsOdd ${listNumsIsOdd}")
		debugParse("listOperationsIsOdd ${listOperationsIsOdd}")
		debugParse("$difOfSize")
		debugParse("~~~~~~~~~~")
		debugParse("${listOperations}")
		debugParse("${listNums}")
		//return mutableListOf(Pair(Operation.anyOperator(false), calculatorDefType(0.0)))
		return ret
	}
	private fun calculateNums(w: MutableList<Pair<Operation.anyOperator, calculatorDefType>>): calculatorDefType
	{
		
		val wSize = w.size
		if (wSize == 0) return  calculatorDefType(0.0)
		val (fOp, fNum) = w[0]
		debugParse("FirstOp:$fOp;fNum:$fNum")
		var ret = calculatorDefType(fNum.toString())
		/*lateinit*/ var lastOperator: Operation.r_operator? = null
		if (!fOp.isVariable) {
				fOp as Operation.r_operator
				lastOperator = fOp
		} else {
			fOp as Operation.variable
			val variableVal = fOp.value
			TODO("full variable supports")
		}
		// not need step by 2.
		for (idx in 1 until w.size)
		{
			val (op, num) = w[idx]
			debugParse("num: $num")
			debugParse("op: $op")
			debugParse("IDX: $idx")
			if (op.isVariable)
			{	
				op as Operation.variable
				TODO("Full variable supports")
			}
			else {
				op as Operation.r_operator // better lateinit ther
				val operatorForUsing = if(lastOperator != null) lastOperator else op
				debugParse("Operator for using: $operatorForUsing")
				val nValue = op.t.doOperation(ret, num, operatorForUsing.t)
				debugParse("nValue = $nValue")
				if (nValue != null) ret = calculatorDefType(nValue.toString())
				else debugParse("nValue is null. ignore for now. is warning, but just for now. not for future")
				if (op.t != Operation.type.unknownOrVariable) lastOperator = op
				//lastNum = num
			}
			debugParse("ret: ${ret}")
			debugParse("lastOperator: ${lastOperator}")
			debugParse("~~~~~")

		}
		return ret
	}
	fun doParse(line: String): Number? 
	{
		if (line.isEmpty()) return null
		// toDo change variable places.
		val (numbers, characters) = pre_parse(line)	
		val w = CharactersToNum(numbers, characters)
		debugParse("NEW values")
		val sum = calculateNums(w)
		debugParse("Sum: $sum")
		return sum
	}
    }
}
fun main() {
	calculator().run()
}
