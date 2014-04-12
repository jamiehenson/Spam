// Spam filter - Generation of HashMap for training data, with feature
// control settings
// Jamie Henson

import java.io.*;
import java.util.*;

public class HashGen 
{
	private static HashMap<String, double[]> trainingHash = new HashMap<String, double[]>();
	private HashMap<String, Integer> spamHash = new HashMap<String, Integer>();
	private HashMap<String, Integer> hamHash = new HashMap<String, Integer>();
	private int hamCount = 0, spamCount = 0, spamShare = 0, hamShare = 0;
	private static String fold;
	
	// Feature switches
	private boolean removeBoundaryWords = true;
	private boolean sameCase = false;
	private boolean alphaNumOnly = true;
	private boolean noMassiveWords = true;
	private boolean noTinyWords = true;
	
	// Feature settings
	private int lowerLimit = 1, upperLimit = 105;
	private int wordCutOff = 8, wordLowCut = 1;

	private void wordScrubber()
	{
		
		Iterator<Map.Entry<String,Integer>> hamWord = hamHash.entrySet().iterator();
		while (hamWord.hasNext()) {
		    Map.Entry<String,Integer> entry = hamWord.next();
		    Integer val = entry.getValue();
		    if (removeBoundaryWords)
		    {
			    if (val < lowerLimit || val > upperLimit){
			        hamWord.remove();
			        if (spamHash.containsKey(entry.getKey())) spamHash.remove(entry.getKey());
			    }
		    }
		}
	
		Iterator<Map.Entry<String,Integer>> spamWord = spamHash.entrySet().iterator();
		while (spamWord.hasNext()) {
		    Map.Entry<String,Integer> entry = spamWord.next();
		    Integer val = entry.getValue();
		    if (removeBoundaryWords)
		    {
			    if (val < lowerLimit || val > upperLimit){
			        spamWord.remove();
			        if (hamHash.containsKey(entry.getKey())) hamHash.remove(entry.getKey());
			    }
		    }
		}
	}
	
	private void populateHash()
	{
		// Gather all training words into a big pile
		HashSet<String> wordsUsed = new HashSet<String>();
		wordsUsed.addAll(spamHash.keySet());
		wordsUsed.addAll(hamHash.keySet());
		
		// Co-efficient for Laplace Smoothing, default is 1
		int smoothing = 1;
		
		// Count occurrences of each word in either list, add smoothing
		for (String word : wordsUsed) 
		{
			int occurrencesS = 0, occurrencesH = 0;
			if (spamHash.containsKey(word)) 
			{
				occurrencesS = spamHash.get(word) + smoothing;
				spamCount = spamCount + occurrencesS;
			}
			else
			{
				spamHash.put(word, smoothing);
				occurrencesS = smoothing;
				spamCount = spamCount + smoothing;
			}
		    
		    if (hamHash.containsKey(word)) 
		    {
		    	occurrencesH = hamHash.get(word) + smoothing;
		    	hamCount = hamCount + occurrencesH;
		    }
		    else
			{
				hamHash.put(word, smoothing);
				occurrencesH = smoothing;
				hamCount = hamCount + smoothing;
			}
		    
		    spamHash.put(word,occurrencesS);
		    hamHash.put(word,occurrencesH);
		}
		
		for (String word : wordsUsed) 
		{
			int occS = spamHash.get(word);
			int occH = hamHash.get(word);
			double[] results = new double[2];
		    results[0] = (double) occS / (double) spamCount;
			results[1] = (double) occH / (double) hamCount;
			trainingHash.put(word, results);
		}
		
		double[] shares = new double[2];
		double[] counts = new double[2];
		shares[0] = (double) spamShare / (double) (spamShare + hamShare);
		shares[1] = (double) hamShare / (double) (spamShare + hamShare);
		counts[0] = (double) spamCount;
		counts[1] = (double) hamCount;
		trainingHash.put("#@/%PROB",shares);
	}
	
	public void readTraining(String trainingDir, String foldval)
	{
		try
		{
			//int i = 0;
			trainingDir = trainingDir.concat("/" + foldval);
			fold = foldval;
			System.out.print("Creating data structure from " + fold + ". ");
			File trainingDirectory = new File(trainingDir);
			File[] list = trainingDirectory.listFiles();

			// Go through each file in the directory, split them down into individual words
			// and add to a communal spam or ham pile. Also count how many spam and ham emails
			// there are.
			for (File file : list)
			{
				Scanner scanner = new Scanner(file);
				
				if (file.getName().startsWith("ham"))
				{
					
					while (scanner.hasNextLine()) 
					{
						String body = scanner.nextLine();
						String[] words = body.split(" ");

						for (String indv : words)
						{
							if (!indv.equals(""))
							{
								if (sameCase) indv = indv.toLowerCase();
								if (alphaNumOnly) indv = indv.replaceAll("[^A-Za-z0-9]", "");
								if (noMassiveWords)
								{
									if (indv.length() >= wordCutOff) continue;
								}
								if (noTinyWords)
								{
								  if (indv.length() <= wordLowCut) continue;
								}
								if (hamHash.containsKey(indv))
								{
									int count = hamHash.get(indv);
									hamHash.put(indv, ++count);
								}
								else
								{
									hamHash.put(indv, 1);
								}
							}
						}
					}
					hamShare++;
				}
				else if (file.getName().startsWith("spam"))
				{
					while (scanner.hasNextLine()) 
					{
						String body = scanner.nextLine();
						String[] words = body.split(" ");
						for (String indv : words)
						{
							if (!indv.equals(""))
							{
								if (sameCase) indv = indv.toLowerCase();
								if (alphaNumOnly) indv = indv.replaceAll("[^A-Za-z0-9]", "");
								if (noMassiveWords)
								{
									if (indv.length() >= wordCutOff) continue;
								}
								if (noTinyWords)
								{
								  if (indv.length() <= wordLowCut) continue;
								}
								if (spamHash.containsKey(indv))
								{
									int count = spamHash.get(indv);
									spamHash.put(indv, ++count);
								}
								else
								{
									spamHash.put(indv, 1);
								}
							}
						}
					}
					spamShare++;
				}
				scanner.close();
			}	
			
			wordScrubber();
			populateHash();
			writeHashDown();
			
		} 
		catch (Exception e)
		{
			System.err.println(e);
		}
	}
	
	private void writeHashDown()
	{
		try {
			FileOutputStream fileOut =
		    new FileOutputStream("hash_" + fold + ".jh0422");
		    ObjectOutputStream out =
		         new ObjectOutputStream(fileOut);
		    out.writeObject(trainingHash);
		    out.close();
		    fileOut.close();
		    spamHash.clear();
			hamHash.clear();
			trainingHash.clear();
			System.out.println("Job's a goodun.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[])
	{
		HashGen f = new HashGen();
		f.readTraining(args[0],args[1]);
	}
}
