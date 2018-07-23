package mcms.input;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

/**
 * <h1> Read Simulation Input Data - Service Time </h1>
 *  <p>
 *  Read simulation input data: service times.
 *  Then, format the input data and prepare it for the simulation step.
 * 	</p>
 *  
 * @author	Vahid Nourbakhsh 
 * @version	2.0
 * @since	2018-03-15
 */
public class InputSrvTime {
    private int contact;
    private int group;
    private double tau;

    /**
     * Construct.
     * 
     * 
     * @param contact	The contact type
     * @param group		The agent group
     * @param tau		The servie time. The service time depends on the contact type and the agent group.
     */
    public InputSrvTime(int contact, int group, double tau) {
        this.contact = contact;
        this.group = group;
        this.tau = tau;
    }
    
    //getters and setters
    public int getContact() {
		return contact;
	}

	public void setContact(int contact) {
		this.contact = contact;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public double getTau() {
		return tau;
	}

	public void setTau(double tau) {
		this.tau = tau;
	}
	
	/**
	 * A comparator for sorting groups for each contact.
	 * In the FSF policy, each contact is routed to the fastest agent available.
	 */
    public static Comparator<InputSrvTime> groupTauComparator = new Comparator<InputSrvTime>() {
    	public int compare(InputSrvTime row1, InputSrvTime row2) {
    		// ascending order of groups and tau's
    		return Comparator.comparing(InputSrvTime::getGroup)
   	              .thenComparing(InputSrvTime::getTau)
   	              .compare(row1, row2);    		
        }
	 };

	/**
	 * A comparator for sorting contacts for each group.
	 * In the FSF policy, each groups selects the fastest contact that she can serve.
	 */
    public static Comparator<InputSrvTime> contactTauComparator = new Comparator<InputSrvTime>() {
    	public int compare(InputSrvTime row1, InputSrvTime row2) {
    		// ascending order of contacts and tau's
    		return Comparator.comparing(InputSrvTime::getContact)
   	              .thenComparing(InputSrvTime::getTau)
   	              .compare(row1, row2);
        }
	 };
	 
    @Override
    public String toString() {
        return "[ contact=" + contact + ", group=" + group + ", tau=" + tau + "]";
    };
    
    
    
	/**
	 * 	Reads the service times from input files:
	 * 		The service time depends on the contact type and the agent that the contact is routed to.
	 * 		The service times are exponentially distributed with the given mean.
	 * 
	 *  @param file_name				Name of the File which contains the number of agents in each groups.
	 *  @param file_path				Path to the file location.
	 *  @param num_contact_types 		Total number of contact types. 
	 *  @param num_agent_groups 		Total number of groups. In each group there could be multiple agents.
	 *  @return serviceTimeVec			A nested array list, where each inner element shows the mean service time if a contact of type "i" is routed to an agent in group "j".
	 */
	public static ArrayList<InputSrvTime> readServiceTimes (String file_name, String file_path, int num_contact_types, int num_agent_groups) {
		ArrayList<InputSrvTime> serviceTimeVec = new ArrayList<InputSrvTime>();
		Scanner scanner;
		String line;
		Scanner lineScanner;
		try {
			scanner = new Scanner(new File(file_path+file_name));
			InputSrvTime e;
			while(scanner.hasNextLine()) {
			    line = scanner.nextLine();
			    if(!line.startsWith("#")) {
			    	lineScanner = new Scanner(line);
					e = new InputSrvTime(lineScanner.nextInt(), lineScanner.nextInt(), lineScanner.nextDouble());
					serviceTimeVec.add(e);
			    }
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return serviceTimeVec;
	}

	/**
	 * Convert a ArrayList<List<Number>> list of service times (tau's) to a primitive [][]double 
	 * 
	 *  @param input 				A nested array list, where each element is the mean service time if contact "i" is routed to an agent in group "j". 
	 *  @param num_contact_types	Number of contact types.
	 *  @param num_agent_groups 	Number of agent groups.
	 *  @return output	 			An array where each element indicates the number of agents in the corresponding group. 
	 */
	public static double[][] arrayListToPrimitiveArray(ArrayList<InputSrvTime> input, int num_contact_types, int num_agent_groups) {
		double[][] output = new double[num_contact_types][num_agent_groups];
		for(int j = 0; j < output.length; j++)
		{
			Arrays.fill(output[j], 0);
		}
		
		int row = 0;
		while (row < input.size()) {
			output[input.get(row).getContact()][input.get(row).getGroup()] = input.get(row).getTau();
			row++;
		}
		return output;
	}

	/**
	 * Builds Group-to-Type priority list for routing when an agent becomes free  
	 * 
	 * @param serviceTimeVec 	service time vector array
	 * @param numGroups			number of groups
	 * @return
	 */
	public static int[][] buildGroupToType(ArrayList<InputSrvTime> serviceTimeVec, int numGroups){		
		// Nested array list for priorities. Each inner array list is a group's priority list of contacts.
		// That is group 0's inner list is a list of contacts in order of their service times (from smallest to largest tau's)
		ArrayList<ArrayList<Integer>> priorityArray = new ArrayList<ArrayList<Integer>>();
		
		// An array list of contacts for the current group.
		ArrayList<Integer> listContacts = new ArrayList<Integer>();
		
		// Return a 2-d int array of priorities.
		// The first dimension is a group and the second dimension lists the contacts for that specific group: [group][contacts]
		int[][] priority = new int[numGroups][];	

		// Counters on the input tau vector. It will traverse the tau vector line-by-line 
		// Each row has three elements in order: (i) contact, (ii) group, and (iii) service time, tau
		int next = 0; 
		int nextContact;
		int nextGroup;
		
		// Find the next contact in the tau vector and add it to the list of contacts for the first group
		nextContact = serviceTimeVec.get(next).getContact();
		listContacts.add(new Integer (Integer.valueOf(nextContact)));
		
		// Increase the row counter for the tau vector
		next ++;
		
		// If the tau vector has only one row, return the priority list.
		if (serviceTimeVec.size() == 1) {
			priorityArray.add(new ArrayList<Integer>(listContacts));
			// Build int[][]priority from ArrayList<ArrayList<Integer>> priorityArray
			int i = 0;
			for (ArrayList<Integer> innerArray : priorityArray) {
				priority[i++] = innerArray.stream().mapToInt(j -> j).toArray();
			}
			return priority;
		}


		/* 
		 * When the tau vector has more than one row, proceed
		 * Traverse the tau vector row-by-row and 
		 * when the group changes, add the list of contacts (for this group) 
		 * to the array of contacts, and proceed to the next group.
		 */
		int prevGroup;
		while (next < serviceTimeVec.size()){
			prevGroup = serviceTimeVec.get(next-1).getGroup();
			nextContact = serviceTimeVec.get(next).getContact();
			nextGroup = serviceTimeVec.get(next).getGroup();
			
			// Check if the group has changed.
			if (nextGroup == prevGroup) 
			{
				// Add the new contact to the list of contacts. 
				listContacts.add(new Integer (Integer.valueOf(nextContact)));
			} else {
				/*
				 * Add the current list of contacts to the priority array, 
				 * and start a cleared contacts list with the new contact in it.
				 */
				priorityArray.add(new ArrayList<Integer>(listContacts));
				listContacts.clear();
				listContacts.add(Integer.valueOf(nextContact));
			}
			next ++;
		}
		
		// Finally, add the list of contacts for the last group.
		priorityArray.add(new ArrayList<Integer>(listContacts));
		
		// Build int[][] priority from ArrayList<ArrayList<Integer>> priorityArray
		int i = 0;
		for (ArrayList<Integer> innerArray : priorityArray) {
			priority[i++] = innerArray.stream().mapToInt(j -> j).toArray();
		}
		return priority;
	}
	
	/**
	 * Construct TypeToGroup 2-d array by index-sorting service time matrix tau
	 * Faster Groups have higher priorities.
	 * NOTE: TypeToGroup is NOT equal to GroupToType's transpose
	 *
	 * @param serviceTimeVec		service time vector
	 * @param numContactTypes		number of contact types
	 * @return
	 */
	public static int[][] buildTypeToGroup(ArrayList<InputSrvTime> serviceTimeVec, int numContactTypes){
		// Nested array list for priorities. Each inner array list is a contact's priority list of agent groups.
		// Contact type 0's inner list is a list of groups in order of their service times (from smallest to largest tau's)
		ArrayList<ArrayList<Integer>> priorityArray = new ArrayList<ArrayList<Integer>>();
		
		// An array list of groups for the current contact.
		ArrayList<Integer> listGroup = new ArrayList<Integer>();
		
		// Return a 2-d int array of priorities.
		// The first dimension is a contact and the second dimension lists the groups for that specific contact: [contact][groups]
		int[][] priority = new int[numContactTypes][];	

		// Counters on the input tau vector. It will traverse the tau vector line-by-line 
		// Each row has three elements in order: (i) contact, (ii) group, and (iii) service time, tau
		int next = 0; 
		int nextGroup;
		int nextContact;
		
		// Find the next group in the tau vector and add it to the list of groups for the first contact
		nextGroup = serviceTimeVec.get(next).getGroup();
		listGroup.add(new Integer (Integer.valueOf(nextGroup)));
		
		// Increase the row counter for the tau vector
		next ++;
		
		// If the tau vector has only one row, return the priority list.
		if (serviceTimeVec.size() == 1) {
			priorityArray.add(new ArrayList<Integer>(listGroup));
			// Build int[][]priority from ArrayList<ArrayList<Integer>> priorityArray
			int i = 0;
			for (ArrayList<Integer> innerArray : priorityArray) {
				priority[i++] = innerArray.stream().mapToInt(j -> j).toArray();
			}
			return priority;
		}


		/* 
		 * When the tau vector has more than one row, proceed.
		 * Traverse the tau vector row-by-row and 
		 * when the contact changes, add the list of groups (for this contact) 
		 * to the outer array of contacts, and proceed to the next contact.
		 */
		int prevContact;
		while (next < serviceTimeVec.size()){
			prevContact = serviceTimeVec.get(next-1).getContact();
			nextGroup = serviceTimeVec.get(next).getGroup();
			nextContact = serviceTimeVec.get(next).getContact();
			
			// Check if the contact has changed.
			if (nextContact == prevContact) 
			{
				// Add the new group to the list of groups. 
				listGroup.add(new Integer (Integer.valueOf(nextGroup)));
			} else {
				/*
				 * Add the current list of groups to the outer array of contacts, 
				 * and start a cleared groups list with the new group in it.
				 */
				priorityArray.add(new ArrayList<Integer>(listGroup));
				listGroup.clear();
				listGroup.add(Integer.valueOf(nextGroup));
			}
			next ++;
		}
		
		// Finally, add the list of groups for the last contact.
		priorityArray.add(new ArrayList<Integer>(listGroup));
		
		// Build int[][] priority from ArrayList<ArrayList<Integer>> priorityArray
		int i = 0;
		for (ArrayList<Integer> innerArray : priorityArray) {
			priority[i++] = innerArray.stream().mapToInt(j -> j).toArray();
		}
		return priority;
	}
	
	public static void main(String args[]){
		   ArrayList<InputSrvTime> arraylist = new ArrayList<InputSrvTime>();
		   arraylist.add(new InputSrvTime(1, 1, 0.9));
		   arraylist.add(new InputSrvTime(2, 1, 0.1));
		   arraylist.add(new InputSrvTime(1, 2, 0.4));
		   arraylist.add(new InputSrvTime(1, 3, 0.6));

		   //Sorting rows based on groups and tau's
		   System.out.println("Sorting rows by Groups and then tau's: ");
		   Collections.sort(arraylist, InputSrvTime.groupTauComparator);

		   for(InputSrvTime row: arraylist){
				System.out.println(row);
		   }
		   int[][] priority = buildGroupToType(arraylist, 3);
		   System.out.println("\n AgentGroup-To-ContactType priority list: " + Arrays.deepToString(priority));

		   //Sorting rows based on contacts and tau's
		   System.out.println("\n Soritng rows by contact and then tau's: ");
		   Collections.sort(arraylist, InputSrvTime.contactTauComparator);
		   for(InputSrvTime row: arraylist){
				System.out.println(row);
		   }
		   priority = buildTypeToGroup(arraylist, 2);
		   System.out.println("\n ContactType-To-AgentGroup priority list: " + Arrays.deepToString(priority));
		};
}
