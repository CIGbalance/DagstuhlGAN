/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.idsia.mario.engine.level;

import ch.idsia.mario.engine.sprites.Enemy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author vv
 */
public class LevelParser {
    
    /**
     *
     * @param args
     */
    public static void main(String[] args){
        Level createdLevel;
        createdLevel = createLevel("/media/vv/DATA/svn/DagstuhlGAN/lvlexample.txt");
    }
    
    
    /*"tiles" : {
    0    "X" : ["solid","ground"],
    1    "S" : ["solid","breakable"],
    2    "-" : ["passable","empty"],
    3    "?" : ["solid","question block", "full question block"],
    4    "Q" : ["solid","question block", "empty question block"],
    5    "E" : ["enemy","damaging","hazard","moving"],
    6    "<" : ["solid","top-left pipe","pipe"],
    7    ">" : ["solid","top-right pipe","pipe"],
    8    "[" : ["solid","left pipe","pipe"],
    9    "]" : ["solid","right pipe","pipe"],
    10   "o" : ["coin","collectable","passable"]
    */
    
     public Level createLevel(String filename)
    {
        //Read in level representation
        ArrayList<String> lines = new ArrayList<String>();
        try {
            File file = new File(filename);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                    lines.add(line);
            }
            fileReader.close();
        } catch (IOException e) {
                    e.printStackTrace();
        }
        int width = lines.get(0).length();
        int height = lines.size();
        Level level = new Level(width,height);
        
        
        //Set Level Exit
        //Extend level by that
        level.xExit = width + 8;
        level.yExit = height - 1 - 4;
        
        
        //set Level map
        for(int i=0; i<height; i++){
            for(int j=0; j<width; j++){
                int code = (int) lines.get(i).charAt(j);
                if(code==5){
                    //set Enemy
                    //new SpriteTemplate(type, boolean winged)
                    level.setSpriteTemplate(j, i, new SpriteTemplate(Enemy.ENEMY_GOOMBA, false));
                    //set passable tile: everything not set is passable
                }else{
                    if(code==0){
                        
                    }else if(codeParser(code)!=0){
                        level.setBlock(j, i, (byte) code);
                    }
                }
            }
        }
                
        
        return level;
    }
     
    public int codeParser(int code){
        int output = 0;
        switch(code){
            case 1: output = 16; break; //"S" : ["solid","breakable"]
            case 3: output = 21; break; //"?" : ["solid","question block", "full question block"]
            case 6: output = 10; break; //"<" : ["solid","top-left pipe","pipe"]
            case 7: output = 11; break; //">" : ["solid","top-right pipe","pipe"]
            case 8: output = 26; break; //"[" : ["solid","left pipe","pipe"]
            case 9: output = 27; break; //"]" : ["solid","right pipe","pipe"]
            case 10: output = 34; break; //"o" : ["coin","collectable","passable"]
            default: output=0; break; //"-" : ["passable","empty"],  "Q" : ["solid","question block", "empty question block"],  "E" : ["enemy","damaging","hazard","moving"],
        }
        return output;
    }
    
    public int codeParserASCII(String code){
        int output = 0;
        switch(code){
            case "S": output = 16; break; //"S" : ["solid","breakable"]
            case "?": output = 21; break; //"?" : ["solid","question block", "full question block"]
            case "<": output = 10; break; //"<" : ["solid","top-left pipe","pipe"]
            case ">": output = 11; break; //">" : ["solid","top-right pipe","pipe"]
            case "[": output = 26; break; //"[" : ["solid","left pipe","pipe"]
            case "]": output = 27; break; //"]" : ["solid","right pipe","pipe"]
            case "o": output = 34; break; //"o" : ["coin","collectable","passable"]
            default: output=0; break; //"-" : ["passable","empty"],  "Q" : ["solid","question block", "empty question block"],  "E" : ["enemy","damaging","hazard","moving"],
        }
        return output;
    }
}
