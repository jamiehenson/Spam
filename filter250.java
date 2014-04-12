// Classifier testing program, using all of the training emails as the testing set
// Jamie Henson

import java.io.File;

public class filter250 
{
	public static int rightCount = 0, setSize = 0;
	public static void do250()
	{
		long startTimeTotal, endTimeTotal;
		double durationT;
		System.out.println("Full testing mode.");
		
		startTimeTotal = System.nanoTime();
		
		try
		{
			File trainingDirectory = new File("cross/set5");
			File[] list = trainingDirectory.listFiles();
			setSize = list.length;
			
			String args[] = new String[1];

			for (File file : list)
			{			
				args[0] = file.getPath();
				filter.main(args);
			}
		} 
		catch (Exception e)
		{
			System.err.println(e);
		}
		
		endTimeTotal = System.nanoTime();
		
		System.out.println("--------------------\nProgram performance:");
		
		durationT = -(double) (startTimeTotal - endTimeTotal) / 1000000000.0;
		double perf = (durationT*(10)/60);
		double overallAcc = ((double) rightCount / 250) * 100;
		System.out.println("Total: " + durationT + "s");
		System.out.println("Time for all emails: " + perf + " minutes.");
		System.out.println("Overall accuracy: " + overallAcc + "%");
	}
}
