package reader;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

import static basicMap.Settings.printErrorMsg;
import static basicMap.Settings.printWarnMsg;

public class MarioReader {

    static Map<Character, Integer> tiles = new HashMap();

    static {
        tiles.put('X', 0);
        tiles.put('S', 1);
        tiles.put('-', 2);
        tiles.put('?', 3);
        tiles.put('Q', 4);
        tiles.put('E', 5);  //Enemy
        tiles.put('<', 6);
        tiles.put('>', 7);
        tiles.put('[', 8);
        tiles.put(']', 9);
        tiles.put('o', 10);
        // B and b are not mentioned in the json from VGLC, but represent the cannon and support for launching Bullet Bills
        tiles.put('B', 11); 
        tiles.put('b', 12);
    }

    // Is this width value may be too small for the GAN to learn well?
    static int targetWidth = 28;

    public static void main(String[] args) throws Exception {
        String dir = System.getProperty("user.dir");
        System.out.println("Working Directory = " +
                dir);
        dir += "/marioaiDagstuhl/";
        // String inputFile = "data/mario/example.txt";

        String inputDirectory = "/media/vv/DATA/svn/GameGAN/data/mario/levels/";

        String outputFile = "/media/vv/DATA/svn/GameGAN/data/mario/levels/levels.json";
        String outputdir = "/media/vv/DATA/svn/GameGAN/data/mario/levels/";

        // need to iterate over all the files in a directory

        ArrayList<int[][]> examples = new ArrayList<>();

        File file = new File(inputDirectory);
        String[] fileList = file.list();
        
        //System.out.println(fileList);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        for (String inputFile : fileList) {
            try {
                System.out.println("Reading: " + inputFile);
                int[][] level = readLevel(new Scanner(new FileInputStream(inputDirectory + inputFile)));
                addData(examples, level);
                System.out.println(level);
                System.out.println("Read: " + inputFile);
                
                ArrayList<int[][]> examplesTmp = new ArrayList<>();
                addData(examplesTmp, level);
                String outTmp = gson.toJson(examplesTmp);
                System.out.println("Created JSON String");
                
                PrintWriter writerTmp = new PrintWriter(outputdir + "example" + inputFile + ".json");
                writerTmp.print(outTmp);
                writerTmp.close();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // System.out.println(examples);

        System.out.println("Processed examples");

        String out = gson.toJson(examples);
        System.out.println("Created JSON String");

        // System.out.println(out);

        PrintWriter writer = new PrintWriter(outputFile);

        writer.print(out);
        writer.close();

        System.out.println("Wrote file with " + examples.size() + " examples");


        /*System.out.println("======== test 1 ===============");
        Random rdm = new Random();

        int[][] input1 = new int[5][10];
        for (int i=0;i<input1.length;i++) {
            for (int j=0;j<input1[0].length;j++) {
                input1[i][j] = rdm.nextInt();
            }
        }
        System.out.println(arrayToString(input1));

        System.out.println("======== test 2 ===============");
        int[][] input2 = new int[5][1];
        for (int i=0;i<input2.length;i++) {
            for (int j=0;j<input2[0].length;j++) {
                input2[i][j] = rdm.nextInt();
            }
        }
        System.out.println(arrayToString(input2));*/

    }

    static void addData(ArrayList<int[][]> examples, int[][] level) {
        int h = level.length;

        for (int offset = 0; offset < level[0].length - 1 - targetWidth; offset++) {
            int[][] example = new int[h][targetWidth];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < targetWidth; x++) {
                    example[y][x] = level[y][x + offset];
                }
            }
            examples.add(example);
        }
    }

//    static int[][][][] makeExamples(int[][] level) {
//
//    }

    static int[] oneHot(int x) {
        // System.out.println("Tiles size: " + tiles.size());
        int[] vec = new int[tiles.size()];
        // System.out.println("Index = " + x);
        vec[x] = 1;
        return vec;
    }

    static int[][] readLevel(Scanner scanner) throws Exception {
        String line;
        ArrayList<String> lines = new ArrayList<>();
        int width = 0;
        while (scanner.hasNext()) {
            line = scanner.nextLine();
            width = line.length();
            lines.add(line);
            // System.out.println(line);
        }

        int[][] a = new int[lines.size()][width];
        System.out.println("Arrays length: " + a.length);
        for (int y = 0; y < lines.size(); y++) {
            System.out.println("Processing line: " + lines.get(y));
            for (int x = 0; x < width; x++) {
            	try { // Added error checking to deal with unrecognized tile types
                a[y][x] = tiles.get(lines.get(y).charAt(x));
            	} catch(Exception e) {
            		System.out.println("Problem on ");
            		System.out.println("\ty = " + y);
            		System.out.println("\tx = " + x);
            		System.out.println("\tlines.get(y).charAt(x) = " + lines.get(y).charAt(x));
            		System.exit(1);
            	}
            }
        }

        return a;
    }

    static String arrayToString(int[][] inputArray) {
        String outputStr = "";

        // Null array
        if (inputArray == null) {
            printWarnMsg("arrayToString: null argument passed.");
            return outputStr;
        }

        // Empty array
        int nbRows = inputArray.length;
        if (nbRows==0 ) {
            printWarnMsg("arrayToString: input array is empty.");
            outputStr += "[]";
            return outputStr;
        }
        // Empty array
        int nbCols = inputArray[0].length;
        if (nbCols==0) {
            printWarnMsg("arrayToString: input array is empty.");
            outputStr += "[";
            outputStr += "[]";
            for (int i=1; i<nbRows; i++) { // TODO: 24/11/2017 will this happen?
                outputStr += ", []";
            }
            outputStr += "]";
            return outputStr;
        }

        // Non-empty array case
        outputStr += "[";   // matrix starter
        int i=0;
        outputStr += "[";   // row starter
        outputStr += inputArray[i][0];
        for (int j = 1; j < nbCols - 1; j++) { // column
            outputStr += "," + inputArray[i][j];
        }
        outputStr += "]";
        for (i=1; i<nbRows-1; i++) { // loop rows
            outputStr += ", [";   // row starter
            outputStr += inputArray[i][0];
            for (int j = 1; j < nbCols - 1; j++) { // column
                outputStr += "," + inputArray[i][j];
            }
            outputStr += "]";
        }
        outputStr += "]";
        return outputStr;
    }

}
