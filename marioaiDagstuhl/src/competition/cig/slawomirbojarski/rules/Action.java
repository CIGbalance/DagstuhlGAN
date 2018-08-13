package competition.cig.slawomirbojarski.rules;

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
import java.util.Map;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

import competition.cig.slawomirbojarski.AbstractEnvironment;
import competition.cig.slawomirbojarski.Constants;
import competition.cig.slawomirbojarski.simulator.Position;
import competition.cig.slawomirbojarski.simulator.Simulator;

/** 
 * All actions must extend this class and
 * be added to the template array as well
 * as have a indexing variable defined.
 * 
 * @author Slawomir Bojarski
 */
public abstract class Action {

	/** constants */
	
	// base array of conditions used for cloning
	private static final Action[] TEMPLATE = { 
		new ProgressAction(), 	// 0: PROGRESS
		new AttackAction(),   	// 1: ATTACK
		new EvadeAction(),    	// 2: EVADE
		new PowerUpAction(),  	// 3: POWERUP
		new BackTrackAction(),	// 4: BACKTRACK
		new ClimbAction()		// 5: CLIMB
	}; 
	
	// possible actions - used for array indexing
	public static final int PROGRESS = 0,
							ATTACK = 1, 
							EVADE = 2,
							POWERUP = 3,
							BACKTRACK = 4,
							CLIMB = 5;
	
	/** static variables */
	private boolean wallJumping = false;
	protected Position prevGoal = new Position(Constants.MARIO_POS_X, Constants.MARIO_POS_Y);
		
	/** instance variables */
	public Map<Integer, Integer> weightMap;
	protected AbstractEnvironment absEnv;
	
	/**
	 * Returns what action should be taken based on the abstract environment,
	 * goal set by the specific action instance, and weight map populated by
	 * the specific action instance.
	 */
	public boolean[] getAction( AbstractEnvironment absEnv ) {
		Position start = new Position(Constants.MARIO_POS_X, Constants.MARIO_POS_Y);
		Position goal = new Position(Constants.MARIO_POS_X, Constants.MARIO_POS_Y);
		boolean[] action = new boolean[Constants.NUM_BUTTONS];
			
		// store some things so that other operations have access to them
		this.absEnv = absEnv;
		
		// if Mario isn't over a pit,
		// done with wall-jumping
		if (absEnv.conditions[Condition.IS_PIT_BELOW].value == Condition.FALSE)
			wallJumping = false;
		// if wall-jumping,
		// use previous goal
		if (wallJumping) {
			goal = prevGoal;
			goal.y = (Environment.HalfObsHeight * 2) -1;
				
			// goal can't be below ground
			while (goal.y > 0 && absEnv.isBelowGround(goal.x, goal.y))
				goal.y--;
			// goal can't be in the ground
			while (goal.y > 0 && (absEnv.levelscene[goal.y][goal.x] == Constants.BORDER_FULL || absEnv.levelscene[goal.y][goal.x] == Constants.ANGRYFLOWERPOT_OR_CANON))
				goal.y--;
		}
		
		// if Mario is in a pit, handle the situation regardless of selected action
		if (absEnv.conditions[Condition.IS_PIT_BELOW].value == Condition.TRUE && 
				(absEnv.isObstacleAhead(1) || absEnv.isObstacleBehind(1))) {
			int x = Constants.MARIO_POS_X;
			int y = Constants.MARIO_POS_Y;
			
			// next to both sides of pit? (tiny pit)
			if (absEnv.isObstacleAhead(1) && absEnv.isObstacleBehind(1)) {
				// check if we can wall-jump
				if (absEnv.conditions[Condition.MAY_MARIO_JUMP].value == Condition.TRUE) {
					// goal is to the right
					if (goal.x > Constants.MARIO_POS_X) {
						// shift goal to left side of pit
						while (x > 0 && ! absEnv.canStandOn(x, y))
							x--;
						
						// shift y position to top of pit
						while (y > 0 && absEnv.canStandOn(x, y))
							y--;
					
						wallJumping = true;
					}
					// goal is to the left
					else {
						// shift goal right side of pit
						while (x < ((Environment.HalfObsWidth * 2) - 1) && ! absEnv.canStandOn(x, y))
							x++;
						
						// shift y position to top of pit
						while (y > 0 && absEnv.canStandOn(x, y))
							y--;
						
						wallJumping = true;
					}
				}
				// need to release the jump key (required for wall-jump)
				else {					
					// goal is to the right
					if (goal.x > Constants.MARIO_POS_X) {
						action[Mario.KEY_RIGHT] = true; // stick close to the wall
					}
					// goal is to the left
					else {
						action[Mario.KEY_LEFT] = true; // stick close to the wall
					}
					
					action[Mario.KEY_SPEED] = true; // required for wall-jump
					
					// if almost out of pit, need extra boost
					if (Constants.MARIO_POS_Y == (goal.y + 1))
						action[Mario.KEY_JUMP] = true;
						
					return action;
				}
			}
			// next to right side of pit?
			else if (absEnv.isObstacleAhead(1)) {
				// check if we can wall-jump
				if (absEnv.conditions[Condition.MAY_MARIO_JUMP].value == Condition.TRUE) {
					// shift goal to left side of pit
					while (x > 0 && ! absEnv.canStandOn(x, y))
						x--;
					
					// shift y position to top of pit
					while (y > 0 && absEnv.canStandOn(x, y))
						y--;
				
					wallJumping = true;
				}
				// need to release the jump key (required for wall-jump)
				else {
					action[Mario.KEY_RIGHT] = true; // stick close to the wall
					action[Mario.KEY_SPEED] = true; // required for wall-jump
					
					return action;
				}
			}
			// next to the left side of pit?
			else if (absEnv.isObstacleBehind(1)) {
				// check if we can wall-jump
				if (absEnv.conditions[Condition.MAY_MARIO_JUMP].value == Condition.TRUE) {
					// shift goal right side of pit
					while (x < ((Environment.HalfObsWidth * 2) - 1) && ! absEnv.canStandOn(x, y))
						x++;
					
					// shift y position to top of pit
					while (y > 0 && absEnv.canStandOn(x, y))
						y--;
					
					wallJumping = true;
				}
				// need to release the jump key (required for wall-jump)
				else {
					action[Mario.KEY_LEFT] = true;  // stick close to the wall
					action[Mario.KEY_SPEED] = true; // required for wall-jump
					
					return action;
				}
			}			
			
			goal = new Position(x, y);
		}
				
		// if goal didn't change,
		// get goal associated with action
		if (goal.x == start.x && goal.y == start.y)
			goal = getGoal();
				
		// create a new simulator
		Simulator s = new Simulator(absEnv.conditions, goal, weightMap, absEnv);	
				
		// remember previous goal
		prevGoal = goal;
			
		// get action from simulator
		action = s.simulate();
		
		return action;
	}
	
	/* *****************
	 * Abstract methods
	 * *****************/
	
	/**
	 * Returns the position of the goal.
	 */
	protected abstract Position getGoal();
		
	/* **********************
	 * Static helper methods
	 * **********************/
	
	/**
	 * Returns the number of available actions.
	 */
	public static int numActions() {
		return TEMPLATE.length;
	}
	
	/**
	 * Indexed array access into TEMPLATE array. 
	 */
	public static Action getActionType( int index ) {
		return TEMPLATE[index];
	}
	
	/**
	 * Returns index of same type of action.
	 */
	public static int indexOf( Action act ) {
		for (int i = 0; i < TEMPLATE.length; i++)
			if (TEMPLATE[i].getClass().isInstance(act))
				return i;
		
		return 0;
	}
}