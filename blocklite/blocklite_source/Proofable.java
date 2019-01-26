
public interface Proofable {
	
	public boolean verifyProof(Block block);

	public String generateProof(Block block);
}
