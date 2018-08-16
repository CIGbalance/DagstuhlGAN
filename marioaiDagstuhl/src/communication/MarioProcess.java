package communication;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.Agent.AGENT_TYPE;
import ch.idsia.ai.agents.AgentsPool;
import ch.idsia.ai.agents.human.HumanKeyboardAgent;
import ch.idsia.mario.engine.GlobalOptions;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.simulation.BasicSimulator;
import ch.idsia.mario.simulation.Simulation;
import ch.idsia.mario.simulation.SimulationOptions;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.EvaluationOptions;
import ch.idsia.tools.ToolsConfigurator;
import competition.icegic.robin.AStarAgent;
import competition.cig.slawomirbojarski.MarioAgent;

public class MarioProcess extends Comm {
    private EvaluationOptions evaluationOptions;
    private Simulation simulator;
    private Agent defaultAgent = new AStarAgent();

    public MarioProcess() {
        super();
        this.threadName = "MarioProcess";
    }
    
    public MarioProcess(Agent defaultAgent) {
        super();
        this.threadName = "MarioProcess";
        this.defaultAgent= defaultAgent;
    }

    /**
     * Default mario launcher does not have any command line parameters
     */
    public void launchMario() {
    	String[] options = new String[] {""};
    	launchMario(options, defaultAgent);
    }
 
    /**
     * This version of launching Mario allows for several parameters
     * @param options General command line options (currently not really used)
     * @param humanPlayer Whether a human is playing rather than a bot
     */
    public void launchMario(String[] options, Agent agent) {
        this.evaluationOptions = new CmdLineOptions(options);  // if none options mentioned, all defaults are used.
        // set agents
        AgentsPool.setCurrentAgent(agent);
        // Short time for evolution, but more for human
        //if(agent.getType()==AGENT_TYPE.AI) evaluationOptions.setTimeLimit(20);
        // TODO: Make these configurable from commandline?
       // evaluationOptions.setMaxFPS(agent.getType()==AGENT_TYPE.AI); // Slow for human players, fast otherwise
        evaluationOptions.setVisualization(true); // Set true to watch evaluations
        // Create Mario Component
        ToolsConfigurator.CreateMarioComponentFrame(evaluationOptions);
        evaluationOptions.setAgent(AgentsPool.getCurrentAgent());
        System.out.println(evaluationOptions.getAgent().getClass().getName());
        // set simulator
        this.simulator = new BasicSimulator(evaluationOptions.getSimulationOptionsCopy());
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
