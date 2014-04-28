package section2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class Alignment {
    ArrayList<String> p1 = new ArrayList<String>();
    ArrayList<String> p2= new ArrayList<String>();
    ArrayList<Integer> subMatrix = new ArrayList<Integer>();
    ArrayList<String> subMatChars = new ArrayList<String>();
    int[][] scoreMatrix;
    char[] string1;
    char[] string2;
    int size;
    //0=AA 1=AC 2=AG 3=AT 4=CA 5=CC 6=CG 7=CT 8=GA 9=GC 10=GG 11=GT 12=TA 13=TC 14=TG 15=TT
    int gapPen;
	public Alignment(char[] s1, char[] s2, String f3, int gp, int strLength){
		string1=s1;
		string2=s2;
		size=strLength;
		readMatrix(f3);
		subMatChars.add("AA");
		subMatChars.add("AC");
		subMatChars.add("AG");
		subMatChars.add("AT");
		subMatChars.add("CA");
		subMatChars.add("CC");
		subMatChars.add("CG");
		subMatChars.add("CT");
		subMatChars.add("GA");
		subMatChars.add("GC");
		subMatChars.add("GG");
		subMatChars.add("GT");
		subMatChars.add("TA");
		subMatChars.add("TC");
		subMatChars.add("TG");
		subMatChars.add("TT");
		
		scoreMatrix = new int[size+1][size+1];
	}
	
	
	public int genScore(int x, int y){
		//initialize/clean the scoring matrix
		for (int i=0; i<p1.size(); i++){
			for (int j=0; j<p2.size(); j++){
				
				if(i==0) scoreMatrix[i][j]=-i;
				if(j==0) scoreMatrix[i][j]=-j;
			}
		}
		
		//now we can run the algorithm
		for(int i=1; i<=p1.size(); i++){
			for(int j=1; j<=p2.size();j++){
				int left=scoreMatrix[i-1][j]+gapPen;
				int top = scoreMatrix[i][j-1]+gapPen;
				int diag = scoreMatrix[i-1][j-1]+getWeight(string1[x + i-1]+""+string2[y + j-1]);
				scoreMatrix[i][j]=Math.max(0,Math.max(diag,Math.max(left, top)));
			}
		}
		//We have the scores, but in this case we want to return the max score not the alignment
	    return scoreMatrix[size][size];
	}
	
	private int getWeight(String s){
		int index=0;
		index=subMatChars.indexOf(s);	
		return subMatrix.get(index);
	}
	
	private void readMatrix(String f1){//assumes order of ACGT
		BufferedReader br = null;
		 
		try {
 
			String sCurrentLine;
			String[] temp;
 
			br = new BufferedReader(new FileReader(f1));
			sCurrentLine = br.readLine();//skip first useless line
			
			while ((sCurrentLine = br.readLine()) != null) {
				//System.out.println(sCurrentLine);
				temp=sCurrentLine.split("\t");
				for(int i=1; i<temp.length; i++){//skips the letter
					subMatrix.add(Integer.parseInt(temp[i]));
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
		
	}
}