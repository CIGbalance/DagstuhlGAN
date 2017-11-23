package cmatest;

import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;

public class QuadraticBowl implements IObjectiveFunction {
    // changing floor will change the reason for termination
    // (in conjunction with the target value)
    // see cma.options.stopFitness
    static double floor = 0.0;
    @Override
    public double valueOf(double[] x) {
        double tot = floor;
        for (double a : x) tot += (5+a)*(5+a);
        return tot;
    }

    @Override
    public boolean isFeasible(double[] x) {
        return true;
    }
}
