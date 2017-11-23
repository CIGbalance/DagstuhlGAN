package fr.inria.optimization.cmaes.fitness;
import java.util.Random;

/** one can access the desired fitness function by giving its number
 * in the constructor method. Refer to the source code for the
 * numbers. This class is a stub (and hack) so far.
 * 
 */
public class FunctionCollector extends AbstractObjectiveFunction {

	public FunctionCollector (double function_number, 
			int flgRotate, 
			double axisratio) {

		actFun = (int) (function_number);
		rotate = flgRotate; 
		scaling = axisratio == 0 ? 1. : axisratio;

		if (actFun > maxFuncNumber)
			actFun = 1; /* sphere */
		
		// assign all functions by number here
		funs[0]  = new RandFun();
		funs[10]  = new Sphere();

		// convex-quadratic
        funs[30]  = new Cigar(axisratio == 0 ? 1e3 : scaling); 
        funs[40]  = new Tablet(axisratio == 0 ? 1e3 : scaling);
		funs[50]  = new Elli(axisratio == 0 ? 1e3 : scaling);
        funs[60]  = new CigTab(axisratio == 0 ? 1e4 : scaling);
        funs[70]  = new TwoAxes(axisratio == 0 ? 1e3 : scaling);

        // uni-modal, well, essentially 
		funs[80]  = new Rosen();
		funs[90]  = new DiffPow();
        funs[91]  = new ssDiffPow();

        // multi-modal
        funs[150] = new Rastrigin(scaling, 10); 
        funs[160] = new Ackley(scaling);

//      funs[999]  = new Experimental();
//      funs[]  = new ();
//      funs[]  = new ();
        
	}
	final int maxFuncNumber = 999;
	IObjectiveFunction[] funs = new IObjectiveFunction[maxFuncNumber+1];
	int actFun = 0;
	int rotate = 0;
	double scaling = 1;
	Basis B = new Basis();
	
	/** implements the fitness function evaluation according to interface {@link IObjectiveFunction}
	 * 
	 */ 
	@Override
	public double valueOf(double[] x) {
		x = x.clone(); // regard input as imutable, not really Java philosophy
		if (rotate > 0)     // rotate
			x = B.Rotate(x);
		if (scaling != 1) { // scale 
			for (int i = 0; i < x.length; ++i)
				x[i] = Math.pow(10, i/(x.length -1.)) * x[i];
		}
		return funs[actFun] == null ? funs[0].valueOf(x) : funs[actFun].valueOf(x);
	}
	public boolean isFeasible(double x[]) { // unfortunate code duplication
    	//int i;
    	//for (i = 0; i < x.length; ++i)
    	//	if (x[i] < 0.01)
    	//		return false;
    	//return true;
		return funs[actFun].isFeasible(x);
	}
}

/** provides rotation of a search point, basis is chosen with constant seed.
 * 
 */
class RandFun extends AbstractObjectiveFunction {
    java.util.Random rand = new java.util.Random(0);
    @Override
    public double valueOf (double[] x) {
        double res = rand.nextDouble();
        return res;
    }
}
class Sphere extends AbstractObjectiveFunction {
    @Override
    public double valueOf (double[] x) {
        double res = 0;
        for (int i = 0; i < x.length; ++i)
            res += x[i] * x[i];
        return res;
    }
    public boolean isFeasible(double[] x) {
    	//int i;
    	//for (i = 0; i < x.length; ++i)
    	//	if (x[i] < 0.01)
    	//		return false;
    	return true;
    }
}

class Cigar extends AbstractObjectiveFunction {
    Cigar() {
        this(1e3);
    }
    Cigar(double axisratio) {
        factor = axisratio * axisratio;
    }
    public double factor = 1e6;
    @Override
    public double valueOf (double[] x) {
        double res = x[0] * x[0];
        for (int i = 1; i < x.length; ++i)
            res += factor * x[i] * x[i];
        return res;
    }
}
class Tablet extends AbstractObjectiveFunction {
    Tablet() {
        this(1e3);
    }
    Tablet(double axisratio) {
        factor = axisratio * axisratio;
    }
    public double factor = 1e6;
    @Override
    public double valueOf (double[] x) {
        double res = factor * x[0] * x[0];
        for (int i = 1; i < x.length; ++i)
            res += x[i] * x[i];
        return res;
    }
}
class CigTab extends AbstractObjectiveFunction {
    CigTab() {
        this(1e4);
    }
    CigTab(double axisratio) {
        factor = axisratio;
    }
    public double factor = 1e6;
    @Override
    public double valueOf (double[] x) {
    	int end = x.length-1;
        double res = x[0] * x[0] / factor + factor * x[end] * x[end];
        for (int i = 1; i < end; ++i)
            res += x[i] * x[i];
        return res;
    }
}
class TwoAxes extends AbstractObjectiveFunction {
    public double factor = 1e6;
    TwoAxes() {
    }
    TwoAxes(double axisratio) {
        factor = axisratio * axisratio;
    }
    @Override
    public double valueOf (double[] x) {
        double res = 0;
        for (int i = 0; i < x.length; ++i)
            res += (i < x.length/2 ? factor : 1) * x[i] * x[i];
        return res;
    }
}
class ElliRotated extends AbstractObjectiveFunction {
    ElliRotated() {
        this(1e3);
    }
    ElliRotated(double axisratio) {
        factor = axisratio * axisratio;
    }
    public Basis B = new Basis();
    public double factor = 1e6;
    @Override
    public double valueOf (double[] x) {
        x = B.Rotate(x);
        double res = 0;
        for (int i = 0; i < x.length; ++i)
            res += Math.pow(factor,i/(x.length-1.)) * x[i] * x[i]; 
        return res;
    }
}
/** dimensionality must be larger than one */
class Elli extends AbstractObjectiveFunction {
    Elli() {
        this(1e3);
    }
    Elli(double axisratio) {
        factor = axisratio * axisratio;
    }
    public double factor = 1e6;
    @Override
    public double valueOf (double[] x) {
        double res = 0;
        for (int i = 0; i < x.length; ++i)
            res += Math.pow(factor,i/(x.length-1.)) * x[i] * x[i]; 
        return res;
    }
//    public boolean isFeasible(double x[]) {
//    	int i;
//    	for (i = 0; i < x.length; ++i) {
//    		if (x[i] < -0.20 || x[i] > 80) 
//    			return false;
//    	}
//    	return true;
//    }
    
}/** dimensionality must be larger than one */

class DiffPow extends AbstractObjectiveFunction {
    @Override
    public double valueOf (double[] x) {
        double res = 0;
        for (int i = 0; i < x.length; ++i)
            res += Math.pow(Math.abs(x[i]),2.+10*(double)i/(x.length-1.)); 
        return res;
    }
    
}class ssDiffPow extends AbstractObjectiveFunction {
    @Override
    public double valueOf (double[] x) {
        return Math.pow(new DiffPow().valueOf(x), 0.25);
    }
    
}
class Rosen extends AbstractObjectiveFunction {
    @Override
    public double valueOf (double[] x) {
        double res = 0;
        for (int i = 0; i < x.length-1; ++i)
            res += 1e2 * (x[i]*x[i] - x[i+1]) * (x[i]*x[i] - x[i+1]) + 
            (x[i] - 1.) * (x[i] - 1.);
        return res;
    }
}

class Ackley extends AbstractObjectiveFunction {
    double axisratio = 1.;
    Ackley(double axra) {
        axisratio = axra;
    }
    public Ackley() {
    }
    @Override
    public double valueOf (double[] x) {
        double res = 0;
        double res2 = 0;
        double fac = 0;
        for (int i = 0; i < x.length; ++i) {
            fac = Math.pow(axisratio, (i-1.)/(x.length-1.));
            res += fac * fac * x[i]*x[i];
            res2 += Math.cos(2. * Math.PI * fac * x[i]);
        }
        return (20. - 20. * Math.exp(-0.2 * Math.sqrt(res/x.length)) 
                + Math.exp(1.) - Math.exp(res2/x.length));
    }
}
class Rastrigin extends AbstractObjectiveFunction {
    Rastrigin() {
        this(1, 10);
    }
    Rastrigin(double axisratio, double amplitude) {
        this.axisratio = axisratio;
        this.amplitude = amplitude;
    }
    public double axisratio = 1;
    public double amplitude = 10;
    @Override
    public double valueOf (double[] x) {
        double fac;
        double res = 0;
        for (int i = 0; i < x.length; ++i) {
            fac = Math.pow(axisratio,(i-1.)/(x.length-1.));
            if (i == 0 && x[i] < 0)
                fac *= 1.;
            res +=  fac * fac * x[i] * x[i]
               + amplitude * (1. - Math.cos(2.*Math.PI * fac * x[i])); 
        }
        return res;
    }
}
/* Template fitness function 
class fff extends AbstractObjectiveFunction {
    public double valueOf(double[] x) {
        double res = 0;
        for (int i = 0; i < x.length; ++i) {
        }
        return res;
    }
}
*/

class Basis {
	double [][] B; // usually field names should be lower case
    Random rand = new Random(2); // use not always the same basis

    double[] Rotate(double[] x) {
    	GenBasis(x.length);
    	double[] y = new double[x.length];
    	for (int i = 0; i < x.length; ++i) {
    		y[i] = 0;
    		for (int j = 0; j < x.length; ++j)
    			y[i] += B[i][j] * x[j]; 
    	}
    	return y;
    }
    double[][] Rotate(double[][] pop) {
    	double[][] y = new double[pop.length][];
    	for (int i = 0; i < pop.length; ++i) {
    		y[i] = Rotate(pop[i]);
    	}
    	return y;
    }
    
    void GenBasis(int DIM)  
    {
    	if (B != null ? B.length == DIM : false)
    		return;

    	double sp;
    	int i,j,k;

    	/* generate orthogonal basis */
    	B = new double[DIM][DIM];
    	for (i = 0; i < DIM; ++i) {
    		/* sample components gaussian */
    		for (j = 0; j < DIM; ++j) 
    			B[i][j] = rand.nextGaussian();
    		/* substract projection of previous vectors */
    		for (j = i-1; j >= 0; --j) {
    			for (sp = 0., k = 0; k < DIM; ++k)
    				sp += B[i][k]*B[j][k]; /* scalar product */
    			for (k = 0; k < DIM; ++k)
    				B[i][k] -= sp * B[j][k]; /* substract */
    		}
    		/* normalize */
    		for (sp = 0., k = 0; k < DIM; ++k)
    			sp += B[i][k]*B[i][k]; /* squared norm */
    		for (k = 0; k < DIM; ++k)
    			B[i][k] /= Math.sqrt(sp); 
    	}
    }
}
