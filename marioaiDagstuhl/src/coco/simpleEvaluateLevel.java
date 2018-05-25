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
import static viewer.MarioRandomLevelViewer.randomUniformDoubleArray;

/**
 *
 * @author vv
 */
public class simpleEvaluateLevel {

	public static final int BLOCK_SIZE = 16;
	public static final int LEVEL_HEIGHT = 14;	
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        Settings.setPythonProgram();
	MarioEvalFunction eval = new MarioEvalFunction();
        int dim=32;
        
        double[] latentVector = randomUniformDoubleArray(dim);
        double result = eval.valueOf(latentVector);
        System.out.println(result);
        eval.exit();
        System.exit(0);
    }
    
}
