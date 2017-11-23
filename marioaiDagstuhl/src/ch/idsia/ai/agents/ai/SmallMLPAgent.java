package ch.idsia.ai.agents.ai;

import ch.idsia.ai.Evolvable;
import ch.idsia.ai.MLP;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: Apr 28, 2009
 * Time: 2:09:42 PM
 */
public class SmallMLPAgent extends BasicAIAgent implements Agent, Evolvable {

    private MLP mlp;
    final int numberOfOutputs = 6;
    final int numberOfInputs = 21;
    static private final String name = "SmallMLPAgent";

    public SmallMLPAgent() {
        super (name);
        mlp = new MLP (numberOfInputs, 10, numberOfOutputs);
    }

    private SmallMLPAgent(MLP mlp) {
        super (name);
        this.mlp = mlp;
    }

    public Evolvable getNewInstance() {
        return new SmallMLPAgent(mlp.getNewInstance());
    }

    public Evolvable copy() {
        return new SmallMLPAgent(mlp.copy ());
    }

    public void reset() {
        mlp.reset ();
    }

    public void mutate() {
        mlp.mutate ();
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
        double[] outputs = mlp.propagate (inputs);
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
