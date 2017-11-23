package ch.idsia.mario.engine;

import ch.idsia.mario.engine.level.BgLevelGenerator;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.level.LevelGenerator;
import ch.idsia.mario.engine.level.SpriteTemplate;
import ch.idsia.mario.engine.sprites.*;
import ch.idsia.mario.environments.Environment;
import ch.idsia.utils.MathX;

import java.awt.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class LevelScene extends Scene implements SpriteContext, Cloneable
{
    private List<Sprite> sprites = new ArrayList<Sprite>();
    private List<Sprite> spritesToAdd = new ArrayList<Sprite>();
    private List<Sprite> spritesToRemove = new ArrayList<Sprite>();

    public Level level;
    public Mario mario;
    public float xCam, yCam, xCamO, yCamO;
    public Image tmpImage;
    private int tick;

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    private LevelRenderer layer;
    private BgRenderer[] bgLayer = new BgRenderer[2];

    private GraphicsConfiguration graphicsConfiguration;

    public boolean paused = false;
    public int startTime = 0;
    public int timeLeft;

    public int getTotalTime() {  return totalTime; }

    public void setTotalTime(int totalTime) {  this.totalTime = totalTime; }

    private int totalTime = 200;

    //    private Recorder recorder = new Recorder();
    //    private Replayer replayer = null;

    private long levelSeed;
    private MarioComponent renderer;
    private int levelType;
    private int levelDifficulty;
    private int levelLength;
    public int killedCreaturesTotal;
    public int killedCreaturesByFireBall;
    public int killedCreaturesByStomp;
    public int killedCreaturesByShell;

    private static String[] LEVEL_TYPES = {"Overground(0)",
                                           "Underground(1)",
                                           "Castle(2)"};

    public LevelScene(GraphicsConfiguration graphicsConfiguration, MarioComponent renderer, long seed, int levelDifficulty, int type, int levelLength, int timeLimit)
    {
        this.graphicsConfiguration = graphicsConfiguration;
        this.levelSeed = seed;
        this.renderer = renderer;
        this.levelDifficulty = levelDifficulty;
        this.levelType = type;
        this.levelLength = levelLength;
        this.setTotalTime(timeLimit);
        killedCreaturesTotal = 0;
        killedCreaturesByFireBall = 0;
        killedCreaturesByStomp = 0;
        killedCreaturesByShell = 0;
    }

    private String mapElToStr(int el)
    {
        String s = "";
        if  (el == 0 || el == 1)
            s = "##";
        s += (el == mario.kind) ? "#M.#" : el;
        while (s.length() < 4)
            s += "#";
        return s + " ";
    }

    private String enemyToStr(int el)
        {
            String s = "";
            if  (el == 0)
                s = "";
            s += (el == mario.kind) ? "-m" : el;
            while (s.length() < 2)
                s += "#";
            return s + " ";
        }

    private byte ZLevelMapElementGeneralization(byte el, int ZLevel)
    {
        if (el == 0)
            return 0;
        switch (ZLevel)
        {
            case(0):
                switch(el)
                {
                    case 16:  // brick, simple, without any surprise.
                    case 17:  // brick with a hidden coin
                    case 18:  // brick with a hidden flower
                        return 16; // prevents cheating
                    case 21:       // question brick, contains coin
                    case 22:       // question brick, contains flower/mushroom
                        return 21; // question brick, contains something
                }
                return el;
            case(1):
                switch(el)
                {
                    case 16:  // brick, simple, without any surprise.
                    case 17:  // brick with a hidden coin
                    case 18:  // brick with a hidden flower
                        return 16; // prevents cheating
                    case 21:       // question brick, contains coin
                    case 22:       // question brick, contains flower/mushroom
                        return 21; // question brick, contains something                    
                    case(-108):
                    case(-107):
                    case(-106):
                    case(15): // Sparcle, irrelevant
                    case(34): // Coin, irrelevant for the current contest
                        return 0;
                    case(-128):
                    case(-127):
                    case(-126):
                    case(-125):
                    case(-120):
                    case(-119):
                    case(-118):
                    case(-117):
                    case(-116):
                    case(-115):
                    case(-114):
                    case(-113):
                    case(-112):
                    case(-111):
                    case(-110):
                    case(-109):
                    case(-104):
                    case(-103):
                    case(-102):
                    case(-101):                        
                    case(-100):
                    case(-99):
                    case(-98):
                    case(-97):
                    case(-69):
                    case(-65):
                    case(-88):
                    case(-87):
                    case(-86):
                    case(-85):
                    case(-84):
                    case(-83):
                    case(-82):
                    case(-81):
                    case(4):  // kicked hidden brick
                    case(9):
                        return -10;   // border, cannot pass through, can stand on
//                    case(9):
//                        return -12; // hard formation border. Pay attention!
                    case(-124):
                    case(-123):
                    case(-122):
                    case(-76):
                    case(-74):
                        return -11; // half-border, can jump through from bottom and can stand on
                    case(10): case(11): case(26): case(27): // flower pot
                    case(14): case(30): case(46): // canon
                        return 20;  // angry flower pot or cannon
                }
                System.err.println("Unknown value el = " + el + " ; Please, inform the developers");
                return el;
            case(2):
                switch(el)
                {
                    //cancel out half-borders, that could be passed through
                    case(0):
                    case(-108):
                    case(-107):
                    case(-106):
                    case(34): // coins
                    case(15): // Sparcle, irrelevant
                        return 0;
                }
                return 1;  // everything else is "something", so it is 1
        }
        System.err.println("Unkown ZLevel Z" + ZLevel);
        return el; //TODO: Throw unknown ZLevel exception
    }


    private byte ZLevelEnemyGeneralization(byte el, int ZLevel)
    {
        switch (ZLevel)
        {
            case(0):
                switch(el)
                {
                    // cancell irrelevant sprite codes
                    case(Sprite.KIND_COIN_ANIM): 
                    case(Sprite.KIND_PARTICLE):
                    case(Sprite.KIND_SPARCLE):
                    case(Sprite.KIND_MARIO):
                        return Sprite.KIND_NONE;
                }
                return el;   // all the rest should go as is
            case(1):
                switch(el)
                {
                    case(Sprite.KIND_COIN_ANIM):
                    case(Sprite.KIND_PARTICLE):
                    case(Sprite.KIND_SPARCLE):
                    case(Sprite.KIND_MARIO):
                        return Sprite.KIND_NONE;
                    case (Sprite.KIND_FIRE_FLOWER):
                        return Sprite.KIND_FIRE_FLOWER;
                    case (Sprite.KIND_MUSHROOM):
                        return Sprite.KIND_MUSHROOM;
                    case(Sprite.KIND_FIREBALL):
                        return Sprite.KIND_FIREBALL;                    
                    case(Sprite.KIND_BULLET_BILL):
                    case(Sprite.KIND_GOOMBA):
                    case(Sprite.KIND_GOOMBA_WINGED):
                    case(Sprite.KIND_GREEN_KOOPA):
                    case(Sprite.KIND_GREEN_KOOPA_WINGED):
                    case(Sprite.KIND_RED_KOOPA):
                    case(Sprite.KIND_RED_KOOPA_WINGED):
                    case(Sprite.KIND_SHELL):
                        return Sprite.KIND_GOOMBA;
                    case(Sprite.KIND_SPIKY):
                    case(Sprite.KIND_ENEMY_FLOWER):
                    case(Sprite.KIND_SPIKY_WINGED):
                        return Sprite.KIND_SPIKY;
                }
                System.err.println("Z1 UNKOWN el = " + el);
                return el;
            case(2):
                switch(el)
                {
                    case(Sprite.KIND_COIN_ANIM):
                    case(Sprite.KIND_PARTICLE):
                    case(Sprite.KIND_SPARCLE):
                    case(Sprite.KIND_FIREBALL):
                    case(Sprite.KIND_MARIO):
                    case(Sprite.KIND_FIRE_FLOWER):
                    case(Sprite.KIND_MUSHROOM):
                        return Sprite.KIND_NONE;
                    case(Sprite.KIND_BULLET_BILL):
                    case(Sprite.KIND_GOOMBA):
                    case(Sprite.KIND_GOOMBA_WINGED):
                    case(Sprite.KIND_GREEN_KOOPA):
                    case(Sprite.KIND_GREEN_KOOPA_WINGED):
                    case(Sprite.KIND_RED_KOOPA):
                    case(Sprite.KIND_RED_KOOPA_WINGED):
                    case(Sprite.KIND_SHELL):
                    case(Sprite.KIND_SPIKY):
                    case(Sprite.KIND_ENEMY_FLOWER):
                        return 1;
                }
                System.err.println("Z2 UNKNOWNN el = " + el);
                return 1;
        }
        return el; //TODO: Throw unknown ZLevel exception
    }

    public byte[][] levelSceneObservation(int ZLevel)
    {
        byte[][] ret = new byte[Environment.HalfObsWidth*2][Environment.HalfObsHeight*2];
        //TODO: Move to constants 16
        int MarioXInMap = (int)mario.x/16;
        int MarioYInMap = (int)mario.y/16;

        for (int y = MarioYInMap - Environment.HalfObsHeight, obsX = 0; y < MarioYInMap + Environment.HalfObsHeight; y++, obsX++)
        {
            for (int x = MarioXInMap - Environment.HalfObsWidth, obsY = 0; x < MarioXInMap + Environment.HalfObsWidth; x++, obsY++)
            {
                if (x >=0 /*  && x <= level.xExit */ && y >= 0 && y < level.height)
                {
                    ret[obsX][obsY] = ZLevelMapElementGeneralization(level.map[x][y], ZLevel);
                }
                else
                    ret[obsX][obsY] = 0;
//                if (x == MarioXInMap && y == MarioYInMap)
//                    ret[obsX][obsY] = mario.kind;
            }
        }
        return ret;
    }

    public byte[][] enemiesObservation(int ZLevel)
    {
        byte[][] ret = new byte[Environment.HalfObsWidth*2][Environment.HalfObsHeight*2];
        //TODO: Move to constants 16
        int MarioXInMap = (int)mario.x/16;
        int MarioYInMap = (int)mario.y/16;

        for (int w = 0; w < ret.length; w++)
            for (int h = 0; h < ret[0].length; h++)
                ret[w][h] = 0;
//        ret[Environment.HalfObsWidth][Environment.HalfObsHeight] = mario.kind;
        for (Sprite sprite : sprites)
        {
            if (sprite.kind == mario.kind)
                continue;
            if (sprite.mapX >= 0 &&
                sprite.mapX > MarioXInMap - Environment.HalfObsWidth &&
                sprite.mapX < MarioXInMap + Environment.HalfObsWidth &&
                sprite.mapY >= 0 &&
                sprite.mapY > MarioYInMap - Environment.HalfObsHeight &&
                sprite.mapY < MarioYInMap + Environment.HalfObsHeight )
            {
                int obsX = sprite.mapY - MarioYInMap + Environment.HalfObsHeight;
                int obsY = sprite.mapX - MarioXInMap + Environment.HalfObsWidth;
                ret[obsX][obsY] = ZLevelEnemyGeneralization(sprite.kind, ZLevel);
            }
        }
        return ret;
    }

    public float[] enemiesFloatPos()
    {
        List<Float> poses = new ArrayList<Float>();
        for (Sprite sprite : sprites)
        {
            // check if is an influenceable creature
            if (sprite.kind >= Sprite.KIND_GOOMBA && sprite.kind <= Sprite.KIND_MUSHROOM)
            {
                poses.add((float)sprite.kind);
                poses.add(sprite.x);
                poses.add(sprite.y);
            }
        }

        float[] ret = new float[poses.size()];

        int i = 0;
        for (Float F: poses)
            ret[i++] = F;

        return ret;
    }

    public byte[][] mergedObservation(int ZLevelScene, int ZLevelEnemies)
    {
        byte[][] ret = new byte[Environment.HalfObsWidth*2][Environment.HalfObsHeight*2];
        //TODO: Move to constants 16
        int MarioXInMap = (int)mario.x/16;
        int MarioYInMap = (int)mario.y/16;

        for (int y = MarioYInMap - Environment.HalfObsHeight, obsX = 0; y < MarioYInMap + Environment.HalfObsHeight; y++, obsX++)
        {
            for (int x = MarioXInMap - Environment.HalfObsWidth, obsY = 0; x < MarioXInMap + Environment.HalfObsWidth; x++, obsY++)
            {
                if (x >=0 /*&& x <= level.xExit*/ && y >= 0 && y < level.height)
                {
                    ret[obsX][obsY] = ZLevelMapElementGeneralization(level.map[x][y], ZLevelScene);
                }
                else
                    ret[obsX][obsY] = 0;
//                if (x == MarioXInMap && y == MarioYInMap)
//                    ret[obsX][obsY] = mario.kind;
            }
        }

//        for (int w = 0; w < ret.length; w++)
//            for (int h = 0; h < ret[0].length; h++)
//                ret[w][h] = -1;
//        ret[Environment.HalfObsWidth][Environment.HalfObsHeight] = mario.kind;
        for (Sprite sprite : sprites)
        {
            if (sprite.kind == mario.kind)
                continue;
            if (sprite.mapX >= 0 &&
                sprite.mapX > MarioXInMap - Environment.HalfObsWidth &&
                sprite.mapX < MarioXInMap + Environment.HalfObsWidth &&
                sprite.mapY >= 0 &&
                sprite.mapY > MarioYInMap - Environment.HalfObsHeight &&
                sprite.mapY < MarioYInMap + Environment.HalfObsHeight )
            {
                int obsX = sprite.mapY - MarioYInMap + Environment.HalfObsHeight;
                int obsY = sprite.mapX - MarioXInMap + Environment.HalfObsWidth;
                // quick fix TODO: handle this in more general way.
                if (ret[obsX][obsY] != 14)
                {
                    byte tmp = ZLevelEnemyGeneralization(sprite.kind, ZLevelEnemies);
                    if (tmp != Sprite.KIND_NONE)
                        ret[obsX][obsY] = tmp;
                }
            }
        }

        return ret;
    }

    private String encode(byte[][] state, Generalizer generalize)
    {
        String estate = "";

        return estate;
    }


    // Encode

    public String bitmapLevelObservation(int ZLevel)
    {
        String ret = "";
        int MarioXInMap = (int)mario.x/16;
        int MarioYInMap = (int)mario.y/16;

        char block = 0;
        byte bitCounter = 0;
        int totalBits = 0;
        int totalBytes = 0;
        for (int y = MarioYInMap - Environment.HalfObsHeight, obsX = 0; y < MarioYInMap + Environment.HalfObsHeight; y++, obsX++)
        {
            for (int x = MarioXInMap - Environment.HalfObsWidth, obsY = 0; x < MarioXInMap + Environment.HalfObsWidth; x++, obsY++)
            {
                ++totalBits;
                if (bitCounter > 15)
                {
                    // update a symbol and store the current one
                    ret += block;
                    ++totalBytes;
                    block = 0;
                    bitCounter = 0;
                }
                if (x >=0 && x <= level.xExit && y >= 0 && y < level.height)
                {
                    int temp = ZLevelMapElementGeneralization(level.map[x][y], ZLevel);
                    if (temp != 0)
                        block |= MathX.powsof2[bitCounter];
                }
                ++bitCounter;
            }
//            if (block != 0)
//            {
//                System.out.println("block = " + block);
//                show(block);
//            }

        }

        if (bitCounter > 0)
            ret += block;

//        try {
//            String s = new String(code, "UTF8");
//            System.out.println("s = " + s);
//            ret = s;
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//        System.out.println("totalBits = " + totalBits);
//        System.out.println("totalBytes = " + totalBytes);
//        System.out.println("ret = " + ret);

        return ret;
    }

    public String bitmapEnemiesObservation(int ZLevel)
    {
        String ret = "";
        byte[][] enemiesObservation = enemiesObservation(ZLevel);
        int MarioXInMap = (int)mario.x/16;
        int MarioYInMap = (int)mario.y/16;

        char block = 0;
        char bitCounter = 0;
        int totalBits = 0;
        int totalBytes = 0;
        for (int i = 0; i < enemiesObservation.length; ++i)
        {
            for (int j = 0; j < enemiesObservation[0].length; ++j)
            {
                ++totalBits;
                if (bitCounter > 7)
                {
                    // update a symbol and store the current one
                    ret += block;
                    ++totalBytes;
                    block = 0;
                    bitCounter = 0;
                }
                int temp = enemiesObservation[i][j] ;
                if (temp != -1)
                    block |= MathX.powsof2[bitCounter];
                ++bitCounter;
            }
//            if (block != 0)
//            {
//                System.out.println("block = " + block);
//                show(block);
//            }

        }

        if (bitCounter > 0)
            ret += block;

//        System.out.println("totalBits = " + totalBits);
//        System.out.println("totalBytes = " + totalBytes);
//        System.out.println("ret = " + ret);
        return ret;
    }


    public List<String> LevelSceneAroundMarioASCII(boolean Enemies, boolean LevelMap,
                                                   boolean mergedObservationFlag,
                                                   int ZLevelScene, int ZLevelEnemies){
//        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));//        bw.write("\nTotal world width = " + level.width);
        List<String> ret = new ArrayList<String>();
        if (level != null && mario != null)
        {
            ret.add("Total world width = " + level.width);
            ret.add("Total world height = " + level.height);
            ret.add("Physical Mario Position (x,y): (" + mario.x + "," + mario.y + ")");
            ret.add("Mario Observation Width " + Environment.HalfObsWidth*2);
            ret.add("Mario Observation Height " + Environment.HalfObsHeight*2);
            ret.add("X Exit Position: " + level.xExit);
            int MarioXInMap = (int)mario.x/16;
            int MarioYInMap = (int)mario.y/16;
            ret.add("Calibrated Mario Position (x,y): (" + MarioXInMap + "," + MarioYInMap + ")\n");

            byte[][] levelScene = levelSceneObservation(ZLevelScene);
            if (LevelMap)
            {
                ret.add("~ZLevel: Z" + ZLevelScene + " map:\n");
                for (int x = 0; x < levelScene.length; ++x)
                {
                    String tmpData = "";
                    for (int y = 0; y < levelScene[0].length; ++y)
                        tmpData += mapElToStr(levelScene[x][y]);
                    ret.add(tmpData);
                }
            }

            byte[][] enemiesObservation = null;
            if (Enemies || mergedObservationFlag)
            {
                enemiesObservation = enemiesObservation(ZLevelEnemies);
            }

            if (Enemies)
            {
                ret.add("~ZLevel: Z" + ZLevelScene + " Enemies Observation:\n");
                for (int x = 0; x < enemiesObservation.length; x++)
                {
                    String tmpData = "";
                    for (int y = 0; y < enemiesObservation[0].length; y++)
                    {
//                        if (x >=0 && x <= level.xExit)
                            tmpData += enemyToStr(enemiesObservation[x][y]);
                    }
                    ret.add(tmpData);
                }
            }

            if (mergedObservationFlag)
            {
//                ret.add("~ZLevel: Z" + ZLevelScene + "===========\nAll objects: (LevelScene[x,y], Sprite[x,y])==/* Mario ~> MM */=====\n");
//                for (int x = 0; x < levelScene.length; ++x)
//                {
//                    String tmpData = "";
//                    for (int y = 0; y < levelScene[0].length; ++y)
//                        tmpData += "(" + levelScene[x][y] + "," + enemiesObservation[x][y] + ")";
//                    ret.add(tmpData);
//                }

                byte[][] mergedObs = mergedObservation(ZLevelScene, ZLevelEnemies);
                ret.add("~ZLevelScene: Z" + ZLevelScene + " ZLevelEnemies: Z" + ZLevelEnemies + " ; Merged observation /* Mario ~> #M.# */");
                for (int x = 0; x < levelScene.length; ++x)
                {
                    String tmpData = "";
                    for (int y = 0; y < levelScene[0].length; ++y)
                        tmpData += mapElToStr(mergedObs[x][y]);
                    ret.add(tmpData);
                }
            }
        }
        else
            ret.add("~level or mario is not available");
        return ret;
    }
    
    
    public void init()
    {
        try
        {
            Level.loadBehaviors(new DataInputStream(LevelScene.class.getResourceAsStream("resources/tiles.dat")));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(0);
        }
        /*        if (replayer!=null)
         {
         level = LevelGenerator.createLevel(2048, 15, replayer.nextLong());
         }
         else
         {*/
//        level = LevelGenerator.createLevel(320, 15, levelSeed);
        level = LevelGenerator.createLevel(levelLength, 15, levelSeed, levelDifficulty, levelType);
        
        //        }

        /*        if (recorder != null)
         {
         recorder.addLong(LevelGenerator.lastSeed);
         }*/


        paused = false;
        //Sprite.spriteContext = this;
        sprites.clear();
        layer = new LevelRenderer(level, graphicsConfiguration, 320, 240);
        for (int i = 0; i < 2; i++)
        {
            int scrollSpeed = 4 >> i;
            int w = ((level.width * 16) - 320) / scrollSpeed + 320;
            int h = ((level.height * 16) - 240) / scrollSpeed + 240;
            Level bgLevel = BgLevelGenerator.createLevel(w / 32 + 1, h / 32 + 1, i == 0, levelType);
            bgLayer[i] = new BgRenderer(bgLevel, graphicsConfiguration, 320, 240, scrollSpeed);
        }
        mario = new Mario(this);
        sprites.add(mario);
        startTime = 1;

        timeLeft = totalTime*15;

        tick = 0;
    }
    
      public void init(Level level)
    {
        try
        {
            Level.loadBehaviors(new DataInputStream(LevelScene.class.getResourceAsStream("resources/tiles.dat")));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(0);
        }
        /*        if (replayer!=null)
         {
         level = LevelGenerator.createLevel(2048, 15, replayer.nextLong());
         }
         else
         {*/
//        level = LevelGenerator.createLevel(320, 15, levelSeed);
        this.level = level;
        
        //        }

        /*        if (recorder != null)
         {
         recorder.addLong(LevelGenerator.lastSeed);
         }*/


        paused = false;
        //Sprite.spriteContext = this;
        sprites.clear();
        layer = new LevelRenderer(level, graphicsConfiguration, 320, 240);
        for (int i = 0; i < 2; i++)
        {
            int scrollSpeed = 4 >> i;
            int w = ((level.width * 16) - 320) / scrollSpeed + 320;
            int h = ((level.height * 16) - 240) / scrollSpeed + 240;
            Level bgLevel = BgLevelGenerator.createLevel(w / 32 + 1, h / 32 + 1, i == 0, levelType);
            bgLayer[i] = new BgRenderer(bgLevel, graphicsConfiguration, 320, 240, scrollSpeed);
        }
        mario = new Mario(this);
        sprites.add(mario);
        startTime = 1;

        timeLeft = totalTime*15;

        tick = 0;
    }

    public int fireballsOnScreen = 0;

    List<Shell> shellsToCheck = new ArrayList<Shell>();

    public void checkShellCollide(Shell shell)
    {
        shellsToCheck.add(shell);
    }

    List<Fireball> fireballsToCheck = new ArrayList<Fireball>();

    public void checkFireballCollide(Fireball fireball)
    {
        fireballsToCheck.add(fireball);
    }

    public void tick()
    {
        if (GlobalOptions.TimerOn)
                timeLeft--;
        if (timeLeft==0)
        {
            mario.die();
        }
        xCamO = xCam;
        yCamO = yCam;

        if (startTime > 0)
        {
            startTime++;
        }

        float targetXCam = mario.x - 160;

        xCam = targetXCam;

        if (xCam < 0) xCam = 0;
        if (xCam > level.width * 16 - 320) xCam = level.width * 16 - 320;

        /*      if (recorder != null)
         {
         recorder.addTick(mario.getKeyMask());
         }
         
         if (replayer!=null)
         {
         mario.setKeys(replayer.nextTick());
         }*/

        fireballsOnScreen = 0;

        for (Sprite sprite : sprites)
        {
            if (sprite != mario)
            {
                float xd = sprite.x - xCam;
                float yd = sprite.y - yCam;
                if (xd < -64 || xd > 320 + 64 || yd < -64 || yd > 240 + 64)
                {
                    removeSprite(sprite);
                }
                else
                {
                    if (sprite instanceof Fireball)
                    {
                        fireballsOnScreen++;
                    }
                }
            }
        }

        if (paused)
        {
            for (Sprite sprite : sprites)
            {
                if (sprite == mario)
                {
                    sprite.tick();
                }
                else
                {
                    sprite.tickNoMove();
                }
            }
        }
        else
        {
            tick++;
            level.tick();

            boolean hasShotCannon = false;
            int xCannon = 0;

            for (int x = (int) xCam / 16 - 1; x <= (int) (xCam + layer.width) / 16 + 1; x++)
                for (int y = (int) yCam / 16 - 1; y <= (int) (yCam + layer.height) / 16 + 1; y++)
                {
                    int dir = 0;

                    if (x * 16 + 8 > mario.x + 16) dir = -1;
                    if (x * 16 + 8 < mario.x - 16) dir = 1;

                    SpriteTemplate st = level.getSpriteTemplate(x, y);

                    if (st != null)
                    {
                        if (st.lastVisibleTick != tick - 1)
                        {
                            if (st.sprite == null || !sprites.contains(st.sprite))
                            {
                                st.spawn(this, x, y, dir);
                            }
                        }

                        st.lastVisibleTick = tick;
                    }

                    if (dir != 0)
                    {
                        byte b = level.getBlock(x, y);
                        if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_ANIMATED) > 0)
                        {
                            if ((b % 16) / 4 == 3 && b / 16 == 0)
                            {
                                if ((tick - x * 2) % 100 == 0)
                                {
                                    xCannon = x;
                                    for (int i = 0; i < 8; i++)
                                    {
                                        addSprite(new Sparkle(this, x * 16 + 8, y * 16 + (int) (Math.random() * 16), (float) Math.random() * dir, 0, 0, 1, 5));
                                    }
                                    addSprite(new BulletBill(this, x * 16 + 8 + dir * 8, y * 16 + 15, dir));
                                    hasShotCannon = true;
                                }
                            }
                        }
                    }
                }

            for (Sprite sprite : sprites)
            {
                sprite.tick();
            }

            for (Sprite sprite : sprites)
            {
                sprite.collideCheck();
            }

            for (Shell shell : shellsToCheck)
            {
                for (Sprite sprite : sprites)
                {
                    if (sprite != shell && !shell.dead)
                    {
                        if (sprite.shellCollideCheck(shell))
                        {
                            if (mario.carried == shell && !shell.dead)
                            {
                                mario.carried = null;
                                shell.die();
                                ++this.killedCreaturesTotal;
                            }
                        }
                    }
                }
            }
            shellsToCheck.clear();

            for (Fireball fireball : fireballsToCheck)
            {
                for (Sprite sprite : sprites)
                {
                    if (sprite != fireball && !fireball.dead)
                    {
                        if (sprite.fireballCollideCheck(fireball))
                        {
                            fireball.die();
                        }
                    }
                }
            }
            fireballsToCheck.clear();
        }

        sprites.addAll(0, spritesToAdd);
        sprites.removeAll(spritesToRemove);
        spritesToAdd.clear();
        spritesToRemove.clear();
    }

    private DecimalFormat df = new DecimalFormat("00");
    private DecimalFormat df2 = new DecimalFormat("000");

    public void render(Graphics g, float alpha)
    {
        int xCam = (int) (mario.xOld + (mario.x - mario.xOld) * alpha) - 160;
        int yCam = (int) (mario.yOld + (mario.y - mario.yOld) * alpha) - 120;

        if (GlobalOptions.MarioAlwaysInCenter)
        {
        }
        else
        {
            //int xCam = (int) (xCamO + (this.xCam - xCamO) * alpha);
            //        int yCam = (int) (yCamO + (this.yCam - yCamO) * alpha);
            if (xCam < 0) xCam = 0;
            if (yCam < 0) yCam = 0;
            if (xCam > level.width * 16 - 320) xCam = level.width * 16 - 320;
            if (yCam > level.height * 16 - 240) yCam = level.height * 16 - 240;
        }
        //      g.drawImage(Art.background, 0, 0, null);

        for (int i = 0; i < 2; i++)
        {
            bgLayer[i].setCam(xCam, yCam);
            bgLayer[i].render(g, tick, alpha);
        }

        g.translate(-xCam, -yCam);

        for (Sprite sprite : sprites)
        {
            if (sprite.layer == 0) sprite.render(g, alpha);
        }

        g.translate(xCam, yCam);

        layer.setCam(xCam, yCam);
        layer.render(g, tick, paused?0:alpha);
        layer.renderExit0(g, tick, paused?0:alpha, mario.winTime==0);

        g.translate(-xCam, -yCam);

        // TODO: Dump out of render!
        if (mario.cheatKeys[Mario.KEY_DUMP_CURRENT_WORLD])
            for (int w = 0; w < level.width; w++)
                for (int h = 0; h < level.height; h++)
                    level.observation[w][h] = -1;

        for (Sprite sprite : sprites)
        {
            if (sprite.layer == 1) sprite.render(g, alpha);
            if (mario.cheatKeys[Mario.KEY_DUMP_CURRENT_WORLD] && sprite.mapX >= 0 && sprite.mapX < level.observation.length &&
                    sprite.mapY >= 0 && sprite.mapY < level.observation[0].length)
                level.observation[sprite.mapX][sprite.mapY] = sprite.kind;

        }

        g.translate(xCam, yCam);
        g.setColor(Color.BLACK);
        layer.renderExit1(g, tick, paused?0:alpha);

//        drawStringDropShadow(g, "MARIO: " + df.format(Mario.lives), 0, 0, 7);
//        drawStringDropShadow(g, "#########", 0, 1, 7);


        drawStringDropShadow(g, "DIFFICULTY:   " + df.format(this.levelDifficulty), 0, 0, this.levelDifficulty > 6 ? 1 : this.levelDifficulty > 2 ? 4 : 7 ); drawStringDropShadow(g, "CREATURES:" + (mario.world.paused ? "OFF" : "ON"), 19, 0, 7);
        drawStringDropShadow(g, "SEED:" + this.levelSeed, 0, 1, 7);
        drawStringDropShadow(g, "TYPE:" + LEVEL_TYPES[this.levelType], 0, 2, 7);                  drawStringDropShadow(g, "ALL KILLS: " + killedCreaturesTotal, 19, 1, 1);
        drawStringDropShadow(g, "LENGTH:" + (int)mario.x/16 + " of " + this.levelLength, 0, 3, 7); drawStringDropShadow(g, "by Fire  : " + killedCreaturesByFireBall, 19, 2, 1);
        drawStringDropShadow(g,"COINS    : " + df.format(mario.coins), 0, 4, 4);                      drawStringDropShadow(g, "by Shell : " + killedCreaturesByShell, 19, 3, 1);
        drawStringDropShadow(g, "MUSHROOMS: " + df.format(mario.gainedMushrooms), 0, 5, 4);                  drawStringDropShadow(g, "by Stomp : " + killedCreaturesByStomp, 19, 4, 1);
        drawStringDropShadow(g, "FLOWERS  : " + df.format(mario.gainedFlowers), 0, 6, 4);


        drawStringDropShadow(g, "TIME", 32, 0, 7);
        int time = (timeLeft+15-1)/15;
        if (time<0) time = 0;
        drawStringDropShadow(g, " "+df2.format(time), 32, 1, 7);

        drawProgress(g);

        if (GlobalOptions.Labels)
        {
            g.drawString("xCam: " + xCam + "yCam: " + yCam, 70, 40);
            g.drawString("x : " + mario.x + "y: " + mario.y, 70, 50);
            g.drawString("xOld : " + mario.xOld + "yOld: " + mario.yOld, 70, 60);
        }

        if (startTime > 0)
        {
            float t = startTime + alpha - 2;
            t = t * t * 0.6f;
            renderBlackout(g, 160, 120, (int) (t));
        }
//        mario.x>level.xExit*16
        if (mario.winTime > 0)
        {
            float t = mario.winTime + alpha;
            t = t * t * 0.2f;

            if (t > 900)
            {
                renderer.levelWon();
                //              replayer = new Replayer(recorder.getBytes());
//                init();
            }

            renderBlackout(g, mario.xDeathPos - xCam, mario.yDeathPos - yCam, (int) (320 - t));
        }

        if (mario.deathTime > 0)
        {
//            float t = mario.deathTime + alpha;
//            t = t * t * 0.4f;
//
//            if (t > 1800)
//            {
                renderer.levelFailed();
                //              replayer = new Replayer(recorder.getBytes());
//                init();
//            }

//            renderBlackout(g, (int) (mario.xDeathPos - xCam), (int) (mario.yDeathPos - yCam), (int) (320 - t));
        }
    }

    public LevelRenderer getLayer() {
        return layer;
    }

    public void setLayer(LevelRenderer layer) {
        this.layer = layer;
    }

    public BgRenderer[] getBgLayer() {
        return bgLayer;
    }

    public void setBgLayer(BgRenderer[] bgLayer) {
        this.bgLayer = bgLayer;
    }

    private void drawProgress(Graphics g) {
        String entirePathStr = "......................................>";
        double physLength = (levelLength - 53)*16;
        int progressInChars = (int) (mario.x * (entirePathStr.length()/physLength));
        String progress_str = "";
        for (int i = 0; i < progressInChars - 1; ++i)
            progress_str += ".";
        progress_str += "M";
        try {
        drawStringDropShadow(g, entirePathStr.substring(progress_str.length()), progress_str.length(), 28, 0);
        } catch (StringIndexOutOfBoundsException e)
        {
//            System.err.println("warning: progress line inaccuracy");
        }
        drawStringDropShadow(g, progress_str, 0, 28, 2);
    }

    public void drawStringDropShadow(Graphics g, String text, int x, int y, int c)
    {
        drawString(g, text, x*8+5, y*8+5, 0);
        drawString(g, text, x*8+4, y*8+4, c);
    }

    private void drawString(Graphics g, String text, int x, int y, int c)
    {
        char[] ch = text.toCharArray();
        for (int i = 0; i < ch.length; i++)
        {
            g.drawImage(Art.font[ch[i] - 32][c], x + i * 8, y, null);
        }
    }

    private void renderBlackout(Graphics g, int x, int y, int radius)
    {
        if (radius > 320) return;

        int[] xp = new int[20];
        int[] yp = new int[20];
        for (int i = 0; i < 16; i++)
        {
            xp[i] = x + (int) (Math.cos(i * Math.PI / 15) * radius);
            yp[i] = y + (int) (Math.sin(i * Math.PI / 15) * radius);
        }
        xp[16] = 320;
        yp[16] = y;
        xp[17] = 320;
        yp[17] = 240;
        xp[18] = 0;
        yp[18] = 240;
        xp[19] = 0;
        yp[19] = y;
        g.fillPolygon(xp, yp, xp.length);

        for (int i = 0; i < 16; i++)
        {
            xp[i] = x - (int) (Math.cos(i * Math.PI / 15) * radius);
            yp[i] = y - (int) (Math.sin(i * Math.PI / 15) * radius);
        }
        xp[16] = 320;
        yp[16] = y;
        xp[17] = 320;
        yp[17] = 0;
        xp[18] = 0;
        yp[18] = 0;
        xp[19] = 0;
        yp[19] = y;

        g.fillPolygon(xp, yp, xp.length);
    }


    public void addSprite(Sprite sprite)
    {
        spritesToAdd.add(sprite);
        sprite.tick();
    }

    public void removeSprite(Sprite sprite)
    {
        spritesToRemove.add(sprite);
    }

    public float getX(float alpha)
    {
        int xCam = (int) (mario.xOld + (mario.x - mario.xOld) * alpha) - 160;
        //        int yCam = (int) (mario.yOld + (mario.y - mario.yOld) * alpha) - 120;
        //int xCam = (int) (xCamO + (this.xCam - xCamO) * alpha);
        //        int yCam = (int) (yCamO + (this.yCam - yCamO) * alpha);
        if (xCam < 0) xCam = 0;
        //        if (yCam < 0) yCam = 0;
        //        if (yCam > 0) yCam = 0;
        return xCam + 160;
    }

    public float getY(float alpha)
    {
        return 0;
    }

    public void bump(int x, int y, boolean canBreakBricks)
    {
        byte block = level.getBlock(x, y);

        if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BUMPABLE) > 0)
        {
            bumpInto(x, y - 1);
            level.setBlock(x, y, (byte) 4);
            level.setBlockData(x, y, (byte) 4);

            if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_SPECIAL) > 0)
            {
                if (!mario.large)
                {
                    addSprite(new Mushroom(this, x * 16 + 8, y * 16 + 8));
                }
                else
                {
                    addSprite(new FireFlower(this, x * 16 + 8, y * 16 + 8));
                }
            }
            else
            {
                mario.getCoin();
                addSprite(new CoinAnim(this, x, y));
            }
        }

        if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BREAKABLE) > 0)
        {
            bumpInto(x, y - 1);
            if (canBreakBricks)
            {
                level.setBlock(x, y, (byte) 0);
                for (int xx = 0; xx < 2; xx++)
                    for (int yy = 0; yy < 2; yy++)
                        addSprite(new Particle(x * 16 + xx * 8 + 4, y * 16 + yy * 8 + 4, (xx * 2 - 1) * 4, (yy * 2 - 1) * 4 - 8));
            }
            else
            {
                level.setBlockData(x, y, (byte) 4);
            }
        }
    }

    public void bumpInto(int x, int y)
    {
        byte block = level.getBlock(x, y);
        if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE) > 0)
        {
            mario.getCoin();
            level.setBlock(x, y, (byte) 0);
            addSprite(new CoinAnim(this, x, y + 1));
        }

        for (Sprite sprite : sprites)
        {
            sprite.bumpCheck(x, y);
        }
    }

//    public void update(boolean[] action)
//    {
//        System.arraycopy(action, 0, mario.keys, 0, 6);
//    }

    public int getStartTime() {  return startTime / 15;    }

    public int getTimeLeft() {        return timeLeft / 15;    }
    
    //Added from Baumgarten
    @Override protected Object clone() throws CloneNotSupportedException 
    {
    	LevelScene c = (LevelScene) super.clone();
    	c.mario = (Mario) this.mario.clone();
    	c.level = (Level) this.level.clone();
    	c.mario.world = c;
    	
    	List<Sprite> clone = new ArrayList<Sprite>(this.sprites.size());
        for(Sprite item: this.sprites) 
        {
        	if (item == mario)
        	{
        		clone.add(c.mario);
        	}
        	else
        	{
        		Sprite s = (Sprite) item.clone();
        		if (s.kind == Sprite.KIND_SHELL && ((Shell) s).carried && c.mario.carried != null)
        			c.mario.carried = s;
        		s.world = c;
        		clone.add(s);
        	}
        }
        c.sprites = clone;
    	return c;
    }
}