package mcms.fsf;

public class ArrayOperations {
	// in an array of doubles picks one's index randomly weighted by the weights
	// array
	public static int probselect(double rand, double[] weights, boolean block) {
		if (block==true){
			// Block some contacts
			double[] cprob = new double[weights.length];
			double sum = 0;
			for (int k = 0; k < weights.length; k++) {
				sum += weights[k];
				cprob[k] = sum;
			}
			// double myrand=rand; // Math.random(); OR new MRG32k3a().nextDouble();
			int selected = -1; //The default -1 means the contact is blocked
			for (int k = 0; k < weights.length; k++) {
				if (cprob[k] > rand) {
					selected = k;
					break;
				}
			}
			return selected;
		}
		else {
			// Don not block contacts. Normalize the weights
			double sumWeights = arraysum(weights);
			double[] prob = new double[weights.length]; // Normalized weights
			for (int k = 0; k < weights.length; k++) {
				prob[k] = weights[k] / sumWeights;
			}
			double[] cprob = new double[weights.length];
			double sum = 0;
			for (int k = 0; k < weights.length; k++) {
				sum += prob[k];
				cprob[k] = sum;
			}
			// double myrand=rand; // Math.random(); OR new MRG32k3a().nextDouble();
			int selected = 0; 
			for (int k = 0; k < weights.length; k++) {
				if (cprob[k] > rand) {
					selected = k;
					break;
				}
			}
			return selected;
		}
	}

	// in a row of a 2D-array of doubles picks one's index randomly weighted by
	// the weights array
	public static int probselect(double rand, double[][] twoDweights, int row, boolean block) {
		double[] weights = twoDweights[row];
		return probselect(rand, weights, block);
	}

	public static double arraysum(double[] array) // Returns a column array
	{
		int m = array.length; // number of elements
		double sum = 0;
		for (int x = 0; x < m; x++)
			sum = sum + array[x];
		return sum;
	}
	
	public static int arraysum(int[] array) // Returns a column array
	{
		int m = array.length; // number of elements
		int sum = 0;
		for (int x = 0; x < m; x++)
			sum = sum + array[x];
		return sum;
	}

	public static double arrayaverage(int[] array) // Returns a column array
	{
		int m = array.length; // number of elements
		double avg = 0;
		avg = arraysum(array)/m;
		return avg;
	}
	
	public static double arrayaverage(double[] array) // Returns a double
	{
		int m = array.length; // number of elements
		double avg = 0;
		avg = arraysum(array)/m;
		return avg;
	}
	public static double[] columnsum(double[][] array) // Returns a row array
	{
		int m = array.length; // number of rows
		int n = array[0].length; // number of columns
		double[] rowarray = new double[m];

		for (int x = 0; x < m; x++) {
			double sum;
			sum = 0;
			for (int y = 0; y < n; y++)
				sum = sum + array[x][y];
			rowarray[x] = sum;
		}
		return rowarray;
	}

	public static double[] rowsum(double[][] array) // Returns a column array
	{
		int m = array.length; // number of rows
		int n = array[0].length; // number of columns
		double[] colarray = new double[n];

		for (int y = 0; y < n; y++) {
			double sum;
			sum = 0;
			for (int x = 0; x < m; x++)
				sum = sum + array[x][y];
			colarray[y] = sum;
		}
		return colarray;
	}
	
	public static int[][] arrayTranspose (int[][] array) {
		int[][] transpose=new int[array[0].length][array.length];
		for (int i=0; i<array[0].length; i++){
			for (int j=0; j<array.length; j++){
				transpose[i][j]=array[j][i];
			}
		}
		return transpose;
	}
	
	public static double[][] arrayTranspose (double[][] array) {
		double[][] transpose=new double[array[0].length][array.length];
		for (int i=0; i<array[0].length; i++){
			for (int j=0; j<array.length; j++){
				transpose[i][j]=array[j][i];
			}
		}
		return transpose;
	}
}
