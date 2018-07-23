package mcms.main;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import mcms.contactcenters.MCMSContactFactory;
import mcms.contactcenters.MCMSMeasures;
import umontreal.iro.lecuyer.contactcenters.PeriodChangeEvent;
import umontreal.iro.lecuyer.contactcenters.contact.PiecewiseConstantPoissonArrivalProcess;
import umontreal.iro.lecuyer.contactcenters.queue.StandardWaitingQueue;
import umontreal.iro.lecuyer.contactcenters.router.Router;
import umontreal.iro.lecuyer.contactcenters.server.AgentGroup;
import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.randvar.RandomVariateGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.simevents.Sim;
import umontreal.iro.lecuyer.stat.Tally;
import umontreal.iro.lecuyer.stat.matrix.MatrixOfTallies;

/**
 * <h2> Simulating the Fastest-Server-First (FSF) Routing Policy </h2>
 * <p>
 * This code simulates the Fastest-Server-First (a.k.a. Closest Driver) routing policy for the Multi-Class, Multi-Server Queuing System. <br> 
 * <p> 
 * The simulation runs for the specified period length (measured in any desired unit time such as minute, hours, etc.).
 * Also the simulator repeats the simulation for the specified number of replications.
 * </p> 
 * 
 * 
 * @author	Vahid Nourbakhsh 
 * @version	2.0
 * @since	2018-07-17
 */

public class SimRunner {
	public static PeriodChangeEvent PCE;  // Event marking the beginning of each simulation period
	
	// Statistical collectors
	public static int numArrived; // Counter for arrival of contacts to the system (arrivals of any type) at the current replication
	public static int totNumArrived; // total arrival across all replications
	public static int[] repNumArrived; // Number of contacts arrived at contact centers NOT agent groups
	public static int[] numArrivedType;  // Arrivals tuple for the current replication [type]
	public static int[][] numArrivedTypeRep; // Arrivals tuple [type][replication]
	public static int numBlocked; // Counter for number of calls that are blocked and do not enter the system
	public static int totNumBlocked;
	public static int[] repNumBlocked; // Number of blocked contacts at the current replication
	public static int numServed; // Counter for number of served within or not within the AWT
	public static int totNumServed;
	public static int[] repNumServed; // Number of served contacts at the current replication
	public static int numGoodSL; // Counter for the contacts that got good service, i.e., within the AWT
	public static int totNumGoodSL;
	public static int[] repNumGoodSL; // Number of contacts served within AWT at the current replication
	public static double[][] numGoodSLKP;  // Counter for the calls that got good service per call type K, and period P
	public static double[][] totNumGoodSLKP;
	public static MatrixOfTallies<Tally> repNumGoodSLKP;
	public static double waitTime; // Wait time tracker at the current replication
	public static double[] repWaitTime;
	public static double totWaitTime;
	
	// Instance Variables
	private int numContacts; 
	private int numGroups;  
	private int[][] typeToGroup; 
	private int[][] groupToType;  
	private int[][] numAgents;  
	private double[][] demand; 
	private double[][] tau; 
	private int currentRep; 
	private PiecewiseConstantPoissonArrivalProcess[] arrivProc; 
	private AgentGroup[] groups; 
	private StandardWaitingQueue[] queues; 
	private RandomVariateGen[][] randStream; 
	
	/**
	* Constructor
	*  
	* @param numContacts 				Number of contact types
	* @param numGroups				Number of agent groups (simply called groups) 
	* @param typeToGroup	Priority list for assigning contact types to groups when a contact arrives
	* @param groupToType	Priority list for assigning groups to contact types when an agent in the group becomes free
	* @param numAgents		Number of agents in each group in each period 
	* @param demand			Demand (arrival) rate of each contact type in each period
	* @param tau			Mean service time for each (contact, group) tuple
	* @param arriProc		Distribution of the contacts arrivals. Each contact type has a separate arrival rate. Contact arrival follows Poisson distribution.
	* @param groups			Number of agents in each group
	* @param queue			Waiting queues for cotacts that find agents busy. There is one queue per agent group.
	* @param randomStream	Random number generator for service times. Service Time depends on the agent and the group.
	*/
	SimRunner(int numContacts, int numGroups, int[][] typeToGroup, int[][] groupToType, int[][] numAgents, double[][] demand, double[][] tau, int num_runs, int numPeriods, double periodDuration, double simDuration, double startTime, boolean randomServiceTime, double acceptableWaitTime) {
		this.numContacts = numContacts;
		this.numGroups = numGroups;
		this.typeToGroup = typeToGroup;
		this.groupToType = groupToType;
		this.numAgents = numAgents;
		this.demand = demand;
		this.tau = tau;
		this.arrivProc = new PiecewiseConstantPoissonArrivalProcess[numContacts];
		this.groups = new AgentGroup[numGroups]; 
		this.queues = new StandardWaitingQueue[numContacts];
		this.randStream = new RandomVariateGen[numContacts][numGroups];
		
		// Initialize Statistical collectors
		repNumArrived = new int[num_runs]; // Number of contacts arrived at contact centers NOT agent groups
		numArrivedTypeRep = new int[numContacts][num_runs]; // Number of contacts arrived at contact centers NOT agent groups
		totNumArrived = 0; // Total arrival across all replications
		repNumServed = new int[num_runs]; // Number of served contacts at the current replication
		totNumServed = 0; // Total across all replications
		repNumBlocked = new int[num_runs]; // Number of blocked contacts at the current replication
		totNumBlocked = 0; // Total across all replications
		numArrivedType = new int[numContacts]; // Arrivals of contact type k
		repWaitTime = new double[num_runs]; // Total queue wait time of contacts for each replication
		totWaitTime = 0; // Total across all replications
		numGoodSLKP = new double[numContacts][numPeriods]; // Number of contacts served within AWT: contact type k and period p
		repNumGoodSL= new int[num_runs]; // Number of contacts served within AWT: replication
		totNumGoodSL = 0; // Total across all replications
		repNumGoodSLKP = MatrixOfTallies.createWithTally(numContacts, numPeriods);	// Number of good-served contacts: contact k and period p
		
		/* PeriodChangeEvent: One dummy preliminary warm-up period, P main periods, and one wrap-up period,
		 * main periods start at time STARTINGTIME. 
		 */
		PCE = new PeriodChangeEvent(periodDuration, numPeriods + 2, startTime);  

		/* For each contact type create a Poisson arrival process. 
		 * MRG32k3a() is a random number generator.
		 */
		for (int k = 0; k < this.numContacts; k++) { 
			arrivProc[k] = new PiecewiseConstantPoissonArrivalProcess(PCE, new MCMSContactFactory(k), this.demand[k], new MRG32k3a());
		}
		
		// Initialize groups and queues
		for (int i = 0; i < numGroups; i++) groups[i] = new AgentGroup(PCE, this.numAgents[i]);
		for (int q = 0; q < queues.length; q++) queues[q] = new StandardWaitingQueue();

		// For Debugging: Fix the random seed
		// Set the random seeds for random number generator MRG32k3a().
		/*
		long[] randomseed = new long[6];
		for (int i = 0; i < randomseed.length; i++) {
			if (i < 3) {randomseed[i]=Math.round(4294967086L*Math.random())+1;}
			if (i >= 3) {randomseed[i]=Math.round(4294944442L*Math.random())+1;}
		}
		MRG32k3a.setPackageSeed(randomseed);
		*/
		
		// Assign ExponentialGen[K][I] a random number stream for service times 
		// ExponentialGen follows exponential distribution
		for (int q = 0; q < this.numContacts; q++) {
			for (int i = 0; i < this.numGroups; i++) {
				randStream[q][i] = new ExponentialGen(new MRG32k3a(), 1/tau[q][i]); // TAU: mean service time and 1/TAU: mean service rate 
			}
		}

		// Create a router 
		Router router = new FSFRouter(this.numContacts, this.numContacts, this.numGroups, this.typeToGroup, this.groupToType, this.tau, this.randStream, randomServiceTime);  // int numTypes, int numQueues, int numGroups, int[][] TypeToGroup, int[][] GroupToType, double[][] TAU

		// Define a listener (statistic collector): Upon a contact arrival notify the router.
		for (int k = 0; k < this.numContacts; k++) arrivProc[k].addNewContactListener(router);
		
		// Define agent groups for the router
		for (int i = 0; i < this.numGroups; i++)	router.setAgentGroup(i, groups[i]); 

		// Define waiting queues for the router
		for (int q = 0; q < queues.length; q++)	queues[q] = new StandardWaitingQueue();
		for (int q = 0; q < queues.length; q++) router.setWaitingQueue(q, queues[q]); 
		
		// Define a listener (statistic collector): Add ContactMeasures to the ExitedContactListener for updating statistics
		router.addExitedContactListener(new MCMSMeasures(acceptableWaitTime)); 
	}
	
	/**
	 * Simulates/runs one replication/run.
	 * Collects simulation statistics/results.
	 * The simulation runs for {@link #P}+2 periods (one warm-up and one wrap-up period).
	 * 
	 * @param numPeriods	Number of simulation periods
	 */
	public void simulateOneReplication(int numPeriods) {
		// Initialize the simulation
		Sim.init(); // Initialize simulation
		PCE.init(); // Initialize the period change event
		for (int k = 0; k < this.numContacts; k++) arrivProc[k].init(); // Initialize the arrival processes
		for (int i = 0; i < this.numGroups; i++) groups[i].init(); // Initialize the groups with their agents
		for (int q = 0; q < queues.length; q++) queues[q].init(); // Initialize the queues
		
		// Initialize statistical collectors
		Arrays.fill(numArrivedType, 0);  waitTime = 0;
		numArrived = numGoodSL = numServed = 0;
		for (int k = 0; k < this.numContacts; k++)
			for (int p = 0; p < numPeriods; p++)
				numGoodSLKP[k][p] = 0;
		
		// Start the simulation
		for (int k = 0; k < this.numContacts; k++) arrivProc[k].start();
		PCE.start();
		Sim.start();
		
		// Terminate the simulation
		PCE.stop();
		
		// Collect/save statistics
		for (int k=0; k<this.numContacts; k++) numArrivedTypeRep[k][currentRep] = numArrivedType[k];
		numArrived = IntStream.of(numArrivedType).sum();
		repNumArrived[currentRep] = numArrived;
		repNumBlocked[currentRep] = numBlocked; 
		repNumServed[currentRep] = numServed; 
		repNumGoodSL[currentRep] = numGoodSL; 
		repNumGoodSLKP.add(numGoodSLKP);
		repWaitTime[currentRep] = waitTime;
		repNumGoodSLKP.init();
	}
	
	/**
	 * Simulates/runs all replications.
	 * Collects simulation statistics/results.
	 * 
	 * @param numReps		Number of simulation replications
	 * @param numPeriods	Number of simulation periods
	 */
	public void simulate(int numReps, int numPeriods) {
		// Replication counter
		
		// Run the simulation for the number of replications
		for (int r = 0; r < numReps; r++) simulateOneReplication(numPeriods);

		// Calculate aggregate statistics
		totNumArrived = IntStream.of(repNumArrived).sum();
		totNumBlocked = IntStream.of(repNumBlocked).sum(); 
		totNumServed = IntStream.of(repNumServed).sum(); 
		totNumGoodSL = IntStream.of(repNumGoodSL).sum(); 
		totWaitTime = DoubleStream.of(repWaitTime).sum();
	}
}