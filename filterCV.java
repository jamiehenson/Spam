import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

// Cross-validation testing module
// Jamie Henson

public class filterCV 
{
	private static HashMap<String, double[]> trainingHash = new HashMap<String,double[]>();
	private String[] testingMail;
	private String testingFileName;
	private static int rightCount = 0, rightTotal = 0, falsePos = 0, falseNeg = 0, posTotal = 0, negTotal = 0;
	
	@SuppressWarnings("unchecked")
	public void readHashCV(int testFold)
	{
		try
		{
			HashMap<String, double[]> tempHash = new HashMap<String, double[]>();
			
			for (int i = 0; i < 10; i++)
			{
				if (i != testFold)
				{
					File file = new File("hash_set" + testFold + ".jh0422");
					FileInputStream f = new FileInputStream(file);  
					ObjectInputStream s = new ObjectInputStream(f);
					tempHash = (HashMap<String,double[]>)s.readObject();         
					trainingHash.putAll(tempHash);
					tempHash.clear();
					s.close();
				}
			} 
		} catch (Exception e)
		{
			System.err.println("Reading failed.");
		}
	}
	
	private void readTestingCV(String testingDir, int foldval)
	{
		try
		{
			File trainingDirectory = new File(testingDir + foldval);
			File[] list = trainingDirectory.listFiles();
			
			for (File file : list)
			{			
				String testingMailStr = "";
				Scanner scanner = new Scanner(file);
				while (scanner.hasNextLine()) 
				{
					String line = scanner.nextLine();
					testingMailStr = testingMailStr + "\n" + line;
				}
				scanner.close();
				testingFileName = file.getName();
				testingMail = testingMailStr.split(" ");  
				classify();
			}
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
		if (match) 
		{
			rightCount++; 
			rightTotal++;
		}
		else
		{
			if (result.equals("spam"))
			{
				falsePos++;
				posTotal++;
			}
			if (result.equals("ham")) 
			{
				falseNeg++;
				negTotal++;
			}
		}
		return;
	}

	public static void doCV()
	{
		long startTimeTotal, endTimeTotal;
		double durationT;
		filterCV f = new filterCV();
		
		System.out.println("Cross validation mode.");
		
		startTimeTotal = System.nanoTime();
		for (int i = 0; i < 10; i++)
		{
			System.out.print("Using set " + i + " as testing set. ");

			f.readHashCV(i);
			f.readTestingCV("cross/set",i);
			
			double percent = ((double) rightCount / 250) * 100;
			double fP = ((double) falsePos / 250) * 100;
			double fN = ((double) falseNeg / 250) * 100;
			System.out.println("Accuracy: " + percent + "%, False positives: " + fP + "%, False negatives: " + fN + "%");
			
			trainingHash.clear();
			rightCount = 0;
			falseNeg = 0;
			falsePos = 0;
		}
		endTimeTotal = System.nanoTime();
		
		System.out.println("--------------------\nProgram performance:");
		durationT = - (double) (startTimeTotal - endTimeTotal) / 1000000000.0;
		double overallAcc = ((double) rightTotal / 2500) * 100;
		double overallPos = ((double) posTotal / 2500) * 100;
		double overallNeg = ((double) negTotal / 2500) * 100;
		System.out.println("Total: " + durationT + "s");
		System.out.println("Overall accuracy: " + overallAcc + "%");
		System.out.println("Overall false positives: " + overallPos + "%");
		System.out.println("Overall false negatives: " + overallNeg + "%");
	}
}
