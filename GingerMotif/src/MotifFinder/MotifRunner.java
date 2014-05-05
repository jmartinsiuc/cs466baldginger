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
	static final int[] rangeNM = {0,1,2};
	static final int[] rangeML = {6,7,8};
	static final int[] rangeSC = {5,10,20};
	static final int[] rangeBW = {30};//{1,5,10,20,30};
	static ArrayList<Integer> sites = new ArrayList<Integer>();
	static ArrayList<Integer> pSites = new ArrayList<Integer>();
	static ArrayList<double[]> O_PWM = new ArrayList<double[]>();
	static ArrayList<double[]> P_PWM = new ArrayList<double[]>();
	
	public static void main(String[] args) {
		long startTime, endTime, overStart, overEnd;
		Section1_methods s;
		section2_methods s2;
		String addString;
		ArrayList<double[]> benchmarkArr= new ArrayList<double[]>();
		//overStart = System.nanoTime();
		for(int i=0; i<rangeNM.length; i++){
			for(int j=0; j<rangeML.length; j++){
				for(int k=0; k<rangeSC.length; k++){
					for(int l=0; l<rangeBW.length; l++){
						addString = "BW_"+ rangeBW[l]+"_NM_" + rangeNM[i]+
										"_ML_"+rangeML[j]+"_SC_"+rangeSC[k]+"_";
						System.out.println("round: " +addString);
						startTime = System.nanoTime();
						s = new Section1_methods(rangeML[j],rangeNM[i],500,rangeSC[k],addString);
						s2 = new section2_methods(rangeBW[l],addString);
						endTime = System.nanoTime();
						benchmark(rangeNM[i], rangeML[j], rangeSC[k], rangeBW[l], endTime - startTime, addString, benchmarkArr);
						//System.out.println("Time for round " +addString+": "+(endTime - startTime));
					}
				}
			}
		}
		writeBenchmark("./src/Outputs/benchmarkdata.txt", benchmarkArr);
		//overEnd =  System.nanoTime();
		//System.out.println("totalruntime is: " +(overEnd - overStart));
	}

	private static void writeBenchmark(String fn, ArrayList<double[]> benchmarkArr) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fn);
			pw.println(">"+fn);
			pw.println("\tNM\tML\tSC\tBW\trOEntropy\trPEntropy\tpercentmatch\toffByOne\truntime");
			double[] temp;
			for(int i=0;i<benchmarkArr.size();i++){
				temp = benchmarkArr.get(i);
				pw.println((int)temp[0] +"\t" + (int)temp[1] +"\t" + 
							(int)temp[2] +"\t" + (int)temp[3] +"\t" + 
							temp[4] +"\t" + temp[5] +"\t" + (int)temp[6] +"\t"+
							temp[7] +"\t" + temp[8]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pw.close();
	}

	private static void benchmark(int NM, int ML, int SC, int BW, long runtime, String addString, ArrayList<double[]> benchmarkArr) {
		getSites(addString);
		int wildNum = getMotifs(addString);
		double rOEntropy= genRelativeEntropy(O_PWM);
		double rPEntropy= genRelativeEntropy(P_PWM);
		double percentSiteMatch = sites_matching();
		double offByOne = isNumOff(wildNum);
		double[] temp = {(double)NM,(double)ML,(double)SC,(double)BW,rOEntropy,rPEntropy,percentSiteMatch, offByOne, runtime};
		benchmarkArr.add(temp);
		sites.clear();
		pSites.clear();
		O_PWM.clear();
		P_PWM.clear();
	}

	private static double isNumOff(int num) {
		int numSame=0;
		for(int i=0; i<pSites.size(); i++){
			if((pSites.get(i)+ num) == sites.get(i)) numSame++;
		}
		if(numSame == 0 && num>1) return isNumOff(num-1);
		if(numSame/pSites.size()>.25) return 1;
		return 0;
	}

	private static double sites_matching() {
		int numSame=0;
		for(int i=0; i<pSites.size(); i++){
			if(pSites.get(i) == sites.get(i)) numSame++;
		}
		return numSame/pSites.size();
	}

	//TODO:  this function is probably wrong, relative entropy is calculated per letter?
	private static double genRelativeEntropy(ArrayList<double[]> PWM) {
		int sum=0;
		double pTot;
		double maxval;
		for(int i=0; i<PWM.size(); i++){
			pTot = PWM.get(i)[0] + PWM.get(i)[1] + PWM.get(i)[2] + PWM.get(i)[3];
			maxval = Math.max(PWM.get(i)[0], Math.max(PWM.get(i)[1], Math.max(PWM.get(i)[2], PWM.get(i)[3])));
			for(int j=0; j<4; j++){
				sum += (PWM.get(i)[j]/pTot)*Math.log((PWM.get(i)[j]/pTot)/(1/4));
			}
		}
		return 0;
	}
	


	private static double getI(char c) {
		switch(c){
			case 'A': case 'C': case 'G': case 'T': return 1;
			default: return 1/4;
		}
	}
	
	private static double getIN(int[] m){
		double sum = m[0] + m[1] + m[2] +m[3];
		return (m[0]*m[0] + m[1]*m[1] + m[2]*m[2] + m[3]*m[3])/(sum*sum);
	}

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
