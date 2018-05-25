/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coco;

import basicMap.Settings;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.level.LevelParser;
import cmatest.MarioEvalFunction;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import reader.JsonReader;
import static reader.JsonReader.JsonToDoubleArray;

/**
 *
 * @author vv
 */
public class evaluateLevel {

	public static final int BLOCK_SIZE = 16;
	public static final int LEVEL_HEIGHT = 14;	
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        Settings.setPythonProgram();
	MarioEvalFunction eval = null;
        
	// Read input level
	if (args.length > 0) {
            String strLatentVector = args[0].toString();
            //System.out.println(strLatentVector);
            //String strLatentVector = "[0.577396866201949, 0.7814522617215477, -0.4290037786827649, -0.7939910428259774, 0.4272655228644559, -0.4788319759161429, 0.7092257647567968, -0.7713656070501105, 0.751081985876608, -0.7008837870643055, -0.8246193253163093, 0.4346130846640391, 0.2710622366012579, 0.3800424999852043, -0.34590660496032954, -0.19199367234086404, 0.5044818940867084, -0.011195732877252087, 0.5709757275778345, -0.7538647677412429, 0.47367984400311164, -0.5113195303794107, -0.12246166856201363, -0.4884180730654383, 0.43980759625918864, -0.2091391993828877, 0.17176289356831398, -0.800069855849241, 0.3543235185624359, 0.2933150105639751, -0.6907799166245733, 0.570466705780844]";
            strLatentVector = strLatentVector.replace("[", "");
            strLatentVector = strLatentVector.replace("]", "");
            //System.out.println(strLatentVector);
            String[] parts = strLatentVector.split(", ");
            //System.out.println(parts);
            double [] level = new double[parts.length];
            for(int i=0; i<parts.length; i++){
                level[i] = Double.valueOf(parts[i]);
            }
            
            String problem = args[1].toString();
            String dim = args[2].toString();
            eval = new MarioEvalFunction(problem, dim);
            
                    
            if(!eval.isFeasible(level)){
                PrintWriter writer = new PrintWriter("objectives.txt", "UTF-8");
                writer.println("0");
                writer.close();
            }else{
                PrintWriter writer = new PrintWriter("objectives.txt", "UTF-8");
                writer.println("1");
                writer.println(eval.valueOf(level));
                writer.close();
            }
	} else {
            throw new IOException("no input received");
	}
        eval.exit();
        System.exit(0);
    }
    
}
