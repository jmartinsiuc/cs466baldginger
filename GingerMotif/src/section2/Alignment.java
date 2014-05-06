package section2;


import java.util.ArrayList;


public class Alignment {
    static final int[] subMatrix = {1,0,0,0,
    								0,1,0,0,
									0,0,1,0,
									0,0,0,1};
    ArrayList<String> subMatChars = new ArrayList<String>();
    //int[][] scoreMatrix;
    char[] string1;
    char[] string2;
    int size;
    //0=AA 1=AC 2=AG 3=AT 4=CA 5=CC 6=CG 7=CT 8=GA 9=GC 10=GG 11=GT 12=TA 13=TC 14=TG 15=TT
    int gapPen;
	public Alignment(char[] s1, char[] s2, int strLength){
		string1=s1;
		string2=s2;
		size=strLength;
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
		
		//scoreMatrix = new int[size+1][size+1];
	}
	
	
	public int genScore(int x, int y) throws RuntimeException{
		//initialize/clean the scoring matrix
		/*for (int i=0; i<size+1; i++){
			for (int j=0; j<size+1; j++){
				if(i==0) scoreMatrix[i][j]=-i;
				if(j==0) scoreMatrix[i][j]=-j;
			}
		}*/
		int diag=0;//, diag2;
		//modified algorithm that just takes the diagonal, since that's all we're checking for a give i,j
		
		for(int i=1; i<=size; i++){
			if(string1[x + i-1] == string2[y + i-1]) diag++;
			//diag+=getWeight(string1[x + i-1]+""+string2[y + i-1]);
		}
		/*
		//now we can run the algorithm
		for(int i=1; i<=size; i++){
			for(int j=1; j<=size;j++){
				diag2 = scoreMatrix[i-1][j-1]+getWeight(string1[x + i-1]+""+string2[y + j-1]);
				scoreMatrix[i][j] = Math.max(diag,0);
			}
		}
		//We have the scores, but in this case we want to return the max score not the alignment
		//return scoreMatrix[size][size];
		//if(diag!=scoreMatrix[size][size]) System.out.println("is out");
		*/
		
		
		return diag;
	}
	
	public int getWeight(String s){
		int index=0;
		index=subMatChars.indexOf(s);
		return subMatrix[index];
	}
}