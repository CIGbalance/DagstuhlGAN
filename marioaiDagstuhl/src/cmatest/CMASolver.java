package cmatest;

import fr.inria.optimization.cmaes.CMAEvolutionStrategy;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;

import java.util.Arrays;

public class CMASolver {
    // adapted from CMAExample1 with the main function
    // becoming the run function

    public static void main(String[] args) {
        CMASolver solver = new CMASolver(new QuadraticBowl(), 10, 1000);
        double[] solution = solver.run();
        System.out.println("Best solution = " + Arrays.toString(solution));
    }

    IObjectiveFunction fitFun;
    int nDim;
    CMAEvolutionStrategy cma;

    public CMASolver(IObjectiveFunction fitFun, int nDim, int maxEvals) {
        this.fitFun = fitFun;
        this.nDim = nDim;
        cma = new CMAEvolutionStrategy();
        cma.readProperties(); // read options, see file CMAEvolutionStrategy.properties
        cma.setDimension(nDim); // overwrite some loaded properties
        cma.setInitialX(0.0); // in each dimension, also setTypicalX can be used
        cma.setInitialStandardDeviation(0.5); // also a mandatory setting
        cma.options.stopFitness = -1e6; // 1e-14;       // optional setting
        // cma.options.stopMaxIter = 100;
        cma.options.stopMaxFunEvals = maxEvals;
        System.out.println("Diagonal: " + cma.options.diagonalCovarianceMatrix);

    }

    public void setDim(int n) {
        cma.setDimension(n);
    }

    public void setInitialX(double x) {
        cma.setInitialX(x);
    }

    public void setObjective(IObjectiveFunction fitFun) {
        this.fitFun = fitFun;
    }

    public void setMaxEvals(int n) {
        cma.options.stopMaxFunEvals = n;
    }

    public double[] run() {

        // new a CMA-ES and set some initial values

        // initialize cma and get fitness array to fill in later
        double[] fitness = cma.init();  // new double[cma.parameters.getPopulationSize()];

        // initial output to files
        cma.writeToDefaultFilesHeaders(0); // 0 == overwrites old files

        // iteration loop
        while (cma.stopConditions.getNumber() == 0) {

            // --- core iteration step ---
            double[][] pop = cma.samplePopulation(); // get a new population of solutions
            for (int i = 0; i < pop.length; ++i) {    // for each candidate solution i
                // a simple way to handle constraints that define a convex feasible domain
                // (like box constraints, i.e. variable boundaries) via "blind re-sampling"
                // assumes that the feasible domain is convex, the optimum is
                while (!fitFun.isFeasible(pop[i]))     //   not located on (or very close to) the domain boundary,
                    pop[i] = cma.resampleSingle(i);    //   initialX is feasible and initialStandardDeviations are
                //   sufficiently small to prevent quasi-infinite looping here
                // compute fitness/objective value
                fitness[i] = fitFun.valueOf(pop[i]); // fitfun.valueOf() is to be minimized
            }
            cma.updateDistribution(fitness);         // pass fitness array to update search distribution
            // --- end core iteration step ---

            // output to files and console
            cma.writeToDefaultFiles();
            int outmod = 150;
            if (cma.getCountIter() % (15 * outmod) == 1) {
                // cma.printlnAnnotation(); // might write file as well
            }
            if (cma.getCountIter() % outmod == 1) {
                // cma.println();
            }
        }
        // evaluate mean value as it is the best estimator for the optimum
        // cma.setFitnessOfMeanX(fitFun.valueOf(cma.getMeanX())); // updates the best ever solution

        // final output
        cma.writeToDefaultFiles(1);
        cma.println();
        cma.println("Terminated due to");
        for (String s : cma.stopConditions.getMessages())
            cma.println("  " + s);
        cma.println("best function value " + cma.getBestFunctionValue()
                + " at evaluation " + cma.getBestEvaluationNumber());

        // System.out.println("Best solution is: " + Arrays.toString(cma.getBestX()));
        // we might return cma.getBestSolution() or cma.getBestX()
        // return cma.getBestX();

        return cma.getBestRecentX();

    }

}

