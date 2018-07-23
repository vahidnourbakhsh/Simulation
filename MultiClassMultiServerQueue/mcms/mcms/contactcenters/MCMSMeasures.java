package mcms.contactcenters;

import mcms.main.SimRunner;
import umontreal.iro.lecuyer.contactcenters.contact.Contact;
import umontreal.iro.lecuyer.contactcenters.queue.DequeueEvent;
import umontreal.iro.lecuyer.contactcenters.router.ExitedContactListener;
import umontreal.iro.lecuyer.contactcenters.router.Router;
import umontreal.iro.lecuyer.contactcenters.server.EndServiceEvent;

/**
 * <h1> Update Statistical Collectors </h1>
 *  <p>
 *  Updates the statistical counters after an event: 
 *  Differnt events are: New Contact Arrived, Contact Dequeued, End of Service at the Agent 
 * 	</p>
 *  
 * @author	Vahid Nourbakhsh 
 * @version	2.0
 * @since	2018-03-18
 */
public class MCMSMeasures implements ExitedContactListener {
	private double acceptableWaitTime;
	public MCMSMeasures (double acceptableWaitTime) {
		this.acceptableWaitTime = acceptableWaitTime;
	}
	
	/**
	 * Our system does not block any job.
	 */
	public void blocked(Router router, Contact contact, int bType) {}
	
	/**
	 * The counter are updated in the {@link #served(Router, EndServiceEvent)}.
	 */
	public void dequeued(Router router, DequeueEvent ev){}

	/**
	 * Update counters, when an agent finishes serving a contact.
	 */
	public void served(Router router, EndServiceEvent ev) {
		final Contact contact = ev.getContact();
		final int period = SimRunner.PCE.getPeriod(contact.getArrivalTime())-1;  // Determine the current simulation period
		if (SimRunner.PCE.isMainPeriod(SimRunner.PCE.getCurrentPeriod())) { // If the current period is not a warm-up or wrap-up period, update the statistics.
			SimRunner.waitTime += contact.getTotalQueueTime();  // Total wait time (across all calls)
			++SimRunner.numServed; // Total number of served contacts
			if (contact.getTotalQueueTime() <= acceptableWaitTime) { // If the contact is served within the Acceptable Waiting Time (AWT), update counters for contacts that received good service. 
				++SimRunner.numGoodSL; // Total number of contacts that received good service (served within the AWT)
				++SimRunner.numGoodSLKP[contact.getTypeId()][period]; // Number of contacts of a certain type in a certain period that received good service.
			} 
		}
	}
}