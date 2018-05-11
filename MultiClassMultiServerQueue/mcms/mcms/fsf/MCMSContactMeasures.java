package mcms.fsf;

import umontreal.iro.lecuyer.contactcenters.contact.Contact;
import umontreal.iro.lecuyer.contactcenters.queue.DequeueEvent;
import umontreal.iro.lecuyer.contactcenters.router.ExitedContactListener;
import umontreal.iro.lecuyer.contactcenters.router.Router;
import umontreal.iro.lecuyer.contactcenters.server.EndServiceEvent;

public class MCMSContactMeasures implements ExitedContactListener {
	/**
	 * Update statistical collectors 
	 **/
	public void blocked(Router router, Contact contact, int bType) {}

	public void dequeued(Router router, DequeueEvent ev){}

	public void served(Router router, EndServiceEvent ev) {
		final Contact contact = ev.getContact();
		final int period = Main.PCE.getPeriod(contact.getArrivalTime())-1;  		
		if (Main.PCE.isMainPeriod(Main.PCE.getCurrentPeriod())) {
			Main.waitTime += contact.getTotalQueueTime(); 
			++Main.numServed;
			if (contact.getTotalQueueTime() <= Main.AWT) {
				++Main.numGoodSL;
				++Main.numGoodSLKP[contact.getTypeId()][period];
			} 
		}
	}
}