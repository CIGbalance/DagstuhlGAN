package ch.idsia.mario.environments;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Mar 28, 2009
 * Time: 8:51:57 PM
 * Package: .Environments
 */

public interface Environment
{
    public static final int numberOfButtons = 5;
    public static final int numberOfObservationElements = 486 + 1;
    public static final int HalfObsWidth = 11;
    public static final int HalfObsHeight = 11;

    // always the same dimensionality: 22x22
    // always centered on the agent

    // KILLS
    

    // Chaning ZLevel during the game on-the-fly;
    // if your agent recieves too ambiguous observation, it might request for more precise one for the next step


    public byte[][] getCompleteObservation();   // default: ZLevelScene = 1, ZLevelEnemies = 0

    public byte[][] getEnemiesObservation();    // default: ZLevelEnemies = 0

    public byte[][] getLevelSceneObservation(); // default: ZLevelScene = 1

    public float[] getMarioFloatPos();

    public int getMarioMode();

    public float[] getEnemiesFloatPos();

    public boolean isMarioOnGround();
    public boolean mayMarioJump();
    public boolean isMarioCarrying();

    public byte[][] getMergedObservationZ(int ZLevelScene, int ZLevelEnemies);
    public byte[][] getLevelSceneObservationZ(int ZLevelScene);
    public byte[][] getEnemiesObservationZ(int ZLevelEnemies);

    public int getKillsTotal();
    public int getKillsByFire();
    public int getKillsByStomp();
    public int getKillsByShell();

    // Pilot (test) additions
    public boolean canShoot();
    
    // For Server usage only, Java agents should use non-bitmap versions.
    public String getBitmapEnemiesObservation();

    public String getBitmapLevelObservation();

}
