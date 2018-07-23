package mcms.main;

import umontreal.iro.lecuyer.contactcenters.contact.Contact;
import umontreal.iro.lecuyer.contactcenters.queue.DequeueEvent;
import umontreal.iro.lecuyer.contactcenters.queue.WaitingQueue;
import umontreal.iro.lecuyer.contactcenters.router.Router;
import umontreal.iro.lecuyer.contactcenters.server.Agent;
import umontreal.iro.lecuyer.contactcenters.server.AgentGroup;
import umontreal.iro.lecuyer.contactcenters.server.EndServiceEvent;
import umontreal.iro.lecuyer.randvar.RandomVariateGen;


/**
 * <h1> Fastest-Server-First (FSF) Router </h1>
 *  <p>
 *  This class {@linkplain #FSFRouter(int, int, int, int[][], int[][], double[][], RandomVariateGen[][])} extends the Router class from {@linkplain umontreal.iro.lecuyer.contactcenters.router.Router}.
 *  It implements the Fastest-Server-First (a.k.a. Closest Driver) routing policy.
 * 
 *  The router responsibilities in the Multi-Class Multi-Server queuing system:
 *  <ul>
 *  <li> The router determines if an agent can serve a contact.</li>
 *  <li> The router chooses an agent for a contact that is dequeued. </li>
 *  <li> The router queues contacts that are waiting for service. </li>
 *  <li> The router selects a contact for an idle agent. </li>
 *  </ul>    
 * 	</p>
 * 
 *  The FSF router works as follows:
 *  <ul>
 * <li> When an agent becomes free, among queues who have contacts waiting (i.e., queue length>=1), 
 * choose the queue with the fastest service rate, i.e., with the minimum {@code tau}, 
 * where tau is the service time (i.e. the reciprocate of the service rate). </li> 
 * <li> If an arriving contact of type {@code k} finds no calls of that type waiting for service 
 * and agents of one or more matching groups available, select an agent of group {@code i} 
 * with the maximum service rate, i.e., with the minimum {@code tau}.
 * </li> 
 * </ul>
 *  
 * @author	Vahid Nourbakhsh 
 * @version	2.0
 * @since	2018-03-18
 */
public class FSFRouter extends Router {
	/**
	 * Constructor.
	 * 
	 * @param		typeToGroup		The priority list used when a contact should be routed to an agent.
	 * @param		groupToType		The priority list used when an agent should be assigned to a contact.
	 * @param		tau				The mean service time (i.e., the reciprocate of the mean service time).
	 * @param		randomGen		Random number generator. This random number stream would be used for determining the service times, tau's.
	 */
	private int[][] typeToGroup; 
	private int[][] groupToType;
	private double[][] tau;
	private RandomVariateGen[][] randomGen;
	private boolean randServiceTime;
	FSFRouter(int numTypes, int numQueues, int numGroups, int[][] typeToGroup, int[][] groupToType, double[][] tau, RandomVariateGen[][]  randomGen, boolean randServiceTime) {
		super(numTypes, numQueues, numGroups);
		this.typeToGroup = typeToGroup;
		this.groupToType = groupToType;
		this.tau = tau;
		this.randomGen = randomGen;
		this.randServiceTime = randServiceTime;
	}

	/**
	 * Determine if the agent can serve (is eligible to serve) the contact, 
	 * regardless of the availability.
	 * 
	 * @param	i			agent group {@code i}
	 * @param	k			contact type {@code k}
	 * @return 	boolean 	true if agent group {@code i} can serve customer type {@code k}
	 */
	public boolean canServe(int i, int k) {
		if (groupToType[i][k] >= 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Select an agent group or a queue for a contact that arrives:
	 * @param	ct		A contact instance 
	 * @return 	ese		Return the selected agent to serve the contact 
	 * 					if there is no queued contact, or
	 * 					return null if the agents are busy
	 */
	protected EndServiceEvent selectAgent (Contact ct) {
		EndServiceEvent ese;
		int G=typeToGroup[ct.getTypeId()].length;
		for (int g=0; g<G; g++ ){
			AgentGroup group = getAgentGroup(typeToGroup[ct.getTypeId()][g]);
			if (group.getNumFreeAgents()>0){
				if (randServiceTime){
					ct.setDefaultServiceTime(randomGen[ct.getTypeId()][group.getId()].nextDouble()); // Exponentially distributed service time
				}
				else {
					ct.setDefaultServiceTime(tau[ct.getTypeId()][group.getId()]); // Deterministic service time
				}						
				ese=group.serve(ct);
				return ese;
			}
		}
		return null; // return null if all agents are busy
	}

	/**
	 * Queue selection for incoming contacts that find all agents busy (selectAgent returns null): 
	 * Selects a waiting queue and appends the contact to the end of the queue
	 * 
	 * @param	ct		A contact instance
	 * @return 	queue	The dequeue event if the contact could be queued, or null otherwise.
	 * 
	 * Note from the source package {@link umontreal.iro.lecuyer.contactcenters}: 
	 * Constructor DequeueEvent(WaitingQueue queue,Contact contact, double enqueueTime) is rarely used directly;
	 * the recommended way to create dequeue events is to use WaitingQueue.add(Contact).
	 */
	protected DequeueEvent selectWaitingQueue(Contact ct) {
		final WaitingQueue queue = getWaitingQueue(ct.getTypeId());
		return queue.add(ct);
	}

	/**
	 * Select a contact for an agent that become free.
	 * That is, select a contact from a queue for an idle agent.
	 * <br>
	 * <b> Steps: </b>
	 * <li> Choose a non-empty queue.</li>
	 * <li> Pick a contact from the chosen queue. </li>
	 *
	 * Notice that contacts are queued at the contact nodes. 
	 * We have one waiting queue per each contact type.
	 * 
	 * @param	group		The group that the agent belongs to.
	 * @param	agent		The agent in the group.
	 * @return	dqe			The dequeue event if there is a contact in one of the queues; null, otherwise.
	 *  
	 */
	protected DequeueEvent selectContact(AgentGroup group, Agent agent) {
		DequeueEvent dqe;
		int T=groupToType[group.getId()].length;
		for (int t=0; t<T; t++ ){
			WaitingQueue queue = getWaitingQueue(groupToType[group.getId()][t]);
			if (queue.isEmpty()){
				continue;
			}
			else {
				Contact ct=queue.getFirst().getContact();
				if (randServiceTime){
					ct.setDefaultServiceTime(randomGen[ct.getTypeId()][group.getId()].nextDouble()); // Random (exponential) service time
				}
				else {
					ct.setDefaultServiceTime(tau[ct.getTypeId()][group.getId()]); // Deterministic service time
				}
				dqe=queue.removeFirst(DEQUEUETYPE_BEGINSERVICE);
				return dqe;
			}
		}
		return null; // return null if all queues are empty
	}
	
	/**
	 * An implementation of the abstract method, 
	 * {@linkplain  umontreal.iro.lecuyer.contactcenters.router.Router.checkWaitingQueues}
	 * 
	 * This method is called when the agent group contains no more
	 * <b>online</b> agents, i.e., {@link AgentGroup.getNumAgents()} returns 0. 
	 * We check each waiting queue accessible for agents in the group to
	 * determine if they need to be cleared. A queue is cleared if no agent,
	 * whether free or busy, is available to serve any contact in it
	 * We don't need this method. We assume that if there is a contact
	 * there is an agent group able to serve it (it's agents could be
	 * free or busy, though). So, we don't need to clear the queues.
	 * 
	 * @param	group	An agent group.
	 */
	protected void checkWaitingQueues(AgentGroup group) {
	}
}
