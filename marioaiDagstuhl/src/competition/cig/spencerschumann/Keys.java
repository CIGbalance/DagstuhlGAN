package competition.cig.spencerschumann;

/**
 *
 * @author Spencer Schumann
 */
public class Keys {
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int DOWN = 2;
    public static final int JUMP = 3;
    public static final int SPEED = 4;
    public static final int UP = 5;
    public static final int SPECIAL = 11;

    public static String toString(int k) {
        switch (k) {
            case LEFT: return "Left";
            case RIGHT: return "Right";
            case DOWN: return "Down";
            case JUMP: return "Jump";
            case SPEED: return "Speed";
            case UP: return "Up";
            case SPECIAL: return "Special";
            default: return "Unknown";
        }
    }
}
