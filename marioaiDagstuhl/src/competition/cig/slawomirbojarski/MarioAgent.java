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
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.idsia.ai.agents.LearningAgent;
import ch.idsia.mario.environments.Environment;

import competition.cig.slawomirbojarski.rules.Action;
import competition.cig.slawomirbojarski.rules.Condition;
import competition.cig.slawomirbojarski.rules.MarioRule;
import competition.cig.slawomirbojarski.rules.MarioRule.RuleEval;

/**
 * Agent for Mario Intelligent 2.0 that uses a simple rule-based system.
 * Each rule has a set of conditions and a corresponding action.  Most conditions
 * can have values of true, false, or don't care (wildcard).  A few conditions have
 * more possible values (e.g. Mario's size).  Whenever the agent is asked to 
 * perform an action, the agent looks at the environment to determine its current 
 * conditions and tries to match those conditions to a rule.  The first matching 
 * rule determines the agent's action.  If a matching rule is not found, then the 
 * agent will perform the previous action.
 * 
 * @author Slawomir Bojarski
 */
public class MarioAgent implements Agent {		
	/** static variables */
	public static String filename = "out.rules";
	public static MarioRule[] rules;
	
	/** instance variables */
	private Condition[] conditions;
	private int prevAction;
        String name = "REALM";

	/**
	 * Constructor.
	 */
	public MarioAgent() {	
		// if rules are empty, grab rules from file
		if (rules == null)
			loadRulesFromFile(filename);
		
		// if rules are still empty, add hand-coded rules
		if (rules == null)
			loadRulesFromFile("hand-coded.rules");
		this.reset();
	}
	

	/**
	 * Resets instance variables used by the agent.
	 */
	public void reset() {
            conditions = Condition.allConditions();
            prevAction = Action.PROGRESS;
    }
	
	/**
	 * Load the given rules into the agent.
	 * 
	 * @param newRules
	 * 		the rules the agent should use
	 */
	public static void loadRules( MarioRule[] newRules ) {
		rules = newRules;
	}
	
	/**
	 * Inform the agent that a new episode is coming, switch to a different rule set.
	 * <p>
	 * <b>Note:</b> Used in Learning Track of CIG 2010 competition
	 */
	public void newEpisode() {
		SharedResource sr = new SharedResource();
		
		// initial setup for learning
		if (!SharedResource.learningMode) {			
			// setup shared resource
			SharedResource.learningMode = true;
			
			// start ECJ in a separate thread
			new Thread(new ThreadedECJ()).start();
		}
		
		rules = sr.consumeRules();
	}
	
	/**
	 * Receive reward after a learning evaluation.
	 * <p>
	 * <b>Note:</b> Used in Learning Track of CIG 2010 competition
	 */
	public void giveReward(float reward) {		
		SharedResource sr = new SharedResource();
		sr.produceFitness(reward);
	}

	/**
	 * Pick best set of rules after all trials are done.
	 * <p>
	 * <b>Note:</b> Used in Learning Track of CIG 2010 competition
	 */
	public void learn() {	
		if (SharedResource.bestRules != null)
			rules = SharedResource.bestRules;
	}

	/**
	 * Observes the given environment to determine the agent's current conditions, 
	 * which are compared against the conditions of each rule in the population.
	 * <p>
	 * The action corresponding to the first matching rule is returned as the action 
	 * to execute.  If no matching rule is found, then the default action is used.
     *
     * @return
     * 		list representing which buttons are being pressed
	 */
    public boolean[] getAction(Environment observation) {
    	long startTime = System.currentTimeMillis(); // start timer    	
    	AbstractEnvironment absEnv = new AbstractEnvironment(prevAction, conditions, observation.getLevelSceneObservation(),
                observation.getEnemiesObservation(), observation.getMarioFloatPos(), observation.getEnemiesFloatPos());
    	RuleEval bestEval = new RuleEval(-1, -1); // set to a sentinel value
    	Action ruleAction = Action.getActionType(Action.PROGRESS); // set default action
    	
    	// if at beginning of level and there is a pit below,
    	// return empty action to until ground below comes into view
    	if (absEnv.marioPos[0] < 128 && absEnv.isPitBelow(Constants.MARIO_POS_X, Constants.MARIO_POS_Y))
    		return new boolean[Constants.NUM_BUTTONS];
    	    	    	
    	// get agent's current conditions
    	conditions[Condition.PREV_ACTION].value = this.prevAction;
    	conditions[Condition.MARIO_SIZE].value = observation.getMarioMode();
    	conditions[Condition.IS_ENEMY_CLOSE_UPPER_LEFT].value = absEnv.isEnemyCloseUpperLeft(Constants.CLOSE_ENEMY) ? Condition.TRUE : Condition.FALSE;
    	conditions[Condition.IS_ENEMY_CLOSE_UPPER_RIGHT].value = absEnv.isEnemyCloseUpperRight(Constants.CLOSE_ENEMY) ? Condition.TRUE : Condition.FALSE;
    	conditions[Condition.IS_ENEMY_CLOSE_LOWER_LEFT].value = absEnv.isEnemyCloseLowerLeft(Constants.CLOSE_ENEMY) ? Condition.TRUE : Condition.FALSE;
    	conditions[Condition.IS_ENEMY_CLOSE_LOWER_RIGHT].value = absEnv.isEnemyCloseLowerRight(Constants.CLOSE_ENEMY) ? Condition.TRUE : Condition.FALSE;
    	conditions[Condition.BRICKS_PRESENT].value = absEnv.bricksPresent(Constants.CLOSE_ITEM) ? Condition.TRUE : Condition.FALSE;
    	conditions[Condition.POWERUPS_PRESENT].value = absEnv.powerUpItemsPresent(Constants.CLOSE_ITEM) ? Condition.TRUE : Condition.FALSE;
    	conditions[Condition.DEAD_END].value = absEnv.deadEnd() ? Condition.TRUE : Condition.FALSE;
    	conditions[Condition.TUNNEL].value = absEnv.tunnel() ? Condition.TRUE : Condition.FALSE;
    	conditions[Condition.MAY_MARIO_JUMP].value = observation.mayMarioJump() ? Condition.TRUE : Condition.FALSE;
    	conditions[Condition.MAY_MARIO_SHOOT].value = observation.canShoot() ? Condition.TRUE : Condition.FALSE;
    	conditions[Condition.IS_MARIO_ON_GROUND].value = observation.canShoot() ? Condition.TRUE : Condition.FALSE;
    	conditions[Condition.IS_MARIO_CARRYING].value = observation.isMarioCarrying() ? Condition.TRUE : Condition.FALSE;
    	conditions[Condition.IS_OBSTACLE_AHEAD].value = absEnv.isObstacleAhead(Constants.CLOSE_OBSTACLE) ? Condition.TRUE : Condition.FALSE;
    	conditions[Condition.IS_OBSTACLE_BEHIND].value = absEnv.isObstacleBehind(Constants.CLOSE_OBSTACLE) ? Condition.TRUE : Condition.FALSE;
    	conditions[Condition.IS_PIT_AHEAD].value = absEnv.isPitAhead(Constants.CLOSE_OBSTACLE) ? Condition.TRUE : Condition.FALSE;
    	conditions[Condition.IS_PIT_BEHIND].value = absEnv.isPitBehind(Constants.CLOSE_OBSTACLE) ? Condition.TRUE : Condition.FALSE;
    	conditions[Condition.IS_PIT_BELOW].value = absEnv.isPitBelow(Constants.MARIO_POS_X, Constants.MARIO_POS_Y) ? Condition.TRUE : Condition.FALSE;
    	    	
    	// find the best matching rule in the population
    	for (int i = 0; rules != null && i < rules.length && rules[i] != null; i++) {
    		RuleEval eval = rules[i].matchConditions(conditions);
    		
    		// do we have a matching rule with the most exact matches?
    		if (eval.generalMatches == rules[i].conditions.length && eval.exactMatches > bestEval.exactMatches) {
    			bestEval = eval;
    			ruleAction = rules[i].action; // seems to be best
    		}  		
    	}
    	    	
    	boolean[] action = ruleAction.getAction(absEnv);
    	if (Constants.VERBOSITY >= 2) {
    		System.out.println("\tTotal time to get action: " + (System.currentTimeMillis() - startTime) + "ms");
    		System.out.println("\tAction that determined path: " + Action.getActionType(Action.indexOf(ruleAction)).getClass().getSimpleName());
    	}
    	
    	// remember last action
    	prevAction = Action.indexOf(ruleAction);
    	
    	return action;
    }
    
    /**
     * Print current environment conditions.
     */
	public void printConditions() {
		String result = "[";
		
		// append condition values separated by commas
		for (int i = 0; i < conditions.length; i++) {
			result += conditions[i];
			
			// append comma if not last
			if (i != conditions.length - 1)
				result += ",";
		}
				
		System.out.println(result += "]");
	}
    
	/**
	 * Load rules from the given file.
	 * 
	 * @param file
	 * 		file to load rules from
	 */
    private void loadRulesFromFile( String file ) {
    	List<MarioRule> fileRules = new ArrayList<MarioRule>();
    	MarioRule newRule;
		BufferedReader reader;
		String line;
    	
    	try {
			reader = new BufferedReader(new FileReader(file));
			
			// grab rules from file
			while ((line = reader.readLine()) != null) {
				// skip line if comment or empty line
				if (line.startsWith("#") || line.trim().isEmpty())
					continue;
				
				newRule = new MarioRule();
				newRule.readRuleFromString(line, null);
				fileRules.add(newRule);
			}
				
			// make room for the rules
			rules = new MarioRule[fileRules.size()];
			
			// add the rules
			fileRules.toArray(rules);
			
			// close stream
			reader.close();
		} catch (FileNotFoundException e) {
			// do nothing
		} catch (IOException e) {
			e.printStackTrace();
		}
    }


    @Override
    public AGENT_TYPE getType() {
        return AGENT_TYPE.AI;
    }

    @Override
    public String getName() {
        return(this.name);
    }

    @Override
    public void setName(String name) {
        this.name=name;
    }


}