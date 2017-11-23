package competition.cig.spencerschumann;

/**
 *
 * @author Spencer Schumann
 */
public class Action {
    public boolean left;
    public boolean right;
    public boolean down;
    public boolean jump;
    public boolean speed;
    public boolean up;
    public boolean special;

    public boolean[] toArray() {
        boolean[] array = new boolean[5];
        array[Keys.LEFT] = left;
        array[Keys.RIGHT] = right;
        array[Keys.DOWN] = down;
        array[Keys.JUMP] = jump;
        array[Keys.SPEED] = speed;
        // Ignore up and special when converting
        return array;
    }

    public void fromArray(boolean[] array) {
        left = array[Keys.LEFT];
        right = array[Keys.RIGHT];
        down = array[Keys.DOWN];
        jump = array[Keys.JUMP];
        speed = array[Keys.SPEED];
        // Ignore up and special when converting
    }
}
