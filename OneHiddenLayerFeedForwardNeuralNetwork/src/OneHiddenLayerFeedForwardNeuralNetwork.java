import java.util.ArrayList;
import java.util.Random;

public class OneHiddenLayerFeedForwardNeuralNetwork {
	protected static Random rand = new Random();
	private ArrayList<InputNode> inputNodes;
	private ArrayList<HiddenNode> hiddenNodes;
	private ArrayList<OutputNode> outputNodes;
	private ArrayList<Edge> hiddenOutEdges;
	private ArrayList<Edge> inputHiddenEdges;
	private InputNode hiddenBias;
	private InputNode outputBias;
	
	
	private static double getEdgeWeight() {
		double toReturn = 5*rand.nextDouble()+1;
		if(rand.nextBoolean()) {
			toReturn*= -1;
		}
		return toReturn;
	}
	
	private static void setEdgesHiddenNodeToOutputLayer(ArrayList<Edge> edgeList, HiddenNode hiddenNode, ArrayList<OutputNode> outputLayer) {
		for(OutputNode outputNode: outputLayer) {
			Edge edge = new Edge(hiddenNode, outputNode, getEdgeWeight());
			hiddenNode.getTrailingEdges().add(edge);
			outputNode.getLeadingEdges().add(edge);
			edgeList.add(edge);
		}
	}
	
	private static void setEdgesInputNodeToOutputLayer(ArrayList<Edge> edgeList, InputNode inputNode, ArrayList<OutputNode> outputLayer) {
		for(OutputNode outputNode: outputLayer) {
			Edge edge = new Edge(inputNode, outputNode, getEdgeWeight());
			outputNode.getLeadingEdges().add(edge);
			edgeList.add(edge);
		}
	}
	
	private static void setEdgesInputNodeToHiddenLayer(ArrayList<Edge> edgeList, InputNode inputNode, ArrayList<HiddenNode> hiddenLayer) {
		for(HiddenNode hiddenNode: hiddenLayer) {
			Edge edge = new Edge(inputNode, hiddenNode, getEdgeWeight());
			hiddenNode.getLeadingEdges().add(edge);
			edgeList.add(edge);
		}
	}
	
	// sets edges between the hidden and output layers, putting them into edgeList as they are made
	private static void setEdgesHiddenLayerToOutputLayer(ArrayList<Edge> edgeList, ArrayList<HiddenNode> layer1, ArrayList<OutputNode> layer2) {
		for(HiddenNode hiddenNode: layer1) {
			setEdgesHiddenNodeToOutputLayer(edgeList, hiddenNode, layer2);
		}
	}
	
	// sets edges between the hidden and output layers, putting them into edgeList as they are made
	private static void setEdgesInputLayerToHiddenLayer(ArrayList<Edge> edgeList, ArrayList<InputNode> layer1, ArrayList<HiddenNode> layer2) {
		for(InputNode inputNode: layer1) {
			setEdgesInputNodeToHiddenLayer(edgeList, inputNode, layer2);
		}
	}
		
	public OneHiddenLayerFeedForwardNeuralNetwork(int numInputs, int numHiddenNodes, int numOutputs, ActivationFunction hiddenActivationFunction, ActivationFunction outputActivationFunction, ErrorFunction errorFunction) {
		this.inputNodes = new ArrayList<InputNode>(numInputs);
		this.hiddenNodes = new ArrayList<HiddenNode>(numHiddenNodes);
		this.outputNodes = new ArrayList<OutputNode>(numOutputs);
		this.hiddenOutEdges = new ArrayList<Edge>((numHiddenNodes+1)*numOutputs);
		this.inputHiddenEdges = new ArrayList<Edge>((numInputs+1)*numHiddenNodes);
		//instantiate nodes
		for(int i = 0; i < numInputs; i += 1) {
			this.inputNodes.add(new InputNode());
		}
		this.hiddenBias = new InputNode();
		for(int i = 0; i < numHiddenNodes; i += 1) {
			this.hiddenNodes.add(new HiddenNode(hiddenActivationFunction));
		}
		this.outputBias = new InputNode();
		for(int i = 0; i < numOutputs; i += 1) {
			this.outputNodes.add(new OutputNode(errorFunction, outputActivationFunction));
		}
		this.hiddenBias.setOut(1);
		this.outputBias.setOut(1);
		
		//set all edges
		setEdgesInputLayerToHiddenLayer(this.inputHiddenEdges, this.inputNodes, this.hiddenNodes);
		setEdgesInputNodeToHiddenLayer(this.inputHiddenEdges, this.hiddenBias, this.hiddenNodes);
		setEdgesHiddenLayerToOutputLayer(this.hiddenOutEdges, this.hiddenNodes, this.outputNodes);
		setEdgesInputNodeToOutputLayer(this.hiddenOutEdges, this.outputBias, this.outputNodes);
	}
	
	
	private void feedForwardNoReturn(double[] inputs) {
		assert inputs.length == this.inputNodes.size();
		
		//set the inputs, then feed forward
		for(int i = 0; i < this.inputNodes.size(); i += 1) {
			this.inputNodes.get(i).setOut(inputs[i]);
		}
		for(int i = 0; i < this.hiddenNodes.size(); i += 1) {
			this.hiddenNodes.get(i).feedForward();
		}
		for(int i = 0; i < this.outputNodes.size(); i += 1) {
			this.outputNodes.get(i).feedForward();
		}
	}
	
	//updates the weights based on current node values
	private void backPropogate(double[] expectedOutputs, double learningRate, double momentum) {
		assert expectedOutputs.length == this.outputNodes.size();
		
		//set all node deltas
		for(int i = 0; i < this.outputNodes.size(); i += 1) {
			this.outputNodes.get(i).setTarget(expectedOutputs[i]);
			this.outputNodes.get(i).setNodeDelta();
			//System.out.println("node "+this.outputNodes.get(i).id+" has node delta "+this.outputNodes.get(i).getNodeDelta());
		}
		for(int i = 0; i < this.hiddenNodes.size(); i += 1) {
			this.hiddenNodes.get(i).setNodeDelta();
			//System.out.println("node "+this.hiddenNodes.get(i).id+" has node delta "+this.hiddenNodes.get(i).getNodeDelta());
		}
		
		//update weights
		for(Edge edge: this.hiddenOutEdges) {
			edge.updateWeight(learningRate, momentum);
		}
		for(Edge edge: this.inputHiddenEdges) {
			edge.updateWeight(learningRate, momentum);
		}
	}
	
	//returns the error
	public void train(double[] inputs, double[] expectedOutputs, double learningRate, double momentum) {
		feedForwardNoReturn(inputs);
		backPropogate(expectedOutputs, learningRate, momentum);
	}
	
	public double[] feedForward(double[] inputs) {
		feedForwardNoReturn(inputs);
		double[] output = new double[this.outputNodes.size()];
		for(int i = 0; i < this.outputNodes.size(); i += 1) {
			output[i] = this.outputNodes.get(i).getOut();
		}
		return output;
	}
	
	// intended for testing
	public void setOutputTargets(double[] expectedOutputs) {
		assert expectedOutputs.length == this.outputNodes.size();
		
		for(int i = 0; i < this.outputNodes.size(); i += 1) {
			this.outputNodes.get(i).setTarget(expectedOutputs[i]);
		}
	}
	
	//returns error based on last train/feedForward run
	public double getError() {
		double error = 0;
		for(OutputNode node: this.outputNodes) {
			error += node.getError();
		}
		return error;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		for(Edge edge: this.inputHiddenEdges) {
			str.append(edge.toString());
			str.append("\n");
		}
		for(Edge edge: this.hiddenOutEdges) {
			str.append(edge.toString());
			str.append("\n");
		}
		return str.toString();
	}
	
}
