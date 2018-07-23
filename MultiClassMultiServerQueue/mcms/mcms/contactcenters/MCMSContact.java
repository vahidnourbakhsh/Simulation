package mcms.contactcenters;

import umontreal.iro.lecuyer.contactcenters.contact.Contact;

/**
 * <h1> Contact Class </h1>
 *  <p>
 *  Extends the Contact class from the Contact Centers package.
 *  Check the documentation for:
 *  	umontreal.iro.lecuyer.contactcenters.contact.Contact
 * 	</p>
 *  
 * @author	Vahid Nourbakhsh 
 * @version	2.0
 * @since	2018-03-18
 */

public class MCMSContact extends Contact {
		MCMSContact(int type) {
			super(type); // type is the contact type. Each contact type has its specific arrival rate and service time. The service time depends on the contact type and the agent group to which it is routed.
		}
	}