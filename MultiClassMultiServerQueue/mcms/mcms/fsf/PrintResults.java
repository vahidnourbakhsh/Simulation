package mcms.fsf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.io.File;

public class PrintResults {
	
	public static DecimalFormat numberFormat () { 
	int numDecimals = Main.RESULTSDECIMALPOINTS;
	StringBuffer outputBuffer = new StringBuffer(numDecimals);
	for (int i = 0; i < numDecimals; i++){
	   outputBuffer.append("#");
	}
		DecimalFormat decimalForm = new DecimalFormat("#0." + outputBuffer.toString());
		return decimalForm;
	}
	
	public static void printStatisticsToConsole(int exNum) {
			System.out.println("Example Number " + exNum); 
			DecimalFormat decimalForm = numberFormat();
			System.out.println("EW(tot) (average over all periods and replications)");
			double totEW=Main.totWaitTime/Main.SIMDURATION;  // sum over all replications. 
			System.out.println(decimalForm.format(totEW));
			System.out.println();
			System.out.println("EW(avg) (average over all periods and replications)");
			double avgEW=Main.totWaitTime/Main.totNumServed;  // sum over all replications. 
			System.out.println(decimalForm.format(avgEW));
			System.out.println();
			System.out.println("SL(tot) (average across periods and replications)");
			double totSL=Main.totNumGoodSL/Main.SIMDURATION;  // average over all periods and replications. 
			System.out.println(decimalForm.format(totSL));
			System.out.println();
			System.out.println("Results (sum over all periods & average of replications): ");
			System.out.println("  EW(tot): "+decimalForm.format(Main.totWaitTime/Main.SIMDURATION) + "  SL(tot): " + Main.totNumGoodSL + "  Served: " + Main.totNumServed + "  Blocked: "+ Main.totNumBlocked + "  Arrived: " +  Main.totNumArrived);
			System.out.println();	
		}

	public static void printResultsToFile (int exNum, File simResultsFile) {

		BufferedWriter bw = null;
		try {
			if (!simResultsFile.exists()) {
				simResultsFile.createNewFile();
			}
			DecimalFormat decimalForm = numberFormat();
			FileWriter fw = new FileWriter(simResultsFile);
			bw = new BufferedWriter(fw);

			bw.write("Num Periods: "+Main.PCE.getNumPeriods()+ "  Num Main Periods: " + Main.PCE.getNumMainPeriods() + "  Each Period Length: " + Main.PERIODDURATION + "  Num Replications: " + Main.NUMREPS); bw.newLine();
			bw.write("Total Simulation Duration (Num Main Period * Each Period Length * Num Replications): " + Main.SIMDURATION); bw.newLine();bw.newLine();
			
			bw.write("EW(tot) (average over all periods and replications)"); bw.newLine();
			double totEW = Main.totWaitTime/Main.SIMDURATION; 
			bw.write(decimalForm.format(totEW)); bw.newLine();bw.newLine();
	
			bw.write("EW(avg) (average over all periods and replications)"); bw.newLine();
			double avgEW=Main.totWaitTime/Main.totNumServed;
			bw.write(decimalForm.format(avgEW)); bw.newLine();bw.newLine();	
	
			
			bw.write("Results (sum over all periods & average of replications): "); bw.newLine();
			bw.write("  EW(tot): " + decimalForm.format(Main.totWaitTime) + "  Served: " + decimalForm.format(Main.totNumServed) + "  Blocked: "+ decimalForm.format(Main.totNumBlocked) + "  Arrived: " + decimalForm.format(Main.totNumArrived)); bw.newLine();bw.newLine();
			bw.write("Results (average of all periods & replications): "); bw.newLine();
			bw.write("  EW(tot): " + decimalForm.format(Main.totWaitTime/Main.SIMDURATION) + "  Served: " + decimalForm.format(Main.totNumServed/Main.SIMDURATION) + "  Blocked: "+ decimalForm.format(Main.totNumBlocked/Main.SIMDURATION) + "  Arrived: " + decimalForm.format(Main.totNumArrived/Main.SIMDURATION)); bw.newLine();bw.newLine();
			
			
			bw.write("SL(tot) (average across periods and replications):"); bw.newLine();
			double totSL=Main.totNumGoodSL/Main.SIMDURATION; 
			bw.write(decimalForm.format(totSL)); bw.newLine();bw.newLine();
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		finally
		{ 
			try{
				if(bw!=null)
					bw.close();
			}catch(Exception ex){
				System.out.println("Error in closing the BufferedWriter"+ex);
			}
		}
	}
	
	}