package competition.cig.slawomirbojarski;

/**
 * Copyright (c) 2010, Slawomir Bojarski <slawomir.bojarski@maine.edu>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

/**
 * Basically copied the BasicAIAgent class from the 
 * Mario AI code and made some minor modifications.
 */
public class ModifiedBasicAIAgent implements Agent {
    protected boolean action[] = new boolean[Environment.numberOfButtons];
    protected String name = "Instance_of_BasicAIAgent._Change_this_name";

    /*final*/ protected byte[][] levelScene;
    /*final */protected byte[][] enemies;
    protected byte[][] mergedObservation;

    protected float[] marioFloatPos = null;
    protected float[] enemiesFloatPos = null;

    protected int[] marioState = null;

    protected int marioStatus;
    protected int marioMode;
    protected boolean isMarioOnGround;
    protected boolean isMarioAbleToJump;
    protected boolean isMarioAbleToShoot;
    protected boolean isMarioCarrying;
    protected int getKillsTotal;
    protected int getKillsByFire;
    protected int getKillsByStomp;
    protected int getKillsByShell;

    // values of these variables could be changed during the Agent-Environment interaction.
    // Use them to get more detailed or less detailed description of the level.
    // for information see documentation for the benchmark <link: marioai.org/marioaibenchmark/zLevels
    protected int zLevelScene = 1;
    protected int zLevelEnemies = 0;


    public ModifiedBasicAIAgent(String s) {
        setName(s);
        levelScene = new byte[Environment.HalfObsHeight * 2][Environment.HalfObsWidth * 2];
        enemies = new byte[Environment.HalfObsHeight * 2][Environment.HalfObsWidth * 2];
        mergedObservation = new byte[Environment.HalfObsHeight * 2][Environment.HalfObsWidth * 2];
    }

    public void integrateObservation(int[] serializedLevelSceneObservationZ, int[] serializedEnemiesObservationZ, float[] marioFloatPos, float[] enemiesFloatPos, int[] marioState) {
        int k = 0;
        for (int i = 0; i < levelScene.length; ++i) {
            for (int j = 0; j < levelScene[0].length; ++j) {
                levelScene[i][j] = (byte)serializedLevelSceneObservationZ[k];
                enemies[i][j] = (byte)serializedEnemiesObservationZ[k++];
                mergedObservation[i][j] = levelScene[i][j];
                // Simulating merged observation!
                if (enemies[i][j] != 0) {
                    mergedObservation[i][j] = enemies[i][j];
                }
//                System.out.print(observation[i][j] + "\t");
            }
//            System.out.println();
        }
        this.marioFloatPos = marioFloatPos;
        this.enemiesFloatPos = enemiesFloatPos;
        this.marioState = marioState;

        marioStatus = marioState[0];
        marioMode = marioState[1];
        isMarioOnGround = marioState[2] == 1 ;
        isMarioAbleToJump = marioState[3] == 1;
        isMarioAbleToShoot = marioState[4] == 1;
        isMarioCarrying = marioState[5] == 1;
        getKillsTotal = marioState[6];
        getKillsByFire = marioState[7];
        getKillsByStomp = marioState[8];
        getKillsByShell = marioState[9];        
    }

    public boolean[] getAction() {
        return new boolean[Environment.numberOfButtons];
    }

  /*  public void integrateObservation(Environment environment) {
        levelScene = environment.getLevelSceneObservationZ(zLevelScene);
        enemies = environment.getEnemiesObservationZ(zLevelEnemies);
        mergedObservation = environment.getMergedObservationZZ(zLevelScene, zLevelEnemies);

        this.marioFloatPos = environment.getMarioFloatPos();
        this.enemiesFloatPos = environment.getEnemiesFloatPos();
        this.marioState = environment.getMarioState();

        // It also possible to use direct methods from Environment interface.
        //
        marioStatus = environment.
        marioMode = environment.getMarioMode();
        isMarioOnGround = environment.isMarioOnGround();
        isMarioAbleToJump = environment.mayMarioJump();
        isMarioAbleToShoot = environment.canShoot();
        isMarioCarrying = environment.isMarioCarrying();
        getKillsTotal = environment.getKillsTotal();
        getKillsByFire = environment.getKillsByFire();
        getKillsByStomp = environment.getKillsByStomp();
        getKillsByShell = environment.getKillsByShell();
    }*/

    public void reset() {
        action = new boolean[Environment.numberOfButtons];// Empty action
    }

    @Deprecated
    public boolean[] getAction(Environment observation) {
        return new boolean[Environment.numberOfButtons]; // Empty action
    }

    public AGENT_TYPE getType() {
        return Agent.AGENT_TYPE.AI;
    }

    public String getName() {        
    	return name;    
    }

    public void setName(String Name) { 
    	this.name = Name;    
    }
}
