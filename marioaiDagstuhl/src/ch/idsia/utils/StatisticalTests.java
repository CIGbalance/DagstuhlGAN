package ch.idsia.utils;

import java.io.PrintStream;

/**
 * This class will contain commonly used statistical
 * tests.  At present, the only test available is
 * the t-test.  This can be used in paired or unpaired
 * modes, and in each case one or two-sided tests may
 * be applied.  The other useful feature
 */


public class StatisticalTests {

    //static Class dummy = Stats.class;

    public static double sqr(double x) {
        return x * x;
    }

    public static double sumSquareDiff(double[] x, double mean) {
        double tot = 0.0;
        for (int i = 0; i < x.length; i++)
            tot += sqr(x[i] - mean);
        return tot;
    }

    public static double correlation(double[] x, double[] y) {
        double mx = Stats.mean(x);
        double my = Stats.mean(y);
        double xy = sumProdDiff(x, y, mx, my);
        double xx = sumSquareDiff(x, mx);
        double yy = sumSquareDiff(y, my);
        return xy / Math.sqrt(xx * yy);
    }

    public static double sumSquare(double[] x) {
        double tot = 0.0;
        for (int i = 0; i < x.length; i++)
            tot += x[i] * x[i];
        return tot;
    }

    public static double sumProdDiff(double[] x, double[] y, double mx, double my) {
        double tot = 0.0;
        for (int i = 0; i < x.length; i++)
            tot += (x[i] - mx) * (y[i] - my);
        return tot;
    }

    /**
     * Calculates the probability with which the
     * null hypothesis (that the means are equal)
     * can be rejected in favour of the alternative
     * hypothesis that they are not equal (hence uses
     * a two-sided test).  The samples are not paired
     * and do not have to be of equal size,
     */
    public static double tNotPairedOneSided(double[] s1, double[] s2) {
        return tNotPaired(s1, s2, false);
    }

    /**
     * Calculates the probability with which the
     * null hypothesis (that the means are equal)
     * can be rejected in favour of the alternative
     * hypothesis that one is greater than the other
     * (hence uses a single-sided test).
     * The samples are not paired
     * and do not have to be of equal size,
     */
    public static double tNotPairedTwoSided(double[] s1, double[] s2) {
        return tNotPaired(s1, s2, true);
    }

    /**
     * Calculates the probability with which the
     * null hypothesis (that the means are equal)
     * can be rejected in favour of the alternative
     * hypothesis.  Uses a twoSided test if twoSided = true,
     * otherwise uses a one-sided test.
     */
    public static double tNotPaired(double[] s1, double[] s2, boolean twoSided) {

        double m1 = Stats.mean(s1);
        double m2 = Stats.mean(s2);
        double ss1 = sumSquareDiff(s1, m1);
        double ss2 = sumSquareDiff(s2, m2);

        return tNotPaired(m1, m2, ss1, ss2, s1.length, s2.length, twoSided);
    }

    public static double tNotPaired(
            double m1, double m2, double ss1, double ss2, int n1, int n2, boolean twoSided) {
        double nu = n1 + n2 - 2;
        double stderr =
                Math.sqrt((ss1 + ss2) * (1.0 / n1 + 1.0 / n2) / nu);
        double t = (m1 - m2) / stderr;

        if (twoSided)
            return tTest(t, nu);
        else
            return tSingle(t, nu);

    }

    /**
     * Applies a one-sided t-test to two arrays of paired samples.
     * The arrays must be the same size; failure to ensure this
     * could cause an ArrayOutOfBoundsException.
     */
    public static double tPairedOneSided(double[] s1, double[] s2) {
        return tPaired(s1, s2, false);
    }

    /**
     * Applies a two-sided t-test to two arrays of paired samples.
     * The arrays must be the same size; failure to ensure this
     * could cause an ArrayOutOfBoundsException.
     */

    public static double tPairedTwoSided(double[] s1, double[] s2) {
        return tPaired(s1, s2, true);
    }

    /**
     * Applies a t-test to two arrays of paired samples.
     * One or two-sided is chosen depending on the value of
     * the boolean variable 'two-sided'.
     * The arrays must be the same size; failure to ensure this
     * could cause an ArrayOutOfBoundsException.
     */

    public static double tPaired(double[] s1, double[] s2, boolean twoSided) {
        double[] d = new double[s1.length];
        for (int i = 0; i < d.length; i++)
            d[i] = s1[i] - s2[i];
        return tPaired(d, twoSided); // , Stats.mean(s1) - Stats.mean(s2));
    }

    /*
      private static double tPaired2(double[] s1, double[] s2) {
        double var1 = Stats.variance(s1);
        double var2 = Stats.variance(s2);
        double covar = Stats.covar(s1, s2);
        // System.out.println("Covar = " + covar);
        double nu = s1.length - 1;
        double m1 = Stats.mean(s1);
        double m2 = Stats.mean(s2);
        double stderr = Math.sqrt((var1 + var2 - 2 * covar) / s1.length);
        double t = (m1 - m2) / stderr;
        return tTest(t, nu);
      }
    */
    /**
     * Applies the t-test to an array of the differences
     * between paired samples of observations.
     */

    public static double tPairedOneSided(double[] d) {
        return tPaired(d, false);
    }

    public static double tPairedTwoSided(double[] d) {
        return tPaired(d, true);
    }

    public static double tPaired(double[] d, boolean twoSided) {
        // need to decide whether to do single or double sided!
        double mean = Stats.mean(d);
        double variance = Stats.variance(d);
        // double var2 = sumSquareDiff(d, mean) / (d.length - 1);
        System.out.println(mean + " : " + variance);
        // Wait.Input();
        double stderr = Math.sqrt(variance / d.length);

        double nu = d.length - 1;

        double t = mean / stderr;

        System.out.println("t = " + t);

        if (twoSided)
            return tTest(t, nu);
        else
            return tSingle(t, nu);
    }

    /**
     * This method returns the distance x from the mean m
     * such that the area under the t distribution
     * (estimated from the data d) between (m - x) and (m + x) is equal
     * to conf.
     * <p/>
     * In other words, it finds the desired confidence interval of the
     * mean of the population from which the data is
     * drawn.  For example, if conf = 0.95, then there
     * is a 95% chance that the mean lies between (m - x) and (m + x).
     *
     * @param d    the array of data
     * @param conf the desired confidence interval
     * @return the spread around the sample mean of the population mean
     *         within that confidence interval
     */

    public static double confDiff(double[] d, double conf) {
        // find the alpha which gives this confidence level

        double mean = Stats.mean(d);
        // System.out.println("SDEV = " + Stats.sdev(d));
        double stderr = Stats.stderr(d);
        double nu = d.length - 1.0;

        double t = findt(1.0 - (1.0 - conf) / 1, nu);
        // System.out.println(t + "\t" + conf + "\t" + nu);

        return t * stderr;

    }

    private static void printConfs(double[] d, double conf, PrintStream ps) {
        double cd = confDiff(d, conf);
        double m = Stats.mean(d);
        ps.println("At " + conf + " : " + (m - cd) + " < " + m + " < " + (m + cd));
    }

    /**
     * Finds the value of t that would match the required
     * confidence and nu.
     */

    public static double findt(double conf, double nu) {
        double eps = 0.00001; // accuracy
        // do a binary search for it
        double lower = 1.0;
        double upper = 100.0;
        double mid = 0.0;


        for (int i = 0; i < 100; i++) {
            mid = (lower + upper) / 2;
            // each time, if mid is too big
            // fix lower to mid
            // else fix upper to mid

            double cur = tTest(mid, nu);
            if (Math.abs(conf - cur) < eps) {
                // System.out.println("Converged to " + cur + " in " + i + " iterations");
                return mid;
            }

            if (cur < conf) // mid too small
                lower = mid;
            else // mid too big
                upper = mid;
        }
        return mid;
    }

    /**
     * Bin root - a binary search for a square root.
     * I wrote this purely to check my recollection
     * of how this kind of search can be used to invert
     * functions.
     */

    private static double binRoot(double x) {
        double eps = 0.0001; // accuracy
        // do a binary search for it
        // find the square root of x using a binary search
        double lower = 0.0;
        double upper = x;
        double mid = 0.0;


        for (int i = 0; i < 100; i++) {
            mid = (lower + upper) / 2;
            // each time, if mid is too big
            // fix lower to mid
            // else fix upper to mid

            double cur = mid * mid;
            if (Math.abs(x - cur) < eps) {
                // System.out.println("Converged to " + cur + " in " + i + " iterations");
                return mid;
            }

            if (cur < x) // mid too small
                lower = mid;
            else // mid too big
                upper = mid;
        }
        return mid;
    }

    /**
     * Applies the two-sided t-test given the value of t and nu.
     * To do this it calls betai.
     */

    public static double tTest(double t, double nu) {
        double a = nu / 2.0;
        double b = 0.5;
        double x = nu / (nu + t * t);
        return 1.0 - betai(a, b, x); // to be done
    }

    /**
     * Applies the single-sided t-test given the value of t and nu.
     * To do this it calls betai.
     */

    public static double tSingle(double t, double nu) {
        return 1.0 - (1.0 - tTest(t, nu)) / 2;
    }

    protected static double gammln(double xx) {
        double stp = 2.50662827465;

        double x, tmp, ser;
        x = xx - 1.0;
        tmp = x + 5.5;
        tmp = (x + 0.5) * Math.log(tmp) - tmp;
        ser = 1.0
                + 76.18009173 / (x + 1.0)
                - 86.50532033 / (x + 2.0)
                + 24.01409822 / (x + 3.0)
                - 1.231739516 / (x + 4.0)
                + 0.120858003 / (x + 5.0)
                - 0.536382e-5 / (x + 6.0);

        return tmp + Math.log(stp * ser); // finish
    }

    protected static double betai(double a, double b, double x) {
        // can be used to find t statistic
        double bt;
        if ((x < 0.0) || (x > 1.0))
            System.out.println("Error in betai: " + x);
        if ((x == 0.0) || (x == 1.0))
            bt = 0.0;
        else
            bt = Math.exp(gammln(a + b) - gammln(a) - gammln(b)
                    + a * Math.log(x)
                    + b * Math.log(1.0 - x));
        if (x < (a + 1.0) / (a + b + 2.0))
            return bt * betacf(a, b, x) / a;
        else
            return 1.0 - bt * betacf(b, a, 1.0 - x) / b;
    }

    protected static double betacf(double a, double b, double x) {

        int maxIts = 100;
        double eps = 3.0e-7;

        double tem, qap, qam, qab, em, d;
        double bz, bpp, bp, bm, az, app;
        double am, aold, ap;

        am = 1.0;
        bm = 1.0;
        az = 1.0;
        qab = a + b;
        qap = a + 1.0;
        qam = a - 1.0;
        bz = 1.0 - qab * x / qap;
        for (int m = 1; m <= maxIts; m++) {
            em = m;
            tem = em + em;
            d = em * (b - m) * x / ((qam + tem) * (a + tem));
            ap = az + d * am;
            bp = bz + d * bm;
            d = -(a + em) * (qab + em) * x / ((a + tem) * (qap + tem));
            app = ap + d * az;
            bpp = bp + d * bz;
            aold = az;
            am = ap / bpp;
            bm = bp / bpp;
            az = app / bpp;
            bz = 1.0;
            if (Math.abs(az - aold) < eps * Math.abs(az))
                return az;
        }
        System.out.println("a or b too big, or maxIts too small");
        return -1;
    }

    /**
     * Fills an array with uniform random numbers
     * within +/- 0.5 of the mean.
     */

    private static void fillUniform(double[] d, double mean) {
        for (int i = 0; i < d.length; i++)
            d[i] = Math.random() + mean - 0.5;
    }

    private static void test() {
        System.out.println(tTest(1.311, 29));
        System.out.println(tSingle(1.311, 29));
        System.out.println(tTest(1.699, 29));
        System.out.println(tTest(2.045, 29));
        System.out.println(tTest(0.9, 29));
        System.out.println(tTest(0.95, 29));
    }

    /**
     * This uses an example from Statistics for Business and Economics
     * (page 293 - 294)
     * to check the calculation of the confidence intervals
     * for the mean of a dataset.
     * <p/>
     * The data is: double[] mpg = {18.6, 18.4, 19.2, 20.8, 19.4, 20.5};
     * <p/>
     * Running the program proiduces the following output:
     * <p/>
     * <pre>
     * At 0.8  : 18.89 < 19.48 < 20.07
     * At 0.9  : 18.67 < 19.48 < 20.29
     * At 0.95 : 18.45 < 19.48 < 20.51
     * At 0.99 : 17.86 < 19.48 < 21.09
     * </pre>
     * <p/>
     * <p/>
     * This matches closely with the book - any differences
     * are due to small errors in the book version due to
     * the limited number of decimal places used (2) in the
     * handworked example.
     */

    public static void confTest() {
        double[] mpg = {18.6, 18.4, 19.2, 20.8, 19.4, 20.5};

        // System.out.println("Variance2 = " + Stats.variance2(mpg));

        // System.out.println("Mean = " + Stats.mean(mpg));
        // System.out.println("Var = " + Stats.variance(mpg));
        // Wait.Input();
        // System.exit(0);

        confTest(mpg);
        // Wait.Input();
    }

    private static void confTest(double[] s) {

        printConfs(s, 0.8, System.out);
        printConfs(s, 0.9, System.out);
        printConfs(s, 0.95, System.out);
        printConfs(s, 0.99, System.out);

    }

    private static void addConst(double[] v1, double[] v2, double c) {
        for (int i = 0; i < v1.length; i++)
            v2[i] = v1[i] + c;
    }

    /**
     * Runs a t-test on some text book data.
     * <p/>
     * The data is from page 362 of Statistics for Business and Economics
     */

    private static void testT() {

        double[] s1 = {137, 135, 83, 125, 47, 46, 114, 157, 57, 144};
        double[] s2 = {53, 114, 81, 86, 34, 66, 89, 113, 88, 111};

        System.out.println("(Paired (one))     Reject h0 with prob. " + tPairedOneSided(s1, s2));
        System.out.println("(Paired (two))     Reject h0 with prob. " + tPairedTwoSided(s1, s2));
        System.out.println("(Not paired (one)) Reject h0 with prob. " + tNotPairedOneSided(s1, s2));
        System.out.println("(Not paired (two)) Reject h0 with prob. " + tNotPairedTwoSided(s1, s2));

    }


    /**
     * Runs through some text-book utils to check that
     * the statistical tests are working properly.
     */

    public static void main(String[] args) {

        // test the t-statistic

        // example from business stats book used in order to check it

        confTest();
        // testT();
        System.exit(0);
        /*
        test();

        System.out.println("\n\n" + binRoot(81));

        Wait.Input();

        double t = 0.11;
        double nu = 9.0;

        System.out.println(tTest(t, nu));

        */
        int n = 10;

        double[] dd = new double[n];
        fillUniform(dd, 0.0);

        double[] d = new double[n];
        for (int i = 0; i < 10; i++) {
            fillUniform(d, i / 10.0);
            // addConst(dd, d, i / 10.0);
            // System.out.println(i + "\t " + tPaired(d, dd)  + "\t " + tPaired2(d, dd) + "\t" + tNotPaired(d, dd));
        }

    }

}

