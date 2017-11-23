package ch.idsia.ai;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: Apr 28, 2009
 * Time: 2:15:10 PM
 */
public class MLP implements FA<double[], double[]>, Evolvable {

        private double[][] firstConnectionLayer;
    private double[][] secondConnectionLayer;
    private double[] hiddenNeurons;
    private double[] outputs;
    private double[] inputs;
    //private double[] targetOutputs;
    public double mutationMagnitude = 0.1;


    public static double mean = 0.0f;        // initialization mean
    public static double deviation = 0.1f;   // initialization deviation

    public static final Random random = new Random();
    public double learningRate = 0.01;

    public MLP(int numberOfInputs, int numberOfHidden, int numberOfOutputs) {

        firstConnectionLayer = new double[numberOfInputs][numberOfHidden];
        secondConnectionLayer = new double[numberOfHidden][numberOfOutputs];
        hiddenNeurons = new double[numberOfHidden];
        outputs = new double[numberOfOutputs];
        //targetOutputs = new double[numberOfOutputs];
        inputs = new double[numberOfInputs];
        initializeLayer(firstConnectionLayer);
        initializeLayer(secondConnectionLayer);
    }

    public MLP(double[][] firstConnectionLayer, double[][] secondConnectionLayer, int numberOfHidden,
               int numberOfOutputs) {
        this.firstConnectionLayer = firstConnectionLayer;
        this.secondConnectionLayer = secondConnectionLayer;
        inputs = new double[firstConnectionLayer.length];
        hiddenNeurons = new double[numberOfHidden];
        outputs = new double[numberOfOutputs];
    }

    protected void initializeLayer(double[][] layer) {
        for (int i = 0; i < layer.length; i++) {
            for (int j = 0; j < layer[i].length; j++) {
                layer[i][j] = (random.nextGaussian() * deviation + mean);
            }
        }
    }

    public MLP getNewInstance() {
        return new MLP(firstConnectionLayer.length, secondConnectionLayer.length, outputs.length);
    }

    public MLP copy() {
        MLP copy = new MLP(copy(firstConnectionLayer), copy(secondConnectionLayer),
                hiddenNeurons.length, outputs.length);
        copy.setMutationMagnitude(mutationMagnitude);
        return copy;
    }

    private double[][] copy(double[][] original) {
        double[][] copy = new double[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
        }
        return copy;
    }

    public void mutate() {
        mutate(firstConnectionLayer);
        mutate(secondConnectionLayer);
    }

    private void mutate(double[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] += random.nextGaussian() * mutationMagnitude;
        }
    }

    private void mutate(double[][] array) {
        for (double[] anArray : array) {
            mutate(anArray);
        }
    }

    public void psoRecombine(MLP last, MLP pBest, MLP gBest) {
        // Those numbers are supposed to be constants. Ask Maurice Clerc.
        final double ki = 0.729844;
        final double phi = 2.05;

        double phi1 = phi * random.nextDouble();
        double phi2 = phi * random.nextDouble();
        //System.out.println("phi1: "+phi1+" phi2: "+phi2);
        //System.out.println(" LAST:" + last);
        //System.out.println(" PBEST:" + pBest);
        //System.out.println(" GBEST:" + gBest);
        //System.out.println(" THIS:" + toString());
        for (int i = 0; i < inputs.length; i++) {
            for (int j = 0; j < hiddenNeurons.length; j++) {
                firstConnectionLayer[i][j] = (double) (firstConnectionLayer[i][j] + ki * (firstConnectionLayer[i][j] - ((double[][]) (last.firstConnectionLayer))[i][j]
                        + phi1 * (((double[][]) (pBest.firstConnectionLayer))[i][j] - firstConnectionLayer[i][j])
                        + phi2 * (((double[][]) (gBest.firstConnectionLayer))[i][j] - firstConnectionLayer[i][j])));
            }
        }

        for (int i = 0; i < hiddenNeurons.length; i++) {
            for (int j = 0; j < outputs.length; j++) {
                secondConnectionLayer[i][j] = (double) (secondConnectionLayer[i][j] + ki * (secondConnectionLayer[i][j] - ((double[][]) (last.secondConnectionLayer))[i][j]
                        + phi1 * (((double[][]) (pBest.secondConnectionLayer))[i][j] - secondConnectionLayer[i][j])
                        + phi2 * (((double[][]) (gBest.secondConnectionLayer))[i][j] - secondConnectionLayer[i][j])));
            }
        }

    }

    private void clear(double[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = 0;
        }
    }

    public void reset() {
    }

    public double[] approximate(double[] doubles) {
        return propagate(doubles);
    }

    public double[] propagate(double[] inputIn) {
        if (inputs != inputIn) {
            System.arraycopy(inputIn, 0, this.inputs, 0, inputIn.length);
        }
        if (inputIn.length < inputs.length)
            System.out.println("NOTE: only " + inputIn.length + " inputs out of " + inputs.length + " are used in the network");
        propagateOneStep(inputs, hiddenNeurons, firstConnectionLayer);
        tanh(hiddenNeurons);
        propagateOneStep(hiddenNeurons, outputs, secondConnectionLayer);
        tanh(outputs);

        return outputs;

    }

    private void propagateOneStep(double[] fromLayer, double[] toLayer, double[][] connections) {
        clear(toLayer);
        for (int from = 0; from < fromLayer.length; from++) {
            for (int to = 0; to < toLayer.length; to++) {
                toLayer[to] += fromLayer[from] * connections[from][to];
                //System.out.println("From : " + from + " to: " + to + " :: " +toLayer[to] + "+=" +  fromLayer[from] + "*"+  connections[from][to]);
            }
        }
    }

    public double backPropagate(double[] targetOutputs) {
        // Calculate output error
        double[] outputError = new double[outputs.length];

        for (int i = 0; i < outputs.length; i++) {
            //System.out.println("Node : " + i);
            outputError[i] = dtanh(outputs[i]) * (targetOutputs[i] - outputs[i]);
            //System.out.println("Err: " + (targetOutputs[i] - outputs[i]) +  "=" + targetOutputs[i] +  "-" + outputs[i]);
            //System.out.println("dnet: " +  outputError[i] +  "=" + (dtanh(outputs[i])) +  "*" + (targetOutputs[i] - outputs[i]));

            if (Double.isNaN(outputError[i])) {
                System.out.println("Problem at output " + i);
                System.out.println(outputs[i] + " " + targetOutputs[i]);
                System.exit(0);
            }
        }

        // Calculate hidden layer error
        double[] hiddenError = new double[hiddenNeurons.length];

        for (int hidden = 0; hidden < hiddenNeurons.length; hidden++) {
            double contributionToOutputError = 0;
            // System.out.println("Hidden: " + hidden);
            for (int toOutput = 0; toOutput < outputs.length; toOutput++) {
                // System.out.println("Hidden " + hidden + ", toOutput" + toOutput);
                contributionToOutputError += secondConnectionLayer[hidden][toOutput] * outputError[toOutput];
                // System.out.println("Err tempSum: " + contributionToOutputError +  "=" +secondConnectionLayer[hidden][toOutput]  +  "*" +outputError[toOutput] );
            }
            hiddenError[hidden] = dtanh(hiddenNeurons[hidden]) * contributionToOutputError;
            //System.out.println("dnet: " + hiddenError[hidden] +  "=" +  dtanh(hiddenNeurons[hidden])+  "*" + contributionToOutputError);
        }

        ////////////////////////////////////////////////////////////////////////////
        //WEIGHT UPDATE
        ///////////////////////////////////////////////////////////////////////////
        // Update first weight layer
        for (int input = 0; input < inputs.length; input++) {
            for (int hidden = 0; hidden < hiddenNeurons.length; hidden++) {

                double saveAway = firstConnectionLayer[input][hidden];
                firstConnectionLayer[input][hidden] += learningRate * hiddenError[hidden] * inputs[input];

                if (Double.isNaN(firstConnectionLayer[input][hidden])) {
                    System.out.println("Late weight error! hiddenError " + hiddenError[hidden]
                            + " input " + inputs[input] + " was " + saveAway);
                }
            }
        }

        // Update second weight layer
        for (int hidden = 0; hidden < hiddenNeurons.length; hidden++) {

            for (int output = 0; output < outputs.length; output++) {

                double saveAway = secondConnectionLayer[hidden][output];
                secondConnectionLayer[hidden][output] += learningRate * outputError[output] * hiddenNeurons[hidden];

                if (Double.isNaN(secondConnectionLayer[hidden][output])) {
                    System.out.println("target: " + targetOutputs[output] + " outputs: " + outputs[output] + " error:" + outputError[output] + "\n" +
                            "hidden: " + hiddenNeurons[hidden] + "\nnew conn weight: " + secondConnectionLayer[hidden][output] + " was: " + saveAway + "\n");
                }
            }
        }

        double summedOutputError = 0.0;
        for (int k = 0; k < outputs.length; k++) {
            summedOutputError += Math.abs(targetOutputs[k] - outputs[k]);
        }
        summedOutputError /= outputs.length;

        // Return something sensible
        return summedOutputError;
    }


    private double sig(double val) {
        return 1.0d / (1.0d + Math.exp(-val));
    }

    private void tanh(double[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = Math.tanh(array[i]);
            // for the sigmoid
            // array[i] = array[i];
            //array[i] = sig(array[i]);//array[i];//
        }
    }

    private double dtanh(double num) {
        //return 1;
        return (1 - (num * num));
        // for the sigmoid
        //final double val = sig(num);
        //return (val*(1-val));
    }

    private double sum() {
        double sum = 0;
        for (double[] aFirstConnectionLayer : firstConnectionLayer) {
            for (double anAFirstConnectionLayer : aFirstConnectionLayer) {
                sum += anAFirstConnectionLayer;
            }
        }
        for (double[] aSecondConnectionLayer : secondConnectionLayer) {
            for (double anASecondConnectionLayer : aSecondConnectionLayer) {
                sum += anASecondConnectionLayer;
            }
        }
        return sum;
    }

    public double getMutationMagnitude() {
        return mutationMagnitude;
    }

    public void setMutationMagnitude(double mutationMagnitude) {
        this.mutationMagnitude = mutationMagnitude;
    }

    public static void setInitParameters(double mean, double deviation) {
        System.out.println("PARAMETERS SET: " + mean + "  deviation: " + deviation);

        MLP.mean = mean;
        MLP.deviation = deviation;
    }

    public void println() {
        System.out.print("\n\n----------------------------------------------------" +
                "-----------------------------------\n");
        for (double[] aFirstConnectionLayer : firstConnectionLayer) {
            System.out.print("|");
            for (double anAFirstConnectionLayer : aFirstConnectionLayer) {
                System.out.print(" " + anAFirstConnectionLayer);
            }
            System.out.print(" |\n");
        }
        System.out.print("----------------------------------------------------" +
                "-----------------------------------\n");
        for (double[] aSecondConnectionLayer : secondConnectionLayer) {
            System.out.print("|");
            for (double anASecondConnectionLayer : aSecondConnectionLayer) {
                System.out.print(" " + anASecondConnectionLayer);
            }
            System.out.print(" |\n");
        }
        System.out.print("----------------------------------------------------" +
                "-----------------------------------\n");
    }

    public String toString() {
        int numberOfConnections = (firstConnectionLayer.length * firstConnectionLayer[0].length) +
                (secondConnectionLayer.length * secondConnectionLayer[0].length);
        return "Straight mlp, mean connection weight " + (sum() / numberOfConnections);
    }

    public void ssetLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public double[] getOutputs() {
        double[] outputsCopy = new double[outputs.length];
        System.arraycopy(outputs, 0, outputsCopy, 0, outputs.length);
        return outputsCopy;
    }

    public double[] getWeightsArray() {
        double[] weights = new double[inputs.length * hiddenNeurons.length + hiddenNeurons.length * outputs.length];

        int k = 0;
        for (int i = 0; i < inputs.length; i++) {
            for (int j = 0; j < hiddenNeurons.length; j++) {
                weights[k] = firstConnectionLayer[i][j];
                k++;
            }
        }
        for (int i = 0; i < hiddenNeurons.length; i++) {
            for (int j = 0; j < outputs.length; j++) {
                weights[k] = secondConnectionLayer[i][j];
                k++;
            }
        }
        return weights;
    }

    public void setWeightsArray(double[] weights) {
        int k = 0;

        for (int i = 0; i < inputs.length; i++) {
            for (int j = 0; j < hiddenNeurons.length; j++) {
                firstConnectionLayer[i][j] = weights[k];
                k++;
            }
        }
        for (int i = 0; i < hiddenNeurons.length; i++) {
            for (int j = 0; j < outputs.length; j++) {
                secondConnectionLayer[i][j] = weights[k];
                k++;
            }
        }
    }

    public int getNumberOfInputs() {
        return inputs.length;
    }

    public void randomise() {
        randomise(firstConnectionLayer);
        randomise(secondConnectionLayer);
    }

    protected void randomise(double[][] layer) {
        for (int i = 0; i < layer.length; i++) {
            for (int j = 0; j < layer[i].length; j++) {
                layer[i][j] = (Math.random() * 4.0) - 2.0;
            }
        }
    }

    public double[] getArray() {
        return getWeightsArray();
    }

    public void setArray(double[] array) {
        setWeightsArray(array);
    }

}
