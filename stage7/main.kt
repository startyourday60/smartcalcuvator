package calculator
//import kotlin.math.*
// behavior for some operation
/*
	Parser of old version can be replaced to onlys regex in future version. because it's need. some code there is deprecated but works.
*/
/*const*/ var debugEnabled = false
typealias calculatorDefType = java.math.BigDecimal
const val calculatorDefTypeText = "java.math.BigDecimal"
class Operation
{
    companion object {
	val globalVars = mutableMapOf<String, calculatorDefType>()
	fun getType(ch: Char): type {
		val type_iterator = type.values()
		for (t in type_iterator)
		{
			if (ch == t.ch || ch in t.Additional) return t
		}
		return type.unknownOrVariable

	}
	fun isWType(t: type) = t == type.plus || t == type.minus || t == type.exp || t == type.div || t == type.mod || t == type.equal
	
    }
    enum class type(val ch: Char, val Additional: List<Char> = listOf<Char>())
    {
        plus('+', listOf(' ')),
        minus('-'), exp('*'), div('/'),
        mod('%'), oBracket('('), cBrocket(')'), 
	equal('='), pow('^'), unknownOrVariable(' ') {
		public fun notNeedThing() = println("just test")
	};
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
                    pow -> operandFirst.pow(operandSecond.toInt())
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
		fun onOfDebug() {
		 debugEnabled = !debugEnabled
		 println("debug enabled is $debugEnabled")
		}
		val commandsMap = mapOf("/help" to ::doHelp, "/exit" to ::doExit, "/debug" to ::onOfDebug)
		//val commandsText = listOf("/help", "/exit")
		//val commandsFuns = listOf(::doHelp, ::doExit)
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
			if (v.first() != '/') println("Unknown variable") //println("Bad variable $v")
			else {
				val comIDX = calculator.commands.commandsMap.contains(v)
				if (comIDX == false) println("Unknown command") //: $v, try /help")
				else calculator.commands.commandsMap[v]!!()
			}
		} catch (e: Parser.invalidException) {
			println("Invalid expression")
			if (debugEnabled) println(e.toString())
		} catch(e: java.lang.NumberFormatException) {
			println("Invalid expression")
			if (debugEnabled) println(e.toString())
		} catch(e: Parser.invalidIdentifierException) {
			if (debugEnabled) println(e.toString())
			println("Invalid identifier")
		} catch(e: Parser.invalidAssigment) {
			println("Invalid assignment")
		} catch(e: Parser.PreParserException) {
			println("Pre parser exception: ${e.toString()}")
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
    class invalidIdentifierException : Exception()
    class invalidAssigment: Exception()
    companion object {
	private fun debugParse(msg: String) = if (debugEnabled) println("[PARSER DEBUG] $msg") else {} // 
	private fun _checkIsValidException(l: String) {
		// check to brackets etc
		// TODO: to preparser check the 
		if (l.split('=').size > 2) throw invalidAssigment()
		if (l.count { it == '(' } - l.count { it == ')' } != 0 || Regex("\\d+(\\s+)?(\\*|\\/|\\^){2,}(\\s+)\\d+").find(l) != null) throw invalidException()
	}
	private fun pre_parse(line: String): Pair<MutableList<calculatorDefType>, MutableList<Operation.anyOperator>> {
		_checkIsValidException(line)
		fun createRegexForExpr(w: String) = Regex("\\d+(\\.\\d+)?\\s*$w\\s*\\d+(\\.\\d+)?")

		val replaceBracketToNum = {
				l: String ->
				//todo: fit it regex for ((1+1)+2)+3 like expression. but for now i think is ok
				// digit just to \d+(\.\d+)? in future version
				val bracketRegex = Regex("\\(\\d+(\\.\\d+)?(\\s+)?((\\+|-|\\*|^|%)(\\s+)?\\d+(\\.\\d+)?)?((\\s?)(\\d+)?(\\+|-|\\*|^|%)?(\\s+)?\\d+(\\.\\d+)?)+\\)") // check
				val bracketNum = Regex("\\(\\d+(\\.\\d+)?\\)") // 
				
				val matches_bracketNum = bracketNum.findAll(l)
				val nums = matches_bracketNum.map { it.groupValues[0] }.toList()
		
				debugParse("replaceBracketToNum")
				var r = l 
				for(num in nums) {
					if (debugEnabled) println("r for now: $r")
					r = r.replace(num, num.replace("(","").replace(")",""))
				}
				
				//val brackets = r.split(bracketRegex) // IS NOT WORKS
				val matches = bracketRegex.findAll(r)
				val expr = matches.map { it.groupValues[0] }.toList()
				// from last bracket to first bracket i think
				for (e in expr.size - 1 downTo 0) {
					val w = expr[e].replace("(","").replace(")","")
					if (debugEnabled) println("Expression: $e")
					if (debugEnabled) println("$w")
					r = r.replace(w, doParse(w).toString())
					if (debugEnabled) println("r for now is $r")
				}
				r
		}
		fun getWithoutBrackets(l: String): String {
			var ret = l
			while ('(' in ret || ')' in ret) {
				ret = replaceBracketToNum(ret)
			}
			return ret
		}

		fun HighPriorityCalculate(s: String): String {
			var ret = s
			
			fun debugHighPriority(M: String) = if(debugEnabled) println("[HIGH PRIORITY DEBUG] $M") else {}
			
			val powRegex = createRegexForExpr("\\^")
			val matches_pow = powRegex.findAll(s)
			val expr_pow = matches_pow.map { it.groupValues[0] }.toList()
			
			debugHighPriority("expr_pow: ${expr_pow}")
			val expRegex = createRegexForExpr("\\*")
			val matches_exp = expRegex.findAll(s)
			
			val expr_exp = matches_exp.map { it.groupValues[0] }.toList()
			debugHighPriority("expr_exp: ${expr_exp}")
			
			val divRegex = createRegexForExpr("/")
			val matches_div = divRegex.findAll(s)
			val expr_div = matches_div.map { it.groupValues[0] }.toList()
			debugHighPriority("expr_div: ${expr_div}")
			
			doPow@for(p in expr_pow) {
			    debugHighPriority("expr(expr_pow): $p")
			    ret = HighPriorityCalculate(ret.replace(p, doParse(p).toString()))
			    debugHighPriority("ret now: $ret")
			}
			doExp@for(ex in expr_exp) {
			    debugHighPriority("expr(expr_exp): $ex")
			    ret = HighPriorityCalculate(ret.replace(ex, doParse(ex).toString()))
			    debugHighPriority("ret now: $ret")
			}
			doDiv@for(d in expr_div) {
			    debugHighPriority("expr(expr_div): $d")
			    ret = HighPriorityCalculate(ret.replace(d, doParse(d).toString()))
			    debugHighPriority("ret now: $ret")
			}
			debugHighPriority("return: $ret")
			return ret
	      } // stack overflow FIX.!~		
		
		// 

				
		// TODO: NORMAL NAME FOR VARIABLE HERE
		val isVariableSet = Regex("^[a-zA-Z]*\\s*=\\s*(-|\\+)?(\\d|[a-zA-Z])*\\b").matches(line.trim())
		//todo. only for test. is optional i think in real practice that exception
		if (Regex("^\\w*\\d\\w*?\\b").matches(line.trim())) throw invalidIdentifierException()
		
		// DIVIDE is not works coRRECTLY. because is conflicts with commands for now. but for our solution is optional. butfor  good practice fix it.
		
		var _line = line // _line is line with changed name of variables to them values
		debugParse("operation global vars: ${Operation.globalVars}")
		for(p in Operation.globalVars) {
			val (key, value) = p
			debugParse("$key = $value")
			val regForVar = Regex("$key\\b")
			_line = _line.replace(regForVar, value.toString())
			debugParse("mLine ${_line}")
		}
		//println("Line now: ${_line}")
		
		val withoutBrackets = getWithoutBrackets(_line)
		debugParse("withoutBrackets: $withoutBrackets")
		var lineWVariables = 
		if (!createRegexForExpr("\\*").matches(withoutBrackets) && !createRegexForExpr("\\^").matches(withoutBrackets) && !createRegexForExpr("\\/").matches(withoutBrackets)) 
		HighPriorityCalculate(withoutBrackets) else withoutBrackets
		
		// TODO: check there on abc=a*2 like operation. but for now is ok
		if (isVariableSet) {
			debugParse("Is variable set")
			val _gV = Operation.globalVars
			val _s = line.filter { it != ' ' }.split('=')
			val f = _s[0]
			val s = _s[1]
			if (isBadVarName(f)) throw invalidIdentifierException()
			// if second value from f=s not number
			if (!Regex("^(-|\\+)?(\\d)*\\.?\\d*?$").matches(s)) {
				if (isBadVarName(s)) throw invalidIdentifierException()
				debugParse("operator that in right part is not num. check to $s key")
				if (!_gV.containsKey(s)) throw badVariableException(s)
				debugParse("_gV contains key $s")
				val realValue = calculatorDefType(_gV[s].toString())
				debugParse("realValue pre inited")
				_gV[f] = realValue
				debugParse("_gV")
				debugParse("Now value for _gV set by other variable ${s}")
				// there will be exit from fun
				return Pair(mutableListOf(realValue), mutableListOf(Operation.variable(f, realValue)))
			} else {
				debugParse("is numeric")
				/*
				 *	Is sets in another place of code. search by TODO (full) (calculateNums)
				 * */
			}
			
		}

		val wSpace = lineWVariables.split(" ")
		if (wSpace.size > 1) {
			var foundOperater = false
			for (w in wSpace)
			{
				val tmp = Operation.getType(w.firstOrNull()?: ' ')
				if ( Operation.isWType(tmp) ) foundOperater = true
				else debugParse("$w not found operator")
			}
			if (!foundOperater) throw invalidException()
			debugParse("found operator")
		}
		debugParse("isVariableSet: $isVariableSet and $line ")
		if (!isVariableSet && '=' in lineWVariables) throw invalidIdentifierException()
		val withoutSpace = if (!isVariableSet) lineWVariables.filter { it != ' ' } else line.filter { it != ' ' }
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
	      	
		val parsed_characters = parseCharacters(characters, numbers)
		debugParse("MutableList<Char> = ${characters}")
		debugParse("MutableList<calculatorDefType(${calculatorDefTypeText})> = ${numbers}")
		debugParse("Parsed charactersList: ${parsed_characters}")

		return Pair(numbers, parsed_characters)
	}
	private fun isBadVarName(s: String): Boolean {
		debugParse("badvarname check $s")
		s.forEach {
			if (it !in 'a'..'z' && it !in 'A'..'Z') return true
		}
		return false
	}
	// [deprecated] have to be deleted in future version.
	private fun parseCharacters(list: MutableList<Char>, numbers: MutableList<calculatorDefType>): MutableList<Operation.anyOperator>
	{
		val rline = list.joinToString("")
		if (Regex("\\/\\w+").matches(rline)) {
			debugParse("Is command")
			throw badVariableException(rline)
		}
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
			else if (tmp != Operation.type.unknownOrVariable && variableText.isEmpty()) 
			{
				debugParse("Is not variable")
				if (variableText.isEmpty()) returnValue.add(Operation.r_operator(element.toString(), tmp))
			}
			else if (tmp == Operation.type.unknownOrVariable){
				debugParse("(parseCharacters) variable text")
				variableText += element
			} //else variableText += element
		}
		//TODO("CHECK if variable inited. if yeap not throw")
		if (!variableText.isEmpty()) {
			debugParse("variableText exists $variableText")
			//println("variableText: $variableText")
			throw badVariableException(variableText)
		}
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
		debugParse("check to difSize")
		if (difOfSize == 0 && sListOperations > 0 && !listOperations[0].isVariable) throw invalidException()
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
	private fun calculateNums(w: MutableList<Pair<Operation.anyOperator, calculatorDefType>>): calculatorDefType?
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
			val variableName = fOp.name
			variableName.forEach {
				if (it !in 'a'..'z' && it !in 'A'..'Z') throw invalidException()
			}
			Operation.globalVars[variableName] = calculatorDefType(variableVal.toString())
			debugParse("OPERATION GLOBAL VARS: ${Operation.globalVars}")
			debugParse("new $variableName = $variableVal")
			return null //Operation.globalVars[variableName]!!
			//TODO("full variable supports")
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
fun main(args: Array<String>) {
	if (args.contains("-d")) debugEnabled = true
	calculator().run()
}
