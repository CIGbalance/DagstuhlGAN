package cmatest.marioobjectives;

import competition.cig.robinbaumgarten.AStarAgent;

/**
 * Launches Robin Baumgarten's A* agent in Mario level and rates the level based on the agent's progress
 * @author schrum2
 *
 */
public class AStarAgentProgressMarioObjective extends AgentProgressMarioObjective {

	public AStarAgentProgressMarioObjective() {
		super(new AStarAgent());
	}

}
