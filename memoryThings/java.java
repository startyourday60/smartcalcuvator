class Main {
	public static void main(String[] args) {
		System.gc();
		long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		System.out.println("Used memory: " + usedMemory);
	}
}
