
public class TranscationDelay {
	
	private long startMills;
	private long endMills;
	
	public final static long TX_COUNT = 6;
	public final static float TX_INTERVAL = 1000.0f / TX_COUNT;

	public void startTimestamp() {
		this.startMills = System.currentTimeMillis();
		// System.out.println("start time: " + startMills);
	}
	
	public void endTimestamp() {
		this.endMills = System.currentTimeMillis();
		// System.out.println("end time: " + endMills);
	}
	
	
	public void sleep() {
		long useMills = endMills - startMills;
		
		if(TX_INTERVAL <= useMills) {
			return;
		}
		
		long leftMills = (long)(TX_INTERVAL - useMills);
		
		try {
			Thread.sleep(leftMills);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isTimeUp() {
		long useMills = endMills - startMills;
		return (TX_INTERVAL <= useMills);
	}
}
