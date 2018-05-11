package mcms.fsf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BuildLists {
	public static void main(String[] args) {
		double[][] tau = {{0.9, 0.4, 0.1, 0.4}, {0.7, 0.1, 0.2, 0.3}, {0.4, 0.5, 0.3, 0.2}, {0.5, 0.2, 0.4, 0.1}};
		int[][] TypeToGroup=serviceTimeToPriority(tau);
		System.out.println("TypeToGroup");
		System.out.println(Arrays.deepToString(TypeToGroup));
		int[][] GroupToType=serviceTimeToPriority(ArrayOperations.arrayTranspose(tau));
		System.out.println("GroupToType");
		System.out.println(Arrays.deepToString(GroupToType));
	}
	
	public static int[][] serviceTimeToPriority(double[][] tau){
		/**
		 * Construct TypeToGroup array by index-sorting service time matrix tau
		 * NOTE: This method works IFF all groups can serve all contacts, i.e., 
		 * all (call,group) combinations are defined in the matrix tau.
		 * If the matrix is not full use "serviceTimeToPriorityGroupToType" 
		 * and "serviceTimeToPriorityTypeToGroup"
		 * Faster Groups have higher priorities.
		 * Similarly, construct GroupToType array. Faster contacts have higher priorities.
		 * NOTE: TypeToGroup is NOT the GroupToType transpose
		 */
		int K=tau.length;
		int I=tau[0].length;
		int[][] priority=new int[K][I];
		
		for (int k=0; k<K; k++){
			HashMap<Integer, Double> hmap = new HashMap<Integer, Double>();
			for (int i=0; i<I; i++){hmap.put(i, tau[k][i]);}
			Map<Integer, String> smap = sortByValuesDecreasing(hmap); 
			Set set2 = smap.entrySet();
			Iterator iterator2 = set2.iterator();
			int counter=0;
			while(iterator2.hasNext()) {
				Map.Entry me2 = (Map.Entry)iterator2.next();
				priority[k][counter++]=(int) me2.getKey();
			}
		}
		return priority;
	}
	
	public static int[][] buildTypeToGroup(ArrayList<List<Number>> wEIGHTS_Vec){
		/**
		 * Construct TypeToGroup array by index-sorting service time matrix tau
		 * Faster Groups have higher priorities.
		 * NOTE: TypeToGroup is NOT the GroupToType transpose
		 */
		//Find the largest k
		int K=(int) wEIGHTS_Vec.get(wEIGHTS_Vec.size()-1).get(0)+1;
		//Find the largest i
		int m=0;
		int current=0;
		int I=1;
		while (m<=wEIGHTS_Vec.size()-1) {
			current =(int) wEIGHTS_Vec.get(m).get(1)+1;
			if (current > I) { 
				I= current;
			}
			m++;
		}
		int[][] priority=new int[K][];
		for (int k=0; k<K; k++){
			HashMap<Integer, Double> hmap = new HashMap<Integer, Double>();
			for (int row=0; row < wEIGHTS_Vec.size(); row++) {
				if ((int)wEIGHTS_Vec.get(row).get(0)==k && (double)wEIGHTS_Vec.get(row).get(2)>0){
					hmap.put((Integer) wEIGHTS_Vec.get(row).get(1), (Double) wEIGHTS_Vec.get(row).get(2));
					}
			}
			Map<Integer, String> smap = sortByValuesIncreasing(hmap); 
			Set set2 = smap.entrySet();
			Iterator iterator2 = set2.iterator();
			int counter=0;
			priority[k]= new int [set2.size()];
			while(iterator2.hasNext()) {
				Map.Entry me2 = (Map.Entry)iterator2.next();
				priority[k][counter]=(int) me2.getKey();
				counter++;
			}
		}
		return priority;
	}

	public static int[][] buildGroupToType(ArrayList<List<Number>> WEIGHTS_Vec){
		/**
		 * Construct GroupToType array by index-sorting service time matrix tau
		 * Faster Groups have higher priorities.
		 * NOTE: TypeToGroup is NOT equal to GroupToType's transpose
		 */
		int K=(int) WEIGHTS_Vec.get(WEIGHTS_Vec.size()-1).get(0)+1;
		int m=0;
		int current=0;
		int I=1;
		while (m<=WEIGHTS_Vec.size()-1) {
			current =(int) WEIGHTS_Vec.get(m).get(1)+1;
			if (current > I) { 
				I= current;
			}
			m++;
		}
		int[][] priority=new int[I][];
		
		for (int i=0; i<I; i++){
			HashMap<Integer, Double> hmap = new HashMap<Integer, Double>();
			for (int row=0; row < WEIGHTS_Vec.size(); row++) {
				if ((int)WEIGHTS_Vec.get(row).get(1)==i && (double)WEIGHTS_Vec.get(row).get(2)>0){
					hmap.put((Integer) WEIGHTS_Vec.get(row).get(0), (Double) WEIGHTS_Vec.get(row).get(2));
					}
			}
			Map<Integer, String> smap = sortByValuesIncreasing(hmap); 
			Set set2 = smap.entrySet();
			Iterator iterator2 = set2.iterator();
			priority[i] = new int [set2.size()];
			int counter=0;
			while(iterator2.hasNext()) {
				Map.Entry me2 = (Map.Entry)iterator2.next();
				priority[i][counter]=(int) me2.getKey();
				counter++;
			}
		}
		return priority;
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap sortByValuesIncreasing(HashMap map) {
		/**
		 * This method sorts the HashMap according to its entries descending 
		 * and returns a sorted LinkedHashMap
		 */
		List mylist = new LinkedList(map.entrySet());
		// Define Custom Sorter for sorting tau's (hashmap values/entries) increasingly
		Collections.sort(mylist, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue())
						.compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		
		// Copy the sorted list "mylist" in a LinkedHashMap 
		// LinkedHashMap preserves the order (here the insertion order)
		HashMap sortedHashMap = new LinkedHashMap();
		for (Iterator it = mylist.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		} 
		return sortedHashMap;
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap sortByValuesDecreasing(HashMap map) {
		/**
		 * This method sorts the HashMap according to its entries descending 
		 * and returns a sorted LinkedHashMap
		 */
		List mylist = new LinkedList(map.entrySet());
		// Define Custom Sorter for sorting x_ij's (hashmap values/entries) decreasingly
		Collections.sort(mylist, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue())
						.compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		
		// For x_ij entries are sorted decreasingly
		Collections.reverse(mylist);
		
		// Copy the sorted list "mylist" in a LinkedHashMap 
		// LinkedHashMap preserves the order (here the insertion order)
		HashMap sortedHashMap = new LinkedHashMap();
		for (Iterator it = mylist.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		} 
		return sortedHashMap;
	}
}