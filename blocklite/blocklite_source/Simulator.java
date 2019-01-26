import java.sql.Timestamp;
import java.util.Random;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Iterator;
import java.util.HashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class Simulator {
	
	private int numPeers;
	
	private int mainDiff = 1;
	private int subDiff = 0;
	private int expectIntervalMills;
	private int blockIntervalMills;
	
	private double minPropDelay = 10;
	private double maxPropDelay = 500;
	private double qDelayParameter = 12.0/1024.0;
	
	private Boolean[] nodeTypes = null;
	private Boolean[][] connectionArray = null;
	
	private Double[] cpuPower = null;
	private Double[] txnMean = null;
	private Double[][] bottleNeck = null; 
	private Double[][] propagationDelay = null;
	
	private long startMills;
	private long maxMills; 
	
	//Priortiy Queue of events to be executed and finished
	private PriorityQueue<Event> pendingEvents = new PriorityQueue<Event>();
	private PriorityQueue<Event> finishedEvents = new PriorityQueue<Event>();
	
	private Block genesisBlock  = null;
	private ArrayList<Node> nodeList = new ArrayList<Node>();
	
	private PrintWriter blockChainHistory = null;
	private PrintWriter blockCreateTime = null;
	
	private PrintWriter transactionReceive = null;
	private PrintWriter txCreateTime = null;
	
	private int txnCnt=0;
	private int blockCnt=0;
	private int staleCnt=0;
	private int totalCnt=0;
	
	public static int RUN_MILLS = 10 *60 * 1000;
	
	/*public static int[] DIFFCULT_MILLS = {
			10, 30, 100, 500, 1000, 
			2000, 5000, 10000, 13000, 20000, 
			500000, 80000, 120000, 180000, 250000};*/
	
	public static void main(String[] args) {
		Simulator simulator = new Simulator();
		simulator.run(args);
	}
	
	public void run(String[] args) {
		
		this.numPeers = readIntArg(args, 0, 4);
		this.expectIntervalMills = readIntArg(args, 1, 10 * 60 * 1000);
		
		if(false == start() ) {
			return;
		}
		
		selectDiffcult();		
		createGenesisBlock();
		createConectionGraph();
		configPropagationDelay();
		initBlockAndvent();
		executeEvent();
		writeHistory();
	}
	
	private int readIntArg(String[] args, int index, int defaultValue) {
		int result = defaultValue;
		
		if(args != null && args.length > index) {
			try{
				result = Integer.parseInt(args[index]);
			} catch(Exception e) {
				e.printStackTrace();
				result = defaultValue;
			}
		} 
		
		return result;
	}
	
	private void selectDiffcult() {
		int gap = 0;
		int minGap = Integer.MAX_VALUE;
		
		int[][] diffcultMills = MinnerTimer.readDiffcultsMills();
		
		if(diffcultMills == null) {
			diffcultMills = MinnerTimer.runMillsOfDiffcults();
		}
		
		this.mainDiff = 5;
		this.subDiff = 0;
		
		for (int i=0; i<diffcultMills.length; i++ ) {
			for(int j=0; j<diffcultMills[i].length; j++ ) {
				gap = Math.abs(diffcultMills[i][j] - expectIntervalMills);
				if(gap < minGap) {
					minGap = gap;
					mainDiff = i;
					subDiff = j;
					blockIntervalMills = diffcultMills[i][j];
				}
			}
		}
		
		System.out.println("select diffcult: " + mainDiff + "." + subDiff + ", expectIntervalMills: " +  expectIntervalMills + ", realIntervalMills: " + blockIntervalMills);
	}
	
	private boolean start() {
		
		minPropDelay = 10;
		maxPropDelay = 500;
		qDelayParameter = 12.0/1024.0;
		
		nodeList.removeAll(nodeList);
		pendingEvents.removeAll(pendingEvents);
		finishedEvents.removeAll(finishedEvents);
		
		this.startMills = System.currentTimeMillis();
		this.maxMills = startMills + RUN_MILLS; 
		
		Timestamp start= new Timestamp(startMills);
		System.out.println("start at: " + start);
		
		try {
			txCreateTime = new PrintWriter("transactionCreate.txt", "UTF-8");
			transactionReceive = new PrintWriter("transactionReceive.txt", "UTF-8");
			blockChainHistory = new PrintWriter("blockChainHistory.txt", "UTF-8");
			blockCreateTime = new PrintWriter("blockCreateTime.txt", "UTF-8"); 
		} catch(IOException ex){
			ex.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	//Genesys Block
	private void createGenesisBlock() {
		Timestamp genesisTime = new Timestamp(System.currentTimeMillis());
		this.genesisBlock = new Block("genesis", genesisTime);
	
		//Generating numPeers number of nodes with randomly choosing fast and slow property
		//type true for fast nodes and false for lazy nodes
		this.nodeTypes = new Boolean[numPeers];
		Random randType = new Random(System.nanoTime());
		for(int i=0; i<numPeers; i++){
			String nodeID = "Node_"+i;
			boolean type = (randType.nextInt()%2==0);
			nodeTypes[i] = type;
			Timestamp creationTime = new Timestamp(System.currentTimeMillis());
			Node newNode = new Node(nodeID, type, creationTime, genesisBlock);
			nodeList.add(i,newNode);
		}
	}
	
	//to create a connencted graph with each node connected to a random number of other nodes
	private void createConectionGraph() {
		this.connectionArray = new Boolean[numPeers][numPeers];
		for(int i = 0; i<numPeers; i++){
			for(int j = 0; j<numPeers; j++){
				connectionArray[i][j]=false;
			}
		}
		
		Random connRand = new Random(System.nanoTime());
		int n1Num = connRand.nextInt(numPeers);
		Boolean[] tempConnection = new Boolean[numPeers];
		for(int i = 0; i<numPeers; i++){
			tempConnection[i] = false;
		}
		
		tempConnection[n1Num] = true;
		int newNum = connRand.nextInt(numPeers);
		while(tempConnection[newNum]){
			newNum = connRand.nextInt(numPeers);
		}
		
		tempConnection[newNum] = true;
		connectionArray[n1Num][newNum] = true;
		connectionArray[newNum][n1Num] = true;
		
		int numNode = 1;
		while (numNode <= numPeers){
			newNum = connRand.nextInt(numPeers);
			while(tempConnection[newNum]) {
				newNum = connRand.nextInt(numPeers);
			}
			int oldNum = connRand.nextInt(numPeers);
			while(!tempConnection[oldNum]){
				oldNum = connRand.nextInt(numPeers);
			}
	
			connectionArray[oldNum][newNum] = true;
			connectionArray[newNum][oldNum] = true;
			numNode++;
		}
	
		int maxRemainingEdges = ((numPeers-1)*(numPeers-2))/2;
		int remainingEdges = connRand.nextInt()%maxRemainingEdges;
		while(remainingEdges>0){
			int i = connRand.nextInt(numPeers);
			int j = connRand.nextInt(numPeers);
			if(!connectionArray[i][j]){
				connectionArray[i][j] = true;
				connectionArray[j][i] = true;
				remainingEdges--;
			}
		}
	}
	
	private void configPropagationDelay() {
		//Creating a 2D array to store Propagation delay between each pair of nodes
		this.propagationDelay = new Double[numPeers][numPeers];
		Random randProp = new Random(System.nanoTime());
		for(int i=0; i<numPeers; i++){			
			for(int j=0; j<numPeers; j++){
				if(i<=j){					
					boolean makeConnection = connectionArray[i][j];
					if(makeConnection == false ) {
						continue;
					}
					
					nodeList.get(i).connect2Node(nodeList.get(j));
					propagationDelay[i][j] = minPropDelay + randProp.nextDouble()*(maxPropDelay - minPropDelay);
					
					//To mantain the symmetry of the propagation delay
					nodeList.get(j).connect2Node(nodeList.get(i));
					propagationDelay[j][i] = propagationDelay[i][j];
				}
			}
		}
	
		//Creating a array to store the bottle neck link between each pair of nodes
		this.bottleNeck = new Double[numPeers][numPeers];
		for(int i=0; i<numPeers; i++){
			for(int j=0; j<numPeers; j++){
				if(connectionArray[i][j]){
					if(nodeList.get(i).getType() && nodeList.get(j).getType()) {
						bottleNeck[i][j] = 100.0;
					} else {
						bottleNeck[i][j] = 5.0;
					}
				}								
			}
		}
	
		//Assigning mean to generate T_k later for each node from an exponential distribution
		this.cpuPower = new Double[numPeers];
		Random randCpu = new Random(System.nanoTime());
		for(int i=0; i<numPeers; i++){
			double cpuMean = 10 + randCpu.nextDouble()*1;
			cpuPower[i] = 1/cpuMean;
		}
	
		//Assigning mean to generate transaction later for each node from an exponential distribution
		this.txnMean = new Double[numPeers];
		Random randMean = new Random(System.nanoTime());
		for(int i=0; i<numPeers; i++){
			double tempTxnMean = 50 + randMean.nextDouble()*50; // deterministic: constant value for more experiments
			txnMean[i] = 1/tempTxnMean;
		}
	}
	
	private void initBlockAndvent() {
		
		//****master node modification to make deterministic
		// long simTime = 1000*1000;
		// this.maxMills = this.startMills + (long)(Math.random()*simTime);
		
		//Every node here tries to generate a block on the genesis block
		for(int i=0; i<numPeers; i++){
			//*****master node change to make deterministic
			Timestamp nextBlockTime = nextBlockTimestamp(this.startMills, cpuPower[i]);
			
			//register a new block generation event
			Block newBlock = nodeList.get(i).generateBlock(genesisBlock, nextBlockTime, mainDiff, subDiff);
			Event newEvent = new Event(Event.GENERATE_BLOCK_EVENT, newBlock, nextBlockTime, i);
			nodeList.get(i).nextBlockTime = nextBlockTime;
			pendingEvents.add(newEvent);
		}
	
		//To generate initial set of transactions to start the simulator
		for(int i=0; i<numPeers; i++) {
			//****master node modification to make deterministic
			Timestamp nextTxnTime = nextTranscationTimestamp(this.startMills, txnMean[i]);
			nodeList.get(i).nextTxnTime = nextTxnTime;
			
			int rcvNum = randInt(numPeers, i);
			String receiverID = nodeList.get(rcvNum).getUID();
			Transaction newTransaction = nodeList.get(i).generateTxn(receiverID, 0, nextTxnTime);
			
			//register generate transcation event
			Event newEvent = new Event(Event.GENERATE_TRANSCATION_EVENT, newTransaction, nextTxnTime);
			pendingEvents.add(newEvent);
		}
	}
	
	private void executeEvent() {
		//Timestamp of the next event to be executed
		Timestamp nextEventTime = pendingEvents.peek().getEventTimestamp();
		Iterator<Event> eventItr = pendingEvents.iterator();
		
		long runMills = System.currentTimeMillis() - startMills;
		
		PrintWriter eventExecuteTime = null; 
		try {
			eventExecuteTime = new PrintWriter("eventExecuteTime.txt", "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		while(nextEventTime.getTime() < this.maxMills && eventItr.hasNext() ) {
			Event nextEvent = pendingEvents.poll();
			finishedEvents.add(nextEvent);
			
			if(eventExecuteTime != null) {
				eventExecuteTime.println(nextEvent.getEventName() + ", nextEventTime: " + nextEventTime + ", nextEvent.getEventTimestamp(): " + nextEvent.getEventTimestamp());
			}
			
			Timestamp eventTime = null;
			
			if(nextEvent.getEventType() == Event.RECEIVE_BLOCK_EVENT) {
				eventTime = hanldeReceiveBlockEvent(nextEvent, nextEventTime);
			} else if(nextEvent.getEventType() == Event.GENERATE_BLOCK_EVENT) {
				eventTime = hanldeGenerateBlockEvent(nextEvent, nextEventTime);
			} else if(nextEvent.getEventType() == Event.RECEIVE_TRANSCATION_EVENT) {
				eventTime = hanldeReceiveTransactionEvent(nextEvent, nextEventTime);
			} else if(nextEvent.getEventType() == Event.GENERATE_TRANSCATION_EVENT){
				eventTime = hanldeGenerateTransactionEvent(nextEvent, nextEventTime);
			} else {
				System.out.println("Error: Wrong Eventtype Detected.");
			}
			
			if( eventTime != null ) {
				nextEventTime = eventTime;
			} 
//			else {
//				System.out.println("wrong event time");
//			}
			
			//pendingEvents.
			
			runMills = System.currentTimeMillis() - startMills;
			
		}//All types of events execution finished here
		
		System.out.println("run end at: " + new Timestamp(System.currentTimeMillis()) + ", total time: " + runMills + " ms");
		System.out.println("Simulator mills: "+ RUN_MILLS);
		System.out.println("total Block: " +  totalCnt + ", stale block: "+ staleCnt + ", sucess block: "+ blockCnt+", Transaction Count: " + txnCnt);
//		System.out.println("Generate Block: " + (RUN_MILLS*1.0f/blockCnt) + " ms, Generate Transaction : " + RUN_MILLS*1.0f/txnCnt + " ms");
		System.out.println("Generate Block: " + (RUN_MILLS*1.0f/blockCnt/60000) + " b/min, Generate Transaction : " + RUN_MILLS*1.0f/txnCnt/60000 + " t/s");
		
		if(eventExecuteTime != null) {
			eventExecuteTime.close();
		}
	}
	
	private Timestamp hanldeReceiveBlockEvent(Event event, Timestamp eventTime) {
		//Code to execute receive Block event
		int currentNum = event.getReceiverNum();
		Node currentNode = nodeList.get(currentNum);

		Block currentBlock = event.getEventBlock();
		String currentBlockID = currentBlock.getBlockID();
		
		if(currentNode.checkForwarded(currentBlockID)) {
			return null;
		} else {
			currentNode.addForwarded(currentBlockID);
		}
		
		if(currentNode.addBlock(currentBlock)) {
			//check if any pending blocks can be added
			currentNode.addPendingBlocks();
		}
							
		if(currentBlock.getDepth() > currentNode.probParentBlock.getDepth()) {
			//updating the probable parent block
			currentNode.probParentBlock = currentBlock;
			currentNode.calculateBTC();
			
			//to Generate the next block for the sending node
			Timestamp newBlockTime = nextBlockTimestamp(eventTime.getTime(), cpuPower[currentNum]);
			currentNode.nextBlockTime = newBlockTime;
			Block newBlock = currentNode.generateBlock(currentBlock, newBlockTime, mainDiff, subDiff);
			Event newEvent = new Event(Event.GENERATE_BLOCK_EVENT, newBlock, newBlockTime, currentNum);
			newEvent.blockDelay = new BlockDelay();
			newEvent.blockDelay.startTimestamp();
			pendingEvents.add(newEvent);			
		}
		
		// transfer the block to other peers nodes
		transferBlock2Peers(currentNum, currentBlock, eventTime);
		
		//Timestamp of the next event to be executed
		blockChainHistory.println("Block "+currentNum+" received "+currentBlockID+" at depth "+ currentBlock.getDepth());
		return pendingEvents.peek().getEventTimestamp();
	}
	
	private Timestamp hanldeGenerateBlockEvent(Event nextEvent, Timestamp nextEventTime) {
		//Code to execute generate Block
		int creatorNum = nextEvent.getCreatorNum();
		Node currentNode = nodeList.get(creatorNum);
		Block currentBlock = nextEvent.getEventBlock();
		Timestamp nextBlockTime = currentNode.nextBlockTime;
		
		/*boolean timeUp = true;
		  if(nextEvent.blockDelay != null) {
		   nextEvent.blockDelay.endTimestamp();
		   timeUp = nextEvent.blockDelay.isTimeUp();
		}
		  
		if(timeUp == false){
			finishedEvents.remove(nextEvent);
			pendingEvents.add(nextEvent);
		} else*/ if (!( nextBlockTime.after(nextEventTime) || nextBlockTime.before(nextEventTime) )) { 
			// Only execute this if the node still decides to execute it
			// adding pending transaction to the new block
			currentNode.addPendingTx2NewBlock(currentBlock);
			
			//adding a status message to keep track that the newly created block is going to be added in current node 
			currentNode.addForwarded(currentBlock.getBlockID());
			
			//**************time tacking***********
			//currentBlock.createTime = System.currentTimeMillis();
			//*************time tracking ends******
			 
			 //finally adding the block in the current node
			boolean addBlockSuccess = currentNode.addBlock(currentBlock);
			currentNode.probParentBlock = currentBlock;
			currentNode.calculateBTC();
			
			totalCnt++;
			if(addBlockSuccess && currentBlock.verifyHash()) { //if adding block in current node is successful, transfer the block to other peers nodes
				blockCnt += 1;
				blockCreateTime.println("Block created "+currentBlock.getBlockID()+" by "+ creatorNum+" Time:"+currentBlock.getCreationTime());
				String intervalTime = String.valueOf(currentBlock.getCreationTime().getTime() - currentBlock.getParentBlock().getCreationTime().getTime());
				blockChainHistory.println("Node "+creatorNum+" created Block "+currentBlock.getBlockID()+ " at Depth "+ currentBlock.getDepth() + " ON "+currentBlock.getParentBlockID()+" add interval:"+ intervalTime);
				
				transferBlock2Peers(creatorNum, currentBlock, nextEventTime);
		
			} else {
				staleCnt++; // stale block
			}
			
			//to Generate the next block for the sending node
			Timestamp newBlockTime = nextBlockTimestamp(nextEventTime.getTime(), cpuPower[creatorNum]);

			Block newBlock =currentNode.generateBlock(currentBlock, newBlockTime, mainDiff, subDiff);
			
			//Generate new block by the sending node for the purpose of next transaction
			Event newEvent = new Event(Event.GENERATE_BLOCK_EVENT, newBlock, newBlockTime, creatorNum);
			newEvent.blockDelay = new BlockDelay();
			newEvent.blockDelay.startTimestamp();
			pendingEvents.add(newEvent);
		}
		
		//Updating the time to execute next evnet
		return pendingEvents.peek().getEventTimestamp();	
	}
	
	private Timestamp hanldeReceiveTransactionEvent(Event nextEvent, Timestamp nextEventTime) {
		// Code to execute receive Transaction
		int receiverNum = nextEvent.getReceiverNum();
		int senderNum = nextEvent.getSenderNum();
		Node currentNode = nodeList.get(receiverNum);
		Transaction newTxn = nextEvent.getEventTransaction();
		String newTxnID = newTxn.getTxnID();
		
		// Only execute if it has not already forwarded the same transaction earlier
		if(!(currentNode.checkForwarded(newTxnID))) { 
			currentNode.addForwarded(newTxnID);
			
			// add transactions to allTxns list 
			if(!currentNode.allTxns.contains(newTxn)){
				currentNode.allTxns.add(newTxn);
			} // end
			
			int txnReceiverNum = Integer.parseInt((newTxn.getReceiverID()).split("_")[1]);
			transactionReceive.print("Transaction Id "+ newTxnID+" Money receiver :"+txnReceiverNum+" "+"Message Receiver :"+receiverNum);
			if(txnReceiverNum == receiverNum){ //checking the transaction is meant for that node or not
				if(currentNode.addTxn(newTxn)) {
					transactionReceive.print(" Money Added!!");
				} else {
					transactionReceive.print(" Canceled!!");
				}
			}
			transactionReceive.println();
			
			// transfer the new transcation to other peers nodes
			transferTranscation2Peers(receiverNum, senderNum, newTxn, nextEventTime);
			
			//Timestamp of the next event to be executed
			return nextEvent.getEventTimestamp();
		}
		
		return null;
	}
	
	private Timestamp hanldeGenerateTransactionEvent(Event nextEvent, Timestamp nextEventTime) {
		//Code to handle generate Transaction event
		txnCnt += 1;
		Transaction newTxn = nextEvent.getEventTransaction();
		String senderID = newTxn.getSenderID();
		int senderNum = Integer.parseInt(senderID.split("_")[1]);
		Node currNode = nodeList.get(senderNum);
		
		/*boolean timeUp = true;
		  if(nextEvent.txnDelay != null) {
		   nextEvent.txnDelay.endTimestamp();
		   timeUp = nextEvent.txnDelay.isTimeUp();
		}
		  
		if(timeUp == false){
			finishedEvents.remove(nextEvent);
			pendingEvents.add(nextEvent);
			return pendingEvents.peek().getEventTimestamp();
		}*/

		//random to generate an amount for the transaction
		Random updateRand = new Random(System.nanoTime());
		float newAmount = updateRand.nextFloat()*currNode.getCurrOwned();
		newTxn.updateAmount(newAmount);

		//add transactions to allTxns list 
		if(!currNode.allTxns.contains(newTxn)){
			currNode.allTxns.add(newTxn);
		}//end
		
		//Adding the transaction at the sender end.
		boolean addTxnSuccess = currNode.addTxn(newTxn);
		currNode.addForwarded(newTxn.getTxnID());
		if(addTxnSuccess) {			//proceeding only when the transaction is successfully added
			if (newAmount!=0){
				transactionReceive.println(senderID + " sends money: " + newTxn.getAmount()+ " to " + newTxn.getReceiverID()+" , owned money now: "+ nodeList.get(senderNum).getCurrOwned());
				
				// transfer the new transcation to other peers nodes
				transferTranscation2Peers(senderNum, -1, newTxn, nextEventTime);
			}
			
			//to Generate the next transaction for the sending node
			Timestamp nextTxnTime = nextTranscationTimestamp(nextEventTime.getTime(), txnMean[senderNum]);

			currNode.nextTxnTime = nextTxnTime;
			
			int rcvNum = randInt(numPeers, senderNum);
			String receiverID = nodeList.get(rcvNum).getUID();

			Transaction newTransaction = currNode.generateTxn(receiverID, 0, nextTxnTime);
			Event newEvent = new Event(Event.GENERATE_TRANSCATION_EVENT, newTransaction, nextTxnTime);
			
			newEvent.txnDelay = new TranscationDelay();
			newEvent.txnDelay.startTimestamp();
			pendingEvents.add(newEvent);
			
			txCreateTime.println("Transcation created "+newTransaction.getTxnID()+" by "+ senderNum + " Time:"+newTransaction.getTxnTime() );

			//Updating the time to execute next event
			return pendingEvents.peek().getEventTimestamp();
		}
		
		return null;
	}
	
	// transfer the new block to other peers nodes 
	private void transferBlock2Peers(int senderIdx, Block newBlock, Timestamp nextEventTime) {
		Node currentNode = nodeList.get(senderIdx);
		
		for(int i=0; i<numPeers; i++){
			Node nextNode = currentNode.connectedNodeAt(i);
			if(nextNode == null){
				break;
			}
			int nextNodeIdx = Integer.parseInt(nextNode.getUID().split("_")[1]);
			
			float qDelayP1 = randFloat();
			long qDelay = (long)((-1*Math.log(qDelayP1)*bottleNeck[senderIdx][nextNodeIdx])/qDelayParameter);
			long pDelay = Math.round(propagationDelay[senderIdx][nextNodeIdx]);
			long msgDelay = 0;
			if(bottleNeck[senderIdx][nextNodeIdx]!=null){
				msgDelay = Math.round(1000.0/bottleNeck[senderIdx][nextNodeIdx]);
			}
			Timestamp receiveTime = new Timestamp(nextEventTime.getTime()+ qDelay + pDelay+msgDelay);									
			Event newEvent = new Event(Event.RECEIVE_BLOCK_EVENT, newBlock, receiveTime, nextNodeIdx, senderIdx);
			pendingEvents.add(newEvent);
		}
	}
	
	// transfer the new transcation to other peers nodes 
	private void transferTranscation2Peers(int senderIdx, int superIdx, Transaction newTxn, Timestamp nextEventTime) {
		Node currentNode = nodeList.get(senderIdx);
		for(int i=0; i<numPeers; i++){
			Node nextNode = currentNode.connectedNodeAt(i);							
			if(nextNode == null){
				break;
			} 	
			int nextNodeNum = Integer.parseInt(nextNode.getUID().split("_")[1]);
			if (nextNodeNum != superIdx) {
				double qDelayP1 = randFloat();
				long qDelay = (long)((-1*Math.log(qDelayP1)*bottleNeck[senderIdx][nextNodeNum])/qDelayParameter);
				long pDelay = Math.round(propagationDelay[senderIdx][nextNodeNum]);
				Timestamp receiveTime = new Timestamp(nextEventTime.getTime()+ qDelay + pDelay);
				Event newEvent = new Event(Event.RECEIVE_TRANSCATION_EVENT, newTxn, receiveTime, nextNodeNum, senderIdx);
				pendingEvents.add(newEvent);
			}
		}
	}
	
	private void writeHistory() {
		double sum = 0;
		for(int i=0; i<numPeers; i++){ 
			float value = nodeList.get(i).getCurrOwned();
			sum = sum + value;
			transactionReceive.println(value); //storing all the transactions by all peers
		}
		
		transactionReceive.println("Total money :"+sum); //total transaction
		txCreateTime.close();
		transactionReceive.close();
		blockChainHistory.close();
		blockCreateTime.close();
	
		for(int i=0; i<numPeers; i++){ // Note: all node's block-chain is stored in node.blockchain arraylist
			HashMap<String, Block> tempBlockChain = nodeList.get(i).blockChain; //getting each node's chain
			String root = "genesis";
			String fileName = "file_"+i+".txt";
			try{
				PrintWriter writer = new PrintWriter(fileName,"UTF-8");
				writer.println("Node "+i+", Details:");
				writer.println("Type : "+(nodeTypes[i]?"fast":"slow"));
				writer.println("CPU power : "+cpuPower[i]);
				writer.println("Connected to :");
				for(int j=0; j<numPeers; j++){
					if(connectionArray[i][j]){
						writer.println("NID: "+ nodeList.get(j).getUID() +", PD: "+ propagationDelay[i][j] +", BS: "+ bottleNeck[i][j]);
					}
				}
				writer.println("\nStored Tree:");
	
				printTree(writer ,root, tempBlockChain);
				writer.close();
			}
			catch (IOException e){
				e.printStackTrace();
			}			
		}
	}
	
	public Timestamp nextBlockTimestamp(long baseTime, double mean) {
		// 1. time by simulator 
		double nextTimeOffset = randFloat();
		double nextTimeGap = -1*Math.log(nextTimeOffset)/mean;
		//System.out.println("block nextTimeGap: " + nextTimeGap);
		//return new Timestamp(baseTime + (long)nextTimeGap*1000);
//		long gap = System.currentTimeMillis()% (10 * 1000);
//		int interval = this.blockIntervalMills + (int) gap;
		int interval = this.blockIntervalMills + (int)(nextTimeGap % 10 - 5 ) * 1000;
//		System.out.println("blockIntervalMills: " + this.blockIntervalMills + ", block interval: " + interval);
		
		//int diffcult = 5;
		return new Timestamp(baseTime + interval);
		
		//2. time by 60*1000
		//return new Timestamp(baseTime + 60*1000 + (int)(nextTimeGap % 10 - 5 ) * 1000);
		
		//3. real time
		//return new Timestamp(System.currentTimeMillis());
	}
	
	public Timestamp nextTranscationTimestamp(long baseTime, double mean) {
		// 1. time by simulator
		double nextTimeOffset = randFloat();
		double nextTimeGap = -1*Math.log(nextTimeOffset)/mean;
		//System.out.println("transcation nextTimeGap: " + nextTimeGap);
		//return new Timestamp(baseTime + (long)nextTimeGap*100);
		
		//2. time by 60*1000
		return new Timestamp(baseTime + 1000/6 + (int)(nextTimeGap % 10 - 5));
		
		//3. real time
		//return new Timestamp(System.currentTimeMillis());
	}
	
	public float randFloat() {
		Random randNext = new Random(System.nanoTime());
		float nextValue = randNext.nextFloat();
		while (nextValue == 0.0){
			nextValue = randNext.nextFloat();
		}
		return nextValue;
	}
	
	public int randInt(int base, int except) {
		Random receiveRand = new Random(System.nanoTime());
		int nextValue = receiveRand.nextInt(base);
		while(nextValue == except){
			nextValue = receiveRand.nextInt(base);
		}
		return nextValue;
	}
	
	public static void printTree(PrintWriter writer,String root, HashMap<String, Block> blockChain){		
		Block rootBlock = blockChain.get(root);
		if(rootBlock != null){
			ArrayList<String> childList = blockChain.get(root).getChildList();
			int childListSize = childList.size();
			int i = 0;
			while(i<childListSize){
				String newRoot = childList.get(i);
				printTree(writer, newRoot, blockChain);
				i++;
			}
			Block parent = blockChain.get(root).getParentBlock();
			if(parent != null){
				writer.println(root+","+parent.getBlockID());
			}					
		}
	}
}

