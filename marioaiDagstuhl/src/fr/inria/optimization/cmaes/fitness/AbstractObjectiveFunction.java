package fr.inria.optimization.cmaes.fitness;


/** extending from this abstract class implements a generic isFeasible method and adds the  
 * IObjectiveFunctionParallel interface to a class that implements 
 * the interface IObjectiveFunction */
public abstract class AbstractObjectiveFunction implements 
IObjectiveFunction,
IObjectiveFunctionParallel  { 
    abstract public double valueOf(double[] x);
    public double [] valuesOf(double[][] pop) {
        double [] res = new double[pop.length];
        for (int i = 0; i < pop.length; ++i)
            res[i] = valueOf(pop[i]);
        return res;
    }
    public boolean isFeasible(double[] x) {
    	return true;
    }
}
