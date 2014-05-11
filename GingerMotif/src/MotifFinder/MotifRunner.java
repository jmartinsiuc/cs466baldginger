package MotifFinder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import section1.Section1_methods;
import section2.section2_methods;

public class MotifRunner {

	/**
	 * @param args
	 */
	static final int[] rangeNM = {0,2};
	static final int[] rangeML = {6,7};
	static final int[] rangeSC = {5,20};
	static final int[] rangeBW = {30};
	static ArrayList<Integer> sites = new ArrayList<Integer>();
	static ArrayList<Integer> pSites = new ArrayList<Integer>();
	static ArrayList<double[]> O_PWM = new ArrayList<double[]>();
	static ArrayList<double[]> P_PWM = new ArrayList<double[]>();
	
	public static void main(String[] args) {
		long overStart, overEnd;
		int DML = 8, DNM = 1, DSC = 10;
		ArrayList<double[]> benchmarkArr= new ArrayList<double[]>();
		overStart = System.nanoTime();
		for (int o=0;o<10;o++){
			benchmarkArr.add(genInst(30, DNM, DML, DSC, 500, o));
			for(int i=0; i<rangeNM.length; i++){
				benchmarkArr.add(genInst(30, rangeNM[i], DML, DSC, 500, o));
			}
			for(int i=0; i<rangeML.length; i++){
				benchmarkArr.add(genInst(30, DNM, rangeML[i], DSC, 500, o));
			}
			for(int i=0; i<rangeSC.length; i++){
				benchmarkArr.add(genInst(30, DNM, DML, rangeSC[i], 500, o));
			}
		}
		writeBenchmark("./src/Outputs/benchmarkdata.txt", benchmarkArr);
		overEnd =  System.nanoTime();
		System.out.println("totalruntime is: " +(overEnd - overStart));
	}
	
	/**
	 * generates  data for benchmarking
	 * @param BW - beamwidth of the given run
	 * @param NM - the number of mutations for the given run
	 * @param ML - the Motif Length for the given run
	 * @param SC - the sequence count for the given run
	 * @param SL - the the sequence length for the given run
	 * @param iter - the given iteration (out of ten) for the given run
	 * @return - a double array as a line for benchmarking
	 */
	private static double[] genInst(int BW, int NM, int ML, int SC, int SL, int iter){
		String addString = "run_" + iter + "_NM_" + NM + "_ML_" + ML + "_SC_" + SC+"_";
		//System.out.println("round: " +addString);
		long startTime = System.nanoTime();
		Section1_methods s = new Section1_methods(ML,NM,SL,SC,addString);
		section2_methods s2 = new section2_methods(BW,addString);
		long endTime = System.nanoTime();
		return benchmark(iter, NM, ML, SC, BW, endTime - startTime, addString);
	}

	/**
	 * Writes the benchmark data to easily formatted file
	 * @param fn - the given filename
	 * @param benchmarkArr - the given array of benchmarking data
	 */
	private static void writeBenchmark(String fn, ArrayList<double[]> benchmarkArr) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fn);
			pw.println(">"+fn);
			pw.println("RUN\tNM\tML\tSC\tBW\trOE\trPE\t%mch\toB1\truntime");
			double[] temp;
			for(int i=0;i<benchmarkArr.size();i++){
				temp = benchmarkArr.get(i);
				pw.println((int)temp[0] +"\t" + (int)temp[1] +"\t" + 
							(int)temp[2] +"\t" + (int)temp[3] +"\t" + 
							(int)temp[4] +"\t" + temp[5] +"\t" + temp[6] +"\t"+
							temp[7] +"\t" + temp[8] +"\t" + temp[9]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pw.close();
	}

	/** generates a line of benchmark data
	 * @param BW - beamwidth of the given run
	 * @param NM - the number of mutations for the given run
	 * @param ML - the Motif Length for the given run
	 * @param SC - the sequence count for the given run
	 * @param SL - the the sequence length for the given run
	 * @param iter - the given iteration (out of ten) for the given run
	 * @param runtime - the runtime for the given run
	 * @param addString - the string modifier for the given run's filename(s)
	 * @return  a double array as a line for benchmarking
	 */
	private static double[] benchmark(int iter, int NM, int ML, int SC, int BW, long runtime, String addString) {
		getSites(addString);
		int wildNum = getMotifs(addString);
		double rOEntropy= genRelativeEntropy(O_PWM);
		double rPEntropy= genRelativeEntropy(P_PWM);
		double percentSiteMatch = sites_matching();
		double offByOne = isNumOff(wildNum);
		double[] temp = {(double)iter,(double)NM,(double)ML,(double)SC,(double)BW,rOEntropy,rPEntropy,(double)percentSiteMatch, offByOne, runtime};
		sites.clear();
		pSites.clear();
		O_PWM.clear();
		P_PWM.clear();
		return temp;
	}

	/**returns the percentage of sites which are either accurate or off by at most num
	 * @param num - the given maximum number of off distance
	 * @return returns the percentage of predicted sites that match the above condition
	 */
	private static double isNumOff(int num) {
		double numSame=0;
		for(int i=0; i<pSites.size(); i++){
			if((int)((int)pSites.get(i)+ num) == (int)sites.get(i)) numSame++;
		}
		if(numSame == 0 && num>1) return isNumOff(num-1);
		if(numSame/pSites.size()>.25) return 1;
		return 0;
	}

	/**
	 * @return the ratio of sites that match between the given and predicted sites
	 */
	private static double sites_matching() {
		double numSame=0;
		for(int i=0; i<pSites.size(); i++){
			if((int)pSites.get(i) == (int)sites.get(i)) numSame++;
		}
		return (double)numSame/(double)pSites.size();
	}

	/** generates relative entropy for the given pwm
	 * @param PWM - the given pwm
	 * @return - the relative entropy of the array given know background probability
	 */
	private static double genRelativeEntropy(ArrayList<double[]> PWM) {
		double sum=0;
		double pTot;
		for(int i=0; i<PWM.size(); i++){
			pTot = PWM.get(i)[0] + PWM.get(i)[1] + PWM.get(i)[2] + PWM.get(i)[3];
			for(int j=0; j<4; j++){
				if(PWM.get(i)[j]!=0){
					sum += (PWM.get(i)[j]/pTot) * 
							Math.log((PWM.get(i)[j]/pTot)/(.25));
				}
			}
		}
		return sum;
	}
	


	/**
	 *  Pulls the pwm's from their respective files
	 * @param addString - string identifier for the file
	 * @return - the number of wildcards at the beginning of the motif
	 */
	private static int getMotifs(String addString) {
		BufferedReader br = null;
		int numWilds=0;
		String inFile = "./src/Inputs/"+addString+"motif.txt";
		String outFile = "./src/Outputs/"+addString+"predictedmotif.txt";
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(inFile));
			sCurrentLine = br.readLine();//skip first useless line
			
			while ((sCurrentLine = br.readLine()) != null) {
				String[] r = sCurrentLine.split(" ");
				int i;

				for(i=0; i<r[2].length(); i++){
					switch(r[2].charAt(i)){
						case 'A': double[] mat={1,0,0,0}; O_PWM.add(mat); break;
						case 'C': double[] mat1={0,1,0,0}; O_PWM.add(mat1); break;
						case 'G': double[] mat2={0,0,1,0}; O_PWM.add(mat2); break;
						case 'T': double[] mat3={0,0,0,1}; O_PWM.add(mat3); break;
						default: double[] mat4={1,1,1,1}; O_PWM.add(mat4); break;
					}
				}
				i=0;
				while(i<r[2].length() && r[2].charAt(i)== '*'){
					numWilds++;
					i++;
				}
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
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(outFile));
			sCurrentLine = br.readLine();//skip first useless line
			
			while ((sCurrentLine = br.readLine()) != null) {
				String[] r = sCurrentLine.split("\t");
				double[] l = new double[4];
				for(int i=0; i<4; i++){
					l[i] = Integer.parseInt(r[i]);
				}
				P_PWM.add(l);
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
		return numWilds;
	}
	
	/** reads the predicted and actual sites from file
	 * @param addString - string identifier for the file
	 */
	private static void getSites(String addString) {
		BufferedReader br = null;
		String inFile = "./src/Inputs/"+addString+"sites.txt";
		String outFile = "./src/Outputs/"+addString+"predictedsites.txt";
		try {
			br = new BufferedReader(new FileReader(inFile));
			String sCurrentLine = br.readLine();//skip first useless line
			
			while ((sCurrentLine = br.readLine()) != null) {
				sites.add(Integer.parseInt(sCurrentLine));
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
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(outFile));
			sCurrentLine = br.readLine();//skip first useless line
			
			while ((sCurrentLine = br.readLine()) != null) {
				pSites.add(Integer.parseInt(sCurrentLine));
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
}
