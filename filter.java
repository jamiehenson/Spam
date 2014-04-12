import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

// Spam Filter using a naive Bayesian classifier and Laplace smoothing
// Jamie Henson

public class filter 
{
	private HashMap<String, double[]> trainingHash = new HashMap<String,double[]>();
	private String[] testingMail;
	private String testingFileName;
	private static int rightCount = 0;
	
	@SuppressWarnings("unchecked")
	private void readHashAll()
	{
		try
		{
			File file = new File("hash_all.jh0422");  
			FileInputStream f = new FileInputStream(file);  
			ObjectInputStream s = new ObjectInputStream(f);  
			trainingHash = (HashMap<String,double[]>)s.readObject();         
			s.close();
		} catch (Exception e)
		{
			System.err.println("Reading failed.");
		}
	}
	
	private void readTesting(String testingDir)
	{
		try
		{
			// Read in the testing file, split the contents down into
			// individual words, and store in an array
			
			String testingMailStr = "";
			if (testingDir.endsWith(".txt"))
			{
				Scanner scanner = new Scanner(new File (testingDir));
				while (scanner.hasNextLine()) 
				{
					String line = scanner.nextLine();
					testingMailStr = testingMailStr + "\n" + line;
				}
				scanner.close();
			}
			testingFileName = testingDir.substring(11,testingDir.length());
			testingMail = testingMailStr.split(" ");  
		} 
		catch (Exception e)
		{
			System.err.println(e);
		}
	}
	
	private double[] checkWord(String word)
	{	
		double[] results = new double[2];
		word = word.replaceAll("(\\r|\\n)", "");

		// Get number of occurrences of each word in known spam and ham emails
		if (trainingHash.containsKey(word)){
			 results = trainingHash.get(word);
		}

		return results;
	}
	
	private void classify()
	{
		ArrayList<Double> wordProbsS = new ArrayList<Double>();
		ArrayList<Double> wordProbsH = new ArrayList<Double>();
		double spamProb = 0, hamProb = 0;
		
		double[] shares = trainingHash.get("#@/%PROB");
				
		// Check each word's probability, and store both ham and spam probabilities
		// in an array
		for (String word : testingMail)
		{
			double[] checkResults = checkWord(word);
			wordProbsS.add(checkResults[0]);
			wordProbsH.add(checkResults[1]);
		}
		
		// Change no-showers to 1, so that it has no bearing on the calculation and
		// and keep a running product of the probability for the message
		for (Double prob : wordProbsS)
		{
			if (prob == 0) prob = 1.0;
			spamProb += Math.log(prob);
		}  
		
		for (Double prob : wordProbsH)
		{
			if (prob == 0) prob = 1.0;
			hamProb += Math.log(prob);
		}
		
		// Determine how many of all emails are spam and ham
		double spamFrac = (double) shares[0] / (double) (shares[0] + shares[1]);
		double hamFrac = (double) shares[1] / (double) (shares[0] + shares[1]);
		
		spamProb += Math.log(spamFrac);
		hamProb += Math.log(hamFrac);
		
		// Compare probabilities. The higher one wins.
		String result = (spamProb > hamProb) ? "spam" : "ham";
		boolean match = (testingFileName.startsWith(result));
		System.out.println(result + ", " + (filter250.rightCount + 1)+ ", " + match + ", " + testingFileName);
		if (match) 
		{
			rightCount++; 
			filter250.rightCount++;
		}
		return;
	}

	public static void main(String args[])
	{
		filter f = new filter();
		
		// Run Cross Validation
		if (args[0].equals("-cv")) 
		{
			filterCV.doCV();
			return;
		}
		
		// Run the 250-test run checker
		else if (args[0].equals("-250"))
		{
			filter250.do250();
			return;
		}
		else f.readHashAll();

    // Go ahead with the classification of a single e-mail
		f.readTesting(args[0]);
		f.classify();
	}
}
