/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.idsia.mario.engine.level;

import basicMap.Settings;
import ch.idsia.mario.engine.sprites.Enemy;
import java.io.FileInputStream;
import java.util.List;
import java.util.Scanner;
import reader.MarioReader;

/**
 *
 * @author vv
 */
public class LevelParser {
    
    public static final int BUFFER_WIDTH = 15; // This is the extra space added at the start and ends of levels
    public LevelParser(){
        
    }    
    
     
    //For Testing purposes to creae straight from example files
    public static Level createLevelASCII(String filename) throws Exception
    {
    	int[][] level = MarioReader.readLevel(new Scanner(new FileInputStream(filename)));
        return createLevel(level);        
    }

    public static Level createLevel(int[][] input){
        int width = input[0].length;
    	int height = input.length;
    	int extraStones = BUFFER_WIDTH;
    	Level level = new Level(width+2*extraStones,height);
        //Set Level Exit
        //Extend level by that
        level.xExit = width+extraStones+1; // Push exit point over by 1 so that goal post does not overlap with other level sprites
        level.yExit = height-1;
        
        for(int i=0; i<extraStones; i++){
            level.setBlock(i, height-1, (byte) 9);
        }
        for(int i=0; i<extraStones; i++){
            level.setBlock(width+i+extraStones, height-1, (byte) 9);
        }
        
        //set Level map
        //revert order of iterating rows bottom -> top (so that below tiles can be checked for building tubes etc)
        for(int i=height-1; i>=0; i--){
            for(int j=width-1; j>=0; j--){
                int code = input[i][j];
                if(code>=9){
                    //set Enemy
                    int code_below=0;
                    if(i+1<height){//just in case we are in bottom row
                       code_below = input[i+1][j]; 
                    }
                    level.setSpriteTemplate(j+extraStones, i, getEnemySprite(code,code_below==2));
                }else if(code==8){//bullet bill
                    level.setBlock(j + extraStones, i, Settings.tilesAdv.get("bb").byteValue());//bullet bill shooter
                    if(i+1<height && input[i+1][j]==2){
                        level.setBlock(j + extraStones, i+1, Settings.tilesAdv.get("bbt").byteValue());//bullet bill top 
                        for(int k=i+2; k<height; k++){
                            if(input[k][j]==2){
                                level.setBlock(j+extraStones, k, Settings.tilesAdv.get("bbb").byteValue());
                            }else{
                                break;
                            }
                        }
                    }
                }else if(code==6 || code==7){//tubes + plants
                    level.setBlock(j + extraStones, i, Settings.tilesAdv.get("ttl").byteValue());
                    level.setBlock(j + extraStones +1, i, Settings.tilesAdv.get("ttr").byteValue());
                    for(int k=i+1; k<height; k++){
                        if(input[k][j]==2 || (j<width-1 && input[k][j+1]==2)){
                            level.setBlock(j+extraStones, k, Settings.tilesAdv.get("tbl").byteValue());
                            level.setBlock(j+extraStones+1, k, Settings.tilesAdv.get("tbr").byteValue());
                        }else{
                            break;
                        }
                    }
                    if(code==7){
                        level.setSpriteTemplate(j + extraStones, i, new SpriteTemplate(Enemy.ENEMY_FLOWER, false));
                    }
                }else if(code!=2) {
                    level.setBlock(j+extraStones, i, Settings.tilesMario.get(code).byteValue());
                }
            }
        }
                
        return level;
    }

    private static int[] toIntArray(List<Integer> list){
        int[] ret = new int[list.size()];
        for(int i = 0;i < ret.length;i++)
            ret[i] = list.get(i);
        return ret;
    }
    
    public static Level createLevelJson(List<List<Integer>> input)
    {
        int[][] output = new int[input.size()][];
        int i = 0;
        for (List<Integer> nestedList : input) {
            output[i++] = toIntArray(nestedList);
        }
        return createLevel(output);
    }
  
    
    public static SpriteTemplate getEnemySprite(int code, boolean flying){
        int type = 0;
        switch(code){
            case 9:
                type=Enemy.ENEMY_GOOMBA;
                break;
            case 10:
                type=Enemy.ENEMY_GREEN_KOOPA;
                break;
            case 11:
                type=Enemy.ENEMY_RED_KOOPA;
                break;
            case 12:
                type=Enemy.ENEMY_SPIKY;
                break;
        }
        SpriteTemplate enemy = new SpriteTemplate(type, flying);
        return enemy;
    }
    
    
       public Level test(){
        Level level = new Level(202,14);
        level.setBlock(1, 13, (byte) 9);
        level.setBlock(2, 13, (byte) 9);
        level.setBlock(3, 13, (byte) 9);
        level.setBlock(4, 13, (byte) 9);
        level.setBlock(5, 13, (byte) 9);
        level.setBlock(6, 13, (byte) 9);
        level.setBlock(7, 13, (byte) 9);
        level.setBlock(4, 10, (byte) 9);
        //level.setSpriteTemplate(3,10, new SpriteTemplate(100, false));
        //level.setBlock(6,10,(byte)(14));
        //level.setBlock(6,11,(byte)(14+16));
        //level.setBlock(6,12,(byte)(14+2*16));
        level.setBlock(3, 10, (byte) 24);
        level.setBlock(6, 10, (byte) 25);
        level.setBlock(7, 10, (byte) 18);
        level.setBlock(5, 10, (byte) 23);
        
        return level;
    }
}
