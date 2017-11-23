package competition.cig.trondellingsen;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import java.awt.Rectangle;

public final class TrondEllingsen_LuckyAgent implements Agent {

    private enum JumpType {

        ENEMY, GAP, WALL, NONE
    }
    private JumpType jumpType = JumpType.NONE;
    private int jumpCount = 0,  jumpSize = -1;
    private float[] prevPos = new float[]{0, 0};
    private boolean[] action;
    private String name;

    public TrondEllingsen_LuckyAgent() {
        setName("TrondEllingsen_LuckyAgent");
    }

    public Agent.AGENT_TYPE getType() {
        return Agent.AGENT_TYPE.AI;
    }

    public String getName() {
        return name;
    }

    public void setName(String Name) {
        this.name = Name;
    }

    public void reset() {
        action = new boolean[Environment.numberOfButtons];
        action[Mario.KEY_RIGHT] = true;
        action[Mario.KEY_SPEED] = true;
    }

    private final static int getWallHeight(final byte[][] levelScene) {
        int y = 12, wallHeight = 0;
        while (y-- > 0 && levelScene[y][12] != 0) {
            wallHeight++;
        }
        return wallHeight;
    }

    private final static boolean dangerOfGap(final byte[][] levelScene) {
        for (int y = 12; y < levelScene.length; y++) {
            if (levelScene[y][12] != 0) {
                return false;
            }
        }
        return true;
    }

    private final static boolean enemyInRange(final Environment e, final Rectangle r) {
        for (int i = 0; i < e.getEnemiesFloatPos().length; i += 3) {
            if (r.contains(e.getEnemiesFloatPos()[i + 1] - e.getMarioFloatPos()[0],
                    e.getMarioFloatPos()[1] - e.getEnemiesFloatPos()[i + 2])) {
                return true;
            }
        }
        return false;
    }

    private final void setJump(final JumpType type, final int size) {
        jumpType = type;
        jumpSize = size;
        jumpCount = 0;
    }

//    @Override
    public final boolean[] getAction(final Environment observation) {
        final float marioSpeed = observation.getMarioFloatPos()[0] - prevPos[0];
        final boolean dangerOfEnemy = enemyInRange(observation, new Rectangle(-13, -57, 105, 87));
        final boolean dangerOfEnemyAbove = enemyInRange(observation, new Rectangle(-28, 28, 58, 45));
        final boolean dangerOfGap = dangerOfGap(observation.getLevelSceneObservation());
        if ((observation.isMarioOnGround() || observation.mayMarioJump()) && !jumpType.equals(JumpType.NONE)) {
            setJump(JumpType.NONE, -1);
        } else if (observation.mayMarioJump()) {
            final int wallHeight = getWallHeight(observation.getLevelSceneObservation());
            if (dangerOfGap && marioSpeed > 0) {
                setJump(JumpType.GAP, marioSpeed < 6 ? (int) (9 - marioSpeed) : 1);
            } else if (marioSpeed <= 1 && !dangerOfEnemyAbove && wallHeight > 0) {
                setJump(JumpType.WALL, wallHeight >= 4 ? wallHeight + 3 : wallHeight);
            } else if (dangerOfEnemy && !(dangerOfEnemyAbove && marioSpeed > 2)) {
                setJump(JumpType.ENEMY, 7);
            }
        } else {
            jumpCount++;
        }
        final boolean isFalling = prevPos[1] < observation.getMarioFloatPos()[1] && jumpType.equals(JumpType.NONE);
        action[Mario.KEY_LEFT] = isFalling && ((dangerOfEnemy && dangerOfEnemyAbove) || dangerOfGap);
        action[Mario.KEY_RIGHT] = !isFalling && !(dangerOfEnemyAbove && jumpType == JumpType.WALL);
        action[Mario.KEY_JUMP] = !jumpType.equals(JumpType.NONE) && jumpCount < jumpSize;
        action[Mario.KEY_SPEED] = !(jumpType.equals(JumpType.ENEMY) && action[Mario.KEY_SPEED] && observation.getMarioMode() == 2);
        prevPos = observation.getMarioFloatPos().clone();
        return action;
    }
}