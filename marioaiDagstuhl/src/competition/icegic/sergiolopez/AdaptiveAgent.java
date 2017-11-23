package competition.icegic.sergiolopez;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

/**
 *
 * @author slopez
 */
public class AdaptiveAgent extends BasicAIAgent implements Agent {

    boolean lowSpeed = false;
    int lowSpeedTicks = 0;
    boolean preLowSpeed = false;
    double speedScale = 0;
    actionType currentAction = actionType.NONE;
    int actionNeededTicks = 0;
    int actionCurrentTicks = 0;
    int stairCaseHit = 0;
    boolean specialGap = false;
    int totalTicks = 0;
    int shootTimeStamp = 0;
    boolean debug = false;
    byte[][] levelScene;
    byte[][] enemies;
    byte[][] onlyLevelScene;

    final int noEnemy = 0;


    enum actionType {
        NORMALJUMP, SHORTJUMP, DODGE_LEFT, DODGE_DOWN, STOP, ESCAPE, NONE,
        KILLJUMP, STOPDOWN, CONTINUE, STAIRCASE, LONGJUMP, SLOWDOWN, SHOOT, SPECIALGAP
    };

    public AdaptiveAgent() {
        super("SergioLopez_AdaptiveAgent");
        reset();
    }

    @Override
    public void reset() {
        action = new boolean[Environment.numberOfButtons];
        action[Mario.KEY_RIGHT] = true;
        action[Mario.KEY_SPEED] = false;
    }

    private void logMessage(String msg) {
        if (debug)
            System.out.println(msg);
    }

    private void logMessage2(String msg) {
        System.out.println(msg);
    }

    /* Dumps scene map */
    private void printScene(byte[][] scene) {
        if (!debug)
            return;
        for (int x = 0; x < 22; ++x) {
            for (int y = 0; y < 22; ++y) {
//                System.out.print(new PrintfFormat("%4d").sprintf(scene[x][y]));
            }
//            System.out.println("");
        }
    }

    private void printScene2(byte[][] scene) {
        for (int x = 0; x < 22; ++x) {
            for (int y = 0; y < 22; ++y) {
//                System.out.print(new PrintfFormat("%4d").sprintf(scene[x][y]));
            }
            System.out.println("");
        }
    }

    /* Looks for a gap in the next 5 spaces.
    Returns the distance to the nearest gap */
    private int detectNearestGap() {
        int y = 12; /* Check floor level */
        boolean gapDetected = false;

        for (int x = 12; x < 17; ++x) {
            if (levelScene[y][x] == 0) {
                gapDetected = true;
                for (int s = y; s < 22; ++s) {
                    if (levelScene[s][x] != 0) {
                        gapDetected = false;
                    }
                }
                if (gapDetected)
                    return x - 11;
            }
        }

        return -1;
    }

    private boolean detectSpecialGap() {
        boolean gapDetected = false;
        int y = 12;

        for (; y < 22; ++y) {
            if (levelScene[y][12] != 0)
                break;
        }

        int height = y - 11;

        logMessage("Height: " + height);

        if (height == 10)
            return false;

        if (height >= 3) {
            for (y = 12; y < 22; ++y) {
                if (levelScene[y][13] != 0)
                    break;
            }
            if ((y - 11) == height) {
                gapDetected = true;
                for (; y < 22; ++y) {
                    if (levelScene[y][14] != 0)
                        gapDetected = false;
                }
                if (gapDetected)
                    return true;
            }
        }

        return false;


    }
    

    /* Looks for a level change in the next 5 spaces.
     * Returns the distance to the level change and its level */
    private int[] detectLevelChange() {
        int y = 12; /* Check floor level */

        //printScene2(onlyLevelScene);

        for (int x = 12; x < 17; ++x) {
            if (onlyLevelScene[y][x] == 0 || (onlyLevelScene[y][x] >= -108 && onlyLevelScene[y][x] <= -106)) {
                for (; y < 22; ++y) {
                    if (onlyLevelScene[y][x] != 0/* && enemies[y][x] == noEnemy*/)
                        return new int[]{x - 11, y - 1};
                }
                return new int[]{-1, 0};
            }
        }
        return new int[]{-1, 0};
    }

    private int[] detectEnemiesInLevel(int level) {
        int enemyCount = 0;
        int firstEnemyDistance = -1;

        for (int x = 12; x < 22; ++x) {
            if (enemies[level][x] != noEnemy) {
                enemyCount++;
                if (firstEnemyDistance == -1)
                    firstEnemyDistance = x - 11;
            }
        }
        if (enemyCount > 0)
            return new int[]{firstEnemyDistance, enemyCount};

        return new int[]{-1, 0};
    }

    /* Looks for a enemy in the next 5 spaces.
    Returns the distance to the nearest enemy */
    private int detectNearestObstacle(Environment observation) {
        int y = 11; /* Check mario's level */

        for (int x = 12; x < 17; ++x) {
            if (levelScene[y][x] != 0) {
                if ((levelScene[y][x] >= -108 && levelScene[y][x] <= -106) 
                        || levelScene[y][x] == 34 || levelScene[y][x] == 10
                        || enemies[y][x] == 25) {
                    continue;
                }
                if (enemies[y][x] != noEnemy) {
                    x = x + 200;
                }
                return x - 11;
            }
        }

       
        y = y - 1;
        for (int x = 12; x <= 17; ++x) {
            if (levelScene[y][x] != 0) {
                if ((levelScene[y][x] >= -108 && levelScene[y][x] <= -106) ||
                     levelScene[y][x] == 34 || levelScene[y][x] == 8) {
                    continue;
                }
                //logMessage("Obstacle detected: " + levelScene[y][x] + " " + y  + "x" + x);
                //logMessage("Distance: " + (x - 11));
                if (enemies[y][x] != noEnemy) {
                    x = x + 200;
                    return x - 11;
                }
            }
        }

        return -1;
    }

    /* Looks for a enemy in the next 5 spaces, five levels over mario.
    Returns the distance to the nearest enemy and its level */
    private int[] detectNearestLowerEnemy() {
        for (int x = 12; x < 17; ++x) {
            for (int y = 12; y < 22; ++y) {
                if (enemies[y][x] != noEnemy && enemies[y][x] != 8) {
                    //logMessage("LowerEnemy detected: " + levelScene[y][x] + " " + y  + "x" + x);
                    //logMessage("Distance: " + (x - 11));
                    //logMessage("Level: " + y);
                    //printScene(levelScene);
                    return new int[]{x - 11, y};
                }
            }
        }

        return new int[]{-1, 0};
    }

    private int[] detectNearestUpperEnemy() {
        for (int y = 10; y > 5; --y) {
            for (int x = 11; x < 20; ++x) {
                if (enemies[y][x] != noEnemy && enemies[y][x] != 12 && enemies[y][x] != 8) {
                    logMessage("UpperEnemy detected: " + enemies[y][x] + " " + y  + "x" + x);
                    //logMessage("Distance: " + (x - 11));
                    //logMessage("Level: " + y);
                    //printScene(levelScene);
                    return new int[]{x - 11, y};
                }
            }
        }

        return new int[]{-1, 0};
    }

    private boolean detectBlocksOverHead() {
        for (int y=10; y > 6; --y)
            for (int x=11; x < 14; ++x)
                if (levelScene[y][x] == 16 || levelScene[y][x] == 17)
                    return true;

        return false;
    }

    private boolean isEnemyNearPosition(int x, int y) {
        if ((enemies[y+1][x] != noEnemy && enemies[y+1][x] != 1) ||
            (enemies[y+1][x+1] != noEnemy && enemies[y+1][x+1] != 1) ||
            (enemies[y][x+1] != noEnemy  && enemies[y][x+1] != 1) ||
            (enemies[y-1][x+1] != noEnemy && enemies[y-1][x+1] != 1) ||
            (enemies[y-1][x] != noEnemy && enemies[y-1][x] != 1))
            return true;
        return false;
    }

    private int evaluateJump(boolean shortJump) {
        double elevationScale = -0.8;
        double jumpScale = 7;
        double x = 11;
        double y = 11;
        boolean hCol = false;
        boolean vCol = false;
        int enemyDanger = 0;
        int jumpDanger = 0;

        
        if (detectBlocksOverHead() && !shortJump)
        {
            logMessage("Blocks over my head!");
            //printScene(levelScene);
            jumpDanger += 2;
        }
         

        if (levelScene[11][12] == -10 && levelScene[10][12] == 0) {
            if (!shortJump) {
                jumpDanger += 5;
            }
            else
                jumpDanger -= 5;
        }

        if (shortJump)
            jumpScale = 3;

        int count = 0;
        while (true) {
            int rX = (int) Math.round(x - 0.5);
            int rY = (int) Math.round(y);
            int rES;

            //logMessage(rX + " " + rY);
            //logMessage(x + " " + y);
            
            if (elevationScale > 0) {
                rES = 1;
            } else {
                rES = -1;
            }

            if (!(rX == 11 && rY == 11)) {
                // Evaluate enemy danger while jumping
                if (isEnemyNearPosition(rX, rY)) {
                    logMessage("Enemy near jump: " + rX + "x" + rY);
                    //printScene(enemies);
                    enemyDanger++;
                }

                if (levelScene[rY + rES][rX + 1] != 0) {
                    if (levelScene[rY][rX + 1] != 0) {
                        hCol = true;
                    }

                    if (levelScene[rY + rES][rX] != 0) {
                        vCol = true;
                    }

                    if (hCol && !vCol) {
                        boolean deepGap = true;
                        for (int s = rY; s < 22; s++) {
                            if (levelScene[s][rX] != 0) {
                                deepGap = false;
                            }
                        }
                        if (deepGap) {
                            jumpDanger += 5;
                            break;
                        }
                    }

                    if (!hCol && !vCol) {
                        boolean deepGap = true;
                        for (int s = rY; s < 22; ++s) {
                            if (levelScene[s][rX] != 0) {
                                deepGap = false;
                            }
                        }
                        if (deepGap) {
                            //logMessage("Deep Gap after Diagonal Collision");
                            jumpDanger += 5;
                            break;
                        }
                        if (levelScene[rY + rES][rX + 1] == -10 &&
                                levelScene[rY + rES ][rX] == 0) {
                            //logMessage("Staircase jump");
                            jumpDanger += 5;
                            break;
                        }

                        jumpDanger += 0;
                        break;
                    }

                    /*if (levelScene[rY + rES][rX + 1] == -12) {
                        //logMessage("Staircase jump");
                        jumpDanger += 0;
                        break;
                    }*/

                    if (vCol && !hCol) {
                        if (levelScene[rY + rES][rX] == -10 &&
                                levelScene[rY + rES][rX-1] == 0) {
                            logMessage("Dangerous jump");
                            jumpDanger += 1;
                            break;
                        }
                    }

                    jumpDanger += 0;
                    break;
                }
            }

            x = x + speedScale;
            y = y + elevationScale;
            count++;
            if (count == jumpScale) {
                elevationScale = 1;
            }
            if (x >= 20 || y >= 20) {
                jumpDanger += 5;
                break;
            }
        }

        printScene(levelScene);
        logMessage("Landing: " + x + "x" + y + ":" + levelScene[(int)x][(int)y]);
        logMessage("Speed: " + speedScale);

        return jumpDanger + enemyDanger;
    }

    private int obstacleHeight (int distance) {
        distance += 11;
        for (int y = 11; y > 0; --y)
            if (levelScene[y][distance] == 0 ||
                (enemies[y][distance] != noEnemy && enemies[y][distance] != 8
                && enemies[y][distance] != 12)) {              
                logMessage("obstacleHeight: " + (11 - y));
                logMessage("obstacleHeight distance: " + distance);
                return 11 - y;
            }
        return -1;
    }

    private int[] searchAlternativeLevel() {
        int blocks = 0;
        int firstBlock = 0;

        for (int y = 10; y > 6; --y) {
            for (int x = 12; x < 22; ++x) {
                if (levelScene[y][x] != 0 && levelScene[y][x] != 34
                     && !(levelScene[y][x] >= -108 && levelScene[y][x] <= -106)) {
                    blocks++;
                    if (firstBlock == 0)
                        firstBlock = x;
                }
            }
            if (blocks > 2)
                return new int[]{firstBlock - 11, y - 1};

            blocks = 0;
        }

        return new int[]{-1, 0};
    }

    private boolean shouldWaitForBullets() {
        boolean bulletsOnScreen = false;

        for (int x = 0; x < 22; ++x) {
            for (int y = 0; y < 22; ++y) {
                if (enemies[y][x] == 8) {
                    logMessage("Bullets on Screen");
                    bulletsOnScreen = true;
                    break;
                }
            }
        }

        if (bulletsOnScreen) {
            for (int x = 1; x < 21; ++x) {
                for (int y = 1; y < 21; ++y) {
                    /*if (levelScene[y][x] == -10 && levelScene[y+1][x-1] == -10
                            && levelScene[y+1][x] == -10 && levelScene[y+1][x+1] == -10
                            && levelScene[y][x-1] != -10 && levelScene[y][x+1] != -10) {
                        logMessage2("Cannons on Screen");
                        return false;
                    }*/
                    if (levelScene[y][x] == 20
                            && levelScene[y][x+1] == 0
                            && levelScene[y][x-1] == 0) {
                        logMessage("Cannons on Screen");
                        return false;
                    }
                }
            }
            
            for (int x = 0; x < 22; ++x) {
                if (levelScene[11][x] == 8) {
                    // Bullets in my level, don't wait
                    return false;
                }
            }


            for (int x = 0; x < 22; ++x) {
                for (int y = 0; y < 22; ++y) {
                    if (enemies[y][x] != 8 && enemies[y][x] != noEnemy &&
                        enemies[y][x] != 12 && enemies[y][x] != 1) {
                        logMessage("Can't wait: " + enemies[y][x]);
                        printScene(enemies);
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private int cannonBallsOverMe() {
        for (int y = 9; y > 4; --y) {
            for (int x = 8; x < 15; ++x) {
                if (enemies[y][x] == 8)
                    return y;
            }
        }
        return -1;
    }

    private boolean enemiesOverMe() {
        for (int y = 10; y > 5; --y)
            if (enemies[y][11] != noEnemy)
                return true;
        return false;
    }

    private void guessSpeed(Environment observation) {
        if (lowSpeed) {
            if (lowSpeedTicks > 8) {
                lowSpeed = false;
                lowSpeedTicks = 0;
            } else if (observation.isMarioOnGround()) {
                lowSpeedTicks++;
            }
        }

        if (currentAction == actionType.STOPDOWN
                || currentAction == actionType.SPECIALGAP) {
            lowSpeed = true;
            lowSpeedTicks = 0;
            preLowSpeed = false;
        }
        else if (levelScene[11][12] != 0 && observation.isMarioOnGround()) {
            if (preLowSpeed) {
                lowSpeed = true;
                lowSpeedTicks = 0;
                preLowSpeed = false;
            }
            else
                preLowSpeed = true;
        }
        if (lowSpeed) {
            speedScale = 0.37;
        } else {
            speedScale = 0.45;
        }
    }

    private boolean detectFloorBehindMe() {
        if (levelScene[12][10] != 0 && levelScene[12][9] != 0 &&
                enemies[11][10] == noEnemy && enemies[11][9] == noEnemy &&
                enemies[11][11] == noEnemy)
            return true;
        return false;
    }

    private boolean[] doAction(actionType thisAction, Environment observation, boolean force) {
        // Check if we're hitting the ground after jumping

        if (!finishAction(observation, force))
            return action;

        if (thisAction == currentAction)
            return action;

        logMessage("Starting action: " + thisAction.toString() + " at " + totalTicks);

        if (thisAction == actionType.NORMALJUMP) {
            currentAction = actionType.NORMALJUMP;
            actionNeededTicks = 16;
            action[Mario.KEY_JUMP] = true;
            action[Mario.KEY_RIGHT] = true;
            action[Mario.KEY_LEFT] = false;
            action[Mario.KEY_DOWN] = false;
        }
        else if (thisAction == actionType.SHORTJUMP) {
            currentAction = actionType.SHORTJUMP;
            actionNeededTicks = 0;
            action[Mario.KEY_JUMP] = true;
            action[Mario.KEY_RIGHT] = true;
            action[Mario.KEY_LEFT] = false;
        }
        else if (thisAction == actionType.LONGJUMP) {
            currentAction = actionType.LONGJUMP;
            actionNeededTicks = 20;
            action[Mario.KEY_JUMP] = true;
            action[Mario.KEY_RIGHT] = true;
            action[Mario.KEY_LEFT] = false;
            action[Mario.KEY_DOWN] = false;
            action[Mario.KEY_SPEED] = true;
        }
        else if (thisAction == actionType.DODGE_LEFT) {
            currentAction = actionType.DODGE_LEFT;
            actionNeededTicks = 5;
            action[Mario.KEY_LEFT] = true;
            action[Mario.KEY_RIGHT] = false;
        }
        else if (thisAction == actionType.ESCAPE) {
            logMessage("Escape!");
            currentAction = actionType.ESCAPE;
            actionNeededTicks = 10;
            action[Mario.KEY_LEFT] = true;
            action[Mario.KEY_RIGHT] = false;
        }
        else if (thisAction == actionType.STOP) {
            currentAction = actionType.STOP;
            actionNeededTicks = 5;
            action[Mario.KEY_RIGHT] = false;
        }
        else if (thisAction == actionType.KILLJUMP) {
            currentAction = actionType.KILLJUMP;
            actionNeededTicks = 2;
            action[Mario.KEY_RIGHT] = false;
            action[Mario.KEY_JUMP] = true;
        }
        else if (thisAction == actionType.STOPDOWN) {
            currentAction = actionType.STOPDOWN;
            actionNeededTicks = 10;
            action[Mario.KEY_DOWN] = true;
            action[Mario.KEY_RIGHT] = false;
            action[Mario.KEY_LEFT] = false;
        }
        else if (thisAction == actionType.CONTINUE) {
            currentAction = actionType.CONTINUE;
            actionNeededTicks = 1;
            action[Mario.KEY_RIGHT] = true;
            //action[Mario.KEY_SPEED] = true;
        }
        else if (thisAction == actionType.STAIRCASE) {
            currentAction = actionType.STAIRCASE;
            actionNeededTicks = 0;
            action[Mario.KEY_JUMP] = true;
        }
        else if (thisAction == actionType.SLOWDOWN) {
            currentAction = actionType.SLOWDOWN;
            actionNeededTicks = 1;
            action[Mario.KEY_RIGHT] = false;
        }
        else if (thisAction == actionType.SHOOT) {
            currentAction = actionType.SHOOT;
            actionNeededTicks = 3;
            action[Mario.KEY_SPEED] = true;
            //action[Mario.KEY_RIGHT] = false;
        }
        else if (thisAction == actionType.SPECIALGAP) {
            currentAction = actionType.SPECIALGAP;
            actionNeededTicks = 100;
            action[Mario.KEY_RIGHT] = true;
        }

        return action;
    }

    private boolean finishAction(Environment observation, boolean force) {

        if (currentAction == actionType.SPECIALGAP) {
            if (!specialGap) {
                // Before jump
                if (!observation.isMarioOnGround()) {
                    action[Mario.KEY_RIGHT] = false;
                    action[Mario.KEY_LEFT] = true;
                    specialGap = true;
                    return false;
                }
            }
            else if (specialGap) {
                if (levelScene[11][10] != 0) {
                    action[Mario.KEY_RIGHT] = true;
                    action[Mario.KEY_LEFT] = false;
                    specialGap = false;
                    return true;
                }
            }
        }
        else if ((currentAction == actionType.NORMALJUMP ||
                  currentAction == actionType.SHORTJUMP ||
                  currentAction == actionType.KILLJUMP ||
                  currentAction == actionType.STAIRCASE ||
                  currentAction == actionType.LONGJUMP) &&
                  observation.isMarioOnGround()) {
            action[Mario.KEY_JUMP] = false;
            action[Mario.KEY_DOWN] = false;
            action[Mario.KEY_SPEED] = false;
            action[Mario.KEY_RIGHT] = true;
            action[Mario.KEY_LEFT] = false;
            currentAction = actionType.NONE;
            actionCurrentTicks = 0;
            return false;
        }
        else if (force && currentAction != actionType.ESCAPE
                && currentAction != actionType.DODGE_LEFT
                && currentAction != actionType.SHOOT) {
            logMessage("Finishing by force action: " + currentAction.toString() + " at " + totalTicks);
            action[Mario.KEY_JUMP] = false;
            action[Mario.KEY_DOWN] = false;
            action[Mario.KEY_SPEED] = false;
            action[Mario.KEY_LEFT] = false;
            action[Mario.KEY_RIGHT] = true;
            currentAction = actionType.NONE;
            actionCurrentTicks = 0;
            return true;
        }
        else if ((actionCurrentTicks >= actionNeededTicks &&
                    currentAction != actionType.NONE)) {
            logMessage("Finishing action: " + currentAction.toString() + " at " + totalTicks);
            action[Mario.KEY_JUMP] = false;
            action[Mario.KEY_DOWN] = false;
            action[Mario.KEY_SPEED] = false;
            action[Mario.KEY_RIGHT] = true;
            action[Mario.KEY_LEFT] = false;
            currentAction = actionType.NONE;
            actionCurrentTicks = 0;
            return true;
        }
        else if (currentAction == actionType.DODGE_LEFT) {
            // Refuse to end current action
            actionCurrentTicks++;
            return false;
        }
        else if (currentAction == actionType.SHOOT) {
            if (actionCurrentTicks == 1) {
                action[Mario.KEY_SPEED] = false;
                actionCurrentTicks++;
                return true;
            }
            actionCurrentTicks++;
            return false;
        }
        else if (currentAction == actionType.ESCAPE) {
            if (actionCurrentTicks == 8) {
                action[Mario.KEY_RIGHT] = false;
                action[Mario.KEY_LEFT] = false;
                actionCurrentTicks++;
                return true;
            }
            actionCurrentTicks++;
            return false;
        }
        else if (currentAction != actionType.NONE)
            actionCurrentTicks++;

        return true;
    }

    @Override
    public boolean[] getAction(Environment observation) {
        assert (observation != null);
        totalTicks++;

        int shouldJump = 0;
        int normalJumpDanger = 0;
        int shortJumpDanger = 0;
        int jumpDanger = 0;
        boolean shortJump = false;
        boolean forceAction = false;
        
        actionType thisAction = actionType.NONE;

        boolean obstacleIsEnemy = false;

        // Get scene state from exposed interface
        levelScene = observation.getCompleteObservation();
        enemies = observation.getEnemiesObservation();
        onlyLevelScene = observation.getLevelSceneObservation();

        //printScene2(levelScene);


        int gapDistance = detectNearestGap();
        int obstacleDistance = detectNearestObstacle(observation);
        if (obstacleDistance > 200) {
            obstacleIsEnemy = true;
            obstacleDistance = obstacleDistance - 200;
        }

        logMessage(gapDistance + " " + obstacleDistance);

        int upperEnemy[] = detectNearestUpperEnemy();
        int lowerEnemy[] = detectNearestLowerEnemy();
        int levelChange[]  = detectLevelChange();

        // Guess speed (adjust lowSpeed properly)
        guessSpeed(observation);

        // Clear staircase condition
        if (!(levelScene[11][12] == -10 && levelScene[10][12] == 0))
            stairCaseHit = 0;

        // Analyze immediate threats
        if (gapDistance == 1) {
            shouldJump += 5;
        } 
        else if (obstacleDistance == 1) {
            if (obstacleIsEnemy) {
                logMessage("ObstacleIsEnemy");
                shouldJump += 5;
            } else if (levelScene[11][12] == -10
                    && levelScene[10][12] == 0
                    && observation.isMarioOnGround()) {
                logMessage("StairCase detected");
                if (stairCaseHit >= 2) {
                    stairCaseHit = 0;
                    thisAction = actionType.STAIRCASE;
                    return doAction(thisAction, observation, true);
                }
                else {
                    // Ugly Hack
                    logMessage("Waiting for staircase");
                    stairCaseHit++;
                    thisAction = actionType.NONE;
                    return doAction(thisAction, observation, true);
                }
            } else {
                shouldJump++;
            }
        }
        else if (detectSpecialGap() && observation.isMarioOnGround()) {
            thisAction = actionType.SPECIALGAP;
            return doAction(thisAction, observation, true);
        }
        else if (obstacleDistance == 2 && obstacleIsEnemy) {
            logMessage("ShouldJumpNOW!");
            //printScene(levelScene);
            shouldJump += 5;
        }

        // Analyze future threats
        if (gapDistance != -1 && gapDistance < 3) {
            shouldJump += 2;
        }


        if (obstacleDistance != -1 && obstacleDistance < 3)
            shouldJump++;


        if (observation.isMarioOnGround()) {
            // We're on the ground.
            // Consider risk of jumping and take the better
            normalJumpDanger = evaluateJump(false);
            shortJumpDanger = evaluateJump(true);

            // Bullets on screen, increse jump danger.
            if (shouldWaitForBullets()) {
                normalJumpDanger += 2;
                shortJumpDanger += 2;
            }

            // Try to avoid those f*cking bullets.
            int cannonBallsLevel = cannonBallsOverMe();
            if (cannonBallsLevel != -1) {
                logMessage("Cannon Balls over me!");
                logMessage("CannonBalls: " + cannonBallsLevel);
                if (cannonBallsLevel >= 14) {
                    normalJumpDanger += 5;
                } else {
                    normalJumpDanger += 5;
                    shortJumpDanger += 5;
                }
            }

            if (obstacleIsEnemy && obstacleDistance < 6) {
                logMessage("foo: " + (obstacleDistance+11));
                printScene(enemies);
                if (enemies[8][obstacleDistance+11] != noEnemy) {
                    logMessage("bar");
                    thisAction = actionType.SHOOT;
                    return doAction(thisAction, observation, true);
                }
            }
        
            logMessage("Lower Enemy distance: " + lowerEnemy[0]);
            logMessage("Lower Enemy lvl: " + lowerEnemy[1]);
            logMessage("Level change distance: " + levelChange[0]);
            logMessage("Level change lvl: " + levelChange[1]);

            
            if (lowerEnemy[0] != -1 && levelChange[0] < 2 && gapDistance == -1) {
                int enemyObstacleDistance = lowerEnemy[0] - levelChange[0];

                if (enemyObstacleDistance < 4/* && obstacleDistance == 0*/) {
                    shouldJump += 5;
                }
                else {
                    thisAction = actionType.SLOWDOWN;
                    return doAction(thisAction, observation, true);
                }
            }


            if (upperEnemy[0] != -1) {
                int oHeight = obstacleHeight(obstacleDistance);
                int enemyLevel = 11 - upperEnemy[1];

                logMessage("upperEnemy distance: " + upperEnemy[0]);
                logMessage("upperEnemy lvl: " + enemyLevel);
                logMessage("obstacleDisntace: " + obstacleDistance);
                logMessage("obstacleHeight: " + oHeight);

                if (obstacleDistance != -1) {
                    if (oHeight == enemyLevel) {
                        // Enemy in my next level
                        int enemyObstacleDistance = upperEnemy[0] - obstacleDistance;
                        logMessage("enemyObstacleDistance: " + enemyObstacleDistance);
                        if (enemyObstacleDistance < 3) {
                            logMessage("enemy on the border");
                            if (obstacleDistance < 4) {
                                /*if (detectFloorBehindMe()) {
                                    logMessage("Try to scape");
                                    thisAction = actionType.ESCAPE;
                                } else {*/
                                    logMessage("Jump and hope for the best");
                                    thisAction = actionType.NORMALJUMP;
                                //}
                                return doAction(thisAction, observation, true);
                            }
                            else {
                                shortJumpDanger += 2;
                            }
                        }
                        else if (oHeight >= 3 && enemyObstacleDistance < 5) {
                            thisAction = actionType.LONGJUMP;
                            return doAction(thisAction, observation, true);
                        }
                        else if (oHeight < 3 && enemyObstacleDistance > 3) {
                            normalJumpDanger += 2;
                        }
                        else if (enemyObstacleDistance < 4) {
                            shortJumpDanger += 2;
                        }
                    }
                    else
                        normalJumpDanger += 2;
                }
            }

            if (shortJumpDanger < normalJumpDanger) {
                shortJump = true;
                jumpDanger = shortJumpDanger;
            } else {
                jumpDanger = normalJumpDanger;
            }


            // Determine action
            if (shouldJump > 0) {
                if (shouldJump >= jumpDanger) {
                    // We seem to be safe jumping, do it.
                    if (shortJump)
                        logMessage("ShortJump! " + speedScale);
                    else
                        logMessage("Jump! " + speedScale);
                    logMessage("shouldJump: " + shouldJump);
                    logMessage("normalJumpDanger: " + normalJumpDanger);
                    logMessage("shortJumpDanger: " + shortJumpDanger);

                    int oHeight = obstacleHeight(obstacleDistance);
                    logMessage("obstacleHeight: " + oHeight);

                    // Plants in their tubes are not our real obstacle
                    if (obstacleIsEnemy && enemies[11][obstacleDistance+11] == 12)
                        obstacleIsEnemy = false;
                    logMessage("stuff: " + obstacleIsEnemy);

                    if (shortJump && !obstacleIsEnemy && obstacleDistance < 2 &&
                        oHeight > 1) {
                        //printScene(levelScene);
                        logMessage("We're stuck, use evasive action: " +
                                oHeight);
                        if (upperEnemy[0] == -1 || upperEnemy[1] > 1) {
                            thisAction  = actionType.NORMALJUMP;
                            forceAction = true;
                        }
                        else if (detectFloorBehindMe()) {
                            thisAction = actionType.DODGE_LEFT;
                            forceAction = true;
                        }
                        else {
                            thisAction = actionType.STOPDOWN;
                            forceAction = true;
                        }
                    }
                    else if (shortJump) {
                        thisAction = actionType.SHORTJUMP;
                        forceAction = true;
                    }
                    else {
                        thisAction = actionType.NORMALJUMP;
                        forceAction = true;
                    }
                }
                else {
                    // Do something else...
                    logMessage("Don't want to jump");
                    logMessage("shouldJump: " + shouldJump);
                    logMessage("normalJumpDanger: " + normalJumpDanger);
                    logMessage("shortJumpDanger: " + shortJumpDanger);
                }
            }
            else {
                // No need to jump.
                // Check for plants
                if ((levelScene[10][12] == 12 && levelScene[10][13] != 20)
                        || (levelScene[11][12] == 12 && levelScene[11][13] != 20)
                        || (levelScene[12][12] == 12 && levelScene[12][13] != 20)
                        || (levelScene[10][11] == 12 && levelScene[10][12] != 20 && observation.getMarioMode() != 0)
                        || (levelScene[11][11] == 12 && levelScene[11][12] != 20)) {
                    logMessage("Plant collision in floor!");
                    thisAction = actionType.DODGE_LEFT;
                    return doAction(thisAction, observation, true);
                }
                // Bullets on  my rear
                if (enemies[10][10] == 8 || enemies[10][11] == 8 || enemies[10][12] == 8) {
                    thisAction = actionType.STOPDOWN;
                    return doAction(thisAction, observation, true);
                }

                // Think about waiting to avoid bullets
                if (shouldWaitForBullets()) {
                    logMessage("Wait for bullets");
                    //printScene2(onlyLevelScene);
                    thisAction = actionType.STOPDOWN;
                }

                // Detect small gaps
                if (levelScene[12][12] == 0 &&
                        (levelScene[12][13] != 0 || levelScene[12][14] != 0)) {
                    logMessage("Small gap detected!");
                    thisAction = actionType.SHORTJUMP;
                    return doAction(thisAction, observation, true);
                }

                // Analyze terrain for better paths
                int[] thisLevelEnemies;
                int[] otherLevelEnemies;

                if (levelChange[0] !=  -1 && levelChange[0] == 1) {
                    logMessage("levelChange detected!" + levelChange[1]);
                    thisLevelEnemies = detectEnemiesInLevel(levelChange[1]);
                } else {
                    thisLevelEnemies = detectEnemiesInLevel(11);
                }


                int otherLevel[] = searchAlternativeLevel();
                if (otherLevel[0] != -1) {
                    otherLevelEnemies = detectEnemiesInLevel(otherLevel[1]);
                    logMessage("This lvl: " + levelChange[1]);
                    logMessage("Another Level: " + otherLevel[1]);
                    logMessage("Another Level distance: " + otherLevel[0]);
                    logMessage("This Enemies: " + thisLevelEnemies[1] +
                                       "\nOther enemies: " + otherLevelEnemies[1]);
                    //printScene(enemies);
                    if (otherLevelEnemies[1] < thisLevelEnemies[1] && otherLevel[0] == 2 &&
                        !enemiesOverMe()/* && !detectBlocksOverHead(levelScene)*/) {
                        printScene(levelScene);
                        logMessage("changeLevel!");
                        thisAction = actionType.NORMALJUMP;
                        forceAction = true;
                        return doAction(thisAction, observation, true);
                    }
                }

                /*if (observation.getMarioMode() == 2
                        && (totalTicks - shootTimeStamp) > 10) {
                    shootTimeStamp = totalTicks;
                    thisAction = actionType.SHOOT;
                    return doAction(thisAction, observation, false);
                }*/

            }
        }
        else {
            // We're jumping...
            
            logMessage("We're jumping");
            if ((levelScene[10][12] == 12 && levelScene[10][13] != 20)
                || (levelScene[11][12] == 12 && levelScene[11][13] != 20)
                || (levelScene[12][12] == 12 && levelScene[12][13] != 20)) {
                logMessage("Plant collision while flying!");
                thisAction = actionType.DODGE_LEFT;
                forceAction = true;
            }
            /*else if ((enemies[10][12] != -1 && enemies[10][12] != 12)
                || (enemies[11][12] != -1 && enemies[11][12] != 12)
                || (enemies[12][12] != -1 && enemies[12][12] != 12)) {
                logMessage("Enemy collision while flying!");
                printScene(levelScene);
                thisAction = actionType.DODGE_LEFT;
                forceAction = true;
            }*/
            else if (levelScene[14][11] != 0 && actionCurrentTicks > 5 &&
                    (enemies[13][13] == 9) /*|| enemies[12][14] == 9 || enemies[12][13] == 9)*/) {
                logMessage("Spike collision while flying!");
                //printScene2(enemies);
                thisAction = actionType.DODGE_LEFT;
                forceAction = true;
            }
        }

        return doAction(thisAction, observation, forceAction);
    }
}
