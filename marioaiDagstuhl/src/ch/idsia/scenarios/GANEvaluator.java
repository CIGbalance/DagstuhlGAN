/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.idsia.scenarios;

import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.level.LevelParser;

/**
 *
 * @author vv
 */
public class GANEvaluator {
    public static void main(String[] args){
        String filename = "/media/vv/DATA/svn/DagstuhlGAN/lvlexample.txt";
        LevelParser parser = new LevelParser();
        Level level = parser.createLevelASCII(filename);
    }
}
