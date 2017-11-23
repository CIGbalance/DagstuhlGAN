package fr.inria.optimization.cmaes;

import java.util.*; // Properties, Arrays.sort, Formatter not needed anymore

/* 
    Copyright 1996, 2003, 2005, 2007 Nikolaus Hansen 
    e-mail: hansen .AT. lri.fr

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License, version 3,
    as published by the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 log of changes: 
     o updateDistribution(double[][], double[], int) introduced,
       for the time being
       updateDistribution(double[][], double[]) evaluates to
       updateDistribution(double[][], double[], 0), but it might become
       updateDistribution(double[][], double[], popsize)
     o init() cannot be called twice anymore, it's saver like this 
     o warning() and error() print also to display-file
     o checkEigenSystem() call is now an option, gives warnings, not
       errors, and has its precision criteria adapted to Java.
     o 06/08 fix: error for negative eigenvalues did not show up 
     o 09/08: diagonal option included
     o updateDistribution(double[][], double[]) is available, which 
       implements an interface, independent of samplePopulation(). 
     o variable locked is set at the end of supplementRemainders, 
       instead at the beginning (09/03/08)
     o bestever is set anew, if its current fitness is NaN (09/03/08)
     o getBestRecentX() now returns really the recent best (10/03/17) 
       (thanks to Markus Kemmerling for reporting this problem)
	 o 2010/12/02: merge of r762 (diagonal option) and r2462 which were 
	   subbranches since r752
	 o test() uses flgdiag to get internally time linear

 WISH LIST:
     o test and consider refinement of 
       updateDistribution(double[][], double[]) that
       implements a "saver" interface, 
       independent of samplePopulation
       for example updateDistribution(ISolutionPoint[] pop)
     o save all input parameters as output-properties file
     o explicit control of data writing behavior in terms of iterations
       to wait until the next writing?
     o clean up sorting of eigenvalues/vectors which is done repeatedly
     o implement a good boundary handling
     o check Java random number generator and/or implement a private one. 
	 o implement a general initialize_with_evaluated_points method, which
	   estimates a good mean and covariance matrix either from all points
	   or only from the lambda best points (actually mu best points then).
	   cave about outlier points. 
     o implement a CMA-ES-specific feed points method for initialization. It should
       accept a population of evaluated points iteratively. It 
       just needs to call updateDistribution with a population as input. 
	 o save z instead of recomputing it? 
     o improve error management to reasonable standard 
     o provide output writing for given evaluation numbers and/or given fitness values
     o better use the class java.lang.Object.Date to handle elapsed times?  

 Last change: $Date: 2011-06-23 $     
 */

/** 
 * implements the Covariance Matrix Adaptation Evolution Strategy (CMA-ES)
 * for non-linear, non-convex, non-smooth, global function minimization. The CMA-Evolution Strategy
 * (CMA-ES) is a reliable stochastic optimization method which should be applied,
 * if derivative based methods, e.g. quasi-Newton BFGS or conjugate
 * gradient, fail due to a rugged search landscape (e.g. noise, local
 * optima, outlier, etc.)  of the objective function. Like a
 * quasi-Newton method the CMA-ES learns and applies a variable metric
 * of the underlying search space. Unlike a quasi-Newton method the
 * CMA-ES does neither estimate nor use gradients, making it considerably more
 * reliable in terms of finding a good, or even close to optimal, solution, finally.
 *
 * <p>In general, on smooth objective functions the CMA-ES is roughly ten times
 * slower than BFGS (counting objective function evaluations, no gradients provided). 
 * For up to <math>N=10</math> variables also the derivative-free simplex
 * direct search method (Nelder & Mead) can be faster, but it is
 * far less reliable than CMA-ES. 
 *
 * <p>The CMA-ES is particularly well suited for non-separable 
 * and/or badly conditioned problems. 
 * To observe the advantage of CMA compared to a conventional
 * evolution strategy, it will usually take about 30&#215;<math>N</math> function
 * evaluations. On difficult problems the complete
 * optimization (a single run) is expected to take <em>roughly</em>  between
 * <math>30&#215;N</math> and <math>300&#215;N<sup>2</sup></math>
 * function evaluations.  
 *  
 * <p>The main functionality is provided by the methods <code>double[][] {@link #samplePopulation()}</code> and 
 * <code>{@link #updateDistribution(double[])}</code> or <code>{@link #updateDistribution(double[][], double[])}</code>. 
 * Here is an example code snippet, see file 
 * <tt>CMAExample1.java</tt> for a similar example, and 
 *   <tt>CMAExample2.java</tt> for a more extended example with multi-starts implemented.
 *   <pre>
        // new a CMA-ES and set some initial values
        CMAEvolutionStrategy cma = new CMAEvolutionStrategy();
        cma.readProperties(); // read options, see file CMAEvolutionStrategy.properties
        cma.setDimension(10); // overwrite some loaded properties
        cma.setTypicalX(0.5); // in each dimension, setInitialX can be used as well
        cma.setInitialStandardDeviation(0.2); // also a mandatory setting 
        cma.opts.stopFitness = 1e-9;          // optional setting

        // initialize cma and get fitness array to fill in later
        double[] fitness = cma.init();  // new double[cma.parameters.getPopulationSize()];

        // initial output to files
        cma.writeToDefaultFilesHeaders(0); // 0 == overwrites old files

        // iteration loop
        while(cma.stopConditions.getNumber() == 0) {

            // core iteration step 
            double[][] pop = cma.samplePopulation(); // get a new population of solutions
            for(int i = 0; i < pop.length; ++i) {    // for each candidate solution i
                fitness[i] = fitfun.valueOf(pop[i]); //    compute fitness value, where fitfun
            }                                        //    is the function to be minimized
            cma.updateDistribution(fitness);         // use fitness array to update search distribution
 
            // output to files
            cma.writeToDefaultFiles();
            ...in case, print output to console, eg. cma.println(), 
               or process best found solution, getBestSolution()...
        } // while 

        // evaluate mean value as it is the best estimator for the optimum
        cma.setFitnessOfMeanX(fitfun.valueOf(cma.getMeanX())); // updates the best ever solution 
        ...retrieve best solution, termination criterion via stopConditions etc...    
        
        return cma.getBestX(); // best evaluated search point 
        
     *   </pre>
     *   The output generated by the function <code>writeToDefaultFiles</code> can be 
     *   plotted in Matlab or Scilab using <tt>plotcmaesdat.m</tt> or
     *   <tt>plotcmaesdat.sci</tt> respectively, see {@link #writeToDefaultFiles()}. 
     *     
</P>     
<P> The implementation follows very closely <a name=HK2004>[3]</a>. It supports small and large 
population sizes, the latter by using the rank-&micro;-update [2],
together with weighted recombination for the covariance matrix, an
improved parameter setting for large populations [3] and an (initially) diagonal covariance matrix [5]. 
The latter is particularly useful for large dimension, e.g. larger 100. 
The default population size is small [1]. An
independent restart procedure with increasing population size [4]
is implemented in class <code>{@link cmaes.examples.CMAExample2}</code>.</P>

 * <P><B>Practical hint</B>: In order to solve an optimization problem in reasonable time it needs to be 
 * reasonably encoded. In particular the domain width of variables should be 
 * similar for all objective variables (decision variables), 
 * such that the initial standard deviation can be chosen the same
 * for each variable. For example, an affine-linear transformation could be applied to
 * each variable, such that its typical domain becomes the interval [0,10]. 
 * For positive variables a log-encoding or a square-encoding 
 * should be considered, to avoid the need to set a hard boundary at zero, 
 * see <A href="http://www.lri.fr/~hansen/cmaes_inmatlab.html#practical">here for a few more details</A>.
 * </P>

<P><B>References</B>
<UL>
<LI>[1] Hansen, N. and A. Ostermeier (2001). Completely
Derandomized Self-Adaptation in Evolution Strategies. <I>Evolutionary
Computation</I>, 9(2), pp. 159-195. 
</LI>
<LI>[2] Hansen, N., S.D. M&uuml;ller and
P. Koumoutsakos (2003). Reducing the Time Complexity of the
Derandomized Evolution Strategy with Covariance Matrix Adaptation
(CMA-ES). <I>Evolutionary Computation</I>, 11(1), pp. 1-18.

<LI>[3] Hansen and Kern (2004). Evaluating the CMA Evolution
Strategy on Multimodal Test Functions. In <I> Eighth International
Conference on Parallel Problem Solving from Nature PPSN VIII,
Proceedings</I>, pp. 282-291, Berlin: Springer.
</LI>
<LI>[4]
Auger, A, and Hansen, N. (2005). A Restart CMA Evolution Strategy
With Increasing Population Size.</A> In <I>Proceedings of the IEEE
Congress on Evolutionary Computation, CEC 2005</I>, pp.1769-1776.
</LI>
<LI>[5]
Ros, R. and N. Hansen (2008). A Simple
Modification in CMA-ES Achieving Linear Time and Space Complexity.
In Rudolph et al. (eds.) <I>Parallel Problem Solving from Nature, PPSN X,
Proceedings</I>, pp. 296-305, Springer.
</LI>
</UL>
</P>

 * @see #samplePopulation()
 * @see #updateDistribution(double[])
 * @author Nikolaus Hansen, 1996, 2003, 2005, 2007
 */
public class CMAEvolutionStrategy implements java.io.Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2918241407634253526L;

	/**
     * 
     */
    public final String versionNumber = new String("0.99.40"); 
    
    /** Interface to whether and which termination criteria are satisfied 
     */
    public class StopCondition { 
    	int index = 0; // number of messages collected == index where to write next message
    	String[] messages = new String[]{""}; // Initialisation with empty string
    	double lastcounteval;
    	/** true whenever a termination criterion was met. clear() 
    	 * re-sets this value to false. 
    	 * @see #clear() 
    	 */
    	public boolean isTrue() {
    		return test() > 0;
    	}
    	/** evaluates to NOT isTrue(). 
    	 * @see #isTrue()
    	 */
    	public boolean isFalse() {
    		return !isTrue();
    	}
    	/** greater than zero whenever a termination criterion was satisfied, zero otherwise. 
    	 * clear() re-sets this value to zero. 
    	 * @return number of generated termination condition messages */
    	public int getNumber() {
    		return test();
    	}

    	/**
    	 * get description messages of satisfied termination criteria.
    	 * The messages start with one of "Fitness:", "TolFun:", "TolFunHist:", 
    	 * "TolX:", "TolUpX:", "MaxFunEvals:", "MaxIter:", "ConditionNumber:", 
    	 * "NoEffectAxis:", "NoEffectCoordinate:". 
    	 * @return String[] s with messages of termination conditions.
    	 *   s[0].equals("") is true if no termination condition is satisfied yet
    	 */
    	public String[] getMessages() { 
    		test();
    		return messages; /* first string might be empty */
    	}                   
    	/** remove all earlier termination condition messages 
    	 */
    	public void clear() {
    		messages = new String[]{""}; 
    		index = 0;
    	}

    	private void appendMessage(String s) { 
    		// could be replaced by ArrayList<String> or Vector<String>
    		// but also String[] can be iterated easily since version 1.5 
    		String [] mold = messages;
    		messages = new String[index + 1];

    		/* copy old messages */
    		for (int i = 0; i < index; ++i) 
    			messages[i] = mold[i];

    		messages[index++] = s + " (iter=" + countiter + ",eval=" + counteval + ")";
    	}

    	/**
    	 * Tests termination criteria and evaluates to  greater than zero when a
    	 * termination criterion is satisfied. Repeated tests append the met criteria repeatedly, 
    	 * only if the evaluation count has changed. 
    	 * @return number of termination criteria satisfied
    	 */
    	int test() { 
    		if (state < 0)
    			return 0;  // not yet initialized
    		if (index > 0 && (counteval == lastcounteval 
    		                    || counteval == lastcounteval+1)) // one evaluation for xmean is ignored
    			return index;  // termination criterion already met

    		lastcounteval = counteval; 
    		
    		/* FUNCTION VALUE */
    		if ((countiter > 1 || state >= 3) && bestever_fit <= options.stopFitness)
    				appendMessage("Fitness: Objective function value dropped below the target function value " +
    						options.stopFitness);

    		/* #Fevals */
    		if (counteval >= options.stopMaxFunEvals)
    			appendMessage("MaxFunEvals: maximum number of function evaluations " + options.stopMaxFunEvals + " reached");

    		/* #iterations */
    		if (countiter >= options.stopMaxIter)
    			appendMessage("MaxIter: maximum number of iterations reached");

    		/* TOLFUN */
    		if ((countiter > 1 || state >= 3)
    			&& Math.max(math.max(fit.history), fit.fitness[fit.fitness.length-1].val)
    					- Math.min(math.min(fit.history),fit.fitness[0].val) <= options.stopTolFun) 
    				appendMessage("TolFun: function value changes below stopTolFun=" + options.stopTolFun);

    		/* TOLFUNHIST */
    		if (options.stopTolFunHist >= 0 && countiter > fit.history.length) {
    			if (math.max(fit.history) - math.min(fit.history) <= options.stopTolFunHist) 
    				appendMessage("TolFunHist: history of function value changes below stopTolFunHist=" + options.stopTolFunHist);
    		}

    		/* TOLX */
    		double tolx = Math.max(options.stopTolX, options.stopTolXFactor * minstartsigma);
    		if (sigma * maxsqrtdiagC < tolx 
    				&& sigma * math.max(math.abs(pc)) < tolx)
    			appendMessage("TolX or TolXFactor: standard deviation below " + tolx);

    		/* TOLXUP */
    		if (sigma * maxsqrtdiagC > options.stopTolUpXFactor * maxstartsigma)
    			appendMessage("TolUpX: standard deviation increased by more than stopTolUpXFactor=" + 
    					options.stopTolUpXFactor + 
    					", larger initial standard deviation recommended");

    		/* STOPNOW */
    		if (options.stopnow)
    		    appendMessage("Manual: flag Options.stopnow set or stop now in .properties file");
    		
    		/* Internal (numerical) stopping termination criteria */

    		/* Test each principal axis i, whether x == x + 0.1 * sigma * rgD[i] * B[i] */
    		for (int iAchse = 0; iAchse < N; ++iAchse) {
    			int iKoo;
    			int l = flgdiag ? iAchse : 0;
    			int u = flgdiag ? iAchse+1 : N;
    			double fac = 0.1 * sigma * diagD[iAchse];
    			for (iKoo = l; iKoo < u; ++iKoo) { 
    				if (xmean[iKoo] != xmean[iKoo] + fac * B[iKoo][iAchse])
    					break; // is OK for this iAchse
    			}
    			if (iKoo == u) // no break, therefore no change for axis iAchse
    				appendMessage("NoEffectAxis: Mutation " + 0.1*sigma*diagD[iAchse] +
    						" in a principal axis " + iAchse + " has no effect");
    		} /* for iAchse */

    		/* Test whether one component of xmean is stuck */
    		for (int iKoo = 0; iKoo < N; ++iKoo) {
    			if (xmean[iKoo] == xmean[iKoo] + 0.2*sigma*Math.sqrt(C[iKoo][iKoo]))
    				appendMessage("NoEffectCoordinate: Mutation of size " + 
    						0.2*sigma*Math.sqrt(C[iKoo][iKoo]) +
    						" in coordinate " + iKoo + " has no effect");
    		} /* for iKoo */

    		/* Condition number */
    		if (math.min(diagD) <= 0)
    			appendMessage("ConditionNumber: smallest eigenvalue smaller or equal zero");
    		else if (math.max(diagD)/math.min(diagD) > 1e7)
    			appendMessage("ConditionNumber: condition number of the covariance matrix exceeds 1e14");
    		return index; // call to appendMessage increments index
    	}
    } // StopCondtion


    void testAndCorrectNumerics() { // not much left here

    	/* Flat Fitness, Test if function values are identical */
    	if (getCountIter() > 1 || (getCountIter() == 1 && state >= 3))
    		if (fit.fitness[0].val == fit.fitness[Math.min(sp.getLambda()-1, sp.getLambda()/2+1) - 1].val) {
    			warning("flat fitness landscape, consider reformulation of fitness, step-size increased");
    			sigma *= Math.exp(0.2+sp.getCs()/sp.getDamps());
    		}

    	/* Align (renormalize) scale C (and consequently sigma) */
    	/* e.g. for infinite stationary state simulations (noise
    	 * handling needs to be introduced for that) */
    	double fac = 1;
    	if (math.max(diagD) < 1e-6) 
    		fac = 1./math.max(diagD);
    	else if (math.min(diagD) > 1e4)
    		fac = 1./math.min(diagD);

    	if (fac != 1.) {
    		sigma /= fac;
    		for(int i = 0; i < N; ++i) {
    			pc[i] *= fac;
    			diagD[i] *= fac;
    			for (int j = 0; j <= i; ++j)
    				C[i][j] *= fac*fac;
    		}
    	}
    } // Test...

    /** options that can be changed (fields can be assigned) at any time to control 
     * the running behavior
     * */
    public CMAOptions options = new CMAOptions();

    private CMAParameters sp = new CMAParameters(); // alias for inside use
    /** strategy parameters that can be set initially 
     * */
    public CMAParameters parameters = sp; // for outside use also

    /** permits access to whether and which termination conditions were satisfied */
    public StopCondition stopConditions = new StopCondition(); 

    int N;
    long seed = System.currentTimeMillis();
    Random rand = new Random(seed); // Note: it also Serializable

    final MyMath math = new MyMath();
    double axisratio; 
    long counteval;
    long countiter;

    long bestever_eval; // C style, TODO:  better make use of class CMASolution?
    double[] bestever_x;
    double bestever_fit = Double.NaN; 
    // CMASolution bestever; // used as output variable

    double sigma = 0.0;
    double[] typicalX; // eventually used to set initialX
    double[] initialX; // set in the end of init()
    double[] LBound, UBound;    // bounds
    double[] xmean;
    double xmean_fit = Double.NaN;
    double[] pc;
    double[] ps;
    double[][] C;
    double maxsqrtdiagC;
    double minsqrtdiagC;
    double[][] B;
    double[] diagD;
    boolean flgdiag; // 0 == full covariance matrix
    
    /* init information */
    double[] startsigma;
    double maxstartsigma;
    double minstartsigma;
    
    boolean iniphase;
 
    /**
     * state (postconditions):
     *  -1 not yet initialized
     *   0 initialized init()
     *   0.5 reSizePopulation
     *   1 samplePopulation, sampleSingle, reSampleSingle
     *   2.5 updateSingle
     *   3 updateDistribution
     */
    double state = -1;
    long citerlastwritten = 0;
    long countwritten = 0;
    int lockDimension = 0;
    int mode = 0;
    final int SINGLE_MODE = 1; // not in use anymore, keep for later developements?
    final int PARALLEL_MODE = 2;

    
    long countCupdatesSinceEigenupdate;
    
    /* fitness information */
    class FitnessCollector {
        double history[];
        IntDouble[] fitness;   // int holds index for respective arx
        IntDouble[] raw; // sorted differently than fitness!
        /** history of delta fitness / sigma^2. Here delta fitness is the minimum of 
         * fitness value differences with distance lambda/2 in the ranking.  */
        double[] deltaFitHist = new double[5];
        int idxDeltaFitHist = 0;
    }
    protected FitnessCollector fit = new FitnessCollector();

    double recentFunctionValue; 
    double recentMaxFunctionValue;
    double recentMinFunctionValue;
    int idxRecentOffspring; 
    
    double[][] arx;
    /** recent population, no idea whether this is useful to be public */
    public double[][] population; // returned not as a copy
    double[] xold;
    
    double[] BDz;
    double[] artmp;
    
    String propertiesFileName = new String("CMAEvolutionStrategy.properties");
    /** postpones most initialization. For initialization use setInitial... 
     * methods or set up a properties file, see file "CMAEvolutionStrategy.properties". */
    public CMAEvolutionStrategy() {
        state = -1;
    }
    
    /** retrieves options and strategy parameters from properties input, see file <tt>CMAEvolutionStrategy.properties</tt> 
     *  for valid properties */
    public CMAEvolutionStrategy(Properties properties) {
        setFromProperties(properties); 
        state = -1;
    }
    /** reads properties (options, strategy parameter settings) from 
     * file <code>propertiesFileName</code>
     * */
    public CMAEvolutionStrategy(String propertiesFileName) {
        this.propertiesFileName = propertiesFileName; 
        state = -1;
    }

    /** @param dimension    search space dimension, dimension of the
     *       objective functions preimage, number of variables
     */
    public CMAEvolutionStrategy(int dimension) {
        setDimension(dimension);
        state = -1;
    }
   
    /** initialization providing all mandatory input arguments at once. The following two 
     * is equivalent
     * <PRE>
          cma.init(N, X, SD);
     * </PRE> and
     * <PRE>
          cma.setInitalX(X);  // 
          cma.setInitialStandardDeviations(SD);
          cma.init(N);
     * </PRE> 
     *
     * The call to <code>init</code> is a point of no return for parameter 
     * settings, and demands all mandatory input be set. <code>init</code> then forces the 
     * setting up of everything and calls 
     * <code>parameters.supplementRemainders()</code>. If <code>init</code> was not called before, it is called once in
     * <code>samplePopulation()</code>. The return value is only provided for sake of convenience. 
     * 
     * @param dimension
     * @param initialX double[] can be of size one, where all variables are set to the 
     * same value, or of size dimension
     * @param initialStandardDeviations can be of size one, where all standard
     * deviations are set to the same value, or of size dimension
     * 
     * @return <code>double[] fitness</code> of length population size lambda to assign and pass
     * objective function values to <code>{@link #updateDistribution(double[])}</code>
     * 
     * @see #init()
     * @see #init(int)
     * @see #setInitialX(double[])
     * @see #setTypicalX(double[])
     * @see #setInitialStandardDeviations(double[])
     * @see #samplePopulation()
     * @see CMAParameters#supplementRemainders(int, CMAOptions)
     */
    public double[] init(int dimension, double[] initialX, double[] initialStandardDeviations) { 
    	setInitialX(initialX);
    	setInitialStandardDeviations(initialStandardDeviations);
    	return init(dimension);
    }
    
    private double[] getArrayOf(double x, int dim) {
    	double[] res = new double[dim];
    	for (int i = 0; i < dim; ++i)
    		res[i] = x;
    	return res;
    }
    /** 
     * 
     * @param x null or x.length==1 or x.length==dim, only for the second case x is expanded
     * @param dim
     * @return <code>null</code> or <code>double[] x</code> with <code>x.length==dim</code>
     */
    private double[] expandToDimension(double[] x, int dim) {
    	if (x == null)
    		return null;
    	if (x.length == dim)
    		return x;
    	if (x.length != 1)
    		error("x must have length one or length dimension");

    	return getArrayOf(x[0], dim);
    }

    /**
     * @param dimension search space dimension 
     * @see #init(int, double[], double[])
     * */
    public double[] init(int dimension) { 
    	setDimension(dimension);
    	return init();
    }
    /** 
     * @see #init(int, double[], double[])
     * */
    public double[] init() {
    	int i;
    	if (N <= 0)
    		error("dimension needs to be determined, use eg. setDimension() or setInitialX()");
    	if (state >= 0)
    		error("init() cannot be called twice");
    	if (state == 0) // less save variant 
    		return new double[sp.getLambda()]; 
    	if (state > 0)  
    		error("init() cannot be called after the first population was sampled");

    	sp = parameters; /* just in case the user assigned parameters */
    	if (sp.supplemented == 0) // a bit a hack
    		sp.supplementRemainders(N, options);
    	sp.locked = 1; // lambda cannot be changed anymore

    	diagD = new double[N];
    	for (i = 0; i < N; ++i)
    		diagD[i] = 1;

    	/* expand Boundaries */
		LBound = expandToDimension(LBound, N);
    	if (LBound == null) {
    		LBound = new double[N];
    		for (i = 0; i < N; ++i)
    			LBound[i] = Double.NEGATIVE_INFINITY;
    	}

		UBound = expandToDimension(UBound, N);
    	if (UBound == null) {
    		UBound = new double[N];
    		for (i = 0; i < N; ++i)
    			UBound[i] = Double.POSITIVE_INFINITY;
    	}

    	/* Initialization of sigmas */
    	if (startsigma != null) { // 
    		if (startsigma.length == 1) {
    			sigma = startsigma[0];
    		} else if (startsigma.length == N) {
    			sigma = math.max(startsigma);
    			if (sigma <= 0)
    				error("initial standard deviation sigma must be positive");
    			for (i = 0; i < N; ++i) {
    				diagD[i] = startsigma[i]/sigma;
    			}
    		} else
    			assert false;
    	} else {
    		// we might use boundaries here to find startsigma, but I prefer to have stddevs mandatory 
    		error("no initial standard deviation specified, use setInitialStandardDeviations()");
    		sigma = 0.5;
    	}

    	if (sigma <= 0 || math.min(diagD) <= 0) {
    		error("initial standard deviations not specified or non-positive, " + 
    		"use setInitialStandarddeviations()"); 
    		sigma = 1;
    	}
    	/* save initial standard deviation */
    	if (startsigma == null || startsigma.length == 1) { 
    		startsigma = new double[N];
    		for (i = 0; i < N; ++i) {
    			startsigma[i] = sigma * diagD[i];
    		}
    	}
    	maxstartsigma = math.max(startsigma);
    	minstartsigma = math.min(startsigma);
    	axisratio = maxstartsigma / minstartsigma; // axis parallel distribution

    	/* expand typicalX, might still be null afterwards */
    	typicalX = expandToDimension(typicalX, N);

    	/* Initialization of xmean */
    	xmean = expandToDimension(xmean, N);
    	if (xmean == null) { 
    		/* set via typicalX */
    		if (typicalX != null) {
    			xmean = typicalX.clone();
    			for (i = 0; i < N; ++i)
    				xmean[i] += sigma*diagD[i] * rand.nextGaussian();
    			/* set via boundaries, is depriciated */
    		} else if (math.max(UBound) < Double.MAX_VALUE
    				&& math.min(LBound) > -Double.MAX_VALUE) {
    			error("no initial search point (solution) X or typical X specified");
    			xmean = new double[N];
    			for (i = 0; i < N; ++i) { /* TODO: reconsider this algorithm to set X0 */
    				double offset = sigma*diagD[i];
    				double range = (UBound[i] - LBound[i] - 2*sigma*diagD[i]); 
    				if (offset > 0.4 * (UBound[i] - LBound[i])) {
    					offset = 0.4 * (UBound[i] - LBound[i]);
    					range = 0.2 * (UBound[i] - LBound[i]);
    				}
    				xmean[i] = LBound[i] + offset + rand.nextDouble() * range;
    			}
    		} else {
    			error("no initial search point (solution) X or typical X specified");
    			xmean = new double[N];
    			for (i = 0; i < N; ++i)
    				xmean[i] = rand.nextDouble();
    		}
    	}

    	assert xmean != null;
    	assert sigma > 0; 
    	
    	/* interpret missing option value */
    	if (options.diagonalCovarianceMatrix < 0) // necessary for hello world message
    		options.diagonalCovarianceMatrix = 1 * 150 * N / sp.lambda; // cave: duplication below
    	
    	/* non-settable parameters */
    	pc = new double[N];
    	ps = new double[N];
    	B = new double[N][N];
    	C = new double[N][N]; // essentially only i <= j part is used

    	xold = new double[N];
    	BDz = new double[N];
    	bestever_x = xmean.clone();
    	// bestever = new CMASolution(xmean);
    	artmp = new double[N];


    	fit.deltaFitHist = new double[5];
    	fit.idxDeltaFitHist = -1;
    	for (i = 0; i < fit.deltaFitHist.length; ++i)
    		fit.deltaFitHist[i] = 1.;

    	// code to be duplicated in reSizeLambda
    	fit.fitness = new IntDouble[sp.getLambda()];   // including penalties, used yet
    	fit.raw = new IntDouble[sp.getLambda()];       // raw function values
    	fit.history = new double[10+30*N/sp.getLambda()];	

    	arx = new double[sp.getLambda()][N];
    	population = new double[sp.getLambda()][N];

    	for (i = 0; i < sp.getLambda(); ++i) {
    		fit.fitness[i] = new IntDouble();
    		fit.raw[i] = new IntDouble();
    	}

    	// initialization
    	for (i = 0; i < N; ++i) {
    		pc[i] = 0;
    		ps[i] = 0;
    		for (int j = 0; j < N; ++j) {
    			B[i][j] = 0;
    		}
    		for (int j = 0; j < i; ++j) {
    			C[i][j] = 0;
    		}
    		B[i][i] = 1;
    		C[i][i] = diagD[i] * diagD[i];
    	}
    	maxsqrtdiagC = Math.sqrt(math.max(math.diag(C)));
    	minsqrtdiagC = Math.sqrt(math.min(math.diag(C)));
    	countCupdatesSinceEigenupdate = 0;
    	iniphase = false; // obsolete

    	/* Some consistency check */
    	for (i = 0; i < N; ++i) {
    		if (LBound[i] > UBound[i])
    			error("lower bound is greater than upper bound");
    		if (typicalX != null) {
    			if (LBound[i] > typicalX[i])
    				error("lower bound '" + LBound[i] + "'is greater than typicalX" + typicalX[i]);
    			if (UBound[i] < typicalX[i])
    				error("upper bound '" + UBound[i] + "' is smaller than typicalX " + typicalX[i]);
    		}
    	}
    	String[] s = stopConditions.getMessages();
    	if(!s[0].equals(""))
    		warning("termination condition satisfied at initialization: \n  " + s[0]);

    	initialX = xmean.clone(); // keep finally chosen initialX
    	
    	timings.start = System.currentTimeMillis();
    	timings.starteigen = System.currentTimeMillis();

    	state = 0;
    	if(options.verbosity > -1)
    		printlnHelloWorld();

    	return new double[sp.getLambda()];

    } // init()

    /** get default parameters in new CMAParameters instance, dimension must 
     * have been set before calling getDefaults
     * 
     * @see CMAParameters#getDefaults(int)
     */
    public CMAParameters getParameterDefaults() {
    	return sp.getDefaults(N);
    }

    /** get default parameters in new CMAParameters instance
     * 
     * @see CMAParameters#getDefaults(int)
     */
    public CMAParameters getParameterDefaults(int N) {
    	return sp.getDefaults(N);
    }

    /** reads properties from default
     * input file CMAEvolutionStrategy.properties and
     * sets options and strategy parameter settings
     * accordingly. Options values can be changed at any time using this function. 
     */
    public Properties readProperties() {
    	return readProperties(propertiesFileName);
    }
    Properties properties = new Properties();
    /** reads properties from fileName and sets strategy parameters and options
     * accordingly
     * @param fileName of properties file
     */
    public Properties readProperties(String fileName) {
        this.propertiesFileName = fileName;
//        if (fileName.equals(""))
//            return properties;
        try {
            java.io.FileInputStream fis = new java.io.FileInputStream(fileName);
            properties.load(fis);
            fis.close();
        } 
        catch(java.io.IOException e) { 
            warning("File '" + fileName + "' not found, no options read");
            // e.printStackTrace();
        }
        setFromProperties(properties);
        return properties;
    }

    /** reads properties from Properties class 
     * input and sets options and parameters accordingly
     * 
     * @param properties java.util.Properties key-value hash table
     * @see #readProperties()
     */
    public void setFromProperties(Properties properties) {
        String s;
        
        options.setOptions(properties);
        
        if (state >= 0) // only options can be changed afterwards
            return;     // defaults are already supplemented 

//        if (properties.containsKey("boundaryLower") &&
//                properties.containsKey("boundaryUpper")) {
//            setBoundaries(parseDouble(getAllToken(properties.getProperty("boundaryLower"))), 
//                    parseDouble(getAllToken(properties.getProperty("boundaryUpper"))));
        if ((s = properties.getProperty("typicalX")) != null) {
            setTypicalX(options.parseDouble(options.getAllToken(s))); 
        }
        if ((s = properties.getProperty("initialX")) != null) {
            setInitialX(options.parseDouble(options.getAllToken(s))); 
        }
        if ((s = properties.getProperty("initialStandardDeviations")) != null) {
            setInitialStandardDeviations(options.parseDouble(options.getAllToken(s)));
        }
        if ((s = properties.getProperty("dimension")) != null) { // parseInt does not ignore trailing spaces
            setDimension(Integer.parseInt(options.getFirstToken(s)));
        }
        if ((s = properties.getProperty("randomSeed")) != null) {
            setSeed(Long.parseLong(options.getFirstToken(s)));
        }
        if ((s = properties.getProperty("populationSize")) != null) {
            sp.setPopulationSize(Integer.parseInt(options.getFirstToken(s)));
        }
        if ((s = properties.getProperty("cCov")) != null) {
            sp.setCcov(Double.parseDouble(options.getFirstToken(s)));
        }
        
    }
//    private void infoVerbose(String s) {
//        println(" CMA-ES info: " + s);
//    }
    
    private void warning(String s) {
        println(" CMA-ES warning: " + s);
    }
    private void error(String s) { // somehow a relict from the C history of this code
        println(" CMA-ES error: " + s);
        //e.printStackTrace();            // output goes to System.err
        //e.printStackTrace(System.out);  // send trace to stdout

        throw new CMAException(" CMA-ES error: " + s);
        //      System.exit(-1); 
    }
    
    /** some simple math utilities */
    class MyMath { // implements java.io.Serializable {
        int itest;
        
        double square(double d) {
            return d*d;
        }
        double prod(double []ar) {
            double res = 1.0;
            for(int i = 0; i < ar.length; ++i)
                res *= ar[i];
            return res;
        }
        
        public double median(double ar[]) {
            // need a copy of ar
            double [] ar2 = new double[ar.length];
            for (int i = 0; i < ar.length; ++i)
                ar2[i] = ar[i];
            Arrays.sort(ar2);
            if (ar2.length % 2 == 0)
                return (ar2[ar.length/2] + ar2[ar.length/2-1]) / 2.;
            else    
                return ar2[ar.length/2];
        }
        
        /** @return Maximum value of 1-D double array */
        public double max(double ar[]) {
            int i;
            double m;
            m = ar[0];
            for (i = 1; i < ar.length; ++i) {
                if (m < ar[i])
                    m = ar[i];
            }
            return m;
        }

        /** sqrt(a^2 + b^2) without under/overflow. **/
        public double hypot(double a, double b) {
            double r  = 0;
            if (Math.abs(a) > Math.abs(b)) {
               r = b/a;
               r = Math.abs(a)*Math.sqrt(1+r*r);
            } else if (b != 0) {
               r = a/b;
               r = Math.abs(b)*Math.sqrt(1+r*r);
            }
            return r;
         }
        /** @return index of minium value of 1-D double array */
        public int minidx(double ar[]) {
            return minidx(ar, ar.length-1);
        }
        
        /** @return index of minium value of 1-D double 
         *   array between index 0 and maxidx 
         * @param ar double[] 
         * @param maxidx last index to be considered */
        public int minidx(double[] ar, int maxidx) {
            int i, idx;
            idx = 0;
            for (i = 1; i < maxidx; ++i) {
                if (ar[idx] > ar[i])
                    idx = i;
            }
            return idx;
        }

        /** @return index of minium value of 1-D double 
         *   array between index 0 and maxidx 
         * @param ar double[] 
         * @param maxidx last index to be considered */
        protected int minidx(IntDouble[] ar, int maxidx) {
            int i, idx;
            idx = 0;
            for (i = 1; i < maxidx; ++i) {
                if (ar[idx].val > ar[i].val)
                    idx = i;
            }
            return idx;
        }

        /** @return index of maximum value of 1-D double array */
        public int maxidx(double ar[]) {
            int i, idx;
            idx = 0;
            for (i = 1; i < ar.length; ++i) {
                if (ar[idx] < ar[i])
                    idx = i;
            }
            return idx;
        }
        /** @return Minimum value of 1-D double array */
        public double min(double ar[]) {
            int i;
            double m;
            m = ar[0];
            for (i = 1; i < ar.length; ++i) {
                if (m > ar[i])
                    m = ar[i];
            }
            return m;
        }
        
        /** @return Maximum value of 1-D Object array where the object implements Comparator 
         *    Example: max(Double arx, arx[0]) */
        public Double max(Double ar[], Comparator<Double> c) {
            int i;
            Double m;
            m = ar[0];
            for (i = 1; i < ar.length; ++i) {
                if (c.compare(m, ar[i]) > 0)
                    m = ar[i];
            }
            return m;
        }
        
        /** @return Maximum value of 1-D IntDouble array */
        public IntDouble max(IntDouble ar[]) {
            int i;
            IntDouble m;
            m = ar[0];
            for (i = 1; i < ar.length; ++i) {
                if (m.compare(m, ar[i]) < 0)
                    m = ar[i];
            }
            return m;
        }
        
        /** @return Minimum value of 1-D IntDouble array */
        public IntDouble min(IntDouble ar[]) {
            int i;
            IntDouble m;
            m = ar[0];
            for (i = 1; i < ar.length; ++i) {
                if (m.compare(m, ar[i]) > 0)
                    m = ar[i];
            }
            return m;
        }
        
        /** @return Minimum value of 1-D Object array defining a Comparator */
        public Double min(Double ar[], Comparator<Double> c) {
            int i;
            Double m;
            m = ar[0];
            for (i = 1; i < ar.length; ++i) {
                if (c.compare(m, ar[i]) < 0)
                    m = ar[i];
            }
            return m;
        }
        
        /**
         * @return Diagonal of an 2-D double array
         */
        public double[] diag(double ar[][]) {
            int i;
            double[] diag = new double[ar.length];
            for (i = 0; i < ar.length && i < ar[i].length; ++i)
                diag[i] = ar[i][i];
            return diag;
        }
        
        /**
         * @return 1-D double array of absolute values of an 1-D double array
         */
        public double[] abs(double v[]) {
            double res[] = new double[v.length];
            for(int i = 0; i < v.length; ++i)
                res[i] = Math.abs(v[i]);
            return res;
        }
    } // MyMath
    
    class Timing {
        Timing(){
            birth = System.currentTimeMillis();
            start = birth; // on the save side 
        }
        long birth; // time at construction, not really in use
        long start; // time at end of init()
        long starteigen; // time after flgdiag was turned off, ie when calls to eigen() start
        long eigendecomposition = 0; // spent time in eigendecomposition
        long writedefaultfiles = 0;        // spent time in writeToDefaultFiles
    }
    Timing timings = new Timing();
    
    /* flgforce == 1 force independent of time measurments, 
     * flgforce == 2 force independent of uptodate-status
     */
    void eigendecomposition(int flgforce) {
        /* Update B and D, calculate eigendecomposition */
        int i, j;
        
        if (countCupdatesSinceEigenupdate == 0 && flgforce < 2)
        	return;

    	//           20% is usually better in terms of running *time* (only on fast to evaluate functions)
    	if (!flgdiag && flgforce <= 0 && 
    			(timings.eigendecomposition > 1000 + options.maxTimeFractionForEigendecomposition 
    					* (System.currentTimeMillis() - timings.starteigen)
    					|| countCupdatesSinceEigenupdate < 1. / sp.getCcov() / N / 5.)) 
    		return;

    	if (flgdiag) {
    		for (i = 0; i < N; ++i) {
    			diagD[i] = Math.sqrt(C[i][i]);
    		}
    		countCupdatesSinceEigenupdate = 0;
        	timings.starteigen = System.currentTimeMillis(); // reset starting time
        	timings.eigendecomposition = 0;             // not really necessary
    	} else {
    		// set B <- C
    		for (i = 0; i < N; ++i)
    			for (j = 0; j <= i; ++j)
    				B[i][j] = B[j][i] = C[i][j];

    		// eigendecomposition
    		double [] offdiag = new double[N];
    		long firsttime = System.currentTimeMillis();
    		tred2(N, B, diagD, offdiag);
    		tql2(N, diagD, offdiag, B);
    		timings.eigendecomposition += System.currentTimeMillis() - firsttime;

    		if (options.checkEigenSystem > 0)
    			checkEigenSystem( N,  C, diagD, B); // for debugging 

    		// assign diagD to eigenvalue square roots
    		for (i = 0; i < N; ++i) {
    			if (diagD[i] < 0) // numerical problem?
    				error("an eigenvalue has become negative");
    			diagD[i] = Math.sqrt(diagD[i]);
    		}
    		countCupdatesSinceEigenupdate = 0;
    	} // end Update B and D
    	if (math.min(diagD) == 0) // error management is done elsewhere
    		axisratio = Double.POSITIVE_INFINITY;
    	else
    		axisratio = math.max(diagD) / math.min(diagD);

    } // eigendecomposition


    /* ========================================================= */
    int
    checkEigenSystem( int N,  double C[][], double diag[], double Q[][]) 
    /* 
       exhaustive test of the output of the eigendecomposition
       needs O(n^3) operations 

       produces error  
       returns number of detected inaccuracies 
    */
    {
        /* compute Q diag Q^T and Q Q^T to check */
      int i, j, k, res = 0;
      double cc, dd; 
      String s;

      for (i=0; i < N; ++i)
    	  for (j=0; j < N; ++j) {
    		  for (cc=0.,dd=0., k=0; k < N; ++k) {
    			  cc += diag[k] * Q[i][k] * Q[j][k];
    			  dd += Q[i][k] * Q[j][k];
    		  }
    		  /* check here, is the normalization the right one? */
    		  if (Math.abs(cc - C[i>j?i:j][i>j?j:i])/Math.sqrt(C[i][i]*C[j][j]) > 1e-10 
    				  && Math.abs(cc - C[i>j?i:j][i>j?j:i]) > 1e-9) { /* quite large */
    			  s = " " + i + " " + j + " " + cc + " " + C[i>j?i:j][i>j?j:i] + " " + (cc-C[i>j?i:j][i>j?j:i]);
    			  warning("cmaes_t:Eigen(): imprecise result detected " + s);
    			  ++res; 
    		  }
    		  if (Math.abs(dd - (i==j?1:0)) > 1e-10) {
    			  s = i + " " + j + " " + dd;
    			  warning("cmaes_t:Eigen(): imprecise result detected (Q not orthog.) " + s);
    			  ++res;
    		  }
    	  }
      return res; 
    }


    
    // Symmetric Householder reduction to tridiagonal form, taken from JAMA package.

    private void tred2 (int n, double V[][], double d[], double e[]) {

    //  This is derived from the Algol procedures tred2 by
    //  Bowdler, Martin, Reinsch, and Wilkinson, Handbook for
    //  Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
    //  Fortran subroutine in EISPACK.

       for (int j = 0; j < n; j++) {
          d[j] = V[n-1][j];
       }

       // Householder reduction to tridiagonal form.
    
       for (int i = n-1; i > 0; i--) {
    
          // Scale to avoid under/overflow.
    
          double scale = 0.0;
          double h = 0.0;
          for (int k = 0; k < i; k++) {
             scale = scale + Math.abs(d[k]);
          }
          if (scale == 0.0) {
             e[i] = d[i-1];
             for (int j = 0; j < i; j++) {
                d[j] = V[i-1][j];
                V[i][j] = 0.0;
                V[j][i] = 0.0;
             }
          } else {
    
             // Generate Householder vector.
    
             for (int k = 0; k < i; k++) {
                d[k] /= scale;
                h += d[k] * d[k];
             }
             double f = d[i-1];
             double g = Math.sqrt(h);
             if (f > 0) {
                g = -g;
             }
             e[i] = scale * g;
             h = h - f * g;
             d[i-1] = f - g;
             for (int j = 0; j < i; j++) {
                e[j] = 0.0;
             }
    
             // Apply similarity transformation to remaining columns.
    
             for (int j = 0; j < i; j++) {
                f = d[j];
                V[j][i] = f;
                g = e[j] + V[j][j] * f;
                for (int k = j+1; k <= i-1; k++) {
                   g += V[k][j] * d[k];
                   e[k] += V[k][j] * f;
                }
                e[j] = g;
             }
             f = 0.0;
             for (int j = 0; j < i; j++) {
                e[j] /= h;
                f += e[j] * d[j];
             }
             double hh = f / (h + h);
             for (int j = 0; j < i; j++) {
                e[j] -= hh * d[j];
             }
             for (int j = 0; j < i; j++) {
                f = d[j];
                g = e[j];
                for (int k = j; k <= i-1; k++) {
                   V[k][j] -= (f * e[k] + g * d[k]);
                }
                d[j] = V[i-1][j];
                V[i][j] = 0.0;
             }
          }
          d[i] = h;
       }
    
       // Accumulate transformations.
    
       for (int i = 0; i < n-1; i++) {
          V[n-1][i] = V[i][i];
          V[i][i] = 1.0;
          double h = d[i+1];
          if (h != 0.0) {
             for (int k = 0; k <= i; k++) {
                d[k] = V[k][i+1] / h;
             }
             for (int j = 0; j <= i; j++) {
                double g = 0.0;
                for (int k = 0; k <= i; k++) {
                   g += V[k][i+1] * V[k][j];
                }
                for (int k = 0; k <= i; k++) {
                   V[k][j] -= g * d[k];
                }
             }
          }
          for (int k = 0; k <= i; k++) {
             V[k][i+1] = 0.0;
          }
       }
       for (int j = 0; j < n; j++) {
          d[j] = V[n-1][j];
          V[n-1][j] = 0.0;
       }
       V[n-1][n-1] = 1.0;
       e[0] = 0.0;
    } 

    // Symmetric tridiagonal QL algorithm, taken from JAMA package.
    
    private void tql2 (int n, double d[], double e[], double V[][]) {

    //  This is derived from the Algol procedures tql2, by
    //  Bowdler, Martin, Reinsch, and Wilkinson, Handbook for
    //  Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
    //  Fortran subroutine in EISPACK.
    
       for (int i = 1; i < n; i++) {
          e[i-1] = e[i];
       }
       e[n-1] = 0.0;
    
       double f = 0.0;
       double tst1 = 0.0;
       double eps = Math.pow(2.0,-52.0);
       for (int l = 0; l < n; l++) {

          // Find small subdiagonal element
    
          tst1 = Math.max(tst1,Math.abs(d[l]) + Math.abs(e[l]));
          int m = l;
          while (m < n) {
             if (Math.abs(e[m]) <= eps*tst1) {
                break;
             }
             m++;
          }

          // If m == l, d[l] is an eigenvalue,
          // otherwise, iterate.
    
          if (m > l) {
             int iter = 0;
             do {
                iter = iter + 1;  // (Could check iteration count here.)
    
                // Compute implicit shift
    
                double g = d[l];
                double p = (d[l+1] - g) / (2.0 * e[l]);
                double r = math.hypot(p,1.0);
                if (p < 0) {
                   r = -r;
                }
                d[l] = e[l] / (p + r);
                d[l+1] = e[l] * (p + r);
                double dl1 = d[l+1];
                double h = g - d[l];
                for (int i = l+2; i < n; i++) {
                   d[i] -= h;
                }
                f = f + h;
    
                // Implicit QL transformation.
    
                p = d[m];
                double c = 1.0;
                double c2 = c;
                double c3 = c;
                double el1 = e[l+1];
                double s = 0.0;
                double s2 = 0.0;
                for (int i = m-1; i >= l; i--) {
                   c3 = c2;
                   c2 = c;
                   s2 = s;
                   g = c * e[i];
                   h = c * p;
                   r = math.hypot(p,e[i]);
                   e[i+1] = s * r;
                   s = e[i] / r;
                   c = p / r;
                   p = c * d[i] - s * g;
                   d[i+1] = h + s * (c * g + s * d[i]);
    
                   // Accumulate transformation.
    
                   for (int k = 0; k < n; k++) {
                      h = V[k][i+1];
                      V[k][i+1] = s * V[k][i] + c * h;
                      V[k][i] = c * V[k][i] - s * h;
                   }
                }
                p = -s * s2 * c3 * el1 * e[l] / dl1;
                e[l] = s * p;
                d[l] = c * p;
    
                // Check for convergence.
    
             } while (Math.abs(e[l]) > eps*tst1);
          }
          d[l] = d[l] + f;
          e[l] = 0.0;
       }
      
       // Sort eigenvalues and corresponding vectors.
    
       for (int i = 0; i < n-1; i++) {
          int k = i;
          double p = d[i];
          for (int j = i+1; j < n; j++) {
             if (d[j] < p) { // NH find smallest k>i
                k = j;
                p = d[j];
             }
          }
          if (k != i) {
             d[k] = d[i]; // swap k and i 
             d[i] = p;   
             for (int j = 0; j < n; j++) {
                p = V[j][i];
                V[j][i] = V[j][k];
                V[j][k] = p;
             }
          }
       }
    } // tql2

    /** not really in use so far, just clones and copies
     * 
     * @param popx genotype
     * @param popy phenotype, repaired
     * @return popy
     */
    double[][] genoPhenoTransformation(double[][] popx, double[][] popy) {
    	if (popy == null || popy == popx || popy.length != popx.length) 
    		popy = new double[popx.length][];
    	
    	for (int i = 0; i < popy.length; ++i)
    		popy[i] = genoPhenoTransformation(popx[i], popy[i]);
    	
    	return popy;
    }
    /** not really in use so far, just clones and copies
     * 
     * @param popx genotype
     * @param popy phenotype, repaired
     * @return popy
     */
    double[][] phenoGenoTransformation(double[][] popx, double[][] popy) {
    	if (popy == null || popy == popx || popy.length != popx.length) 
    		popy = new double[popx.length][];
    	
    	for (int i = 0; i < popy.length; ++i)
    		popy[i] = phenoGenoTransformation(popx[i], popy[i]);
    	
    	return popy;
    }

    /** not really in use so far, just clones and copies
     * 
     * @param x genotype
     * @param y phenotype
     * @return y
     */
    double[] genoPhenoTransformation(double[] x, double[] y) {
    	if (y == null || y == x || y.length != x.length) {
    		y = x.clone();
    		return y; // for now return an identical copy
    	}
    	for(int i = 0; i < N; ++i)
    		y[i] = x[i];
    	return y;		
    }
    /** not really in use so far, just clones and copies
     * 
     * @param x genotype
     * @param y phenotype
     * @return y
     */
    double[] phenoGenoTransformation(double[] x, double[] y) {
    	if (y == null || y == x || y.length != x.length) {
    		y = x.clone();
    		return y; // for now return an identical copy
    	}
    	for(int i = 0; i < N; ++i)
    		y[i] = x[i];
    	return y;		
    }
    
    /**
     * Samples the recent search distribution lambda times
     * @return double[][] population, lambda times dimension array of sampled solutions, 
     *   where <code>lambda == parameters.getPopulationSize()</code> 
     * @see #resampleSingle(int)
     * @see #updateDistribution(double[])
     * @see CMAParameters#getPopulationSize()
     */
    public double[][] samplePopulation() {
        int i, j, iNk;
        double sum;

        if (state < 0)
            init();
        else if (state < 3 && state > 2)
                error("mixing of calls to updateSingle() and samplePopulation() is not possible");
        else    
            eigendecomposition(0); // latest possibility to generate B and diagD
        
        if (state != 1)
            ++countiter; 
        state = 1; // can be repeatedly called without problem
        idxRecentOffspring = sp.getLambda() - 1; // not really necessary at the moment

        
        // ensure maximal and minimal standard deviations
        if (options.lowerStandardDeviations != null && options.lowerStandardDeviations.length > 0)
            for (i = 0; i < N; ++i) {
                double d = options.lowerStandardDeviations[Math.min(i,options.lowerStandardDeviations.length-1)]; 
                if(d > sigma * minsqrtdiagC) 
                    sigma = d / minsqrtdiagC;
            }
        if (options.upperStandardDeviations != null && options.upperStandardDeviations.length > 0)
            for (i = 0; i < N; ++i) {
                double d = options.upperStandardDeviations[Math.min(i,options.upperStandardDeviations.length-1)]; 
                if (d < sigma * maxsqrtdiagC) 
                    sigma = d / maxsqrtdiagC;
            }
        
        testAndCorrectNumerics();
        
        /* sample the distribution */
        for (iNk = 0; iNk < sp.getLambda(); ++iNk) { /*
            * generate scaled
            * random vector (D * z)
            */

            // code duplication from resampleSingle because of possible future resampling before GenoPheno
            /* generate scaled random vector (D * z) */
        	if (flgdiag)  
        	    for (i = 0; i < N; ++i)
        			arx[iNk][i] = xmean[i] + sigma * diagD[i] * rand.nextGaussian();
        	else {
                for (i = 0; i < N; ++i) 
        		    artmp[i] = diagD[i] * rand.nextGaussian();
        	
        	    /* add mutation (sigma * B * (D*z)) */
        	    for (i = 0; i < N; ++i) {
        			for (j = 0, sum = 0; j < N; ++j)
        				sum += B[i][j] * artmp[j];
        			arx[iNk][i] = xmean[i] + sigma * sum;
        		}
            }
            // redo this while isOutOfBounds(arx[iNk])
        }

        // I am desperately missing a const/readonly/visible qualifier. 
        return population = genoPhenoTransformation(arx, population);
        
    } // end samplePopulation()

    /** re-generate the <code>index</code>-th solution. After getting lambda
     * solution points with samplePopulation() the i-th point,
     * i=0...lambda-1, can be sampled anew by resampleSingle(i). 
     * 
     * <PRE>
     * double[][] pop = cma.samplePopulation();
     * // check some stuff, i-th solution went wrong, therefore
     * pop[i] = cma.resampleSingle(i); // assignment to keep the population consistent
     * for (i = 0,...)
     *   fitness[i] = fitfun.valueof(pop[i]);
     * </PRE>
     *
     * @see #samplePopulation()
     */
    public double[] resampleSingle(int index) {
        int i,j;
        double sum;
        if (state != 1)
            error("call samplePopulation before calling resampleSingle(int index)");
        
        /* sample the distribution */
        /* generate scaled random vector (D * z) */
        if (flgdiag)
            for (i = 0; i < N; ++i)
        		arx[index][i] = xmean[i] + sigma * diagD[i] * rand.nextGaussian();
        else {
            for (i = 0; i < N; ++i) 
                artmp[i] = diagD[i] * rand.nextGaussian();
        	
        	/* add mutation (sigma * B * (D*z)) */
        	for (i = 0; i < N; ++i) {
        		for (j = 0, sum = 0; j < N; ++j)
        			sum += B[i][j] * artmp[j];
        		arx[index][i] = xmean[i] + sigma * sum;
        	}
        }
        return population[index] = genoPhenoTransformation(arx[index], population[index]); 
    } // resampleSingle
    
    /** compute Mahalanobis norm of x - mean w.r.t. the current distribution 
     * (using covariance matrix times squared step-size for the inner product). 
     * TODO: to be tested. 
     * @param x
     * @param mean
     * @return Malanobis norm of x - mean: sqrt((x-mean)' C^-1 (x-mean)) / sigma
     */
    public double mahalanobisNorm(double[] x, double[] mean) {
    	double yi, snorm = 0;
    	int i, j; 
    	// snorm = (x-mean)' Cinverse (x-mean) = (x-mean)' (BD^2B')^-1 (x-mean)
    	//       = (x-mean)' B'^-1 D^-2 B^-1 (x-mean) 
    	//       = (x-mean)' B D^-1 D^-1 B' (x-mean)
    	//       = (D^-1 B' (x-mean))' * (D^-1 B' (x-mean))
    	/* calculate z := D^(-1) * B^(-1) * BDz into artmp, we could have stored z instead */
    	for (i = 0; i < N; ++i) {
    		for (j = 0, yi = 0.; j < N; ++j)
    			yi += B[j][i] * (x[j]-mean[j]);
    		// yi = i-th component of B' (x-mean)
    		snorm += yi * yi / diagD[i] / diagD[i];
    	}
    	return Math.sqrt(snorm) / sigma;
    }

	/** update of the search distribution from a population and its 
	 * function values, see {@link #updateDistribution(double[][], double[], 0)}. 
	 * This might become updateDistribution(double[][], double[], popsize)
     * in future. 
     * 
     * @param population  double[lambda][N], lambda solutions
     * @param functionValues  double[lambda], respective objective values of population
     * 
     * @see #samplePopulation()
     * @see #updateDistribution(double[])
     * @see #updateDistribution(double[][], double[], int)
     */
    public void updateDistribution(double[][] population, double[] functionValues) {
    	updateDistribution(population, functionValues, 0);
    }
    
	/** update of the search distribution from a population and its 
	 * function values, an alternative interface for
     * {@link #updateDistribution(double[] functionValues)}. functionValues is used to establish an 
     * ordering of the elements in population. The first nInjected elements do not need to originate 
     * from #samplePopulation() or can have been modified (TODO: to be tested). 
     * 
     * @param population  double[lambda][N], lambda solutions
     * @param functionValues  double[lambda], respective objective values of population
     * @param nInjected  int, first nInjected solutions of population were not sampled by 
     * samplePopulation() or modified afterwards
     * 
     * @see #samplePopulation()
     * @see #updateDistribution(double[])
     */
    public void updateDistribution(double[][] population, double[] functionValues, int nInjected) {
   	    // TODO: Needs to be tested yet for nInjected > 0
    	// pass first input argument
    	arx = phenoGenoTransformation(population, null); // TODO should still be tested
    	for (int i = 0; i < nInjected; ++i) {
    		warning("TODO: checking of injected solution has not yet been tested");
            // if (mahalanobisNorm(arx[0], xmean) > Math.sqrt(N) + 2) // testing: seems fine
            //     System.out.println(mahalanobisNorm(arx[i], xmean)/Math.sqrt(N));
    		double upperLength = Math.sqrt(N) + 2. * N / (N+2.);  // should become an interfaced parameter? 
    		double fac = upperLength / mahalanobisNorm(arx[i], xmean); 
    		if (fac < 1)
    			for (int j = 0; j < N; ++j)
    				arx[i][j] = xmean[j] + fac * (arx[i][j] - xmean[j]);
    	}
    	updateDistribution(functionValues);
    }
    
    /** update of the search distribution after samplePopulation(). functionValues 
     * determines the selection order (ranking) for the solutions in the previously sampled 
     * population. This is just a different interface for updateDistribution(double[][], double[]).  
     * @see #samplePopulation()
     * @see #updateDistribution(double[][], double[])
     */
    public void updateDistribution(double[] functionValues) {
        if (state == 3) {
            error("updateDistribution() was already called");
        }
        if (functionValues.length != sp.getLambda())
        	error("argument double[] funcionValues.length=" + functionValues.length 
        			+ "!=" + "lambda=" + sp.getLambda());
        
        /* pass input argument */
        for (int i = 0; i < sp.getLambda(); ++i) {
            fit.raw[i].val = functionValues[i];
            fit.raw[i].i = i;
        }
        
        counteval += sp.getLambda();
        recentFunctionValue = math.min(fit.raw).val;
        recentMaxFunctionValue = math.max(fit.raw).val;
        recentMinFunctionValue = math.min(fit.raw).val;
        updateDistribution();
    }
    
//    private IntDouble[] computePenalties() {
//    	int i, j, iNk;
//    	/* penalize repairment, eg. for boundaries */
//    	// TODO: figure out whether the change of penalty is too large or fast which can disturb selection
//    	//       this depence in particular on the length of fit.medianDeltaFit
//    	if (true || countiter < fit.deltaFitHist.length || countiter % 1*(N+2) == 0) {
//    		// minimum of differences with distance lambda/2, better the 25%tile?
//    		// assumes sorted array!! 
//    		int ii = (sp.getLambda()) / 2;
//    		double medianDeltaFit = Math.abs(fit.funValues[ii].d - fit.funValues[0].d);
//    		for (i = 1; i + ii < sp.getLambda(); ++i) 
//    			// minimum because of outliers 
//    			medianDeltaFit = Math.min(medianDeltaFit, Math.abs(fit.funValues[ii+i].d - fit.funValues[i].d));
//    		medianDeltaFit /= sigma * sigma; // should be somehow constant, because dfit depends on sigma (verified on sphere)
//    		if (medianDeltaFit > 0) {
////  			System.out.println("set" + medianDeltaFit + " " + math.median(fit.medianDeltaFit));
//    			if (fit.idxDeltaFitHist == -1) // first time: write all fields
//    				for (i = 0; i < fit.deltaFitHist.length; ++i)
//    					fit.deltaFitHist[i] = medianDeltaFit;
//    			if (++fit.idxDeltaFitHist == fit.deltaFitHist.length)
//    				fit.idxDeltaFitHist = 0;
//    			// save last five values in fit.medianDeltaFit
//    			fit.deltaFitHist[fit.idxDeltaFitHist] = medianDeltaFit;
//    		}                
//    	}
//    	/* calculate fitness by adding function value and repair penalty */
//    	double penfactor = 1. * 5. * math.median(fit.deltaFitHist);
//    	for (iNk = 0; iNk < sp.getLambda(); ++iNk) {
//    		double sqrnorm = 0;
//    		double prod = Math.pow(math.prod(diagD), 1.0/(double)N);
//    		/* calculate C^-1-norm of Delta x: norm(D^(-1) * B^(-1) * (Delta x))^2 */
//    		for (i = 0; i < N; ++i) {
//    			double sum = 0.0;
//    			for (j = 0, sum = 0.; j < N; ++j)
//    				sum += B[j][i] * ((arxrepaired[fit.funValues[iNk].i][j] - arx[fit.funValues[iNk].i][j]));
//    			sqrnorm += math.square(sum / (Math.pow(diagD[i], 0.9) * Math.pow(prod, 0.10))); // regularization to I
//    		}
//    		// sqrnorm/N equals approximately 1/sigma^2
//    		fit.fitness[iNk].d = fit.funValues[iNk].d + penfactor * sqrnorm / (N+2); // / (sigma * sigma);
//    		fit.fitness[iNk].i = fit.funValues[iNk].i;
//    		// System.out.println(math.median(fit.medianDeltaFit) + " " + sqrnorm / (N+2)); // / (sigma * sigma));
//    	}
////  	if (countiter % 10 == 1)
////  	System.out.println(math.median(fit.medianDeltaFit) + " " + sqrnorm);
//    	return fit.fitness;
//
//    }

    private void updateDistribution() {
        
        int i, j, k, iNk, hsig;
        double sum;
        double psxps;
        
        if (state == 3) {
            error("updateDistribution() was already called");
        }
        
        /* sort function values */
        Arrays.sort(fit.raw, fit.raw[0]);
        
        for (iNk = 0; iNk < sp.getLambda(); ++iNk) {
        	fit.fitness[iNk].val = fit.raw[iNk].val; // superfluous at time
        	fit.fitness[iNk].i = fit.raw[iNk].i;
        }

        /* update fitness history */ 
        for (i = fit.history.length - 1; i > 0; --i)
            fit.history[i] = fit.history[i - 1];
        fit.history[0] = fit.raw[0].val;
        
        /* save/update bestever-value */
        updateBestEver(arx[fit.raw[0].i], fit.raw[0].val, 
        		counteval - sp.getLambda() + fit.raw[0].i + 1);
        
        /* re-calculate diagonal flag */
        flgdiag = (options.diagonalCovarianceMatrix == 1 || options.diagonalCovarianceMatrix >= countiter); 
        if (options.diagonalCovarianceMatrix == -1) // options might have been re-read
        	flgdiag = (countiter <= 1 * 150 * N / sp.lambda);  // CAVE: duplication of "default"
        
        /* calculate xmean and BDz~N(0,C) */
        for (i = 0; i < N; ++i) {
            xold[i] = xmean[i];
            xmean[i] = 0.;
            for (iNk = 0; iNk < sp.getMu(); ++iNk)
                xmean[i] += sp.getWeights()[iNk] * arx[fit.fitness[iNk].i][i];
            BDz[i] = Math.sqrt(sp.getMueff()) * (xmean[i] - xold[i]) / sigma;
        }
        
        /* cumulation for sigma (ps) using B*z */
        if (flgdiag) {
        	/* given B=I we have B*z = z = D^-1 BDz  */
        	for (i = 0; i < N; ++i) {
        		ps[i] = (1. - sp.getCs()) * ps[i]
        		                               + Math.sqrt(sp.getCs() * (2. - sp.getCs())) 
        		                               * BDz[i] / diagD[i];
        	}
        } else {
        	/* calculate z := D^(-1) * B^(-1) * BDz into artmp, we could have stored z instead */
        	for (i = 0; i < N; ++i) {
        		for (j = 0, sum = 0.; j < N; ++j)
        			sum += B[j][i] * BDz[j];
        		artmp[i] = sum / diagD[i];
        	}
        	/* cumulation for sigma (ps) using B*z */
        	for (i = 0; i < N; ++i) {
        		for (j = 0, sum = 0.; j < N; ++j)
        			sum += B[i][j] * artmp[j];
        		ps[i] = (1. - sp.getCs()) * ps[i]
        		                               + Math.sqrt(sp.getCs() * (2. - sp.getCs())) * sum;
        	}
        }

        /* calculate norm(ps)^2 */
        psxps = 0;
        for (i = 0; i < N; ++i)
            psxps += ps[i] * ps[i];
        
        /* cumulation for covariance matrix (pc) using B*D*z~N(0,C) */
        hsig = 0;
        if (Math.sqrt(psxps)
                / Math.sqrt(1. - Math.pow(1. - sp.getCs(), 2. * countiter))
                / sp.chiN < 1.4 + 2. / (N + 1.)) {
            hsig = 1;
        }
        for (i = 0; i < N; ++i) {
            pc[i] = (1. - sp.getCc()) * pc[i] + hsig
            * Math.sqrt(sp.getCc() * (2. - sp.getCc())) * BDz[i];
        }
        
        /* stop initial phase, not in use anymore as hsig does the job */
        if (iniphase
        		&& countiter > Math.min(1 / sp.getCs(), 1 + N / sp.getMucov()))
        	if (psxps / sp.getDamps()
        			/ (1. - Math.pow((1. - sp.getCs()), countiter)) < N * 1.05)
        		iniphase = false;

        /* this, it is harmful in a dynamic environment
         * remove momentum in ps, if ps is large and fitness is getting worse */
//        if (1 < 3 && psxps / N > 1.5 + 10 * Math.sqrt(2. / N)
//        		&& fit.history[0] > fit.history[1] && fit.history[0] > fit.history[2]) {
//          double tfac;
// 
//        	infoVerbose(countiter + ": remove momentum " + psxps / N + " "
//        			+ ps[0] + " " + sigma);
//
//        	tfac = Math.sqrt((1 + Math.max(0, Math.log(psxps / N))) * N / psxps);
//        	for (i = 0; i < N; ++i)
//        		ps[i] *= tfac;
//        	psxps *= tfac * tfac;
//        }

        /* update of C */
        if (sp.getCcov() > 0 && iniphase == false) {
            
            ++countCupdatesSinceEigenupdate;
            
            /* update covariance matrix */
            for (i = 0; i < N; ++i)
                for (j = (flgdiag ? i : 0); 
                     j <= i; ++j) {
                    C[i][j] = (1 - sp.getCcov(flgdiag))
                    * C[i][j]
                           + sp.getCcov()
                           * (1. / sp.getMucov())
                           * (pc[i] * pc[j] + (1 - hsig) * sp.getCc()
                                   * (2. - sp.getCc()) * C[i][j]);
                    for (k = 0; k < sp.getMu(); ++k) { /*
                    * additional rank mu
                    * update
                    */
                        C[i][j] += sp.getCcov() * (1 - 1. / sp.getMucov())
                        * sp.getWeights()[k]
                                          * (arx[fit.fitness[k].i][i] - xold[i])
                                          * (arx[fit.fitness[k].i][j] - xold[j]) / sigma
                                          / sigma;
                    }
                }
            maxsqrtdiagC = Math.sqrt(math.max(math.diag(C)));
            minsqrtdiagC = Math.sqrt(math.min(math.diag(C)));
        } // update of C
        
        /* update of sigma */
        sigma *= Math.exp(((Math.sqrt(psxps) / sp.chiN) - 1) * sp.getCs()
                / sp.getDamps());
        
        state = 3;
        
    } // updateDistribution()
    
    /** assigns lhs to a different instance with the same values, 
     * sort of smart clone, but it may be that clone is as smart already 
     * 
     * @param rhs
     * @param lhs
     * @return
     */
    double[] assignNew(double[] rhs, double[] lhs) {
    	assert rhs != null; // will produce an error anyway
    	if(lhs != null && lhs != rhs && lhs.length == rhs.length)
    		for(int i = 0; i < lhs.length; ++i)
    			lhs[i] = rhs[i];
    	else
    		lhs = rhs.clone();
    	return lhs;
    }
    void updateBestEver(double[] x, double fitness, long eval) {
        if (fitness < bestever_fit || Double.isNaN(bestever_fit)) {  // countiter == 1 not needed anymore
            bestever_fit = fitness;
            bestever_eval = eval;
            bestever_x = assignNew(x, bestever_x); // save (hopefully) efficient assignment
        }
    }
    
    /** ratio between length of longest and shortest axis 
     * of the distribution ellipsoid, which is the square root
     * of the largest divided by the smallest eigenvalue of the covariance matrix 
     */
    public double getAxisRatio() {
        return axisratio;
    }

    /** get best evaluated solution found so far. 
     * Remark that the distribution mean was not evaluated 
     * but is expected to have an even better function value. 
     * <p>Example: getBestSolution 
     * @return best solution (search point) found so far 
     * @see #getBestRecentSolution() 
     * @see #getBestX() 
     * @see #getMeanX() */
    public CMASolution getBestSolution() {
        return new CMASolution(bestever_x, bestever_fit, bestever_eval);
    }
    
    /** eventually replaces the best-ever solution 
     * 
     * @param fitness function value computed for the solution {@link #getMeanX()}
     * @return best-ever solution
     */
    public CMASolution setFitnessOfMeanX(double fitness) {
    	xmean_fit = fitness;
    	++counteval;
    	updateBestEver(xmean, fitness, counteval);
    	return new CMASolution(bestever_x, bestever_fit, bestever_eval);
    }

    /** get best evaluated search point found so far. 
     * Remark that the distribution mean was not evaluated 
     * but is expected to have an even better function value. 
     * @return best search point found so far as double[]
     * @see #getMeanX() */
    public double[] getBestX() {
        if (state < 0)
            return null;
        return bestever_x.clone();
    }

    /** objective function value of best solution found so far.
     * @return objective function value of best solution found so far
     * @see #getBestSolution()
     */
    public double getBestFunctionValue() {
        if (state < 0)
            return Double.NaN;
        return bestever_fit;
    }
    /* * evaluation count when the best solution was found
     * 
     */
    public long getBestEvaluationNumber() {
    	return bestever_eval;
    }

    /** Get best evaluated solution of the last (recent) iteration. 
     * This solution is supposed to be more robust than the 
     * best ever solution in particular in possible case of 
     * mis-attributed good fitness values.
     * Remark that the distribution mean was not evaluated 
     * but is expected to have an better function value. 
     * @return best solution (search point) in recent iteration 
     * @see #getBestSolution() 
     * @see #getBestRecentX() 
     * @see #getMeanX() */
    public ISolutionPoint getBestRecentSolution() {
    	return new CMASolution(genoPhenoTransformation(arx[fit.raw[0].i], null), 
    			fit.raw[0].val, 
    			counteval - sp.getLambda() + fit.raw[0].i + 1);
    }

    /** best search point of the recent iteration. 
     * @return Returns the recentFunctionValue.
     * @see #getBestRecentFunctionValue()
     */
    public double[] getBestRecentX() {
        return genoPhenoTransformation(arx[fit.raw[0].i], null);
    }

    /** objective function value of the,
     * best solution in the 
     * recent iteration (population)
     * @return Returns the recentFunctionValue.
     * @see #getBestEvaluationNumber()
     * @see #getBestFunctionValue()
     */
    public double getBestRecentFunctionValue() {
        return recentMinFunctionValue;
    }

    /** objective function value of the, 
     * worst solution of the recent iteration.
     * @return Returns the recentMaxFunctionValue.
     */
    public double getWorstRecentFunctionValue() {
        return recentMaxFunctionValue;
    }

    /** Get mean of the current search distribution. The mean should
     * be regarded as the best estimator for the global
     * optimimum at the given iteration. In particular for noisy
     * problems the distribution mean is the solution of choice
     * preferable to the best or recent best. The return value is 
     * <em>not</em> a copy. Therefore it should not be change it, without 
     * deep knowledge of the code (the effect of a mean change depends on
     * the chosen transscription/implementation of the algorithm). 
     * @return mean value of the current search distribution
     * @see #getBestX() 
     * @see #getBestRecentX() 
     */
    public double[] getMeanX() {
        return xmean.clone();
    }

    public int getDimension() {
        return N;
    }

    /**
     * number of objective function evaluations counted so far
     */
    public long getCountEval() {
        return counteval;
    }

    /**
     * number of iterations conducted so far 
     */
    public long getCountIter() {
        return countiter;
    }
    
    /** the final setting of initial <code>x</code> can 
     * be retrieved only after <code>init()</code> was called
     * 
     * @return <code>double[] initialX</code> start point chosen for 
     * distribution mean value <code>xmean</code>
     */ 
    public double[] getInitialX() {
    	if (state < 0)
    	error("initiaX not yet available, init() must be called first");
    	return initialX.clone();
    }
    
    

    /** get used random number generator instance */
    public Random getRand() {
        return rand;
    }

    /** get properties previously read from a property file.
     * 
     * @return java.util.Properties key-value hash table
     * @see #readProperties()
     */
    public Properties getProperties() {
        return properties;
    }
       
    /**@see #setSeed(long) */
    public long getSeed() {
        return seed;
    }
//    /** Set lower and upper boundary in all variables 
//     * 
//     * @param xlow
//     * @param xup
//     */
//    public void setBoundaries(double xlow, double xup) {
//        int len = 1;
//        if (N > 0)
//            len = N;
//        LBound = new double[len];
//        UBound = new double[len];
//        for (int i= 0; i < len; ++i) {
//            LBound[i] = xlow;
//            UBound[i] = xup;
//        }
//    }
//    /** sets lower and upper boundaries in all variables. 
//     * 
//     * @param xlow lower boundary double[], can be 1-D or of length of the number of variables (dimension). 
//     * @param xup see xlow
//     */
//    public void setBoundaries(double[] xlow, double[] xup) {
//        if( xlow == null || xup ==  null)
//            error("boundaries cannot be null");
//        if (xlow.length == 1 && xup.length == 1) {
//            setBoundaries(xlow[0], xup[0]);
//            return;
//        }
//        if ((N > 0 && (N != xlow.length || N != xup.length)) 
//            || (xlow.length != xup.length))
//            error("dimensions of boundaries do not match");
//        this.LBound = xlow;
//        this.UBound = xup;
//        N = xlow.length; // changes N only if N was 0
//    }

    /**
     * number of objective function evaluations counted so far
     */
    public long setCountEval(long c) {
        return counteval = c;
    }

/** search space dimensions must be set before the optimization is started. */
    public void setDimension(int n) {
        if ((lockDimension > 0 || state >= 0) && N != n)
            error("dimension cannot be changed anymore or contradicts to initialX");
        N = n;
    }

    /** sets typicalX value, the same value in each coordinate
     * @see #setTypicalX(double[])
     */
    public void setTypicalX(double x) {
    	if (state >= 0)
    		error("typical x cannot be set anymore");
    	typicalX = new double[]{x}; // allows "late binding" of dimension
    }

    /** sets typicalX value, which will be overwritten by initialX setting from properties 
     * or {@link #setInitialX(double[])} function call. 
     * Otherwise the initialX is sampled normally distributed from typicalX with initialStandardDeviations
     * 
     * @see #setTypicalX(double)
     * @see #setInitialX(double[])
     * @see #setInitialStandardDeviations(double[])
     */
    public void setTypicalX(double[] x) {
    	if (state >= 0)
    		error("typical x cannot be set anymore");
    	if (x.length == 1) { // to make properties work
    		setTypicalX(x[0]);
    		return;
    	}
    	if (N < 1)
    		setDimension(x.length);
    	if (N != x.length)
    		error("dimensions N=" + N + " and input x.length=" + x.length + "do not agree");
    	typicalX = new double[N];
    	for (int i = 0; i < N; ++i)
    		typicalX[i] = x[i];
    	lockDimension = 1;
    }

    public void setInitialStandardDeviation(double startsigma) {
    	if (state >= 0)
    		error("standard deviations cannot be set anymore");
        this.startsigma = new double[]{startsigma};
    }

    public void setInitialStandardDeviations(double[] startsigma) {
        // assert startsigma != null; // assert should not be used for public arg check
    	if (state >= 0)
    		error("standard deviations cannot be set anymore");
    	if (startsigma.length == 1) { // to make properties work
    		setInitialStandardDeviation(startsigma[0]);
    		return;
    	}
    	if (N > 0 && N != startsigma.length)
    		error("dimensions N=" + N + " and input startsigma.length=" 
    				+ startsigma.length + "do not agree");
        if (N == 0)
        	setDimension(startsigma.length);
        assert N == startsigma.length;
        this.startsigma = startsigma.clone();
        lockDimension = 1;
    }

    /** sets <code>initialX</code> to the same value in each coordinate
     * 
     * @param x value
     * @see #setInitialX(double[])
     */
    public void setInitialX(double x) {
    	if (state >= 0)
    		error("initial x cannot be set anymore");
    	xmean = new double[]{x}; // allows "late binding" of dimension N
    }
    
    /** set initial seach point <code>xmean</code> coordinate-wise uniform 
     * between <code>l</code> and <code>u</code>, 
     * dimension needs to have been set before
     * 
     * @param l double lower value
     * @param u double upper value 
     * @see #setInitialX(double[])
     * @see #setInitialX(double[], double[])
     * */
    public void setInitialX(double l, double u) {
    	if (state >= 0)
    		error("initial x cannot be set anymore");
    	if (N < 1)
    		error("dimension must have been specified before"); 
    	xmean = new double[N];
    	for (int i = 0; i < xmean.length; ++i)
    		xmean[i] = l + (u-l) * rand.nextDouble();
    	lockDimension = 1;
    }

    /** set initial seach point <code>x</code> coordinate-wise uniform 
     * between <code>l</code> and <code>u</code>, 
     * dimension needs to have been set before
     * @param l double lower value
     * @param u double upper value */
    public void setInitialX(double[] l, double[] u) {
    	if (state >= 0)
    		error("initial x cannot be set anymore");
    	if (l.length != u.length)
    		error("length of lower and upper values disagree");
    	setDimension(l.length);
    	xmean = new double[N];
    	for (int i = 0; i < xmean.length; ++i)
    		xmean[i] = l[i] + (u[i]-l[i]) * rand.nextDouble();
    	lockDimension = 1;
    }

    /** set initial search point to input value <code>x</code>. <code>x.length==1</code> is possible, otherwise 
     * the search space dimension is set to <code>x.length</code> irrevocably
     * 
     * @param x double[] initial point
     * @see #setInitialX(double)
     * @see #setInitialX(double, double) 
     */
    public void setInitialX(double[] x) {
    	if (state >= 0)
    		error("initial x cannot be set anymore");
    	if (x.length == 1) { // to make properties work
    		setInitialX(x[0]);
    		return;
    	}
        if (N > 0 && N != x.length)
            error("dimensions do not match");
        if (N == 0)
        	setDimension(x.length);
        assert N == x.length;
        xmean = new double[N];
        for (int i = 0; i < N; ++i)
            xmean[i] = x[i];
        lockDimension = 1; // because xmean is set up
    }
    
    public void setRand(Random rand) {
        this.rand = rand;
    }

    /** Setter for the seed for the random number generator
     * java.util.Random(seed). Changing the seed will only take
     * effect before {@link #init()} was called.
     *@param seed a long value to initialize java.util.Random(seed) 
     */ 
    public void setSeed(long seed) { 
        if (state >= 0)
            warning("setting seed has no effect at this point");
        else {
        	if (seed <= 0) 
        		seed = System.currentTimeMillis();
            this.seed = seed;
            rand.setSeed(seed);
        }
    }

        /** printing output in a viewable formatting style. The printing  
         * <pre>
         * Iteration,#Fevals: rb Function Value Delta( best ,worst) |idx: Max SD idx: Min SD  | minsigD  sigma Axisratio | time, in eig
         *   164( 8),   1638: 5.5061568003892640e-08 (-4e-08,3e-08) |  0: 3.3e-05  8: 1.5e-05 | 1.4e-05 5.6e-05   2.34   |  0.1  0.0 
         *</pre>
         * shows the value of getPrintAnnotation() in the first line and in the second line
         * <li>164 iteration number 
         * <li>( 8) recently sampled search point in this iteration, 
         * <li>1638: number of function evaluations
         * <li>5.5061568003892640e-08 objective function value F of the best point 
         * in the recent generation
         * <li>(-4e-08, difference between the best ever evaluated function value to F,
         * <li>3e-08) | difference between the worst function value of the recent generation to F
         * <li>0: index of coordinate with largest standard deviation
         * <li>3.3e-05 respective standard deviation
         * <li>8: index of coordinate with smallest standard deviation
         * <li>1.5e-05 | respective standard deviation
         * <li>index of coordinate with smallest standard deviation: respective standard deviation
         * <li>| 1.4e-05 standard deviation in smallest principal axis direction
         * <li> 5.6e-05 sigma
         * <li> 2.34 axisratio, ie. quotient between the standard deviations in largest an 
         * smallest principal axis directions, ie. square root of the quotient between largest 
         * and smallest eigenvalue of covariance matrix C
         * <li> 0.1 time, overall elapsed time in seconds
         * <li> 0.0 in eig, overall time spent within eigendecompostion
         * @see #getPrintAnnotation()
         * */
    public String getPrintLine() {
            /* 				   String.format(Locale.US, " %1$4d(%2$2d): %3$5d ", 
             new Object[]{
             new Long(countiter),
             new Integer(idxRecentOffspring),
             new Long(counteval)
             })  
             + String.format(Locale.US, "%1$.16e (%2$+.0e %3$.0e)", 
             new Object[]{
             new Double(recentFunctionValue),
             new Double(getBestFunctionValue() - recentFunctionValue),
             new Double(recentMaxFunctionValue - recentFunctionValue)
             })  
             + String.format(Locale.US, "%1$7.2f ", 
             new Object[]{
             new Double(axisratio)
             }) 
             + String.format(Locale.US, "%1$2d:%2$8.1e %3$2d:%4$8.1e", 
             new Object[]{
             new Integer(math.minidx(math.diag(C))), 
             new Double(sigma * 
             Math.sqrt(math.min(math.diag(C)))),
             new Integer(math.maxidx(math.diag(C))), 
             new Double(sigma * 
             Math.sqrt(math.max(math.diag(C))))
             })
             */
            String s;
            if (state < 0)
                s = new String(
                        new PrintfFormat(Locale.US, " %4d").sprintf(countiter) +
                        new PrintfFormat(Locale.US, "(%2d), ").sprintf(0) + 
                        new PrintfFormat(Locale.US, "%6.0d: ").sprintf(counteval));
            else    
                s = new String(
                        new PrintfFormat(Locale.US, " %4d").sprintf(countiter) +
                        new PrintfFormat(Locale.US, "(%2d), ").sprintf(idxRecentOffspring+1) + 
                        new PrintfFormat(Locale.US, "%6.0d: ").sprintf(counteval) + 
                        new PrintfFormat(Locale.US, "%.16e ").sprintf(recentFunctionValue) + 
                        new PrintfFormat(Locale.US, "(%+.0e,").sprintf(getBestFunctionValue() - recentFunctionValue) + 
                        new PrintfFormat(Locale.US, "%.0e) | ").sprintf(recentMaxFunctionValue - recentFunctionValue) + 
                        new PrintfFormat(Locale.US, "%2d:").sprintf(math.maxidx(math.diag(C))) + 
                        new PrintfFormat(Locale.US, "%8.1e ").sprintf(sigma * maxsqrtdiagC) +
                        new PrintfFormat(Locale.US, "%2d:").sprintf(math.minidx(math.diag(C))) + 
                        new PrintfFormat(Locale.US, "%8.1e ").sprintf(sigma * minsqrtdiagC) + 
                        new PrintfFormat(Locale.US, "| %6.1e ").sprintf(sigma*math.min(diagD)) +
                        new PrintfFormat(Locale.US, "%6.1e ").sprintf(sigma) + 
                        new PrintfFormat(Locale.US, "%6.2f").sprintf(axisratio) +
                        new PrintfFormat(Locale.US, "   | %4.1f ").sprintf((System.currentTimeMillis()-timings.start) / 1000.) +
                        new PrintfFormat(Locale.US, "%4.1f ").sprintf(timings.eigendecomposition / 1000.) 
                );
            
            return s;
            
            /*
             return new String(
             new Long(countiter) 
             + " " +						   new Integer(idxRecentOffspring)
             + " " +						   new Long(counteval)
             + " " +						   new Double(recentFunctionValue)
             //				+ " " +  						   new Double(FunctionValue() - recentFunctionValue)
              //				+ " " +  						   new Double(recentMaxFunctionValue - recentFunctionValue)
               + " " +  						   new Double(axisratio)
               + " " +  						   new Integer(math.minidx(math.diag(C))) 
               + " " +  						   new Double(sigma * 
               Math.sqrt(math.min(math.diag(C))))
               + " " +						   new Integer(math.maxidx(math.diag(C))) 
               + " " +						   new Double(sigma * 
               Math.sqrt(math.max(math.diag(C))))
               );
               */
            /* formatting template
             String.format(Locale.US, "%1$6.2e %2$+.0e", 
             new Object[]{
             new Double(),
             new Double()
             })  
             
             */		   
            //		   out.print(math.min(diagD));
    //      out.print(" ");
    //      new DecimalFormat("0.00E0").format((3.34)) + " " + 
    //      (cma.fit.fitness[(cma.parameters.getLambda()/2)].d 
    //      - cma.fit.fitness[0].d) + "," +
    //      cma.fit.fitness[cma.parameters.getLambda()-1].d + ") | " +
            
        }

        /** returns an annotation string for the printings of method println(). */
    public String getPrintAnnotation() {
        String s = new String(
        "Iteration,#Fevals: rb Function Value Delta( best ,worst) |idx: Max SD idx: Min SD  | minsigD  sigma Axisratio | time, in eig");
        //         491( 3),   3924: 1.1245467061992267e+00 (-2e-01,4e-01)  9: 7.8e-05  2: 5.0e-02 | 5.9e-03 1.3e-02 660.41 
    
        return s;
    }

        /** returns an informative initial message of the CMA-ES optimizer */
    public String helloWorld() {
        String s = new String(
                "(" + sp.getMu() + "," + sp.getLambda() 
                + ")-CMA-ES(mu_eff=" + Math.round(10.*sp.getMueff())/10. + "), Ver=\"" 
                + versionNumber 
                + "\", dimension=" + N 
            	+ ", " + options.diagonalCovarianceMatrix + " diagonal iter." 
                + ", randomSeed=" + seed
                + " (" + new Date().toString() + ")");
        return s;
        
    }
    /** calls System.out.println(s) and writes s to the file outcmaesdisp.dat 
     * by default, if writeDisplayToFile option is > 0
     * @see #getPrintLine() 
     */
    public void println(String s) {
        System.out.println(s);
        if (options.writeDisplayToFile > 0)
            writeToFile(options.outputFileNamesPrefix + "disp" + ".dat", s, 1);
    }

    /** calls println(getPrintLine()) 
     * @see #getPrintLine() 
     */
    public void println() {
        println(getPrintLine());
    }

    /** @see #getPrintAnnotation() */
    public void printlnAnnotation() {
        println(getPrintAnnotation());
    }

    /** calls println(helloWorld()) 
         * @see #helloWorld() 
         * @see #println(String)
         */
        public void printlnHelloWorld() {
            println(helloWorld());
    }

    public String getDataRowFitness() {
        String s = new String();    
        s = countiter + " " + counteval + " " + sigma + " " + axisratio + " "
        + bestever_fit + " ";
        if (mode == SINGLE_MODE)
            s += recentFunctionValue + " ";
        else  {
            s += fit.raw[0].val + " ";
            s += fit.raw[sp.getLambda()/2].val + " ";
            s += fit.raw[sp.getLambda()-1].val + " ";
            s += math.min(diagD) + " "  
            	+ (math.maxidx(math.diag(C))+1) + " " + sigma*maxsqrtdiagC + " " 
            	+ (math.minidx(math.diag(C))+1) + " "  + sigma*minsqrtdiagC;
            //for (int i = 0; i < sp.getLambda(); ++i) {
            //    s += fit.funValues[i].d + " ";
            //}
        }
        return s;
    }

    public String getDataRowXRecentBest() {
        int idx = 0;
        if (mode == SINGLE_MODE)
            idx = idxRecentOffspring; 
        String s = new String();    
        s = countiter + " " + counteval + " " + sigma + " 0 " 
        	+ (state == 1 ? Double.NaN : fit.raw[idx].val) + " ";
        for (int i = 0; i < N; ++i) {
            s += arx[fit.raw[idx].i][i] + " ";
        }
        return s;
    }

    public String getDataRowXMean() {
        String s = new String();    
        s = countiter + " " + counteval + " " + sigma + " 0 0 ";
        for (int i = 0; i < N; ++i) {
            s += xmean[i] + " ";
        }
        return s;
    }
    /** 6-th to last column are sorted axis lengths axlen */
    public String getDataRowAxlen() {
    	String s = new String();    
    	s = countiter + " " + counteval + " " + sigma + " " + axisratio + " " 
    	   + maxsqrtdiagC/minsqrtdiagC + " "; 
    	double[] tmp = (double[]) diagD.clone();
    	java.util.Arrays.sort(tmp);
    	for (int i = 0; i < N; ++i) {
    		s += tmp[i] + " ";
    	}
    	return s;
    }
    public String getDataRowStddev() {
    	String s = new String();    
    	s = countiter + " " + counteval + " " + sigma + " " 
        + (1+math.maxidx(math.diag(C))) + " " + (1+math.minidx(math.diag(C))) + " ";
        for (int i = 0; i < N; ++i) {
            s += sigma * Math.sqrt(C[i][i]) + " ";
        }
        return s;
    }
    /** correlations and covariances of the search distribution. The
     * first, '%#'-commented row contains itertation number,
     * evaluation number, and sigma. In the remaining rows the upper
     * triangular part contains variances and covariances
     * sigma*sigma*c_ij. The lower part contains correlations c_ij /
     * sqrt(c_ii * c_jj).  */
    public String getDataC() {
	int i, j;
        String s = new String();    
        s = "%# " + countiter + " " + counteval + " " + sigma + "\n";
        for (i = 0; i < N; ++i) {
	    for (j = 0; j < i; ++j) // ouput correlation in the lower half
		s += C[i][j] / Math.sqrt(C[i][i] * C[j][j]) + " ";
	    for (j = i; j < N; ++j) 
		s += sigma * sigma * C[i][j] + " ";
	    s += "\n";
        }
        return s;
    }
    
    private String[] fileswritten = new String[]{""}; // also (re-)initialized in init()
    /** writes a string to a file, overwrites first, appends afterwards. 
     * <p>Example: cma.writeToFile("cmaescorr.dat", cma.writeC()); 
     * @param filename is a String giving the name of the file to be written
     * @param data is a String of text/data to be written
     * @param flgAppend for flgAppend>0 old data are not overwritten
     */
    public void writeToFile(String filename, String data, int flgAppend) {
        boolean appendflag = flgAppend > 0;
        for (int i = 0; !appendflag && i < fileswritten.length; ++i)
            if(filename.equals(fileswritten[i])) {
                appendflag = true;
            }
        java.io.PrintWriter out = null;
        try {
            out = new java.io.PrintWriter(new java.io.FileWriter(filename, appendflag));
            out.println(data);
            out.flush(); // no idea whether this makes sense
            out.close();
        } catch (java.io.FileNotFoundException e) {
            warning("Could not find file '" + filename  + "'(FileNotFoundException)");
        } catch (java.io.IOException e) {
            warning("Could not open/write to file " + filename);
            //e.printStackTrace();            // output goes to System.err
            //e.printStackTrace(System.out);  // send trace to stdout
        } finally {
            if (out != null)
                out.close();
        }
        // if first time written
        // append filename to fileswritten
        if (appendflag == false) { 
            String s[] = fileswritten;
            fileswritten = new String[fileswritten.length+1];
            for (int i = 0; i < s.length; ++i)
                fileswritten[i] = s[i];
            fileswritten[fileswritten.length-1] = new String(filename);
        }
    }
    /** writes data output to default files. Uses opts.outputFileNamesPrefix to create filenames. 
     * Columns 1-2 are iteration number and function evaluation count,  
     * columns 6- are the data according to the filename. Maximum time spent
     * for writing can be controlled in the properties file. 
     * 
     * <p>The output is written to files that can be printed in Matlab or Scilab (a free
     * and easy to install Matlab "clone").</p>
     * <p>
     * Matlab:
     * <pre>
          cd 'directory_where_outfiles_and_plotcmaesdat.m_file_are'
          plotcmaesdat;
     * </pre>
     * Scilab:
     * <pre>
          cd 'directory_where_outfiles_and_plotcmaesdat.sci_file_are'
          getf('plotcmaesdat.sci');
          plotcmaesdat;
     * </pre>
     * </p>
     * @see #writeToDefaultFiles(String fileNamePrefix)
     * @see #writeToDefaultFiles(int)
     *  */
    public void writeToDefaultFiles() {
        writeToDefaultFiles(options.outputFileNamesPrefix);
    }
    /** writes data output to default files. Maximum time spent
     * for writing can be controlled in the properties file. For negative values
     * no writing takes place, overruling the <code>flgForce</code> input parameter below.
     *  
     * @param flgForce 0==write depending on time spent with writing, 
     * 1==write if the iteration count has changed, 
     * 2==write always, overruled by negative values of maxTimeFractionForWriteToDefaultFiles property
     *  
     * @see #writeToDefaultFiles() */
    public void writeToDefaultFiles(int flgForce) {
        if (flgForce > 0 && countiter != citerlastwritten) 
            citerlastwritten = -1; // force writing if something new is there
        if (flgForce >= 2)
            citerlastwritten = -1; // force writing 
        writeToDefaultFiles(options.outputFileNamesPrefix);
    }
    /** 
     * writes data to files <tt>fileNamePrefix</tt>fit.dat, ...xmean.dat
     * ...xbest.dat, ...std.dat, ...axlen.dat.
     * @see #writeToDefaultFiles() 
     * @param fileNamePrefix prefix String for filenames created to write data */
    public void writeToDefaultFiles(String fileNamePrefix) {

        if (options.maxTimeFractionForWriteToDefaultFiles < 0) // overwrites force flag
            return;
        if (citerlastwritten >= 0) { // negative value forces writing
            if (state < 1)
                return;
            if (countiter == citerlastwritten)
                return; 
            if (options.maxTimeFractionForWriteToDefaultFiles <= 0)
                return;
            if (countiter > 4 && stopConditions.index == 0  // has no effect if stopCondition.test() was not called
                    // iteration gap is less than two times of the average gap, to not have large data holes
                    // spoils the effect of reducing the timeFraction late in the run
                    && countiter - citerlastwritten - 1 < 2.*(countiter - countwritten + 1.) / (countwritten + 1.)
                    // allowed time is exhausted
                    && timings.writedefaultfiles > options.maxTimeFractionForWriteToDefaultFiles 
                    * (System.currentTimeMillis() - timings.start)) 
                return;
        }

        long firsttime = System.currentTimeMillis();
        writeToFile(fileNamePrefix + "fit.dat", getDataRowFitness(), 1);
        writeToFile(fileNamePrefix + "xmean.dat", getDataRowXMean(), 1);
        writeToFile(fileNamePrefix + "xrecentbest.dat", getDataRowXRecentBest(), 1);
        writeToFile(fileNamePrefix + "stddev.dat", getDataRowStddev(), 1); // sigma*sqrt(diag(C))
        writeToFile(fileNamePrefix + "axlen.dat", getDataRowAxlen(), 1);
        timings.writedefaultfiles += System.currentTimeMillis() - firsttime;
//        System.out.println(timings.writedefaultfiles + " " 
//                + (System.currentTimeMillis()-timings.start)  + " " + opts.maxTimeFractionForWriteToDefaultFiles);
        if (countiter < 3)
            timings.writedefaultfiles = 0;
        
        ++countwritten; 
        citerlastwritten = countiter;
    }
    /** writes header lines to the default files. Could become XML if needed. 
     * 
     * @param flgAppend == 0 means overwrite files,  == 1 means append to files
     */
    public void writeToDefaultFilesHeaders(int flgAppend) {
        writeToDefaultFilesHeaders(options.outputFileNamesPrefix, flgAppend);
    }
    /** 
     * Writes headers (column annotations) to files <prefix>fit.dat, ...xmean.dat
     * ...xbest.dat, ...std.dat, ...axlen.dat, and in case the first data
     * line, usually with the initial values. 
     * @param fileNamePrefix String for filenames created to write data */
    public void writeToDefaultFilesHeaders(String fileNamePrefix, int flgAppend) {
        if (options.maxTimeFractionForWriteToDefaultFiles < 0) // overwrites force flag
            return;
        String s = "(randomSeed=" + seed + ", " + new Date().toString() + ")\n";
        writeToFile(fileNamePrefix + "fit.dat", 
        		"%# iteration evaluations sigma axisratio fitness_of(bestever best median worst) mindii "
        		+ "idxmaxSD maxSD idxminSD minSD " 
        		+ s, flgAppend);
        writeToFile(fileNamePrefix + "xmean.dat", 
        "%# iteration evaluations sigma void void mean(1...dimension) " + s, flgAppend);
        if (state == 0)
            writeToFile(fileNamePrefix + "xmean.dat", getDataRowXMean(), 1);
        writeToFile(fileNamePrefix + "xrecentbest.dat", 
        "%# iteration evaluations sigma void fitness_of_recent_best x_of_recent_best(1...dimension) " 
        		+ s, flgAppend);
        writeToFile(fileNamePrefix + "stddev.dat", 
                "%# iteration evaluations sigma idxmaxSD idxminSD SDs=sigma*sqrt(diag(C)) " 
        		+ s, flgAppend);
        if (state == 0)
            writeToFile(fileNamePrefix + "stddev.dat", getDataRowStddev(), 1);
        writeToFile(fileNamePrefix + "axlen.dat", 
            "%# iteration evaluations sigma axisratio stddevratio sort(diag(D)) (square roots of eigenvalues of C) " 
        		+ s, flgAppend);
        if (state == 0)
            writeToFile(fileNamePrefix + "axlen.dat", getDataRowAxlen(), 1);
    }

    /** very provisional error handling. Methods of the class
     * CMAEvolutionStrategy might throw the CMAException, that
     * need not be catched, because it extends the "unchecked"
     * RuntimeException class */ 
    public class CMAException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    
        CMAException(String s) {
            super(s);
        }
    }
}

class IntDouble implements Comparator<IntDouble> {
    int i;    // unique integer value, useful after sorting
    double val; // double value
    public IntDouble(double d, int i) {
        this.val = d;
        this.i = i;
    }
    public IntDouble(double d) {
        this.val = d;
    }
    public IntDouble() {
    }
    public int compare(IntDouble o1, IntDouble o2) {
        if (o1.val < o2.val)
            return -1;
        if (o1.val > o2.val)
            return 1;
        if (o1.i < o2.i)
            return -1;
        if (o1.i > o2.i)
            return 1;
        return 0;
    }
    
    public boolean equals(IntDouble o1, IntDouble o2) {
        if (o1.compare(o1, o2) == 0) // && o1.hashCode() == o2.hashCode()
            return true;
        return false;
    }
} // IntDouble
