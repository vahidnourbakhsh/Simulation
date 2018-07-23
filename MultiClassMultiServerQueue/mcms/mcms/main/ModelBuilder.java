package mcms.main;

import java.util.ArrayList;
/**
 * This class defines a fastest-server-first routing model. <br>
 * The model or routing policy is able to determine agent groups or waiting queues for all types of contacts.
 * 
 * @author	Vahid Nourbakhsh 
 * @version	2.0
 * @since	2018-07-23
 */
public class ModelBuilder {
	int exNum = 2; 
	int numContactTypes = 2;  
	int numGroups = 2; 
	int numReps = 10; 
	int numPeriods = 12; 
	double periodDuration = 30 * 24;
	double simDuration = numReps * numPeriods * periodDuration;
	double startTime = 0;
	boolean randomServiceTime = true;
	double acceptableWaitTime = 10;
	int numDecimals = 6;
	String inputPath = "./mcms/resources/test/input/"; 
	String resultsPath = "./mcms/resources/test/output/";
	String agents = exNum + "agents.dat";
	String demands = exNum + "demands.dat";
	String routes = exNum + "routes.dat";
	/**
	 * Constructor:
	 * 
	 * @param exNum					Example number to be solved
	 * @param numContactTypes		Number of contact types
	 * @param numGroups				Number of agent groups
	 * @param numReps				Number of simulation replications
	 * @param numPeriods			Number of periods in each replication (Notice: there is one warm-up and one wrap-up period, during which no statistics is collected)		
	 * @param periodDuration		Length of each period (I assume minutes but it could be any time unit, second, minute, etc.)
	 * @param simDuration			Total simulation length
	 * @param startTime				PCE: main period starting time
	 * @param randomServiceTime		Random or deterministic service time - True: Random (exponential) service time, or False: deterministic service time
	 * @param acceptableWaitTime	Acceptable queue waiting time
	 * @param numDecimals			Number of decimals for saving results
	 * @param inputPath				Path to the input data. Example: "/USER/Documents/" (On Windows OS use "\\" and on Mac OS use "/" to separate path folders)
	 * @param resultsPath			Path for saving/printing the simulation results
	 * @param agents				Agent groups file name. It defines the number of agents in each group
	 * @param demands				Demands (or arrivals of contacts) file name. It defines the arrival rate for each contact type
	 * @param routes				Routes file name. It defines eligible assignments of contact types to servers and the service times
	 */
	ModelBuilder(int exNum, int numContactTypes, int numGroups, int numReps, int numPeriods, double periodDuration, double simDuration, double startTime, boolean randomServiceTime, double acceptableWaitTime, int numDecimals, String inputPath, String resultsPath, String agents, String demands, String routes) {
		this.exNum = exNum;
		this.numContactTypes = numContactTypes;
		this.numGroups = numGroups;
		this.numReps = numReps;
		this.periodDuration = periodDuration;
		this.simDuration = simDuration;
		this.startTime = startTime;
		this.randomServiceTime = randomServiceTime;
		this.acceptableWaitTime = acceptableWaitTime;
		this.numDecimals = numDecimals;
		this.inputPath = inputPath;
		this.resultsPath = resultsPath;
		this.agents = agents;
		this.demands = demands;
		this.routes = routes;
	}

	/**
	 * <p> Builds a fastest-server-first model. <br> </p>
	 * The model has the following components:
	 * <ul> 
	 * <li> groups: The number of agents in each group </li>
	 * <li> contacts: The arrival rates of contacts. Different contact types have different arrival rates. </li>
	 * <li> serviceTime: Mean service time for each (contact, group) tuple at each period. 
	 * 						NOTE: mean Service time is the reciprocal of the mean service rate. 
	 * 						For each (contact[k], group[i]) combination equal for all periods. </li>  
	 * 	<li> groupToType: group-to-type routing priority list </li>
	 * <li> typeToGroup: type-to-group priority list </li>
	 * </ul> 
	 * @return FSFModel	a fastest-server-first model
	 */
	public SimRunner build() {
		/*
		 *  Read the number of agents in each group at each period. 
		 *  Wrap-up period has the same number of agents as the one before the wrap-up period.
		 */
		int[][] groups = mcms.input.InputAgent.expGroupsAcrosPs(mcms.input.InputAgent.readAgentGroups(agents, inputPath, numContactTypes), numPeriods);
		
		// Arrival rate for contact type k in period p
		double[][] contacts = mcms.input.InputContact.expDemAcrosPs(mcms.input.InputContact.readDemands(demands, inputPath, numGroups), numPeriods);
		
		/*
		 * Mean service time for each (contact, group) tuple at each period
		 * NOTE: mean Service time is the reciprocal of the mean service rate
		 * For each (contact[k], group[i]) combination equal for all periods 
		 */
		ArrayList<mcms.input.InputSrvTime> serviceTimeVec = mcms.input.InputSrvTime.readServiceTimes(routes, inputPath, numGroups, numContactTypes);
		double[][] serviceTime = mcms.input.InputSrvTime.arrayListToPrimitiveArray(serviceTimeVec, numGroups, numContactTypes); //Build TAU from TAU_Vec

		// Build the group-to-type routing priority list
		int[][] groupToType = mcms.input.InputSrvTime.buildGroupToType(serviceTimeVec, numGroups);
		
		// Build the type-to-group priority list
		int[][] typeToGroup = mcms.input.InputSrvTime.buildTypeToGroup(serviceTimeVec, numContactTypes);
		
		SimRunner fsfModel = new SimRunner(numContactTypes, numGroups, typeToGroup, groupToType, groups, contacts, serviceTime, numReps, numPeriods, periodDuration, simDuration, startTime, randomServiceTime, acceptableWaitTime);
		return fsfModel;
		
	}
}
