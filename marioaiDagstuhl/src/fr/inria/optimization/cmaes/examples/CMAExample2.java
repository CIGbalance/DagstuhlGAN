package fr.inria.optimization.cmaes.examples;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import fr.inria.optimization.cmaes.CMAEvolutionStrategy;
import fr.inria.optimization.cmaes.CMAOptions;
import fr.inria.optimization.cmaes.CMASolution;
import fr.inria.optimization.cmaes.fitness.FunctionCollector;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;

/**  
 * Example of how to use the class CMAEvolutionStrategy including restarts with increasing 
 * population size (IPOP). Copy and modify the code to your convenience. Final termination criteria
 * are stopFitness and stopMaxFunEvals (see class {@link CMAOptions}). The remaining  
 * termination criteria invoke a restart with increased population size (see incPopSizeFactor in file
 * CMAEvolutionStrategy.properties). 
 * 
 * @see CMAEvolutionStrategy
 * 
 * @author Nikolaus Hansen, released into public domain. 
 */
public class CMAExample2 {

    public static void main(String[] args) {
        int irun, nbRuns=1;  // restarts, re-read from properties file below
        double [] fitness; 
        CMASolution bestSolution = null; // initialization to allow compilation
        long counteval = 0;              // variables used for restart
        int lambda = 0;
        
        for (irun = 0; irun < nbRuns; ++irun) { // might also terminate before
        	
        	CMAEvolutionStrategy cma = new CMAEvolutionStrategy();

        	// read properties file and obtain some values for "private" use
        	cma.readProperties(); // reads from file CMAEvolutionStrategy.properties
        	//cma.setInitialX(-20, 80);
        	//cma.setInitialStandardDeviation(0.3 * 100);

        	// set up fitness function
        	double nbFunc = cma.options.getFirstToken(cma.getProperties().getProperty("functionNumber"), 10);
        	int rotate = cma.options.getFirstToken(cma.getProperties().getProperty("functionRotate"), 0);
        	double axisratio = cma.options.getFirstToken(cma.getProperties().getProperty("functionAxisRatio"), 0.);
            IObjectiveFunction fitfun = new FunctionCollector(nbFunc, rotate, axisratio);

            // set up restarts
            nbRuns = 1+cma.options.getFirstToken(cma.getProperties().getProperty("numberOfRestarts"), 1);
            double incPopSizeFactor = cma.options.getFirstToken(cma.getProperties().getProperty("incPopSizeFactor"), 1.);
             
            // initialize 
            if (irun == 0) {
            	fitness = cma.init(); // finalize setting of population size lambda, get fitness array
        		lambda = cma.parameters.getPopulationSize(); // retain lambda for restart
        		cma.writeToDefaultFilesHeaders(0); // overwrite output files
        	}
        	else {
                cma.parameters.setPopulationSize((int)Math.ceil(lambda * Math.pow(incPopSizeFactor, irun)));
                cma.setCountEval(counteval); // somehow a hack 
                fitness = cma.init(); // provides array to assign fitness values
            }
            
            // set additional termination criterion
            if (nbRuns > 1) 
                cma.options.stopMaxIter = (long) (100 + 200*Math.pow(cma.getDimension(),2)*Math.sqrt(cma.parameters.getLambda()));

            // iteration loop
            double lastTime = 0, alastTime = 0; // for smarter console output
            while(cma.stopConditions.isFalse()) {

                // --- core iteration step ---
                double[][] pop = cma.samplePopulation(); // get a new population of solutions
                for(int i = 0; i < pop.length; ++i) {    // for each candidate solution i
                	// a simple way to handle constraints that define a convex feasible domain  
                	// (like box constraints, i.e. variable boundaries) via "blind re-sampling" 
                	                                       // assumes that the feasible domain is convex, the optimum is  
    				while (!fitfun.isFeasible(pop[i]))     //   not located on (or very close to) the domain boundary,  
                        pop[i] = cma.resampleSingle(i);    //   initialX is feasible and initialStandardDeviations are  
                                                           //   sufficiently small to prevent quasi-infinite looping here
                    // compute fitness/objective value
                	fitness[i] = fitfun.valueOf(pop[i]); // fitfun.valueOf() is to be minimized
                }
                cma.updateDistribution(fitness);         // pass fitness array to update search distribution
                // --- end core iteration step ---

                // stopping conditions can be changed in file CMAEvolutionStrategy.properties 
                cma.readProperties();  

                // the remainder is output
                cma.writeToDefaultFiles();

                // screen output
                boolean printsomething = true; // for a convenient switch to false
                if (printsomething && System.currentTimeMillis() - alastTime > 20e3) {
                    cma.printlnAnnotation();
                    alastTime = System.currentTimeMillis();
                }
                if (printsomething && (cma.stopConditions.isTrue() || cma.getCountIter() < 4 
                        || (cma.getCountIter() > 0 && (Math.log10(cma.getCountIter()) % 1) < 1e-11)
                        || System.currentTimeMillis() - lastTime > 2.5e3)) { // wait 2.5 seconds
                    cma.println();
                    lastTime = System.currentTimeMillis();
                }
            } // iteration loop

    		// evaluate mean value as it is the best estimator for the optimum
    		cma.setFitnessOfMeanX(fitfun.valueOf(cma.getMeanX())); // updates the best ever solution 

    		// retain best solution ever found 
    		if (irun == 0)
    			bestSolution = cma.getBestSolution();
    		else if (cma.getBestSolution().getFitness() < bestSolution.getFitness())
    			bestSolution = cma.getBestSolution();

            // final output for the run
            cma.writeToDefaultFiles(1); // 1 == make sure to write final result
            cma.println("Terminated (run " + (irun+1) + ") due to");
            for (String s : cma.stopConditions.getMessages()) 
                cma.println("      " + s);
    		cma.println("    best function value " + cma.getBestFunctionValue() 
    				+ " at evaluation " + cma.getBestEvaluationNumber());

            // quit restart loop if MaxFunEvals or target Fitness are reached
            boolean quit = false;
            for (String s : cma.stopConditions.getMessages()) 
                if (s.startsWith("MaxFunEvals") ||
                    s.startsWith("Fitness")) 
                    quit = true;
            if (quit)
                break;
            
            counteval = cma.getCountEval();

            if (irun < nbRuns-1) // after Manual stop give time out to change stopping condition 
            	for (String s : cma.stopConditions.getMessages()) 
            		if (s.startsWith("Manual")) {
            			System.out.println("incomment 'stop now' and press return to start next run");
            			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            			try { in.readLine(); }
            			catch(IOException e) { System.out.println("input not readable"); }
            		}

        } // for irun < nbRuns

        // screen output
        if (irun > 1) {
            System.out.println(" " + (irun) + " runs conducted," 
                    + " best function value " + bestSolution.getFitness() 
                    + " at evaluation " + bestSolution.getEvaluationNumber());
        }

    } // main
} // class


