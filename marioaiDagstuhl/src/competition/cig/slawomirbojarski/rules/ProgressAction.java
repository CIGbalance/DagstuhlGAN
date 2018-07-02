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
 * Represents the objective of reaching the end of a level.  
 * This involves Mario moving towards the right side of the
 * screen while maneuvering around obstacles that get in 
 * his way such as pits, pipes, cannon towers, steps, 
 * uneven ground, and enemies.  Maneuvering around enemies 
 * and avoiding contact is important to making progress.
 * 
 * @author Slawomir Bojarski
 */
public class ProgressAction extends Action {
	
	/** static variables */
	
	public static boolean jumpingPit = false;
		
	/**
	 * Constructor
	 */
	public ProgressAction() {
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
		weightMap.put(Constants.BRICK_NEARBY, 500);
		
		// weights for misc items
		weightMap.put(Constants.JUMP, 5);
		weightMap.put(Constants.PIT, 250);
	}

	/**
	 * Returns the position of the goal.
	 */
	protected Position getGoal() {
		Position lowestGround = new Position(Constants.MARIO_POS_X, moveUpToGround(Constants.MARIO_POS_X, (Environment.HalfObsWidth * 2) - 1));
		Position ground = new Position(Constants.MARIO_POS_X, Constants.MARIO_POS_Y);
		Position pitJumpLeadUp = new Position(Constants.MARIO_POS_X, (Environment.HalfObsWidth * 2) - 1);
		Position pitJumpStart = new Position(Constants.MARIO_POS_X, (Environment.HalfObsWidth * 2) - 1);
		int x = Constants.MARIO_POS_X;
		int y = (Environment.HalfObsWidth * 2) - 1; // bottom edge
		
		// find ground directly underneath
		while (ground.y < (Environment.HalfObsHeight * 2)-1 && !absEnv.canStandOn(ground.x, ground.y + 1))
			ground.y++;
		
		// are we done jumping a pit?
		if (absEnv.marioPos[0] < 128 || 
				(absEnv.isPitBehind(1) && 
				!absEnv.isPitBelow(Constants.MARIO_POS_X, Constants.MARIO_POS_Y)))
			jumpingPit = false;
		
		// are we in the middle of a pit jump 
		// or in the beginning of a level?
		if (jumpingPit || absEnv.marioPos[0] < 128) {
			x = (Environment.HalfObsWidth * 2) - 1;
			y = moveUpToGround(x, y);
		}
		// above or on higher physical ground (not bricks)?
		else if (!ground.equals(lowestGround) && 
				ground.y < (Environment.HalfObsHeight * 2)-1 &&
				absEnv.levelscene[ground.y + 1][ground.x] == Constants.BORDER_FULL) {
			x = (Environment.HalfObsWidth * 2) - 1;
			y = moveDownToGround(x, Constants.MARIO_POS_Y);
		}
		// set up the jump
		else {
			// find start of pit jump by
			// first moving back if above a pit
			// then by moving forward to edge of pit
			while (pitJumpStart.x > 0 && absEnv.isPitBelow(pitJumpStart.x, Constants.MARIO_POS_Y))
				pitJumpStart.x--;
			while (pitJumpStart.x < ((Environment.HalfObsWidth * 2) - 1) && !absEnv.isPitBelow(pitJumpStart.x + 1, Constants.MARIO_POS_Y))
				pitJumpStart.x++;
						
			pitJumpStart.y = moveUpToGround(pitJumpStart.x, pitJumpStart.y);
			
			// set lead up to jump start
			pitJumpLeadUp.x = pitJumpStart.x;
			pitJumpLeadUp.y = pitJumpStart.y;
			
			// find lead up to pit jump
			//
			// ***Note***
			// There are quite a few things going on in this loop.
			// (1) the variable sets a limit for the loop.
			// (2) I check if I can stand at a location and
			// there is nothing blocking me to the left
			// (3) I check for ranges of some variables so that
			// the check above doesn't throw exceptions
			for (int i = 0; 
				i < 3 && pitJumpLeadUp.x > 0 && pitJumpLeadUp.y < ((Environment.HalfObsHeight * 2) - 1) && 
				absEnv.levelscene[pitJumpLeadUp.y][pitJumpLeadUp.x - 1] == 0 && absEnv.canStandOn(pitJumpLeadUp.x - 1, pitJumpLeadUp.y + 1); 
				i++)
				pitJumpLeadUp.x--;
			
			// set goal
			x = pitJumpLeadUp.x;
			y = pitJumpLeadUp.y;
			
			// if Mario is on the same row between the lead up
			// and the jump start (inclusive), goal will shift to the jump start
			if (Constants.MARIO_POS_Y == y && Constants.MARIO_POS_X >= x && Constants.MARIO_POS_X <= pitJumpStart.x)
				x = pitJumpStart.x;
			
			// if on ground at jump start, jump the pit
			if (Constants.MARIO_POS_X == pitJumpStart.x && 
					Constants.MARIO_POS_Y == pitJumpStart.y &&
					absEnv.marioPos[1] % 16 >= 11) {
				x = (Environment.HalfObsWidth * 2) - 1;
				jumpingPit = true;
			}
		}
				
		// if couldn't set proper goal,
		// set y to Mario's level
		if (y == 0)
			y = Constants.MARIO_POS_Y;
				
		return new Position(x, y);
	}
	
	/* ***************
	 * helper methods
	 * ***************/
	
	private int moveUpToGround( int x, int y ) {
		// goal can't be below ground
		while (y > 0 && absEnv.isBelowGround(x, y))
			y--;
		// goal can't be in the ground
		while (y > 0 && (absEnv.levelscene[y][x] == Constants.BORDER_FULL || 
				absEnv.levelscene[y][x] == Constants.ANGRYFLOWERPOT_OR_CANON))
			y--;
		
		return y;
	}
	
	private int moveDownToGround( int x, int y ) {
		// goal can't be in the air
		while (y < ((Environment.HalfObsHeight * 2) -1) && absEnv.levelscene[y + 1][x] != Constants.BORDER_FULL && 
				absEnv.levelscene[y + 1][x] != Constants.ANGRYFLOWERPOT_OR_CANON)
			y++;
		
		return y;
	}
}