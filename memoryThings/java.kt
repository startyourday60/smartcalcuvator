
fun main()
{
	System.gc()
	val usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(); // https://stackoverflow.com/questions/3571203/what-are-runtime-getruntime-totalmemory-and-freememory
	println("used memory: $usedMemory")
	//println(Runtime.getRuntime().totalMemory())
	//println(Runtime.getRuntime().freeMemory())
}
