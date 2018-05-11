package mcms.fsf;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ReadData {
	public static int[] readNumAgents (String Name, String Path, int J) {
		// Number of Agents
		Scanner scanner;
		int [] NUMAGENTS = new int [J];
		int i = 0;
		try {
			scanner = new Scanner(new File(Path+Name));
			scanner.nextLine(); scanner.nextLine(); 
			while(scanner.hasNextInt()){
				scanner.nextInt();
				NUMAGENTS[i++] = scanner.nextInt();
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//System.out.println(Arrays.toString(NUMAGENTS));
		return NUMAGENTS;
	}


	public static double[] readDemand (String Name, String Path, int J) {
		// Demands
		Scanner scanner;
		double [] DEMAND = new double [J];
		int i = 0;
		try {
			scanner = new Scanner(new File(Path+Name));
			scanner.nextLine(); scanner.nextLine();
			while(scanner.hasNextDouble()){
				scanner.nextInt();
				DEMAND[i++] = scanner.nextDouble();
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//System.out.println(Arrays.toString(DEMAND));
		return DEMAND;
	}

	public static double[][] readTau (String Name, String Path, int I, int J) {
		// Service time, tau
		double [][]  tau= new double [I][J];
		Scanner scanner;
		int i = 0;
		int j=0;
		try {
			scanner = new Scanner(new File(Path+Name));
			scanner.nextLine(); scanner.nextLine(); scanner.nextLine();scanner.nextLine();

			while(i<I-1 || j<J){
				scanner.nextInt(); 	scanner.nextInt();
				if (j<J){
					tau[i][j++] = scanner.nextDouble();
				}
				else if (j==J && i<I-1){
					j=0; i++;
					tau[i][j++] = scanner.nextDouble();

				}
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//System.out.println(Arrays.deepToString(tau));
		return tau;
	}
	
	public static ArrayList<List<Number>> readTauadv (String Name, String Path, int I, int J) {
		// Service time, tau
		int i;
		int j;
		ArrayList<List<Number>> tau = new ArrayList<List<Number>>();
		Scanner scanner;
		try {
			scanner = new Scanner(new File(Path+Name));
			scanner.nextLine(); scanner.nextLine(); scanner.nextLine();scanner.nextLine();
			// String endoffile = ";";
			while(scanner.hasNext()){
				i=scanner.nextInt();
				j=scanner.nextInt();
				tau.add(Arrays.asList(i, j, scanner.nextDouble()));
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//System.out.println(Arrays.deepToString(tau));
		return tau;
	}
	
	public static double[][] arrayListToPrimitiveArray(ArrayList<List<Number>> vector, int K, int I) {
		//Build TAU/WEIGHT from TAU_Vec/WEIGHT_Vec
		double[][] myarray = new double[K][I];
		for(int j = 0; j < myarray.length; j++)
		{
			Arrays.fill(myarray[j], 0);
		}
		
		int row = 0;
		while (row < vector.size()) {
			myarray[(int) vector.get(row).get(0)][(int) vector.get(row).get(1)]=(double) vector.get(row).get(2);
			row++;
		}
		return myarray;
	}

	
	public static double[][] readWeight (String Name, String Path, int I, int J) {
		// Weights (x_ij from the SQE solution)
		double [][]  weights= new double [I][J];
		Scanner scanner;
		int i = 0;
		int j=0;
		try {
			scanner = new Scanner(new File(Path+Name));
			scanner.nextLine(); scanner.nextLine(); scanner.nextLine();scanner.nextLine();
			scanner.nextLine(); scanner.nextLine(); scanner.nextLine();scanner.nextLine();
			scanner.nextLine(); scanner.nextLine(); scanner.nextLine();scanner.nextLine();
			scanner.nextLine();

			while(i<I-1 || j<J){
				scanner.nextInt(); 	scanner.nextInt();
				if (j<J){
					weights[i][j++] = scanner.nextDouble();
				}
				else if (j==J && i<I-1){
					j=0; i++;
					weights[i][j++] = scanner.nextDouble();

				}
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//System.out.println(Arrays.deepToString(weights));
		return weights;
	}
	
	public static ArrayList<List<Comparable>> readWeightadvMatrixForm (String Name, String Path, int I, int J) {
		// Weights (x_ij from the SQE solution)
		ArrayList<List<Comparable>> weights = new ArrayList<List<Comparable>>();
		Scanner scanner;
		
		try {
			scanner = new Scanner(new File(Path+Name));
			scanner.nextLine(); scanner.nextLine(); scanner.nextLine();scanner.nextLine();
			scanner.nextLine(); scanner.nextLine(); scanner.nextLine();scanner.nextLine();
			scanner.nextLine(); scanner.nextLine(); scanner.nextLine();scanner.nextLine();
//			scanner.nextLine(); scanner.nextLine();

			for (int i = 0; i<I; i++){
				scanner.nextInt();
				for (int j=0; j<J; j++){
					weights.add(Arrays.asList(i, j, scanner.nextDouble()));
				}
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//System.out.println(Arrays.deepToString(weights));
		return weights;
	}
	
	public static ArrayList<List<Number>> readWeightadvVectorForm (String Name, String Path, int I, int J) {
		// Weights (x_ij from the SQE solution)
		int i = 0;
		int j=0;
		ArrayList<List<Number>> weights = new ArrayList<List<Number>>();
		Scanner scanner;
		try {
			scanner = new Scanner(new File(Path+Name));
			scanner.nextLine(); scanner.nextLine();scanner.nextLine();
			scanner.nextLine(); scanner.nextLine(); scanner.nextLine();
			scanner.nextLine();scanner.nextLine(); scanner.nextLine(); 
			scanner.nextLine();scanner.nextLine();
			// String endoffile = ";";
			while(scanner.hasNext()){
				i=scanner.nextInt();
				j=scanner.nextInt();
				weights.add(Arrays.asList(i, j, scanner.nextDouble()));
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return weights;
	}
	
	public static double[][] addColToDemArray(double[] array, int p){
		final int n=p+2;
		double[][] marray = new double [array.length][n];
		for (int r=0; r<array.length; r++){
			for (int c=1; c<n-1; c++){
				marray[r][c]=array[r];
			}
		}
		return marray;
	}
	
	public static int[][] addColToNumArray(int[] array, int p){
		final int n=p+2;
		int[][] marray = new int [array.length][n];
		for (int r=0; r<array.length; r++){
			for (int c=1; c<n; c++){
				marray[r][c]=array[r];
			}
		}
		return marray;
	}
}
