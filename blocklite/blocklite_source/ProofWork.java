
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ProofWork implements Proofable {
	
	private int mainDiff = 1;
	private int subDiff = 0;
	
	private int nonce = 0;
	private int blockCount = 0;
	
	public ProofWork() {
		;
	}
	
	public int countOfZero(String str) {
		int count = 0;
		if(str != null) {
			char[] chars = str.toCharArray();
			for(char c : chars) {
				if(c == '0') {
					count++;
				}
			}
		}
		return count;
	}
	
	public int getMainDiff() {
		return mainDiff;
	}

	public void setMainDiff(int mainDiff) {
		this.mainDiff = mainDiff;
	}

	public int getSubDiff() {
		return subDiff;
	}

	public void setSubDiff(int subDiff) {
		this.subDiff = subDiff;
	}
	
	public String formatStamp(Timestamp stamp) {
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(stamp);
	}
	
	public boolean verifyProof(Block block) {
		String uBlokckID = block.getBlockID();
		String hash = calculateHash(block);
		return uBlokckID.equals(hash);
	}

	//Calculate new hash based on blocks contents
	private String calculateHash(Block block) {
        String calculatedhash = StringUtil.applySha256( block.getParentBlockID() + block.getCreationTime().getTime() + Integer.toString(nonce) );
		return calculatedhash;
	}

	//Increases nonce value until hash target is reached.
	public String generateProof(Block block) {
		
		String uBlokckID = block.getBlockID();
		
		String target = StringUtil.getDificultyString(mainDiff); //Create a string with difficulty * "0"
		
		if(uBlokckID.substring( 0, mainDiff).equals(target) && countOfZero(uBlokckID) >= (mainDiff + subDiff) ) {
			System.out.println("Block Added!!! : nonce = " + nonce + ", hash = "  + uBlokckID);
		} else {
			
			while(!uBlokckID.substring( 0, mainDiff).equals(target) || countOfZero(uBlokckID) < (mainDiff + subDiff)) {
				nonce ++;
				uBlokckID = calculateHash(block);
			}
			blockCount += 1;
			
			ArrayList<Transaction> txnList = block.getTxnList();
			System.out.println("Block Mined!!! : nonce = " + nonce + ", diffcult " + mainDiff + "." + subDiff + ", hash = "  + uBlokckID + ", transaction counts = " + txnList.size() + ", block counts = " + blockCount);
		}
		
		return uBlokckID;
	}
}
