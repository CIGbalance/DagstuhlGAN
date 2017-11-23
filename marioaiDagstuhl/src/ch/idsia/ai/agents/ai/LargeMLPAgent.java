package ch.idsia.ai.agents.ai;

import ch.idsia.ai.MLP;
import ch.idsia.ai.Evolvable;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: Jun 14, 2009
 * Time: 1:43:04 PM
 */
public class LargeMLPAgent extends BasicAIAgent implements Agent, Evolvable {

    static private String name = "LargeMLPAgent";
    private MLP mlp;
    final int numberOfOutputs = Environment.numberOfButtons;
    final int numberOfInputs = 101;

    public LargeMLPAgent() {
        super (name);
        mlp = new MLP (numberOfInputs, 10, numberOfOutputs);
    }

    private LargeMLPAgent(MLP mlp) {
        super (name);
        this.mlp = mlp;
    }

    public Evolvable getNewInstance() {
        return new LargeMLPAgent(mlp.getNewInstance());
    }

    public Evolvable copy() {
        return new LargeMLPAgent(mlp.copy ());
    }

    public void reset() {
        mlp.reset ();
    }

    public void mutate() {
        mlp.mutate ();
    }

    public boolean[] getAction(Environment observation) {
        double[] inputs;// = new double[numberOfInputs];
        byte[][] scene = observation.getLevelSceneObservation(/*1*/);
        byte[][] enemies = observation.getEnemiesObservation(/*0*/);
        inputs = new double[numberOfInputs];
        int which = 0;
        for (int i = -3; i < 4; i++) {
            for (int j = -3; j < 4; j++) {
                inputs[which++] = probe(i, j, scene);
            }
        }
        for (int i = -3; i < 4; i++) {
            for (int j = -3; j < 4; j++) {
                inputs[which++] = probe(i, j, enemies);
            }
        }
        inputs[inputs.length - 3] = observation.isMarioOnGround() ? 1 : 0;
        inputs[inputs.length - 2] = observation.mayMarioJump() ? 1 : 0;
        inputs[inputs.length - 1] = 1;
        double[] outputs = mlp.propagate (inputs);
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

    private double probe (int x, int y, byte[][] scene) {
        int realX = x + 11;
        int realY = y + 11;
        return (scene[realX][realY] != 0) ? 1 : 0;
    }


}
