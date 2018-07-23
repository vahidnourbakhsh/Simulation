package mcms.output;

import mcms.main.SimRunner;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.io.File;
/**
 * <h1> Output the simulation results </h1>
 * Output the simulation results to the console and save it in a file.
 * 
 * @author	Vahid Nourbakhsh 
 * @version	2.0
 * @since	2018-03-15
 */
public class Output {
	/**
	 * 	Defines a format for printing numbers. That is, defines a format for the number of decimal places.
	 * 
	 * 	@param void
	 *  @see SimRunner#NUM_DECIMALS
	 *  @return DecimalFormat a format with the specified number of decimal places.
	 */
	public static DecimalFormat numberFormat (int numDecimals) { 
	StringBuffer outputBuffer = new StringBuffer(numDecimals);
	for (int i = 0; i < numDecimals; i++) outputBuffer.append("#");
	DecimalFormat decimalForm = new DecimalFormat("#0." + outputBuffer.toString());
	return decimalForm;
	}
	
	/**
	 * 	Prints the simulation results summary to the console.
	 * 
	 * 	@param exNum The example/instance number. It is used for reading (writing) simulation input (output)
	 *  @return void
	 */
	public static void printStatisticsToConsole(int exNum, int numContacts, int numGroups, int numReps, int numPeriods, double periodDuration, double simDuration, double startTime, boolean randomServiceTime, double acceptableWaitTime, int numDecimals) {
		
		DecimalFormat decimalForm = numberFormat(numDecimals);

		System.out.println("Example number: " + exNum + "\n" + "\t Number of contact types: " + numContacts + "\n \t Number of agent groups: " + numGroups + "\n \t Acceptable Waiting Time: " + acceptableWaitTime + "\n"); 
		
		System.out.println("Simulation setting: \n \t Number Periods: "+ SimRunner.PCE.getNumPeriods()+ "\n \t Nummer Main Periods (excluding warm-up and wrap-up periods): " + SimRunner.PCE.getNumMainPeriods() + "\n \t Each Period\'s Length: " + simDuration + "\n \t Number Replications: " + numReps);
		System.out.println("\t Total Net Simulation Duration (Num Main Periods * Each Period\'s Length * Num Replications): " + simDuration + "\n"); 
		
		System.out.println("Simulation results (average over replications):");
		System.out.println("\t Total number of contacts arrived at the system: " + decimalForm.format(SimRunner.totNumArrived));
		System.out.println("\t Total number of contacts served: " + decimalForm.format(SimRunner.totNumServed));
		System.out.println("\t Total number of contacts served within the acceptable waiting time of " + acceptableWaitTime + ": " +  decimalForm.format(SimRunner.totNumGoodSL));
		System.out.println("\t Average number of contacts arrived at the system per unit time: " + decimalForm.format(SimRunner.totNumArrived/simDuration)); 
		System.out.println("\t Average number of contacts served per unit time: " + decimalForm.format(SimRunner.totNumServed/simDuration));
		System.out.println("\t Average number of contacts served within the acceptable waiting time of " + acceptableWaitTime + " per unit time: " + decimalForm.format(SimRunner.totNumServed/simDuration));
		System.out.print("\t EW(avg): Expected queue waiting time of a random contact arrived at the system: ");
		double avgEW = SimRunner.totWaitTime/SimRunner.totNumArrived;
		System.out.println(decimalForm.format(avgEW));	
		System.out.print("\t SL(avg): Average number of contacts that were served within the acceptable waiting time (defined per unit time): ");
		double totSL = SimRunner.totNumGoodSL/simDuration; 
		System.out.println(decimalForm.format(totSL));
	}

	/**
	 * Prints the simulation results summary to a file.	 
	 * @param 	exNum 			The example number
	 * @param	resultsFile		The path and name of the file to be written to.
	 * @return  void
	 */	
	public static void printResultsToFile (String resultsPath, int exNum, int numContacts, int numGroups, int numReps, int numPeriods, double periodDuration, double simDuration, double startTime, boolean randomServiceTime, double acceptableWaitTime, int numDecimals) {
		BufferedWriter bw = null;
		// Name and location of the file for saving the simulation results
		File resultsFile = new File(resultsPath + exNum + "FSF_Sim" + "" + "_K" + numGroups + "_I" + numContacts + ".out");
		
		try {
			if (!resultsFile.exists()) {
				resultsFile.createNewFile();
			}
			DecimalFormat decimalForm = numberFormat(numDecimals);
			FileWriter fw = new FileWriter(resultsFile);
			bw = new BufferedWriter(fw);
			
			bw.write("Example number: " + exNum + "\n" + "\t Number of contact types: " + numContacts + "\n \t Number of agent groups: " + numGroups + "\n \t Acceptable Waiting Time: " + acceptableWaitTime + "\n"); bw.newLine();
			
			bw.write("Simulation setting: \n \t Num Periods: "+ SimRunner.PCE.getNumPeriods()+ "\n \t Num Main Periods (excluding warm-up and wrap-up periods): " + SimRunner.PCE.getNumMainPeriods() + "\n \t Each Period\'s Length: " + simDuration + "\n \t Num Replications: " + numReps + "\n");
			bw.write("\t Total Net Simulation Duration (Num Main Periods * Each Period\'s Length * Num Replications): " + simDuration); bw.newLine();bw.newLine();
			
			bw.write("Simulation results (average over replications): \n");
			bw.write("\t Total number of contacts arrived at the system: " + decimalForm.format(SimRunner.totNumArrived)); bw.newLine();
			bw.write("\t Total number of contacts served: " + decimalForm.format(SimRunner.totNumServed)); bw.newLine();
			bw.write("\t Total number of contacts served within the acceptable waiting time of " + acceptableWaitTime + ": " +  decimalForm.format(SimRunner.totNumGoodSL)); bw.newLine();
			bw.write("\t Average number of contacts arrived at the system per unit time: " + decimalForm.format(SimRunner.totNumArrived/simDuration) + "\n"); 
			bw.write("\t Average number of contacts served per unit time: " + decimalForm.format(SimRunner.totNumServed/simDuration)); bw.newLine();
			bw.write("\t Average number of contacts served within the acceptable waiting time of " + acceptableWaitTime + " per unit time: " + decimalForm.format(SimRunner.totNumServed/simDuration)); bw.newLine();
			bw.write("\t EW(avg): Expected queue waiting time of a random contact arrived at the system: ");
			double avgEW = SimRunner.totWaitTime/SimRunner.totNumArrived;
			bw.write(decimalForm.format(avgEW)); bw.newLine();	
			bw.write("\t SL(avg): Average number of contacts that were served within the acceptable waiting time (defined per unit time): ");
			double totSL = SimRunner.totNumGoodSL/simDuration; 
			bw.write(decimalForm.format(totSL)); bw.newLine();
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		finally
		{ 
			try{
				if(bw!=null)
					bw.close();
			}catch(Exception ex){
				System.out.println("Error in closing the BufferedWriter " + ex);
			}
		}
	}
}