package fr.inria.optimization.cmaes.fitness;

/** Interface to a single-objective function to be minimized, 
 * that accepts an array
 * of points double[][], a population to be evaluated within one call to method valuesOf().
*/
public interface IObjectiveFunctionParallel {

    /** 
     * @param pop is an array of search points to be evaluated, where
     * pop[i] is the i-th point. 
     * @return array of objective function values. The i-th value 
     * is the objective function value of pop[i].
     * */ 
    double[] valuesOf(double pop[][]);
}

