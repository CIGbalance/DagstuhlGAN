package communication;

import ch.idsia.ai.agents.AgentsPool;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.simulation.BasicSimulator;
import ch.idsia.mario.simulation.Simulation;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.EvaluationOptions;
import ch.idsia.tools.ToolsConfigurator;
import competition.icegic.robin.AStarAgent;

public class MarioProcess extends Comm {
    private EvaluationOptions evaluationOptions;
    private Simulation simulator;

    public MarioProcess() {
        super();
        this.threadName = "MarioProcess";
    }

    public void launchMario() {
        String[] options = new String[] {""};
        this.evaluationOptions = new CmdLineOptions(options);  // if none options mentioned, all defaults are used.
        // set agents
        createAgentsPool();
        // TODO: Change this time limit
        evaluationOptions.setTimeLimit(20);
        // TODO: Make these configurable from commandline?
        evaluationOptions.setMaxFPS(true); // Set true to run faster
        evaluationOptions.setVisualization(true); // Set true to watch evaluations
        // Create Mario Component
        ToolsConfigurator.CreateMarioComponentFrame(evaluationOptions);
        evaluationOptions.setAgent(AgentsPool.getCurrentAgent());
        System.out.println(evaluationOptions.getAgent().getClass().getName());
        // set simulator
        this.simulator = new BasicSimulator(evaluationOptions.getSimulationOptionsCopy());
    }

    /**
     * Set the agent that is evaluated in the evolved levels
     */
    public static void createAgentsPool()
    {
    	// TODO: Simple approach for now. Might generalize later
        AgentsPool.setCurrentAgent(new AStarAgent());
    }

    public void setLevel(Level level) {
        evaluationOptions.setLevel(level);
        this.simulator.setSimulationOptions(evaluationOptions);
    }

    /**
     * Simulate a given level
     * @return
     */
    public EvaluationInfo simulateOneLevel(Level level) {
        setLevel(level);
        EvaluationInfo info = this.simulator.simulateOneLevel();
        return info;
    }

    public EvaluationInfo simulateOneLevel() {
        evaluationOptions.setLevelFile("sample_1.json");
        EvaluationInfo info = this.simulator.simulateOneLevel();
        return info;
    }

    @Override
    public void start() {
        this.launchMario();
    }

    @Override
    public void initBuffers() {

    }
}
