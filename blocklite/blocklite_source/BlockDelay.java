
public class BlockDelay {
	
	private long startMills;
	private long endMills;
	
	public final static long BLOCK_COUNT = 1;
	public final static float BLOCK_INTERVAL = 1 * 60 *1000.0f / BLOCK_COUNT;

	public void startTimestamp() {
		this.startMills = System.currentTimeMillis();
		//System.out.println("start time: " + startMills);
	}
	
	public void endTimestamp() {
		this.endMills = System.currentTimeMillis();
		//System.out.println("end time: " + endMills);
	}
	
	
	public void sleep() {
		long useMills = endMills - startMills;
		
		if(BLOCK_INTERVAL <= useMills) {
			return;
		}
		
		long leftMills = (long)(BLOCK_INTERVAL - useMills);
		
		try {
			Thread.sleep(leftMills);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isTimeUp() {
		long useMills = endMills - startMills;
		return (BLOCK_INTERVAL <= useMills);
	}
}
