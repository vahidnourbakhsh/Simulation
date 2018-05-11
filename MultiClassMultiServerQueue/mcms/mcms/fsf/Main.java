package mcms.fsf;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

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

public class Main {
	static int NUMREPS; // Number of simulation replications 
	static int P; // Number of periods in each replication 
	static double PERIODDURATION; // Length of each period (I assume minutes but it could be any time unit, second, minute, etc.)
	static double SIMDURATION; // Total simulation length
	static double STARTINGTIME;  // PCE: main period starting time
	static int DETERMINISTICSERVICETIME; // 0: deterministic service time 1: Random (exponential) service time
	static double AWT; 	// Acceptable Queue waiting time
	static String INPUTPATH ; // File path: required for reading input data from drive
	static String RESULTSPATH; // Path for saving/printing the simulation results
	static int RESULTSDECIMALPOINTS; // number of decimals for saving results 
	static PeriodChangeEvent PCE;  // Event marking the beginning of each simulation period
	
	// Statistical collectors
	static int numArrived; // Counter for arrival of contacts to the system (arrivals of any type) at the current replication
	static int totNumArrived; // total arrival across all replications
	static int[] repNumArrived; // Number of contacts arrived at contact centers NOT agent groups
	static int[] numArrivedType;  // Arrivals tuple for the current replication [type]
	static int[][] numArrivedTypeRep; // Arrivals tuple [type][replication]
	static int numBlocked; // Counter for number of calls that are blocked and do not enter the system
	static int totNumBlocked;
	static int[] repNumBlocked; // Number of blocked contacts at the current replication
	static int numServed; // Counter for number of served within or not within the AWT
	static int totNumServed;
	static int[] repNumServed; // Number of served contacts at the current replication
	static int numGoodSL; // Counter for the contacts that got good service, i.e., within the AWT
	static int totNumGoodSL;
	static int[] repNumGoodSL; // Number of contacts served within AWT at the current replication
	static double[][] numGoodSLKP;  // Counter for the calls that got good service per call type K, and period P
	static double[][] totNumGoodSLKP;
	MatrixOfTallies<Tally> repNumGoodSLKP;
	static double waitTime; // Wait time tracker at the current replication
	static double[] repWaitTime;
	static double totWaitTime; 		
	
	// Declaring fields
	int K; // number of call types
	int I; // number of server groups 
	int[][] typeToGroup; // priority list for assigning call types to groups when a call arrives
	int[][] groupToType; // priority list for assigning groups to call types when a server in the group frees
	int[][] numAgents; // number of agents in each group in each period 
	double[][] demand; // demand (arrival) rate of each call type in each period
	double[][] tau; // Mean service time for each call-server tuple
	int currentRep; // Current replication
	PiecewiseConstantPoissonArrivalProcess[] arrivProc; // Contacts arrival process follows Poisson distribution 
	AgentGroup[] groups; // Groups of homogeneous servers
	StandardWaitingQueue[] queues; // Queues for contacts that find all servers in the group busy and should wait for a server to become available
	RandomVariateGen[][] sgen2;
			
	// Initialization code, constructor
	Main(int K, int I, int[][] typeToGroup, int[][] groupToType, int[][] numAgents, double[][] demand, double[][] tau) {
		this.K=K;
		this.I=I;
		this.typeToGroup=typeToGroup;
		this.groupToType=groupToType;
		this.numAgents=numAgents;
		this.demand=demand;
		this.tau=tau;
		this.arrivProc = new PiecewiseConstantPoissonArrivalProcess[K];
		this.groups = new AgentGroup[I]; 
		this.queues = new StandardWaitingQueue[K]; // One queue for each contact type
		this.sgen2 = new RandomVariateGen[K][I]; // Service time generator for each contact type [k] and agents group [i]


		// Initialize Statistical collectors
		repNumArrived = new int[NUMREPS]; // Number of contacts arrived at contact centers NOT agent groups
		numArrivedTypeRep = new int[K][NUMREPS]; // Number of contacts arrived at contact centers NOT agent groups
		totNumArrived = 0; // total arrival across all replications
		repNumServed = new int[NUMREPS]; // Number of served contacts at the current replication
		totNumServed = 0;
		repNumBlocked = new int[NUMREPS]; // Number of blocked contacts at the current replication
		totNumBlocked = 0;
		numArrivedType = new int[K]; //Arrival to contact type [k]
		repWaitTime = new double[NUMREPS]; //Total queue wait time of contacts
		totWaitTime = 0; 	
		numGoodSLKP = new double[K][P];  
		repNumGoodSL= new int[NUMREPS]; // Number of contacts served within AWT
		totNumGoodSL = 0;
		repNumGoodSLKP = MatrixOfTallies.createWithTally(K, P);		
		
		//PeriodChangeEvent: One dummy preliminary period, P main periods, and one wrap-up period, 
		// main periods start at time STARTINGTIME.
		PCE = new PeriodChangeEvent(PERIODDURATION, P + 2, STARTINGTIME);  

		// For each contact type create a Poisson arrival process 
		// MRG32k3a() is a random number generator
		for (int k = 0; k < K; k++) { 
			arrivProc[k] = new PiecewiseConstantPoissonArrivalProcess(PCE, new MCMSContactFactory(k), demand[k], new MRG32k3a());
		}
		
		// Initialize groups and queues
		for (int i = 0; i < I; i++) groups[i] = new AgentGroup(PCE, numAgents[i]);
		for (int q = 0; q < queues.length; q++) queues[q] = new StandardWaitingQueue();

		// Set the random seeds for random number generator MRG32k3a()
		long[] randomseed = new long[6];
		for (int i=0; i<randomseed.length; i++) {
			if (i <3) {randomseed[i]=Math.round(4294967086L*Math.random())+1;}
			if (i>=3) {randomseed[i]=Math.round(4294944442L*Math.random())+1;}
			}
		MRG32k3a.setPackageSeed(randomseed);

		// assign sgen[K][I] a random number stream for service times 
		// sgen follows exponential distribution
		for (int q = 0; q < K; q++) {
			for (int i = 0; i < I; i++) {
				sgen2[q][i] = new ExponentialGen(new MRG32k3a(), 1/tau[q][i]); // TAU: mean service time and 1/TAU: mean service rate 
			}
		}

		// Create a router 
		Router router = new FSFRouter(K, K, I, typeToGroup, groupToType, tau, sgen2);  // int numTypes, int numQueues, int numGroups, int[][] TypeToGroup, int[][] GroupToType, double[][] TAU

		// Upon a contact arrival notify the router
		for (int k = 0; k < K; k++) arrivProc[k].addNewContactListener(router);
		
		// Define agent groups for the router
		for (int i = 0; i < I; i++)	router.setAgentGroup(i, groups[i]); 

		// Define waiting queues for the router
		for (int q = 0; q < queues.length; q++)	queues[q] = new StandardWaitingQueue();
		for (int q = 0; q < queues.length; q++) router.setWaitingQueue(q, queues[q]); 
		
		// Add ContactMeasures to the ExitedContactListener for updating statistics
		router.addExitedContactListener(new MCMSContactMeasures()); 
	}

	public void simulateOneReplication() {
		/**
		 * Simulate one replication 
		 **/
		Sim.init();
		PCE.init();
		for (int k = 0; k < K; k++) arrivProc[k].init();
		for (int i = 0; i < I; i++) groups[i].init();
		for (int q = 0; q < queues.length; q++) queues[q].init();
		
		Arrays.fill(numArrivedType, 0);  waitTime = 0;
		numArrived = numGoodSL = numServed = 0;
		
		for (int k = 0; k < K; k++)
			for (int p = 0; p < P; p++)
				numGoodSLKP[k][p] = 0;
		for (int k = 0; k < K; k++) arrivProc[k].start();
		PCE.start();
		Sim.start();
		PCE.stop();
		for (int k=0; k<K; k++) numArrivedTypeRep[k][currentRep]=numArrivedType[k];
		repNumArrived[currentRep]= numArrived;
		repNumBlocked[currentRep]=numBlocked; 
		repNumServed[currentRep]=numServed; 
		repNumGoodSL[currentRep]=numGoodSL; 
		repNumGoodSLKP.add(numGoodSLKP);
		repWaitTime[currentRep]=waitTime;
		++currentRep;
		
	}

	public void simulate(int numReps) {
		/**
		 * Simulate all replications 
		 **/
		currentRep = 0;
		repNumGoodSLKP.init();
		for (int r = 0; r < numReps; r++) simulateOneReplication();
		
		totNumArrived = IntStream.of(repNumArrived).sum();
		totNumBlocked = IntStream.of(repNumBlocked).sum(); 
		totNumServed = IntStream.of(repNumServed).sum(); 
		totNumGoodSL = IntStream.of(repNumGoodSL).sum(); 
		totWaitTime = DoubleStream.of(repWaitTime).sum();
	}
	
	public static void main(String[] args) {
		/**
		 * Specify the example that should be simulated 
		 **/
		
		NUMREPS = 5; // Number of simulation replications 
		P = 12; // Number of periods in each replication (Notice: there is one warm-up and one wrap-up period, during which no statistics is collected)
		PERIODDURATION = 30*24; // Length of each period (I assume minutes but it could be any time unit, second, minute, etc.)
		SIMDURATION = NUMREPS * P * PERIODDURATION; // Total simulation length
		STARTINGTIME = 0;  // PCE: main period starting time
		DETERMINISTICSERVICETIME = 1; // 0: deterministic service time 1: Random (exponential) service time
		AWT = 10; 	// Acceptable Queue waiting time
		RESULTSDECIMALPOINTS = 6; // number of decimals for saving results
		
		// File path: required for reading input data from drive
		INPUTPATH = "/PATH/TO/THE/INPUT/FILES/"; // Example: "/USER/Documents/" (On Windows OS use "\\" and on Mac OS use "/" to separate folders)		
		// Path for saving/printing the simulation results
		RESULTSPATH = INPUTPATH;
		int exNum = 1; // example number to be solved.
		int I = 2; // number of contact types 
		int K = 2; // number of agent groups
		
		// Inputs files
		String agents = exNum + "agents-AMPL.dat"; // agent groups file name. It defines the number of agents in each group
		String demands = exNum + "demands-AMPL.dat"; // demands(or arrivals) file name. It defines the arrival rate for each contact type
		String routes = exNum + "routes-AMPL.dat"; // routes name. It defines eligible assignments of contact types to servers

		
		// NUMAGENTS[k][p] gives the number of agents for group i in period p
		// Wrap-up period has the same number of agents as the one before the wrap-up period
		int[][] numAgents=ReadData.addColToNumArray(ReadData.readNumAgents(agents, INPUTPATH, I), P);
		
		// Arrival rate for contact type k in period p
		double[][] demand=ReadData.addColToDemArray(ReadData.readDemand(demands, INPUTPATH, K), P);
		
		// Mean service time for each contact-agent combination at each period
		// NOTE: mean Service time (NOT time, reciprocal of time) for each contact[k]-agent[i] combination equal for all periods [k][i]
		ArrayList<List<Number>> tauVector = ReadData.readTauadv(routes, INPUTPATH, K, I);
		double[][] tau=ReadData.arrayListToPrimitiveArray(tauVector, K, I); //Build TAU from TAU_Vec	

		// Group to type priority list
		int[][] groupToType = BuildLists.buildGroupToType(tauVector);
		
		// Type to group priority list
		int[][] typeToGroup = BuildLists.buildTypeToGroup(tauVector);
		
		// File for saving the simulation results
		File simResultsFile = new File(RESULTSPATH + exNum + "FSF_Sim_Results" + "" + "_I" + K + "_J" + I + ".out");
		
		// Create a new instance of the Main() class
		Main s = new Main (K, I, typeToGroup, groupToType, numAgents, demand, tau);
		
		// Run the simulation 
		s.simulate(NUMREPS);
		
		// Print the results to the console
		PrintResults.printStatisticsToConsole(exNum);
		
		// Save the results
		PrintResults.printResultsToFile(exNum, simResultsFile);
	}
}
