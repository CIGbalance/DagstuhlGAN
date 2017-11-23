package ch.idsia.ai;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: May 24, 2009
 * Time: 11:12:32 PM
 *
 * Simple Recurrent Network (a.k.a. Elman network, Recurrent MLP)
 *
 */
public class SRN implements FA<double[], double[]>, Evolvable {

    protected double[][] firstConnectionLayer;
    protected double[][] recurrentConnectionLayer;
    protected double[][] secondConnectionLayer;
    protected double[] hiddenNeurons;
    protected double[] hiddenNeuronsCopy;
    protected double[] outputs;
    protected double mutationMagnitude = 0.1;

    private final Random random = new Random();

    public SRN(int numberOfInputs, int numberOfHidden, int numberOfOutputs) {
        firstConnectionLayer = new double[numberOfInputs][numberOfHidden];
        recurrentConnectionLayer = new double[numberOfHidden][numberOfHidden];
        secondConnectionLayer = new double[numberOfHidden][numberOfOutputs];
        hiddenNeurons = new double[numberOfHidden];
        hiddenNeuronsCopy = new double[numberOfHidden];
        outputs = new double[numberOfOutputs];
        mutate();
    }

    public SRN(double[][] firstConnectionLayer, double[][] recurrentConnectionLayer,
                 double[][] secondConnectionLayer, int numberOfHidden,
                 int numberOfOutputs) {
        this.firstConnectionLayer = firstConnectionLayer;
        this.recurrentConnectionLayer = recurrentConnectionLayer;
        this.secondConnectionLayer = secondConnectionLayer;
        hiddenNeurons = new double[numberOfHidden];
        hiddenNeuronsCopy = new double[numberOfHidden];
        outputs = new double[numberOfOutputs];
    }

    public double[] propagate(double[] inputs) {

        if (inputs.length != firstConnectionLayer.length)
            System.out.println("NOTE: only " + inputs.length + " inputs out of " + firstConnectionLayer.length + " are used in the network");

        System.arraycopy(hiddenNeurons, 0, hiddenNeuronsCopy, 0, hiddenNeurons.length);
        clear(hiddenNeurons);
        clear(outputs);
        propagateOneStep(inputs, hiddenNeurons, firstConnectionLayer);
        propagateOneStep(hiddenNeuronsCopy, hiddenNeurons, recurrentConnectionLayer);
        tanh(hiddenNeurons);
        propagateOneStep(hiddenNeurons, outputs, secondConnectionLayer);
        tanh(outputs);
        return outputs;
    }

    public SRN getNewInstance() {
        return new SRN(firstConnectionLayer.length, secondConnectionLayer.length, outputs.length);
    }

    public SRN copy() {
        return new SRN(copy(firstConnectionLayer), copy(recurrentConnectionLayer),
                copy(secondConnectionLayer), hiddenNeurons.length, outputs.length);
    }

    public void mutate() {
        mutate(firstConnectionLayer);
        mutate(recurrentConnectionLayer);
        mutate(secondConnectionLayer);
    }

    public void reset() {
        clear(hiddenNeurons);
        clear(hiddenNeuronsCopy);
    }

    public double[] approximate(double[] doubles) {
        return propagate(doubles);
    }

    protected double[][] copy(double[][] original) {
        double[][] copy = new double[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
        }
        return copy;
    }

    protected void mutate(double[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] += random.nextGaussian() * mutationMagnitude;
        }
    }

    protected void mutate(double[][] array) {
        for (double[] anArray : array) {
            mutate(anArray);
        }
    }

    protected void propagateOneStep(double[] fromLayer, double[] toLayer, double[][] connections) {
        for (int from = 0; from < fromLayer.length; from++) {
            for (int to = 0; to < toLayer.length; to++) {
                toLayer[to] += fromLayer[from] * connections[from][to];
            }
        }
    }

    protected void clear(double[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = 0;
        }
    }

    protected void tanh(double[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = Math.tanh(array[i]);
        }
    }



    public String toString() {
        return "RecurrentMLP:" + firstConnectionLayer.length + "/" + secondConnectionLayer.length + "/" + outputs.length;
    }

    public void setMutationMagnitude(double mutationMagnitude) {
        this.mutationMagnitude = mutationMagnitude;
    }

}
