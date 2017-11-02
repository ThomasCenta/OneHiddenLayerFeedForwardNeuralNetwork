import java.util.Random;

public class Main {
	
	static Random rand = new Random();
	

	//make sure to set input/output size of network as well
	public static double[][] getParams() {
		double[] inputs = {rand.nextDouble()*10 - 50, rand.nextDouble()*10-50};
		double[] outputs = {inputs[0]+inputs[1], inputs[0]-inputs[1]};
		double[][] toReturn = {inputs, outputs};
		return toReturn;
	}
	
	public static void train(OneHiddenLayerFeedForwardNeuralNetwork network, double learningRate, double momentum) {
		double[][] params = getParams();
		
		network.train(params[0], params[1], learningRate, momentum);
	}
	
	public static double test(OneHiddenLayerFeedForwardNeuralNetwork network, int numTests) {
		double sum = 0;
		for(int i = 0; i < numTests; i += 1) {
			double[][] params = getParams();
			network.feedForward(params[0]);
			network.setOutputTargets(params[1]);
			sum += network.getError();
		}
		return sum/numTests;
	}

	
	public static void main(String[] args) {
		int numTraining = 100000000;
		int numTests = 10000;
		
		OneHiddenLayerFeedForwardNeuralNetwork network = new OneHiddenLayerFeedForwardNeuralNetwork(2,2,2, new SigmoidActivation(0,1), new LinearActivation(), new SquaredError());
		System.out.println("initial error: "+test(network, numTests));
		for(int t_i = 0; t_i < numTraining; t_i += 1) {
			double learningRate = 1.0/(Math.max(100000, 0.01*t_i));
			double momentum = 0.5;
			train(network, learningRate, momentum);
//			if(t_i < 100) {
//				System.out.println(network.getError());
//			}
		}
		
		System.out.println("after training error: "+test(network, numTests));
	}
}
