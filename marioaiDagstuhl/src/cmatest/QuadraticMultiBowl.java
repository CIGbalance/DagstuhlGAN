package cmatest;

import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;

public class QuadraticMultiBowl implements IObjectiveFunction {

    public static void main(String[] args) {
        QuadraticMultiBowl ob = new QuadraticMultiBowl();
        CMASolver cma = new CMASolver(ob, 30, 10000);
        cma.run();
    }

    // changing floor will change the reason for termination
    // (in conjunction with the target value)
    // see cma.options.stopFitness
    double x1 = 0.2;
    double v1 = 0;
    // x2 is further away but has a better value
    double x2 = 0.9;
    double v2 = 5;

    static double floor = 0.0;

    int count = 0;
    @Override
    public double valueOf(double[] x) {
        count++;
        double d1 = dist(x, x1);
        double d2 = dist(x, x2);
        double value = d1 < d2 ? v1 + d1 : v2 + d2;
        // System.out.format("%d\t %.2f\t %s \n", count, value, prettyString(x));
        return value;
    }

    public double dist(double[] x, double y) {
        double tot = floor;
        for (double a : x) tot += (a-y)*(a-y);
        return tot;
    }

    public String prettyString(double[] a) {
        StringBuilder sb = new StringBuilder();
        for (double x : a) sb.append(String.format("%.1f ",x));
        return sb.toString();
    }


    @Override
    public boolean isFeasible(double[] x) {
        return true;
    }
}
