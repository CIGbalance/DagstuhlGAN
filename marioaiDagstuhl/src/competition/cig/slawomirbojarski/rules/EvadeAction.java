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
 * Represents the objective of evading enemies.  
 * Depending on where the general direction
 * of the enemies Mario would either find high
 * ground or seek shelter under something.
 * 
 * @author Slawomir Bojarski
 */
public class EvadeAction extends Action {

	/**
	 * Constructor
	 */
	public EvadeAction() {
		weightMap = new TreeMap<Integer, Integer>();
		
		// weights for enemies
		weightMap.put(Constants.ENEMY_GOOMBA, 1000);
		weightMap.put(Constants.ENEMY_REDKOOPA, 1000);
		weightMap.put(Constants.ENEMY_GREENKOOPA, 1000);
		weightMap.put(Constants.ENEMY_SPIKY, 1000);
		weightMap.put(Constants.ENEMY_WINGED_GOOMBA, 1000);
		weightMap.put(Constants.ENEMY_WINGED_REDKOOPA, 1000);
		weightMap.put(Constants.ENEMY_WINGED_GREENKOOPA, 1000);
		weightMap.put(Constants.ENEMY_WINGED_SPIKY, 1000);
		weightMap.put(Constants.ENEMY_FLOWER, 1000);
		weightMap.put(Constants.ENEMY_BULLETBILL, 1000);
		
		// weights for scene objects
		weightMap.put(Constants.BORDER_FULL, 1000);
		weightMap.put(Constants.BORDER_HALF, 500);
		weightMap.put(Constants.ANGRYFLOWERPOT_OR_CANON, 1000);
		weightMap.put(Constants.BRICK_REGULAR, 500);
		weightMap.put(Constants.BRICK_QUESTION, 500);
		
		// weights for misc items
		weightMap.put(Constants.JUMP, 5);
		weightMap.put(Constants.PIT, 250);
	}
	
	/**
	 * Returns the position of the goal.
	 */
	protected Position getGoal() {
		Position goal = new Position(Constants.MARIO_POS_X, Constants.MARIO_POS_Y);
		int deltaX = 0;
		int deltaY = 0;
		
		// check each enemy position
		// and aggregate the differences
		// between the enemy positions
		// and Mario's position
		for (int i = 0; i < absEnv.enemiesPos.length; i++) {
			int mod = i % 3; // get position within a triple (enemy type, xPos, yPos)
			
			// x position
			if (mod == 1)
				deltaX += absEnv.enemiesPos[i] - absEnv.marioPos[0];
			// y position
			else if (mod == 2)
				deltaY += absEnv.enemiesPos[i] - absEnv.marioPos[1];
		}
		
		// more threats to the right?
		if (deltaX > 0) {
			// more threats above?
			if (deltaY < 0) {
				goal = findCover(false);
			}
			// more threats at same row?
			else if (deltaY == 0) {				
				goal = findHighGround(false);
			}
			// more threats below
			else {
				// if already safely on high ground,
				// stay put, otherwise find higher ground
				if (! absEnv.canStandOn(Constants.MARIO_POS_X, Constants.MARIO_POS_Y + 1) &&
						! absEnv.isEnemyCloseUpperLeft(Constants.CLOSE_ENEMY) && 
						! absEnv.isEnemyCloseUpperRight(Constants.CLOSE_ENEMY))
					goal = findHighGround(false);
			}
		}
		// more threats at same column or to the left
		else {
			// more threats above?
			if (deltaY < 0)
				goal = findCover(true);
			// more threats at same row?
			else if (deltaY == 0)
				goal = findHighGround(true);
			// more threat below
			else {
				// if already safely on high ground,
				// stay put, otherwise find higher ground
				if (! absEnv.canStandOn(Constants.MARIO_POS_X, Constants.MARIO_POS_Y + 1) &&
						! absEnv.isEnemyCloseUpperLeft(Constants.CLOSE_ENEMY) && 
						! absEnv.isEnemyCloseUpperRight(Constants.CLOSE_ENEMY))
					goal = findHighGround(false);
			}
		}
		
		return goal;
	}
	
	/* ***********************
	 * Private Helper Methods
	 * ***********************/
	
	/**
	 * Returns the position of higher 
	 * ground in the given direction.
	 */
	private Position findHighGround( boolean east ) {
		int x = Constants.MARIO_POS_X;
		int y = Constants.MARIO_POS_Y;
		int xLimit = Constants.MARIO_POS_X;
		int yLimit = 0;
		boolean foundHighGround = false;
		
		// set search boundaries
		if (east)
			xLimit = Environment.HalfObsWidth * 2;
		else
			xLimit = -1;
		
		// find higher ground
		for (int x_pos = Constants.MARIO_POS_X; (east ? x_pos < xLimit : x_pos > xLimit) && !foundHighGround;) {
			for (int y_pos = Constants.MARIO_POS_Y; y_pos > yLimit && !foundHighGround; y_pos--) {				
				// find something that we can stand on
				if (absEnv.levelscene[y_pos - 1][x_pos] == 0 && absEnv.canStandOn(x_pos, y_pos)) {
					x = x_pos;
					y = y_pos - 1;
					foundHighGround = true;
				}
			}
			
			// increment position
			if (east)
				x_pos++;
			else
				x_pos--;
		}
		
		return new Position(x, y);
	}
	
	/**
	 * Returns the position of  
	 * cover in the given direction.
	 */
	private Position findCover( boolean east ) {
		int x = Constants.MARIO_POS_X;
		int y = Constants.MARIO_POS_Y;
		int xLimit = Constants.MARIO_POS_X;
		int yLimit = (Environment.HalfObsHeight * 2) -1;
		boolean foundCover = false;
		
		// set search boundaries
		if (east)
			xLimit = Environment.HalfObsWidth * 2;
		else
			xLimit = -1;
		
		// find cover
		for (int x_pos = Constants.MARIO_POS_X; (east ? x_pos < xLimit : x_pos > xLimit) && !foundCover;) {
			for (int y_pos = Constants.MARIO_POS_Y; y_pos < yLimit && !foundCover; y_pos++) {				
				// find something that we can hide under
				// and set the goal on top of the ground
				// at the same column position
				if (absEnv.canHideUnder(x_pos, y_pos)) {
					int newY = (Environment.HalfObsHeight * 2) -1; // most likely below ground
					
					// goal can't be below ground
					while (newY > 0 && absEnv.isBelowGround(x, newY))
						newY--;
					// goal can't be in the ground
					while (newY > 0 && absEnv.levelscene[newY][x] == Constants.BORDER_FULL || absEnv.levelscene[newY][x] == Constants.ANGRYFLOWERPOT_OR_CANON)
						newY--;
					
					x = x_pos;
					y = newY;
					foundCover = true;
				}
			}
			
			// increment position
			if (east)
				x_pos++;
			else
				x_pos--;
		}
		
		// couldn't find cover?
		if (x == Constants.MARIO_POS_X && y == Constants.MARIO_POS_Y) {
			int newY = (Environment.HalfObsHeight * 2) -1; // most likely below ground
			
			// go right
			if (east)
				x += 1;
			// go left
			else
				x -= 1;
			
			// goal can't be below ground
			while (newY > 0 && absEnv.isBelowGround(x, newY))
				newY--;
			// goal can't be in the ground
			while (newY > 0 && (absEnv.levelscene[newY][x] == Constants.BORDER_FULL || absEnv.levelscene[newY][x] == Constants.ANGRYFLOWERPOT_OR_CANON))
				newY--;
			
			y = newY;
		}			
		
		return new Position(x, y);
	}
}