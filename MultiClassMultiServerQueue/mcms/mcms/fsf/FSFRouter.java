package mcms.fsf;

import umontreal.iro.lecuyer.contactcenters.contact.Contact;
import umontreal.iro.lecuyer.contactcenters.queue.DequeueEvent;
import umontreal.iro.lecuyer.contactcenters.queue.WaitingQueue;
import umontreal.iro.lecuyer.contactcenters.router.Router;
import umontreal.iro.lecuyer.contactcenters.server.Agent;
import umontreal.iro.lecuyer.contactcenters.server.AgentGroup;
import umontreal.iro.lecuyer.contactcenters.server.EndServiceEvent;
import umontreal.iro.lecuyer.randvar.RandomVariateGen;

public class FSFRouter extends Router {
	/**
	 * FSF Router 
	 * - When an agent becomes free, among queues who have contacts waiting (i.e., queue length>=1), 
	 * choose the queue with the fastest service rate, i.e., with Minimum tau
	 * - If an arriving contact of type k finds no calls of that type waiting for service 
	 * and agents of one or more matching groups available, select an agent of group i 
	 * with the maximum service rate 
	 */

	private int[][] TypeToGroup;
	private int[][] GroupToType;
	private double[][] TAU;
	private RandomVariateGen[][] sgen2;
	FSFRouter(int numTypes, int numQueues, int numGroups, int[][] TypeToGroup, int[][] GroupToType, double[][] TAU, RandomVariateGen[][]  sgen2) {
		super(numTypes, numQueues, numGroups);
		this.TypeToGroup=TypeToGroup;
		this.GroupToType=GroupToType;
		this.TAU=TAU;
		this.sgen2 =sgen2;
	}

	// Returns true if agent group i can serve customer type k
	public boolean canServe(int i, int k) {
		if (GroupToType[i][k] >= 0) {
			return true;
		} else {
			return false;
		}
	}

	protected void checkWaitingQueues(AgentGroup group) {
		/**
		 * This method is called when the agent group "group" contains no more
		 * ONLINE agents, i.e., AgentGroup.getNumAgents() returns 0. It must
		 * check each waiting queue accessible for agents in this group to
		 * determine if they need to be cleared. A queue is cleared if no agent,
		 * whether free or busy, is available to serve any contact in it
		 * I don't need this method. I assume that if there is a call
		 * there is an agent group able to serve it (its agents could be
		 * free or busy). So, I don't need to clear the queues.
		 */
	}

	protected EndServiceEvent selectAgent(Contact ct) {
		/**
		 * 	Select an agent for each contact that arrives 
		 *  1- return the selected agent to serve the contact if there is no queued contact
		 *  2- return null if there is a queue  
		 */
		EndServiceEvent ese;
		int G=TypeToGroup[ct.getTypeId()].length;
		for (int g=0; g<G; g++ ){
			AgentGroup group = getAgentGroup(TypeToGroup[ct.getTypeId()][g]);
			if (group.getNumFreeAgents()>0){
				switch (Main.DETERMINISTICSERVICETIME){
				case 0:
					ct.setDefaultServiceTime(TAU[ct.getTypeId()][group.getId()]); //deterministic service time
					break;
				case 1:
					ct.setDefaultServiceTime(sgen2[ct.getTypeId()][group.getId()].nextDouble()); //random (exponential) service time
				}						
				ese=group.serve(ct);
				return ese;
			}
		}
		return null; // return null if all agents are busy
	}

	protected DequeueEvent selectWaitingQueue(Contact ct) {
		/**
		 * Queue selection for incoming contacts that find all agents busy (selectAgent returns null): 
		 * Selects a waiting queue and puts the contact "contact" into it
		 * Returns the dequeue event if the contact could be queued, or null otherwise.
		 * 
		 * Constructor DequeueEvent(WaitingQueue queue,Contact contact, double enqueueTime) is rarely used directly;
		 * the recommended way to create dequeue events is to use WaitingQueue.add(Contact).
		 */

		final WaitingQueue queue = getWaitingQueue(ct.getTypeId());
		return queue.add(ct);
	}

	protected DequeueEvent selectContact(AgentGroup group, Agent agent) {
		/**
		 * 	Queue selection for Agents that become free and want to pick a job from a queue:
		 * Steps:
		 *      1-pick a nonempty queue
		 * 		2-pick a job from the previously chosen queue
		 * 
		 * Remember contacts are queued at the contact types. 
		 * We have one waiting queue per each contact type.	
		 */

		DequeueEvent dqe;
		int T=GroupToType[group.getId()].length;
		for (int t=0; t<T; t++ ){
			WaitingQueue queue = getWaitingQueue(GroupToType[group.getId()][t]);
			if (queue.isEmpty()){
				continue;
			}
			else {
				Contact ct=queue.getFirst().getContact();
				switch (Main.DETERMINISTICSERVICETIME){
				case 0:
					ct.setDefaultServiceTime(TAU[ct.getTypeId()][group.getId()]); //deterministic service time
					break;
				case 1:
					ct.setDefaultServiceTime(sgen2[ct.getTypeId()][group.getId()].nextDouble()); // random(exponential) service time
				}
				dqe=queue.removeFirst(DEQUEUETYPE_BEGINSERVICE);
				return dqe;
			}
		}
		return null; // return null if all queues are empty
	}
}
