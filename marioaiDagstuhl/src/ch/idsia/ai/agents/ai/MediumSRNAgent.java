package ch.idsia.ai.agents.ai;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.Evolvable;
import ch.idsia.ai.SRN;
import ch.idsia.mario.environments.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: Jun 17, 2009
 * Time: 2:50:34 PM
 */
public class MediumSRNAgent extends BasicAIAgent implements Agent, Evolvable {

    private SRN srn;
    final int numberOfOutputs = 6;
    final int numberOfInputs = 53;
    static private final String name = "MediumSRNAgent";

    public MediumSRNAgent() {
        super (name);
        srn = new SRN (numberOfInputs, 10, numberOfOutputs);
    }

    private MediumSRNAgent(SRN srn) {
        super (name);
        this.srn = srn;
    }

    public Evolvable getNewInstance() {
        return new MediumSRNAgent(srn.getNewInstance());
    }

    public Evolvable copy() {
        return new MediumSRNAgent(srn.copy ());
    }

    public void reset() {
        srn.reset ();
    }

    public void mutate() {
        srn.mutate ();
    }

    public boolean[] getAction(Environment observation) {
        byte[][] scene = observation.getLevelSceneObservation(/*1*/);
        byte[][] enemies = observation.getEnemiesObservation(/*0*/);
        double[] inputs = new double[numberOfInputs];

        int which = 0;
        for (int i = -2; i < 3; i++) {
            for (int j = -2; j < 3; j++) {
                inputs[which++] = probe(i, j, scene);
            }
        }
        for (int i = -2; i < 3; i++) {
            for (int j = -2; j < 3; j++) {
                inputs[which++] = probe(i, j, enemies);
            }
        }
        inputs[inputs.length - 3] = observation.isMarioOnGround() ? 1 : 0;
        inputs[inputs.length - 2] = observation.mayMarioJump() ? 1 : 0;
        inputs[inputs.length - 1] = 1;
        double[] outputs = srn.propagate (inputs);
        boolean[] action = new boolean[numberOfOutputs];
        for (int i = 0; i < action.length; i++) {
            action[i] = outputs[i] > 0;
        }
        return action;
    }

    public AGENT_TYPE getType() {
        return AGENT_TYPE.AI;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
    }

    private double probe (int x, int y, byte[][] scene) {
        int realX = x + 11;
        int realY = y + 11;
        return (scene[realX][realY] != 0) ? 1 : 0;
    }
}
