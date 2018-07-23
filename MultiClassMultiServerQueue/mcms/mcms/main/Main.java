package mcms.main;

import mcms.output.Output;

/**
 * Solve an example/instance
 * 
 * <p>
 * <b> STEPS:</b> <br>
 * <ol> 
 * <li> <b> Simulation setup </b>: Choose the simulation duration, number of replications, etc. </li>
 * <li> <b> Instance input and output: </b> Input the path to the input files, and the path for the simulation output. </li> 
 * </ol>
 * </p>
 * 
 * <p>
 * This code simulates the Fastest-Server-First (a.k.a. Closest Driver) routing policy for the Multi-Class, Multi-Server Queuing System. 
 * <h3> Framework: Multi-Class Multi-Server Queuing System </h3>
 * 
 * <p>
 * In our multi-class multi-server queuing system, jobs (here contacts) of types $k \in K$ arrive according to independent Poisson processes with rates $d_k$. 
 * The FSF router does not block any job. Also, there is no abandonment or retrial following the arrival stage.
 * Each job must be routed to a server group $i \in F_k$, where $F_k$ is the set of server groups that are eligible to serve jobs of type $k$. 
 * For completeness, we will denote $I$ as the full set of server groups (henceforth known simply as groups) and $F_i$ as the set of job types that can be served by group $i$.
 * In the contact center literature, the terms job type and server group are referred to as call/contact type and agent group, respectively. 
 * At each server group $i$, there are $k_i$ identical servers. Service times are independent, each exponentially distributed with mean service time $\tau_{ki}$. 
 * </p>
 * 
 * 
 * <p>
 * <h3> FSF Policy </h3>
 * When a job (or call in call centers) of type $i$ arrives, a job-to-group priority list for that job type determines the order in which the groups are checked for a free server (or agent in call centers). 
 * The priority list is an ordered list of all groups that can serve job $i$, i.e., $j \in F_i$, sorted from smallest to largest service times $\tau_{ij}$. 
 * The policy is called FSF because the fastest server has the highest priority in the list. There is one queue for each job type $i \in I$. 
 * If job $i$ finds all groups in $F_i$ busy, it will stay in queue $i$, where it will be served in first-come, first-served order. 
 * Similarly, when a server in group $j$ becomes free, a group-to-job list for that server group determines the order by which the server picks the next job to serve. 
 * The group-to-job list for group $j$ is a list of all job types that server group $j$ can serve, i.e., $i \in F_j$, sorted in increasing order by $\tau_{ij}$'s. 
 * If all queues in the list are empty, then the server stays free.
 * </p>
 *  
 * For more details on the queuing system and the routing policy refer to the paper draft: </br>
 * <a href="https://papers.ssrn.com/sol3/papers.cfm?abstract_id=2967811">https://papers.ssrn.com/sol3/papers.cfm?abstract_id=2967811</a>
 *  
 *  <h3> Prerequisites (Libraries) </h3>
 * <p>
 * Import the following libraries: 
 * 
 * <ul>
 * <li> <b> ContactCenters </b> </li>
 * <a href="http://simul.iro.umontreal.ca/contactcenters/index.html">http://simul.iro.umontreal.ca/contactcenters/index.html</a>
 *
 * <li> <b> Stochastic Simulation in Java (SSJ) </b> </li>
 * <a href="http://simul.iro.umontreal.ca/ssj/indexe.html"> http://simul.iro.umontreal.ca/ssj/indexe.html</a>
 * </ul>
 * </p>
 * 
 * @param args Check above.
 * @return Simulation results will be printed to the console and written to an output file.
 * @version	2.0
 */

public class Main{
	public static void main(String[] args) {
		/*
		 * Simulation setup
		 */
		int exNum = 2; // example number to be solved.
		int numContactTypes = 2; // number of contact types 
		int numGroups = 2; // number of agent groups
		int numReps = 10; // Number of simulation replications
		int numPeriods = 12; // Number of periods in each replication (Notice: there is one warm-up and one wrap-up period, during which no statistics is collected)
		double periodDuration = 30 * 24; // Length of each period (I assume minutes but it could be any time unit, second, minute, etc.)
		double simDuration = numReps * numPeriods * periodDuration; // Total simulation length
		double startTime = 0;  // PCE: main period starting time
		boolean randomServiceTime = true; // 0: deterministic service time 1: Random (exponential) service time
		double acceptableWaitTime = 10; 	// Acceptable Queue waiting time
		int numDecimals = 6; // number of decimals for saving results
		
		/*
		 * Input and output files
		 */
		// File path: required for reading input data from drive
		String inputPath = "./mcms/resources/test/input/"; // Path to the input data. Example: "/USER/Documents/" (On Windows OS use "\\" and on Mac OS use "/" to separate path folders)		
		
		// Path for saving/printing the simulation results
		String resultsPath = "./mcms/resources/test/output/";
		
		// Name of the input files
		String agents = exNum + "agents.dat"; // agent groups file name. It defines the number of agents in each group
		String demands = exNum + "demands.dat"; // demands(or arrivals) file name. It defines the arrival rate for each contact type
		String routes = exNum + "routes.dat"; // routes name. It defines eligible assignments of contact types to servers

		/*
		 * Build a routing model and run the simulation
		 */
		
		// Create the FSF routing model
		ModelBuilder myBuilder = new ModelBuilder(exNum, numContactTypes, numGroups, numReps, numPeriods, periodDuration, simDuration, startTime, randomServiceTime, acceptableWaitTime, numDecimals, inputPath, resultsPath, agents, demands, routes); 
		SimRunner myModel = myBuilder.build();
				
		// Run the simulation model
		myModel.simulate(numReps, numPeriods);
		
		// Print the results to the console
		Output.printStatisticsToConsole(exNum, numGroups, numContactTypes, numReps, numPeriods, periodDuration, simDuration, startTime, randomServiceTime, acceptableWaitTime, numDecimals);
		
		// Save the results
		Output.printResultsToFile(resultsPath, exNum, numGroups, numContactTypes, numReps, numPeriods, periodDuration, simDuration, startTime, randomServiceTime, acceptableWaitTime, numDecimals);
	}
}