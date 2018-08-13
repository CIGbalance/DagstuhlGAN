package basicMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class Settings {
    public static final String WARN_MSG = "[WARN] ";

    public static final String DEBUG_MSG = "[DEBUG] ";
    public static final String ERROR_MSG = "[ERROR] ";
    public static final String INFO_MSG = "[INFO] ";
    public static final double MAX_VALUE = 1000000.0;

    public static final boolean ACCESSIBLE = true;

    public static final String CMD_SEPARATOR = " ";
    public static final String WASSERSTEIN_PATH = "pytorch" + File.separator + "generator_ws.py";
    public static final String WASSERSTEIN_GAN = "pytorch" + File.separator + "netG_epoch_5000.pth";
    public static final String GAN_DIM = "32";
	
	// Jacob: IMPORTANT! This is a system-specific path that I had to set.
	//public static String PYTHON_PROGRAM = "/anaconda/bin/python";
    public static String PYTHON_PROGRAM = "python";

    public static void printWarnMsg(String msg) {
        System.out.println(WARN_MSG + msg);
    }

    public static void printDebugMsg(String msg) {
        System.out.println(DEBUG_MSG + msg);
    }

    public static void printInfoMsg(String msg) {
        System.out.println(INFO_MSG + msg);
    }

    public static void printErrorMsg(String msg) {
        System.out.println(ERROR_MSG + msg);
    }


    public static void setPythonProgram() {
        try {
            Settings.PYTHON_PROGRAM = Files.readAllLines(Paths.get("my_python_path.txt")).get(0); // Should only have one line, get first
        } catch (IOException e) {
            printErrorMsg("Can not find the my_python_path.txt which specifies the python program and locates under DagstuhlGAN.");
            e.printStackTrace();
        }
    }
    
    public static final java.util.Map<Character, Integer> tiles = new HashMap();
    
    static {
        tiles.put('X', 0); //solid
        tiles.put('x', 1); //breakable
        tiles.put('-', 2); //passable
        tiles.put('q', 3); //question with coin
        tiles.put('Q', 4); //question with power up
        tiles.put('o', 5); //coin
        tiles.put('t', 6); //tube
        tiles.put('p', 7); //piranha plant tube
        tiles.put('b', 8); //bullet bill
        tiles.put('g', 9); //goomba
        tiles.put('k', 10); //green koopas + paratroopas
        tiles.put('r', 11); //red koopas + paratroopas
        tiles.put('s', 12); //spiny + winged spiny
    }
    
    public static final java.util.Map<Integer, Integer> tilesMario = new HashMap();
    //encoding can be found in LevelScene ZMap    
    static {
        tilesMario.put(0, 9); //solid
        tilesMario.put(1, 16); //breakable
        tilesMario.put(2, 0); //passable
        tilesMario.put(3, 21); //question with coin
        tilesMario.put(4, 22); //question with power up
        tilesMario.put(5, 34); //coin
    }
    
    public static final java.util.Map<String, Integer> tilesAdv = new HashMap();
    //numbers from the picture files "mapsheet.png"
    static{
        tilesAdv.put("bb", 14+0*16); //bullet bill shooter
        tilesAdv.put("bbt", 14+1*16); //bullet bill top
        tilesAdv.put("bbb", 14+2*16); //bullet bill bottom
        tilesAdv.put("ttl", 10+0+0*16); //tube top left
        tilesAdv.put("ttr", 10+1+0*16); //tube top right
        tilesAdv.put("tbl", 10+0+1*16); //tube bottom left
        tilesAdv.put("tbr", 10+1+1*16); //tube bottom right
    }

    
}
