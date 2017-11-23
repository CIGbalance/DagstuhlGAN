package ch.idsia.ai.agents.ai;

import ch.idsia.ai.Evolvable;
import ch.idsia.ai.SRN;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: May 24, 2009
 * Time: 11:24:54 PM
 */
public class SRNAgent implements Agent, Evolvable {

    private SRN srn;
    private String name;
    final int numberOfOutputs = 6;
    final int numberOfInputs = 21;

    public SRNAgent () {
        srn = new SRN (numberOfInputs, 10, numberOfOutputs);
    }

    private SRNAgent (SRN srn) {
        this.srn = srn;
    }

    public Evolvable getNewInstance() {
        return new SRNAgent(srn.getNewInstance());
    }

    public Evolvable copy() {
        return new SRNAgent (srn.copy ());
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
        double[] inputs = new double[]{probe(-1, -1, scene), probe(0, -1, scene), probe(1, -1, scene),
                                probe(-1, 0, scene), probe(0, 0, scene), probe(1, 0, scene),
                                probe(-1, 1, scene), probe(0, 1, scene), probe(1, 1, scene),
                                probe(-1, -1, enemies), probe(0, -1, enemies), probe(1, -1, enemies),
                                probe(-1, 0, enemies), probe(0, 0, enemies), probe(1, 0, enemies),
                                probe(-1, 1, enemies), probe(0, 1, enemies), probe(1, 1, enemies),
                                observation.isMarioOnGround() ? 1 : 0, observation.mayMarioJump() ? 1 : 0,
                                1};
        double[] outputs = srn.propagate (inputs);
        boolean[] action = new boolean[numberOfOutputs];
        for (int i = 0; i < action.length; i++) {
            action[i] = outputs[i] > 0;
        }
        return action;
    }

    public Agent.AGENT_TYPE getType() {
        return Agent.AGENT_TYPE.AI;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private double probe (int x, int y, byte[][] scene) {
        int realX = x + 11;
        int realY = y + 11;
        return (scene[realX][realY] != 0) ? 1 : 0;
    }

}
