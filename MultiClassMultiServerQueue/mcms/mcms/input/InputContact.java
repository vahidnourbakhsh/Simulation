package mcms.input;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * <h1> Read Simulation Input Data </h1>
 *  <p>
 *  Read simulation input data.
 *  Then, format the input data and prepare it for the simulation step.
 * 	</p>
 *  
 * @author	Vahid Nourbakhsh 
 * @version	2.0
 * @since	2018-03-15
 */

public class InputContact {
	/**
	 *	Reads demand (i.e., arrival rate of contacts of different types)  
	 *  Contacts that enter the system have different type. 
	 *  Each type has its own arrival rate and they all follow Poisson distribution.
	 * 
	 *  @param file_name			Name of the File which contains the number of agents in each groups.
	 *  @param file_path			Path to the file location.
	 *  @param num_contact_types 	Total number of contact types. contacts arrive randomly following a Poisson distribution.
	 *  @return demand 				An array where each element indicates the arrival rate of the corresponding contact type.
	 */
	public static double[] readDemands (String file_name, String file_path, int num_contact_types) {
		Scanner scanner;
		Scanner lineScanner;
		double [] demand = new double [num_contact_types];
		int i = 0;
		String line;
		try {
			scanner = new Scanner(new File(file_path+file_name));
			while(scanner.hasNextLine()) {
			    line = scanner.nextLine();
			    if(!line.startsWith("#")) {
			    	lineScanner = new Scanner(line);
			    	lineScanner.nextInt();
			    	demand[i++] = lineScanner.nextDouble();
			    }
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return demand;
	}
	
	/**
	 * The simulation model runs for multiple periods, 
	 * for example 60 minutes/periods.
	 * As an input to the simulation model, 
	 * we need to submit the arrival/demand rate of each contact type at each period.
	 * We assume that the demand rate does not change over time. 
	 * Thus, this method copies the demand rate for "p+2" periods. 
	 * Notice that there is one warm-up and one wrap-up period. 
	 * During these two periods we do not collect statistics.  
	 * 
	 *  @param input			1-d input array for contact arrival rates.
	 *  @param num_periods		Number of simulation periods.
	 *  @return output 			Input array copied p+2 times.    	 
	 */
	public static double[][] expDemAcrosPs(double[] input, int num_periods){
		final int n=num_periods+2; // number of periods plus one warm-up and one wrap-up period
		double[][] output = new double [input.length][n];
		for (int r=0; r<input.length; r++){
			for (int c=1; c<n-1; c++){
				output[r][c]=input[r];
			}
		}
		return output;
	}
}
