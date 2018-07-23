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

public class InputAgent {
	/**
	 * Reads the number of agents in each group of agents. 
	 * Agents are grouped, where agents in each group have equal service times. 
	 *  @param file_name			Name of the File which contains the number of agents in each groups.
	 *  @param file_path			Path to the file location. Path+Name gives the file location and name.
	 *  @param num_agent_groups		Total number of groups. In each group there could be multiple agents.
	 *  @return agents 				An array of length J. Each element indicates the number of agents in the corresponding group.
	 */
	public static int[] readAgentGroups (String file_name, String file_path, int num_agent_groups) {
		Scanner scanner;
		Scanner linescanner;
		int [] agents = new int [num_agent_groups];
		int i = 0;
		try {
			scanner = new Scanner(new File(file_path+file_name));
			while(scanner.hasNextLine()) {
			    String line = scanner.nextLine();
			    if(!line.startsWith("#")) {
			    	linescanner = new Scanner(line);
			    	linescanner.nextInt();
			    	agents[i++] = linescanner.nextInt();
			    }    
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return agents;
	}

	/**
	 * The simulation model runs for multiple periods, for example 60 minutes/periods.
	 * As an input to the simulation model, we need to submit the number of available 
	 * agents for each agent group at each period. 
	 * This is like the schedule/shifts of agents.
	 * We assume that agents' availability does not change over time. 
	 * Thus, this method copies the demand rate for "p+2" periods. 
	 * Notice that there is one warm-up and one wrap-up period. 
	 * During these two periods we do not collect statistics.  
	 * 
	 *  @param input		1-d input array for contact arrival rates.
	 *  @param num_periods 	Number of simulation periods. 
	 *  @return output 		Input array copied p+2 times.    	 
	 */
	public static int[][] expGroupsAcrosPs(int[] input, int num_periods){
		final int n=num_periods+2;
		int[][] output = new int [input.length][n];
		for (int r=0; r<input.length; r++){
			for (int c=1; c<n; c++){
				output[r][c]=input[r];
			}
		}
		return output;
	}
}
