package communication;

import com.google.gson.Gson;

import java.io.PrintWriter;
import java.util.Random;

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
        System.out.println(out);

        PrintWriter printWriter = new PrintWriter("samplez.json");

        printWriter.println(out);
        printWriter.close();


    }
}
