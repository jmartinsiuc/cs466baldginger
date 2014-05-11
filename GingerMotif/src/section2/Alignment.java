package section2;


import java.util.ArrayList;


/**
 * Alignment subset helper class for the alignment step in the greedy algorithm
 * this function has been optimized for our purposes
 */
public class Alignment {
    char[] string1;
    char[] string2;
    int size;
	public Alignment(char[] s1, char[] s2, int strLength){
		string1=s1;
		string2=s2;
		size=strLength;
	}
	
	
	public int genScore(int x, int y){
		int diag=0;
		//modified algorithm that just takes the diagonal, since that's all we're checking for a give i,j
		for(int i=1; i<=size; i++){
			if(string1[x + i-1] == string2[y + i-1]) diag++;
		}		
		return diag;
	}
}