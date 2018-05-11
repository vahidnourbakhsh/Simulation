package mcms.fsf;

import umontreal.iro.lecuyer.contactcenters.contact.Contact;
import umontreal.iro.lecuyer.contactcenters.contact.ContactFactory;

class MCMSContactFactory implements ContactFactory {
		int type; // type is an identifier, here it is the call type indexed by k
		MCMSContactFactory(int type) {this.type = type;}
		public Contact newInstance() { // newInstance() is a ContactFactory interface method that returns Contact class objects
			Main.numArrivedType[type]++; //counter, number of generated contacts of type [type]
			final MCMSContact contact = new MCMSContact(type);
			//contact.setDefaultServiceTime (sgen.nextDouble()); // For service time that does not depend on the assignment K-I (Comment this for StaticRouter)
			// contact.setDefaultPatienceTime (pgen.nextDouble());
			return contact;
		}
	}
