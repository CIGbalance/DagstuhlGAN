package fr.inria.optimization.cmaes;

import java.util.Properties;

/*
    Copyright 2003, 2005, 2007 Nikolaus Hansen 
    e-mail: hansen .AT. bionik.tu-berlin.de
            hansen .AT. lri.fr

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License, version 3,
    as published by the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

  Last change: $Date: 2010-12-02 23:57:21 +0100 (Thu, 02 Dec 2010) $
 */

/** Simple container of (mostly generic) options for the
 * optimization, like the maximum number of objective
 * function evaluations, see class fields.  No explicit setting of 
 * options is needed to 
 * initialize the CMA-ES ({@link CMAEvolutionStrategy#init()}) 
 * and options of the CMA-ES can be set
 * and changed any time, either via a property file and the method
 * {@link CMAEvolutionStrategy#readProperties()}, or new values can simply be 
 * assigned to the fields of the public <code>opts</code> field of 
 * the class <code>CMAEvolutionStrategy</code> (yeah, I know, not exactly Java style).
 * 
 */
public class CMAOptions implements java.io.Serializable {
        // needs to be public to make sure that a using class can excess Options.
        // Therefore, if not nested, needs to move into a separate file
        
	private static final long serialVersionUID = 2255162105325585121L;

    /** number of initial iterations with diagonal covariance matrix, where
     * 1 means always. Default is 
     * diagonalCovarianceMatrix=0, but this will presumably change in future. 
     * As long as iterations<=diagonalCovarianceMatrix 
     * the internal time complexity is linear in the search space dimensionality
     * (memory requirements remain quadratic). 
     */
    public long diagonalCovarianceMatrix = 0; // -1; 

	/** lower bound for standard deviations (step sizes). The
         * Array can be of any length. The i-th entry corresponds to
         * the i-th variable. If length&#60;dim the last entry is recycled for
         * all remaining variables. Zero entries mean, naturally, no
         * lower bound. <P>CAVE: there is an interference with stopTolX (and stopTolXFactor):
         * if lowerStdDev is larger than stopTolX, the termination criterion
         * can never be satisfied.</P> 
         * <p>Example:
         * <pre> CMAEvolutionStrategy es = new CMAEvolutionStrategy(); 
         * es.options.lowerStandardDeviations = new double[]{1e-4,1e-8}; // 1e-8 for all but first variable
         * </pre> 
         * @see #stopTolX
         * @see #stopTolXFactor
         * */
        public double[] lowerStandardDeviations;
        /** upper bound for standard deviations (step lengths). 
         * Zero entries mean no upper
         * bound. Be aware of the interference with option stopTolUpXFactor. 
         * @see #lowerStandardDeviations
         * @see #stopTolUpXFactor
         * */
        public double[] upperStandardDeviations;

        /** stop if function value drops below the target 
         * function value stopFitness. Default = <code>Double.MIN_VALUE</code> */ 
        public double stopFitness = Double.MIN_VALUE; 
        /** stop if the 
         *  maximum function value difference of all iteration-best 
         * solutions of the last 10 +
         * 30*N/lambda iterations 
         * and all solutions of the recent iteration 
         * become <= stopTolFun. Default = 1e-12. 
         * */
        public double stopTolFun = 1e-12; 
        /** stop if the maximum function value difference of all iteration-best 
         * solutions of the last 10 +
         * 30*N/lambda iterations become smaller than
         * stopTolFunHist. Default = 1e-13. The measured objective
         * function value differences do not include repair
         * penalties. */
        public double stopTolFunHist = 1e-13; // used if non-null
        /** stop if search steps become smaller than stopTolX. Default = 0 */
        public double stopTolX = 0.0; 
        /** stop if search steps become smaller than stopTolXFactor * initial step size. 
         * Default = 1e-11. */
        public double stopTolXFactor = 1e-11; // used if TolX is null
        /** stop if search steps become larger than stopTolUpXFactor
         * * initial step size. Default = 1e3. When this termination
         * criterion applies on a static objective function, the initial 
         * step-size was chosen far too
         * small (or divergent behavior is observed). */
        public double stopTolUpXFactor = 1e3; // multiplier for initial sigma
        /** stop if the number of objective function evaluations exceed stopMaxFunEvals */
        public long stopMaxFunEvals = Long.MAX_VALUE; // it is not straight forward to set a dimension dependent
        											  // default as the user can first set stopMaxFunEvals
        										      // and afterwards the dimension
        /** stop if the number of iterations (generations) exceed stopMaxIter */
        public long stopMaxIter = Long.MAX_VALUE;
        /** if true stopping message "Manual:..." is generated */
        public boolean stopnow = false; 

        /** flag used by methods iterate(), whether to write output to files. 
         * Methods write an output file if flgWriteFile&#62;0. 
         */

        /** determines whether CMA says hello after initialization. 
         * @see CMAEvolutionStrategy#helloWorld()
         *  */
        public int verbosity = 1;
        /** Output files written will have the names outputFileNamesPrefix*.dat */
        public String outputFileNamesPrefix = "outcmaes"; 
        /** if chosen > 0 the console output from functions <code>print...</code> is saved 
         * additionally into a file, by default <tt>outcmaesdisp.dat</tt> */
        public int writeDisplayToFile = 1;
        
        /** only for >= 1 results are always exactly reproducible, as otherwise the update of the 
         * eigensystem is conducted depending on time measurements, defaut is 0.2 */
        public double maxTimeFractionForEigendecomposition = 0.2;
        /** default is 0.1 
         */
        public double maxTimeFractionForWriteToDefaultFiles = 0.1;
        
        /** checks eigendecomposition mainly for debugging purpose, default is 0==no-check; 
         * the function checkEigenSystem requires O(N^3) operations. 
         */
        public int checkEigenSystem = 0;

        /** This is the only place where the reading of a new option needs to be declared 
         * 
         * @param properties
         */
        void setOptions(Properties properties) {
            String s;
            diagonalCovarianceMatrix = getFirstToken(properties.getProperty("diagonalCovarianceMatrix"), diagonalCovarianceMatrix);
            if((s = properties.getProperty("stopFitness")) != null)
                stopFitness = Double.valueOf(getFirstToken(s));
            stopTolFun = getFirstToken(properties.getProperty("stopTolFun"), stopTolFun);
            stopTolFunHist = getFirstToken(properties.getProperty("stopTolFunHist"), stopTolFunHist);
            stopTolX = getFirstToken(properties.getProperty("stopTolX"), stopTolX);
            stopTolXFactor = getFirstToken(properties.getProperty("stopTolXFactor"), stopTolXFactor);
            stopTolUpXFactor = getFirstToken(properties.getProperty("stopTolUpXFactor"), stopTolUpXFactor);
            stopMaxFunEvals = getFirstToken(properties.getProperty("stopMaxFunEvals"), stopMaxFunEvals);
            stopMaxIter = getFirstToken(properties.getProperty("stopMaxIter"), stopMaxIter);
            if ((s = properties.getProperty("upperStandardDeviations")) != null && !s.equals(""))
                upperStandardDeviations = parseDouble(getAllToken(s));
            if ((s = properties.getProperty("lowerStandardDeviations")) != null && !s.equals(""))
                lowerStandardDeviations = parseDouble(getAllToken(s));
            outputFileNamesPrefix = properties.getProperty("outputFileNamesPrefix", outputFileNamesPrefix).split("\\s")[0];
            maxTimeFractionForEigendecomposition = 
                getFirstToken(properties.getProperty("maxTimeFractionForEigendecomposition"), 
                        maxTimeFractionForEigendecomposition);
            maxTimeFractionForWriteToDefaultFiles = 
                getFirstToken(properties.getProperty("maxTimeFractionForWriteToDefaultFiles"), 
                        maxTimeFractionForWriteToDefaultFiles);
            stopnow = "now".equals(getFirstToken(properties.getProperty("stop")));
            writeDisplayToFile = getFirstToken(properties.getProperty("writeDisplayToFile"), writeDisplayToFile);
            checkEigenSystem = getFirstToken(properties.getProperty("checkEigenSystem"), checkEigenSystem);
        }

		/** Returns the double value of the first token of a string s or the default, 
		 *  if the string is null or empty. This method should become generic with respect to the
		 *  type of second argument.  
		 *  @param s string where the first token is read from
		 *  @param def double default value, in case the string is empty*/
		public Double getFirstToken(String s, Double def) {
		    if (s == null)
		        return def;
		    String[] ar = s.split("\\s+");
		    if (ar[0].equals("")) 
		        return def;
		    return Double.valueOf(ar[0]);
		}

		/** should become generic with type argument?  */
		public String getFirstToken(String s) {
		    if (s == null)
		        return ""; 
		    String[] ar = s.split(new String("\\s+"));
		    return ar[0];
		}

		/** Returns the Integer value of the first token of a string s or the default, 
		 *  if the string is null or empty. This method should become generic with respect to the
		 *  type of second argument.  
		 *  @param s string where the first token is read from
		 *  @param def Integer default value, in case the string is empty*/
		public Integer getFirstToken(String s, Integer def) {
		    if (s == null)
		        return def;
		    String[] ar = s.split("\\s+");
		    if (ar[0].equals("")) 
		        return def;
		    return Integer.valueOf(ar[0]);
		}

		//    public <T> T getFirstToken(String s, T def) {
		//        if (s == null)
		//            return def;
		//        String[] ar = s.split("\\s+");
		//        if (ar[0].equals("")) 
		//            return def;
		//        return (T)(ar[0]); /* this fails */
		//    }
		    
		    private String removeComments(String s) {
		        int i;
		        // remove trailing comments
		        i = s.indexOf("#");
		        if (i >= 0)
		            s = s.substring(0,i);
		        i = s.indexOf("!");
		        if (i >= 0)
		            s = s.substring(0,i);
		        i = s.indexOf("%");
		        if (i >= 0)
		            s = s.substring(0,i);
		        i = s.indexOf("//");
		        if (i >= 0)
		            s = s.substring(0,i);
		        return s;
		    }

		/** Returns def if s==null or empty, code dublicate, should become generic */
		private Long getFirstToken(String s, Long def) {
		    if (s == null)
		        return def;
		    String[] ar = removeComments(s).split("\\s+");
		    if (ar[0].equals("")) 
		        return def;
		    return Long.valueOf(ar[0]);
		}

		String[] getAllToken(String s) {
		    // split w.r.t. white spaces regexp \s+
		    return removeComments(s).split("\\s+");
		}

		double[] parseDouble(String[] ars) {
		    double[] ard = new double[ars.length];
		    for(int i = 0; i < ars.length; ++i) {
		        ard[i] = Double.parseDouble(ars[i]);
		    }
		    return ard;
		}
    }

