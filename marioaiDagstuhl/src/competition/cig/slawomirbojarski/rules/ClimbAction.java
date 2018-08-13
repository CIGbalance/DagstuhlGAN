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
import java.util.TreeMap;

import ch.idsia.mario.environments.Environment;

import competition.cig.slawomirbojarski.Constants;
import competition.cig.slawomirbojarski.simulator.Position;

/** 
 * Represents the objective of climbing 'stairs'.  
 * 
 * @author Slawomir Bojarski
 */
public class ClimbAction extends Action {
			
	/**
	 * Constructor
	 */
	public ClimbAction() {
		weightMap = new TreeMap<Integer, Integer>();
		
		// weights for enemies
		weightMap.put(Constants.ENEMY_GOOMBA, 50);
		weightMap.put(Constants.ENEMY_REDKOOPA, 50);
		weightMap.put(Constants.ENEMY_GREENKOOPA, 50);
		weightMap.put(Constants.ENEMY_SPIKY, 50);
		weightMap.put(Constants.ENEMY_WINGED_GOOMBA, 50);
		weightMap.put(Constants.ENEMY_WINGED_REDKOOPA, 50);
		weightMap.put(Constants.ENEMY_WINGED_GREENKOOPA, 50);
		weightMap.put(Constants.ENEMY_WINGED_SPIKY, 50);
		weightMap.put(Constants.ENEMY_FLOWER, 50);
		weightMap.put(Constants.ENEMY_BULLETBILL, 50);
		
		// weights for scene objects
		weightMap.put(Constants.BORDER_FULL, 1000);
		weightMap.put(Constants.BORDER_HALF, 500);
		weightMap.put(Constants.ANGRYFLOWERPOT_OR_CANON, 1000);
		weightMap.put(Constants.BRICK_REGULAR, 500);
		weightMap.put(Constants.BRICK_QUESTION, 500);
		weightMap.put(Constants.BRICK_NEARBY, 200);
		
		// weights for misc items
		weightMap.put(Constants.JUMP, 5);
		weightMap.put(Constants.PIT, 250);
	}

	/**
	 * Returns the position of the goal.
	 */
	protected Position getGoal() {
		Position start = new Position(Constants.MARIO_POS_X, Constants.MARIO_POS_Y);
		Position goal = start;
		Position ground = new Position(Constants.MARIO_POS_X, Constants.MARIO_POS_Y);
		Position lowestGround = new Position(Constants.MARIO_POS_X, moveUpToGround(Constants.MARIO_POS_X, (Environment.HalfObsWidth * 2) - 1));
		
		// find ground
		while (ground.y < (Environment.HalfObsHeight * 2) && !absEnv.canStandOn(ground.x, ground.y + 1))
			ground.y++;
		
		// above or on higher physical ground (not bricks)?
		if (!ground.equals(lowestGround) && 
				ground.y < (Environment.HalfObsHeight * 2) &&
				absEnv.levelscene[ground.y + 1][ground.x] == Constants.BORDER_FULL &&
				(absEnv.levelscene[ground.y + 1][ground.x + 1] == Constants.BORDER_FULL || // ground usually has more ground next to it...
					absEnv.levelscene[ground.y + 1][ground.x - 1] == Constants.BORDER_FULL)) // unlike hard empty bricks
			// far right on same level
			return new Position((Environment.HalfObsWidth * 2) - 1, Constants.MARIO_POS_Y);
		
		// find high ground towards the left
		goal = findHighGround(false);
		
		// avoid skipping 'steps'
		if (!goal.equals(start) && absEnv.prevAction == Action.CLIMB &&
				absEnv.conditions[Condition.IS_MARIO_ON_GROUND].value == Condition.FALSE &&
				Math.abs(goal.y - start.y) > 3)
			goal = prevGoal;
		
		// did the goal change?
		if (goal.equals(start))
			// try the other side
			goal = findHighGround(true);
		
		// avoid skipping 'steps'
		if (!goal.equals(start) && absEnv.prevAction == Action.CLIMB &&
				absEnv.conditions[Condition.IS_MARIO_ON_GROUND].value == Condition.FALSE &&
				(Math.abs(goal.y - start.y) > 3 || 
					goal.x > prevGoal.x))
			goal = prevGoal;
		
		// no higher ground?
		if (goal.equals(start)) {
			// try to finish previous jump
			if (Math.abs(ground.y - start.y) >= Constants.JUMP_DISTANCE_SHORT)
				goal = prevGoal;
			// go towards far right on same level
			else
				return new Position((Environment.HalfObsWidth * 2) - 1, Constants.MARIO_POS_Y - 1);
		}
		
		return goal;
	}
	
	/* ***********************
	 * Private Helper Methods
	 * ***********************/
	
	/**
	 * Returns the position of higher 
	 * ground in the given direction.
	 * <p>
	 * <b>Note:</b> Copied from EvadeAction with minor modifications.
	 */
	private Position findHighGround( boolean east ) {
		int x = Constants.MARIO_POS_X;
		int y = Constants.MARIO_POS_Y;
		int xLimit = Constants.MARIO_POS_X;
		int yLimit = 0;
		boolean foundHighGround = false;
		
		// set search boundaries
		if (east)
			xLimit = Constants.MARIO_POS_X + (Environment.HalfObsWidth / 2);
		else
			xLimit = Constants.MARIO_POS_X - (3);
		
		// find higher ground (prefer locations closer to Mario)
		for (int y_pos = Constants.MARIO_POS_Y; y_pos > yLimit && !foundHighGround; y_pos--) {
			for (int x_pos = Constants.MARIO_POS_X + (east ? 0 : -1); (east ? x_pos < xLimit : x_pos > xLimit) && !foundHighGround;) {				
				// find something that we can stand on
				if (absEnv.levelscene[y_pos - 1][x_pos] == 0 && absEnv.canStandOn(x_pos, y_pos)) {
					x = x_pos;
					y = y_pos - 1;
					foundHighGround = true;
				}
				
				// increment position
				if (east)
					x_pos++;
				else
					x_pos--;
			}
		}
		
		return new Position(x, y);
	}
	
	/**
	 * Returns the position of lower 
	 * ground in the given direction.
	 */
//	private Position findLowGround( boolean east ) {
//		int x = Constants.MARIO_POS_X;
//		int y = Constants.MARIO_POS_Y;
//		int xLimit = Constants.MARIO_POS_X;
//		int yLimit = (Environment.HalfObsHeight * 2) -1;
//		boolean foundLowGround = false;
//		
//		// set search boundaries
//		if (east)
//			xLimit = Constants.MARIO_POS_X + (Environment.HalfObsWidth / 2);
//		else
//			xLimit = Constants.MARIO_POS_X - (Environment.HalfObsWidth / 3);
//		
//		// find lower ground (prefer locations further away)
//		for (int x_pos = xLimit; x_pos != Constants.MARIO_POS_X && !foundLowGround;) {
//			for (int y_pos = Constants.MARIO_POS_Y; y_pos < yLimit && !foundLowGround; y_pos++) {				
//				// find something that we can stand on
//				if (absEnv.levelscene[y_pos - 1][x_pos] == 0 && absEnv.canStandOn(x_pos, y_pos)) {
//					x = x_pos;
//					y = y_pos - 1;
//					foundLowGround = true;
//				}
//			}
//			
//			// increment position
//			if (east)
//				x_pos--;
//			else
//				x_pos++;
//		}
//		
//		return new Position(x, y);
//	}
		
	private int moveUpToGround( int x, int y ) {
		// goal can't be below ground
		while (y > 0 && absEnv.isBelowGround(x, y))
			y--;
		// goal can't be in the ground
		while (y > 0 && (absEnv.levelscene[y][x] == Constants.BORDER_FULL || absEnv.levelscene[y][x] == Constants.ANGRYFLOWERPOT_OR_CANON))
			y--;
		
		return y;
	}
}
