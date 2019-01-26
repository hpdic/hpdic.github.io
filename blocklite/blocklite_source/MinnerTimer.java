import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;

public class MinnerTimer {
	
	public final static int MAX_DIFF = 7;
	public final static int MAX_SUBDIFF = 7;
	
	private final static String DIFF_MILLS_FILE = "diffcult_time.csv"; 
	private final static String FILE_ENCODE = "UTF-8";
	
	public static int[][] readDiffcultsMills() {
		File file = new File(DIFF_MILLS_FILE);
		if(false == file.exists()) {
			return null;
		}
		
		int[][] times = null;
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(DIFF_MILLS_FILE), FILE_ENCODE));
			
			times = new int[MAX_DIFF][MAX_SUBDIFF];
			
			for (int i=0; i<MAX_DIFF; i++ ) {
				for(int j=0; j<MAX_SUBDIFF; j++ ) {
					times[i][j] = i * 60 * 1000 + j * 1000;
				}
			}
			
			String line = null;
			String[] strMills = null;
			
			for (int i=0; i<MAX_DIFF; i++ ) {
			
				if( (line = reader.readLine()) != null ) {
					line = line.trim();
				}
				
				if (line == null || line.length() < 1) {
	            	continue;
	            }
				
				strMills = line.split(",");
				
				if(strMills == null || strMills.length < 1) {
					break;
				}
				
				for(int j=0; j<MAX_SUBDIFF && j< strMills.length; j++ ) {
					times[i][j] = Integer.parseInt(strMills[j]);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		System.out.println("read diffcults mills: ");
		
		int last = MAX_SUBDIFF-1;
		for (int i=0; i<MAX_DIFF; i++ ) {
			for(int j=0; j<last; j++ ) {
				System.out.print(times[i][j] + ",");
			}
			System.out.println(times[i][last]);
		}
		
		return times;
	}
	
	public static int[][] runMillsOfDiffcults() {
		
		Timestamp genesisTime = new Timestamp(System.currentTimeMillis());
		Block parent = new Block("genesis", genesisTime);
		Block miner = new Block("uuid", new Timestamp(System.currentTimeMillis()), parent.getBlockID(), parent, null);
		
		int[][] times = new int[MAX_DIFF][MAX_SUBDIFF];
		
		PrintWriter timeWriter = null; 
		try {
			timeWriter = new PrintWriter(DIFF_MILLS_FILE, FILE_ENCODE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		int diffcult = 1;
		int subDiffcult = 0;
		
		long startTime = System.currentTimeMillis();
		
		for(int i=0; i<MAX_DIFF ; i++) {
			
			subDiffcult = 0;
			
			for(int j=0; j<MAX_SUBDIFF ; j++) {
				startTime = System.currentTimeMillis();
				
				ProofWork powProof = new ProofWork();
				powProof.setMainDiff(diffcult);
				powProof.setSubDiff(subDiffcult);
				miner.setProof(powProof);
				miner.mineBlock();
				
				times[i][j] = (int)(System.currentTimeMillis() - startTime);
				System.out.println("Block Timed! " + miner.getBlockID() + ", diffcult " + diffcult + "." + subDiffcult + ", use time: " + times[i][j] + " ms");
				timeWriter.print(times[i][j]);
				
				if(j < MAX_SUBDIFF-1) {
					timeWriter.print(",");
				}
				
				parent = miner;
				miner = new Block("uuid" + parent.getBlockID(), new Timestamp(System.currentTimeMillis()), parent.getBlockID(), parent, null);
				subDiffcult++;
			}
			
			timeWriter.println();
			timeWriter.flush();
			
			diffcult++;
		}
		
		timeWriter.close();
		
		return times;
	}
	
	/*public static void main(String args[]) {
		runMillsOfDiffcults();
		readDiffcultsMills();
	}*/
}

