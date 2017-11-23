package ch.idsia.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

/**
 * This class is used to model the statistics
 * of a fix of numbers.  For the statistics
 * we choose here it is not necessary to store
 * all the numbers - just keeping a running total
 * of how many, the sum and the sum of the squares
 * is sufficient (plus max and min, for max and min).
 * <p/>
 * Warning: the geometric mean calculation is only valid if all numbers
 * added to the summary are positive (>0) - no warnings
 * are given if this is not the case - you'll just get a wrong answer
 * for the gm() !!!
 */

public class StatisticalSummary implements java.io.Serializable {

    // a temporary fix for an immediate need
    // this should really be handled with a more general
    // predicate class

    public static class Watch {
        double x;
        public int count;

        public Watch(double x) {
            this.x = x;
            count = 0;
        }

        public void note(double val) {
            if (val == x) {
                count++;
            }
        }

        public String toString() {
            return x + " occured " + count + " times ";
        }

        public void reset() {
            count = 0;
        }
    }

    // following line can cause prog to hang - bug in Java?
    // protected long serialVersionUID = new Double("-1490108905720833569").longValue();
    // protected long serialVersionUID = 123;
    public String name; // defaults to ""
    private double logsum; // for calculating the geometric mean
    private double sum;
    private double sumsq;
    private double min;
    private double max;

    private double mean;
    private double gm; // geometric mean
    private double sd;

    // trick class loader into loading this now
    // private static StatisticalTests dummy = new StatisticalTests();

    int n;
    boolean valid;
    public Watch watch;

    public StatisticalSummary() {
        this("");
        // System.out.println("Exited default...");
    }

    public StatisticalSummary(String name) {
        // System.out.println("Creating SS");
        this.name = name;
        n = 0;
        sum = 0;
        sumsq = 0;
        // ensure that the first number to be
        // added will fix up min and max to
        // be that number
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
        // System.out.println("Finished Creating SS");
        watch = null;
        valid = false;
    }

    public final void reset() {
        n = 0;
        sum = 0;
        sumsq = 0;
        logsum = 0;
        // ensure that the first number to be
        // added will fix up min and max to
        // be that number
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
        if (watch != null) {
            watch.reset();
        }
    }


    public double max() {
        return max;
    }

    public double min() {
        return min;
    }

    public double mean() {
        if (!valid)
            computeStats();
        return mean;
    }

    public double gm() {
        if (!valid)
            computeStats();
        return gm;
    }
/* erroneous
  public static double sigDiff( StatisticalSummary s1 , StatisticalSummary s2 ) {
    return StatisticalTests.tNotPaired(
      s1.mean(), s2.mean(), s1.sumsq, s2.sumsq, s1.n, s2.n, true);
  }
*/

    public static double sigDiff(StatisticalSummary s1, StatisticalSummary s2) {
        return StatisticalTests.tNotPaired(
                s1.mean(), s2.mean(), s1.sumSquareDiff(), s2.sumSquareDiff(), s1.n, s2.n, true);
    }

    /**
     * returns the sum of the squares of the differences
     * between the mean and the ith values
     */
    public double sumSquareDiff() {
        return sumsq - n * mean() * mean();
    }

    private void computeStats() {
        if (!valid) {
            mean = sum / n;
            gm = Math.exp(logsum / n);
            double num = sumsq - (n * mean * mean);
            if (num < 0) {
                // avoids tiny negative numbers possible through imprecision
                num = 0;
            }
            // System.out.println("Num = " + num);
            sd = Math.sqrt(num / (n - 1));
            // System.out.println(" Test: sd = " + sd);
            // System.out.println(" Test: n = " + n);
            valid = true;
        }
    }

    public double sd() {
        if (!valid)
            computeStats();
        return sd;
    }

    public int n() {
        return n;
    }

    public double stdErr() {
        return sd() / Math.sqrt(n);
    }

    public void add(StatisticalSummary ss) {
        // implications for Watch?
        n += ss.n;
        sum += ss.sum;
        sumsq += ss.sumsq;
        logsum += ss.logsum;
        max = Math.max(max, ss.max);
        min = Math.min(min, ss.min);
        valid = false;
    }

    public void add(double d) {
        n++;
        sum += d;
        sumsq += d * d;
        if (d > 0) {
            logsum += Math.log(d);
        }
        min = Math.min(min, d);
        max = Math.max(max, d);
        if (watch != null) {
            watch.note(d);
        }
        valid = false;
    }

    public void add(Number n) {
        add(n.doubleValue());
    }

    public void add(double[] d) {
        for (int i = 0; i < d.length; i++) {
            add(d[i]);
        }
    }

    public void add(Vector v) {
        for (int i = 0; i < v.size(); i++) {
            try {
                add(((Number) v.elementAt(i)).doubleValue());
            } catch (Exception e) {
            }
        }
    }

    public String toString() {
        String s = (name == null) ? "" : name + "\n";
        s += " min = " + min() + "\n" +
                " max = " + max() + "\n" +
                " ave = " + mean() + "\n" +
                " sd  = " + sd() + "\n" +
               // " se  = " + stdErr() + "\n" +
               // " sum  = " + sum + "\n" +
               // " sumsq  = " + sumsq + "\n" +
               // " watch = " + watch + "\n" +
                " n   = " + n;
        return s;

    }

    public void save(String path) {
        try {
            ObjectOutputStream oos =
                    new ObjectOutputStream(
                            new FileOutputStream(path));
            oos.writeObject(this);
            oos.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static StatisticalSummary load(String path) {
        try {
            ObjectInputStream ois =
                    new ObjectInputStream(
                            new FileInputStream(path));
            StatisticalSummary ss = (StatisticalSummary) ois.readObject();
            ois.close();
            return ss;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }


    public static void main(String[] args) throws Exception {
        // demonstrate some possible usage...

        StatisticalSummary ts1 = new StatisticalSummary();
        StatisticalSummary ts2 = new StatisticalSummary();
        for (int i = 0; i < 100; i++) {
            ts1.add(i / 10);
            ts2.add(i / 10 + new Double(args[0]).doubleValue());
        }

        System.out.println(ts1);
        System.out.println(ts2);
        System.out.println(StatisticalSummary.sigDiff(ts1, ts2));
        System.out.println((ts2.mean() - ts1.mean()) / ts1.stdErr());

        System.exit(0);

        System.out.println("Creating summaries");

        StatisticalSummary trainSummary = new StatisticalSummary();
        System.out.println("1");
        // StatisticalSummary testSummary = new VisualSummary("EA");
        System.out.println("2");
        // testSummary.watch = new StatisticalSummary.Watch( 1.0 );
        System.out.println("3");
        // StatisticalSummary ostiaTrainSummary = new StatisticalSummary();
        System.out.println("4");
        // ostiaTestSummary = new VisualSummary("OSTIA");
        System.out.println("5");
        // ostiaTestSummary.watch = new StatisticalSummary.Watch( 1.0 );

        System.out.println("Created summaries");


        StatisticalSummary s10 = new StatisticalSummary();
        StatisticalSummary s20 = new StatisticalSummary();
        StatisticalSummary s3 = new StatisticalSummary();
        StatisticalSummary s4 = new StatisticalSummary();
        StatisticalSummary s5 = new StatisticalSummary();
        StatisticalSummary ss = new StatisticalSummary("Hello");
        for (int i = 0; i < 20; i++) {
            ss.add(0.71);
        }
        System.out.println(ss);
        System.exit(0);

        StatisticalSummary s1 = new StatisticalSummary();
        StatisticalSummary s2 = new StatisticalSummary();

        System.out.println(sigDiff(s1, s2));

        for (int i = 0; i < 20; i++) {
            s1.add(Math.random());
            s2.add(Math.random() + 0.5);
            // s1.add(i);
            // s2.add(i+2);
            System.out.println(sigDiff(s1, s2));
        }
    }
}
