package competition.cig.spencerschumann;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

/**
 *
 * @author Spencer Schumann
 */
public class SpencerSchumann_SlideRule implements Agent {

    private Visualization visualization;
    private Tiles tiles;
    private MarioState mario;
    private EnemySimulator enemySim;
    private boolean manualOverride = false;
    private PlanRunner planRunner;
    private MotionSimulator simulator;

    public SpencerSchumann_SlideRule() {
        // Uncomment following line to enable visualization
        //visualization = new Visualization();
        this.reset();
    }

    //    @Override
    public void reset() {
        tiles = new Tiles();
        mario = new MarioState();
        planRunner = new PlanRunner();
        enemySim = new EnemySimulator();
    }

//    @Override
    public boolean[] getAction(Environment observation) {
        float[] marioPos = observation.getMarioFloatPos();
        tiles.addObservation(observation);
        int mx = (int)(marioPos[0] / 16.0f);
        int my = (int)(marioPos[1] / 16.0f);
        // TODO: in the latest code drop, it looks like there is no "mario hole."
        // So, tiles could be removed.
        byte[][] scene = tiles.getScene(mx - observation.HalfObsWidth,
                my - observation.HalfObsHeight,
                observation.HalfObsWidth * 2, observation.HalfObsHeight * 2);
        mario.update(observation);
        Scene sanitizedScene =
                new Scene(observation, scene);
        enemySim.update(sanitizedScene);
        enemySim.update(observation);

        //TODO: what if some of the bugs I've seen are because of thread safety with the visualization class?
        // TODO: still need to make a deep copy of scene when cloning
        if (visualization != null)
            visualization.update(sanitizedScene.clone(), mario.clone(), enemySim.clone());

        boolean[] action = null;
        if (planRunner.isDone() || planRunner.isLastAction() || manualOverride) {
            MovementPlanner planner = new MovementPlanner(sanitizedScene, mario, enemySim.clone());
            PlanRunner plan = planner.planMovement();
            if (plan != null) {
                //System.out.println("New plan.");
                planRunner = plan;
            } else {
                //System.out.println("Can't get to there from here...");
                // TODO: this is a problem.
                
                // For now, just run to the right & shoot.
                //planRunner = new PlanRunner();
                //planRunner.addKey(Keys.RIGHT);
                //planRunner.addKey(Keys.SPEED, 1, 3);
                action = new boolean[5];
                action[Keys.RIGHT] = true;
                action[Keys.SPEED] = action[Keys.JUMP] =
                        observation.mayMarioJump() || !observation.isMarioOnGround();
            }
            if (visualization != null)
                visualization.setPlan(plan != null,
                        planner.getTargetFloor(),
                        planner.projectedX, planner.projectedY);
            //if (simulator == null)
            //    simulator = new MotionSimulator(sanitizedScene, mario);
        }
        //simulator.updateScene(sanitizedScene);
        //if (visualization != null && simulator.getScene() != null)
        //    visualization.update(vs, simulator.getScene(), simulator.mario);

        // TODO: it would be prudent to validate movement, and re-plan
        // if something goes wrong.
        if (action == null)
            action = planRunner.nextAction();
        return action;
    }

    public boolean[] getActionEx(Environment observation, boolean[] keys) {
        //System.out.println("-------------- new time step -----------------");
        boolean[] action = getAction(observation);

        /*if (action == null) {
            System.out.println(" ROBOT: no action!");
        } else {
            System.out.print(" ROBOT: ");
            int i;
            for (i = 0; i < 5; i++) {
                if (action[i])
                    System.out.print(Keys.toString(i) + " ");
            }
            System.out.println("");
        }*/
        
        if (action == null) {
            //System.out.println("No action!");
            manualOverride = true;
        }

        if (keys[Keys.LEFT] ||
            keys[Keys.RIGHT] ||
            keys[Keys.DOWN] ||
            keys[Keys.JUMP] ||
            keys[Keys.SPEED])
        {
            manualOverride = true;
        }

        if (keys[Keys.SPECIAL]) {
            manualOverride = false;
        }

        if (manualOverride)
            action = keys;

        /*System.out.println(String.format("Action:%s%s%s%s%s  Real x:%f  Real vx:%f\n" +
                                         "               Sim x:%f   Sim vx:%f",
                action[Keys.LEFT] ? "L" : "-",
                action[Keys.RIGHT] ? "R" : "-",
                action[Keys.DOWN] ? "D" : "-",
                action[Keys.JUMP] ? "J" : "-",
                action[Keys.SPEED] ? "S" : "-",
                pos.x, pos.vx, simulator.getX(), simulator.getVX()));*/

        /*if (simulator != null) {
            if (simulator.getX() != mario.x) {
                System.out.println(String.format("SIMULATION ERROR: x is %f, expected %f",
                        simulator.getX(), mario.x));
                //simulator.setX(pos.x);
            }
            if (simulator.getY() != mario.y) {
                System.out.println(String.format("SIMULATION ERROR: y is %f, expected %f",
                        simulator.getY(), mario.y));
                //simulator.setY(pos.y);
            }

            simulator.mario.diff("###", mario);

            // TODO: also create and update another, single-stepped simulator here.
            // The result should match the continuously running one.
            MotionSimulator oneStep = new MotionSimulator(simulator.getScene(), simulator.mario);
            oneStep.mario.diff("Checkpoint 1: oneStep vs. simulator", simulator.mario);
            
            simulator.update(action);
            // New simulator position will be checked next time around.

            oneStep.update(action);

            oneStep.mario.diff("Checkpoint 2: oneStep vs. simulator", simulator.mario);

            if (oneStep.getX() != simulator.getX()) {
                System.out.println(String.format("ONE STEP ERROR: x is %f, expected %f",
                        oneStep.getX(), simulator.getX()));
            }
            if (oneStep.getY() != simulator.getY()) {
                System.out.println(String.format("ONE STEP ERROR: y is %f, expected %f",
                        oneStep.getY(), simulator.getY()));
            }
        }*/

        return action;
    }

//    @Override
    public AGENT_TYPE getType() {
        return AGENT_TYPE.AI;
    }

//    @Override
    public String getName() {
        return "SpencerSchumann_SlideRule";
    }

//    @Override
    public void setName(String name) {
        return;
    }

}
