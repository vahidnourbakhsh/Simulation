package mcms.contactcenters;

import mcms.main.SimRunner;
import umontreal.iro.lecuyer.contactcenters.contact.Contact;
import umontreal.iro.lecuyer.contactcenters.contact.ContactFactory;

/**
 * <h1> Contact Factory </h1>
 *  <p>
 *  This class implements the ContactFactory class from the Contact Centers package.
 *  It generates new arrivals/demands for all contact types.
 * 	</p>
 *  
 * @author	Vahid Nourbakhsh 
 * @version	2.0
 * @since	2018-03-15
 */

public class MCMSContactFactory implements ContactFactory {
	int type; // type is an identifier, here it is the contact type indexed by k
	public MCMSContactFactory(int type) {this.type = type;}
	public Contact newInstance() { // newInstance() is a ContactFactory interface method that returns Contact class objects
		SimRunner.numArrivedType[type]++; // Update the counter: the number of generated contacts of type [type]
		final MCMSContact contact = new MCMSContact(type);
		//contact.setDefaultServiceTime (sgen.nextDouble()); // For systems where the contact service time that does not depend on the agent to which it is routed we can assign the service time here
		// contact.setDefaultPatienceTime (pgen.nextDouble()); // For cases where the contacts abandon the system after the queue waiting time exceeds the contact's patience time 
		return contact;
	}
}
