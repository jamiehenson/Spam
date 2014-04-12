import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map.Entry;

// Retreival class to extract the top 10 Spam and Ham words from the HashMap
// Jamie Henson

public class TopWords {
	
	private static HashMap<String,double[]> hash = new HashMap<String,double[]>();
	
	@SuppressWarnings("unchecked")
	private void readHash()
	{
		try
		{
			File file = new File("hash_all.jh0422");  
			FileInputStream f = new FileInputStream(file);  
			ObjectInputStream s = new ObjectInputStream(f);  
			hash = (HashMap<String,double[]>)s.readObject();         
			s.close();
		} catch (Exception e)
		{
			System.err.println("Reading failed.");
		}
	}
	
	private void printArray(String[][] leaders)
	{
		for (int i = 0; i<10; i++)
		{
			System.out.println(i+1 + ") " + leaders[i][0]);
		}
	}
	
	public static void main (String args[])
	{
		TopWords top = new TopWords();
		top.readHash();
		
		String[][] leaders = new String[10][2];
		String[][] leadersH = new String[10][2]; 
		double max = 0, spam = 0, ham = 0;
		String maxWord = null;
		
		hash.remove("#@/%PROB");
		HashMap<String,double[]> hash2 = hash;

		for (int i = 0; i<10; i++)
		{
			for(Entry<String, double[]> entry : hash.entrySet()) {
				double[] pair = entry.getValue();
				spam = pair[0];
			    if (max == 0 || spam > max) {
			        max = spam;
			        maxWord = entry.getKey();
			    }
			}
			leaders[i][0] = maxWord;
			leaders[i][1] = Double.toString(Math.log(spam));
			hash.remove(maxWord);
			max = 0;
			
			for(Entry<String, double[]> entry : hash2.entrySet()) {
				double[] pair = entry.getValue();
				ham = pair[1];
			    if (max == 0 || ham > max) {
			        max = ham;
			        maxWord = entry.getKey();
			    }
			}
			leadersH[i][0] = maxWord;
			leadersH[i][1] = Double.toString(Math.log(spam));
			hash2.remove(maxWord);
			max = 0;
			
		}
		
		System.out.println("Top 10 Spam words!\n--------------------------------");
		top.printArray(leaders);
		
		System.out.println("\nTop 10 Ham words!\n--------------------------------");
		top.printArray(leadersH);
		
	}
}
