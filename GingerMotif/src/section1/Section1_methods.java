package section1;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Section1_methods {
	ArrayList<char[]> sequenceList = new ArrayList<char[]>();
	char[] motif;
	int[] positions;
	ArrayList<Integer>positionList = new ArrayList<Integer>();
	ArrayList<char[]>bindingSites = new ArrayList<char[]>();

	public Section1_methods(int ml, int nm, int sl, int sc, String addString) {
		for(int i=0;i<sc; i++){
			sequenceList.add(genSequences(sl));			
		}
		motif = genSequences(ml);
		positions = new int[nm];
		for (int i=0;i<nm;i++){
			positions[i]=(int) (Math.random()*(ml));
		}
		
		for (int i=0;i<sc;i++){
			bindingSites.add(genSites(sc, ml, nm));
		}
		for(int i=0;i<sequenceList.size();i++){
			plantSite(i);
		}
		
		//change to another location later
		writeFile("./src/Inputs/"+addString+"sequences.fa", sequenceList);
		writeFile2("./src/Inputs/"+addString+"sites.txt", positionList);
		writeMotif("./src/Inputs/"+addString+"motif.txt");
		writeMotiflength("./src/Inputs/"+addString+"motiflength.txt");
	}
	
	private void plantSite(int x){
		int n=(int) (Math.random()*(sequenceList.get(0).length-motif.length));
		int bs=0;
		positionList.add(n);
		for(int i=n;i<motif.length;i++){
			sequenceList.get(x)[i]=bindingSites.get(x)[bs];
			++bs;
		}
	}
	
	private char[] genSites(int sc, int ml, int nm){
		char[] c = new char[ml];
		
		for(int i=0;i<ml;i++){
			c[i]=motif[i];
		}
		for(int i=0;i<nm;i++){
			c[positions[i]]=genNucleotide();
		}
		return c;
	}
	
	private char genNucleotide(){
		int n = (int) (Math.random()*4)+1;
		//System.out.println(n);
		char c = 'A';
		switch(n){
			case 1:c='A';break;
			case 2:c='T';break;
			case 3:c='G';break;
			case 4:c='C';break;
		}
		return c;
	}
	
	private char[] genSequences(int sl){
		
		char[] seq = new char[sl];
		for (int i=0; i<sl; i++){
			seq[i]=genNucleotide();
		}
		return seq;
		
	}
	
	private void writeFile(String fileName, ArrayList<char[]> charArray){
		
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fileName);
			pw.println(">"+fileName);
			for(int i=0;i<charArray.size();i++){
				pw.println(charArray.get(i));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pw.close();
		
	}
	
	private void writeFile2(String fileName, ArrayList<Integer> posArray){
		
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fileName);
			pw.println(">"+fileName);
			for(int i=0;i<posArray.size();i++){
				pw.println(posArray.get(i));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pw.close();
		
	}
	private void writeMotif(String fileName){
		char[]temp=new char[motif.length];
		for(int i=0;i<motif.length;i++){
		 temp[i]=motif[i];	
		}
		for(int i=0;i<positions.length;i++){
			temp[positions[i]]='*';
		}
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fileName);
			pw.println(">"+fileName);
			pw.print("MOTIF1 "+motif.length+" ");
			pw.println(temp);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pw.close();
		
	}
	private void writeMotiflength(String fileName){
		
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fileName);
			pw.println(">"+fileName);
			pw.println(motif.length);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pw.close();
		
	}
}