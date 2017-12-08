package communication;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

import static basicMap.Settings.printWarnMsg;

public class ZCreator {
    public static void main(String[] args) throws Exception {
        int n = 16;
        Random random = new Random();

        double[] x = new double[n];

        for (int i=0; i<n; i++) {
            x[i] = random.nextGaussian();
        }


        double[][] a = new double[1][];
        a[0] = x;

        // now write it out

        Gson gson = new Gson();
        String out = gson.toJson(a);
        System.out.println("Gson: " + out);

        PrintWriter printWriter = new PrintWriter("samplez.json");

        printWriter.println(out);
        printWriter.close();


    }

    /**
     * Generator a Gaussian random vector
     * @param dim
     * @return
     */
    public static double[][] generateGaussianRandomVector(int dim) {
        Random random = new Random();
        double[] x = new double[dim];
        for (int i=0; i<dim; i++) {
            x[i] = random.nextGaussian();
        }
        double[][] v = new double[1][];
        v[0] = x;
        return v;
    }

    /**
     * Write a vector to gson file
     * @param v
     */
    public static void writeVectorToGson(double[][] v, String filename) {
        Gson gson = new Gson();
        String out = gson.toJson(v);
        PrintWriter printWriter = null;
        if (filename==null || filename=="") {
            printWarnMsg("ZCreator:writeVectorToGson: output filename is null or empty, will write to samplez.json.");
            filename = "samplez.json";
        }
        try {
            printWriter = new PrintWriter(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        printWriter.println(out);
        printWriter.close();
    }

    public static void printSampleVectorInGson() {
        double[][] v = generateGaussianRandomVector(16);
        Gson gson = new Gson();
        String out = gson.toJson(v);
        System.out.println(out);
    }

    public static String sampleVectorInGson() {
        double[][] v = generateGaussianRandomVector(16);
        Gson gson = new Gson();
        String out = gson.toJson(v);
        return out;
    }

    public static String printVectorInGson(double[][] v) {
        Gson gson = new Gson();
        String out = gson.toJson(v);
        return out;
    }
}
