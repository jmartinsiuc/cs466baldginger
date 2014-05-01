package MotifFinder;

import section1.Section1_methods;
import section2.section2_methods;

public class MotifRunner {

	/**
	 * @param args
	 */
	static final int[] rangeNM = {0,1,2};
	static final int[] rangeML = {6,7,8};
	static final int[] rangeSC = {5,10,20};
	public static void main(String[] args) {
		long startTime, endTime, overStart, overEnd;
		Section1_methods s;
		section2_methods s2;
		String addString;
		overStart = System.nanoTime();
		for(int i=0; i<rangeNM.length; i++){
			for(int j=0; j<rangeML.length; j++){
				for(int k=0; k<rangeSC.length; k++){
					addString = rangeNM[i]+"_"+rangeML[j]+"_"+rangeSC[k]+"_";
					System.out.println("round: " +addString);
					//int ml, int nm, int sl, int sc
					startTime = System.nanoTime();
					s = new Section1_methods(rangeML[j],rangeNM[i],500,rangeSC[k],addString);
					s2 = new section2_methods(20,addString);
					endTime = System.nanoTime();
					System.out.println("Time for round " +addString+": "+(endTime - startTime));
				}
			}
		}
		overEnd =  System.nanoTime();
		System.out.println("totalruntime is: " +(overEnd - overStart));
	}

}
