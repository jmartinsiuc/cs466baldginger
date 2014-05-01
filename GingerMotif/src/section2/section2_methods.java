package section2;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import section2.Alignment;


public class section2_methods {
	int beamWidth = 20;
	ArrayList<char[]> sequenceList = new ArrayList<char[]>();
	Comparator<alignedStrings> comparator = new matchComparator();
    PriorityQueue<alignedStrings> alignQueue;
    ArrayList<int[]> sitesArrs = new ArrayList<int[]>();
	ArrayList<int[][]> profileArrs = new ArrayList<int[][]>();
	ArrayList<alignedStrings> aList = new ArrayList<alignedStrings>();
	int motifLength;


	public section2_methods(int bWidth, String addString){
		beamWidth = bWidth;
		alignQueue = new PriorityQueue<alignedStrings>(beamWidth + 1, comparator);
		getSequences("./src/Inputs/"+addString+"sequences.fa");
		getML("./src/Inputs/"+addString+"motiflength.txt"); 
		for(int i=0;i<beamWidth;i++){
			profileArrs.add(new int[motifLength][4]);
			sitesArrs.add(new int[sequenceList.size()]);
		}
		genInitialProfiles();
		generateFinalProfiles();
		
		int best = getBestProfile();
		
		writeMotif("./src/Outputs/"+addString+"predictedmotif.txt",best);
		writeSitesList("./src/Outputs/"+addString+"predictedsites.txt",best);
	}
	
	/**
	 * This function generates a list of the best aligned strings of 
	 * 		motiflength in the given two sequences, in order of score
	 * @param cs - the first string
	 * @param cs2 - the second string
	 */
	private void genBestAlignment(char[] cs, char[] cs2) {
		Alignment aligner = new Alignment(cs,cs2, motifLength);
		for(int i=0; i<cs.length-motifLength;i++){
			for(int j=0; j<cs2.length-motifLength;j++){
				alignQueue.add(new alignedStrings(i,j, aligner.genScore(i,j)));
				if (alignQueue.size() > beamWidth)
					alignQueue.poll();
			}
		}
		for(int i=0;i<beamWidth;i++){
			aList.add(alignQueue.poll());
			/*System.out.println("at index " + i+ ":");
			System.out.println("aloc:" + aList.get(i).aloc);
			System.out.println("bloc:" + aList.get(i).bloc);
			System.out.println("score:" + aList.get(i).matchValue);*/
		}
	}
	
	/**
	 *  Generates the initial states of all the profiles
	 */
	private void genInitialProfiles(){
		int[] sites;
		genBestAlignment(sequenceList.get(0), sequenceList.get(1));
		for(int i=0;i<beamWidth;i++){
			sites = sitesArrs.get(i);
			sites[0]=aList.get(i).aloc;
			sites[1]=aList.get(i).bloc;
			addScores(i, sequenceList.get(0), sites[0]);
			addScores(i, sequenceList.get(1), sites[1]);
		}
	}
	
	/**
	 *  Generates the final profiles by going through all other sequences for all profiles
	 */
	private void generateFinalProfiles() {
		
		for(int i=0;i<beamWidth;i++){
			for(int j=2;j<sequenceList.size(); j++)addBest(i,j);
		}
	}
	
	/**
	 * This function adds the scores of a specific L-mer to the given profile
	 * @param profileIndex - the current profile
	 * @param cs - the current sequence
	 * @param index - the current location within the sequence
	 */
	private void addScores(int profileIndex, char[] cs, int index) {
		int[][] profile = profileArrs.get(profileIndex);
		for(int i=0; i<motifLength;i++){
			profile[i][getPlace(cs[index+i])]++;
		}
	}

	/**
	 * This function finds and adds the scores of the lmer in the sequence 
	 * 		that best matches the given profile
	 * @param profileIndex -  the index of the given profile
	 * @param index - the index of the given sequence
	 */
	private void addBest(int profileIndex, int index){
		char[] seq = sequenceList.get(index);
		int maxLoc=0;
		double[] scores = new double[seq.length-motifLength];
		scores[0] = genScore(seq, 0, profileIndex);
		for(int i=0; i< scores.length; i++){
			scores[i] = genScore(seq, i, profileIndex);
			if(scores[i]>scores[maxLoc]) maxLoc=i;
		}
		sitesArrs.get(profileIndex)[index] = maxLoc;
		addScores(profileIndex, seq, maxLoc);
	}

	/**
	 * This function generates the score of a character in an L-mer
	 * @param seq - the sequence that the l-er is a part of
	 * @param loc - to location of the l-mer
	 * @param profileIndex - the index of the current profile
	 * @return the score of a character given the current profile and location
	 */
	private double genScore(char[] seq, int loc, int profileIndex) {
		double score = 0;
		int id, ltotal;
		int[][] profile = profileArrs.get(profileIndex);
		for(int i=0; i<motifLength;i++){
			id=getPlace(seq[loc+i]);
			ltotal = profile[i][0] + profile[i][1] + 
						profile[i][2] + profile[i][3];
			score += ((double)profile[i][id]*(double)profile[i][id])
						/(double)(ltotal*ltotal);
		}
		return score;
	}
	
	/**
	 * This function returns the index of the Profile with the maximum score
	 * @return - the index of the Profile with the maximum score
	 */
	private int getBestProfile(){
		double maxScore=-1, score=0;
		int maxLoc=-1;
		for(int i=0;i<beamWidth;i++){
			score = genProfileScore(profileArrs.get(i));
			if(score>maxScore){
				maxScore = score;
				maxLoc=i;
			}
		}
		return maxLoc;
	}
	
	/**
	 * Generates the Score of a profile
	 * @param profile -  the given profile
	 * @return - the Score of the given profile
	 */
	private double genProfileScore(int[][] profile) {
		double score = 0;
		int sizeSquared = sequenceList.size()*sequenceList.size();
		for(int i=0; i<motifLength;i++){
			score += (double)(profile[i][0]*profile[i][0] +
						profile[i][1]*profile[i][1] +
						profile[i][2]*profile[i][2] +
						profile[i][3]*profile[i][3])/(sizeSquared);
		}
		return score;
	}

	/**
	 * returns the integer index for the given character
	 * @param c - the given character can only be ACGT
	 * @return
	 */
	private int getPlace(char c) {
		switch(c){
			case 'A': return 0;
			case 'C': return 1;
			case 'G': return 2;
			case 'T': return 3;
		}
		//error error biz will cause a crash
		return -1;
	}
	
	private void writeMotif(String fileName, int index) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fileName);
			pw.println(">PMOTIF\t\t"+motifLength);
			int[][] profile = profileArrs.get(index);
			for(int i=0;i<motifLength;i++){
				pw.println(profile[i][0] + "\t" + profile[i][1] + "\t" +
						profile[i][2] + "\t" +profile[i][3]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pw.close();
	}
	
	
	private void writeSitesList(String fileName, int index) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fileName);
			pw.println(">"+fileName);
			int[] sites = sitesArrs.get(index);
			for(int i=0;i<sites.length;i++){
				pw.println(sites[i]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pw.close();
	}

	private void getML(String fileName) {
		BufferedReader br = null;
		 
		try {
			br = new BufferedReader(new FileReader(fileName));
			br.readLine();//skip first useless line
			motifLength =  java.lang.Integer.parseInt(br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void getSequences(String fileName) {
		BufferedReader br = null;
		 
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(fileName));
			sCurrentLine = br.readLine();//skip first useless line
			
			while ((sCurrentLine = br.readLine()) != null) {
				sequenceList.add(sCurrentLine.toCharArray());
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * private class that holds two locations and the score of the alignment from those locations
	 *
	 */
	private class alignedStrings{
		int aloc;
		int bloc;
		double matchValue;
		alignedStrings(int a, int b, double m){
			aloc = a;
			bloc = b;
			matchValue = m;
		}
	}
	
	/**
	 *private comparator for the above convenience class
	 */
	private class matchComparator implements Comparator<alignedStrings>	{
	    @Override
	    public int compare(alignedStrings x, alignedStrings y){
	        return  x.matchValue > y.matchValue ? 1:-1;
	    }
	}
}
