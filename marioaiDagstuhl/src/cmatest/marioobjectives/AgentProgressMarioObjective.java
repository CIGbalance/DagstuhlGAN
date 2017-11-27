package cmatest.marioobjectives;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;

/**
 * General class for sending any agent into a GAN generated level and
 * assessing the level in terms of how far the agent gets.
 * @author schrum2
 *
 */
public class AgentProgressMarioObjective extends MarioLevelObjective {

	private Agent agent; // Agent to evaluate in level to assess fitness
	
	public AgentProgressMarioObjective(Agent agent) {
		this.agent = agent;
	}
	
	/**
	 * Level is rated by how well the provided agent performs in the level
	 */
	@Override
	public double valueOf(Level level) {
		EvaluationOptions options = new CmdLineOptions(new String[]{});
		options.setAgent(agent);
		ProgressTask task = new ProgressTask(options);
        options.setLevel(level); // Method added by schrum2
		task.setOptions(options);
		double[] result = task.evaluate(options.getAgent());
		return result[0]; // Contains distance traveled by agent in level
	}

}
