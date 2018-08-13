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
 * Represents the objective of getting power-ups.
 * Mario chases after any power-up within range
 * and hits bricks to force power-ups out.
 * 
 * @author Slawomir Bojarski
 */
public class PowerUpAction extends Action {
	
	/**
	 * Constructor
	 */
	public PowerUpAction() {
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
		int x = Constants.MARIO_POS_X;
		int y = Constants.MARIO_POS_Y;
		int itemX = Constants.MARIO_POS_X;
		int itemY = Constants.MARIO_POS_Y;
		int brickX = Constants.MARIO_POS_X;
		int brickY = Constants.MARIO_POS_Y;
		byte item = Constants.NO_ENEMY;
		boolean foundPowerUp = false;
		boolean foundQuestionBrick = false; // question bricks have priority (items always contained inside)
		
		// if power-ups are present, grab them
		if (absEnv.conditions[Condition.POWERUPS_PRESENT].value == Condition.TRUE) {
			// get position of power-up
			for (int x_pos = 0; x_pos < Environment.HalfObsWidth * 2 && !foundPowerUp; x_pos++) {
				for (int y_pos = (Environment.HalfObsWidth * 2) -1; y_pos > -1 && !foundPowerUp; y_pos--) {
					item = absEnv.enemies[y_pos][x_pos];
					
					// get position of first power-up found
					if (item == Constants.ITEM_MUSHROOM || item == Constants.ITEM_FIREFLOWER) {
						itemX = x_pos;
						itemY = y_pos;		
						foundPowerUp = true;
					}
				}
			}
			
			// set goal at power-up
			x = itemX;
			y = itemY;
			
			// if the power-up is a mushroom (power-up that moves),
			// shift goal toward ground underneath the power-up
			if (item == Constants.ITEM_MUSHROOM) {
				y = (Environment.HalfObsHeight * 2) -1; // most likely below ground
				
				// goal can't be below ground
				while (y > 0 && absEnv.isBelowGround(x, y))
					y--;
				// goal can't be in the ground
				while (y > 0 && absEnv.levelscene[y][x] == Constants.BORDER_FULL || absEnv.levelscene[y][x] == Constants.ANGRYFLOWERPOT_OR_CANON)
					y--;
				
				//while (y < ((Environment.HalfObsHeight * 2) -2) && !absEnv.canStandOn(x, y + 1))
				//	y++;
			}
			// if the power-up is a fire-flower and not on the
			// same level as Mario, shift goal onto edge of ledge
			else if (item == Constants.ITEM_FIREFLOWER && Constants.MARIO_POS_Y != itemY) {				
				while (x > 0 && absEnv.canStandOn(x, y + 1))
					x--;
				
				// prevent Mario from hitting his head
				if (Constants.MARIO_POS_X == x) {
					if (absEnv.marioPos[0] % 16 >= 12)
						x--;
					else if (absEnv.marioPos[0] % 16 <= 4)
						x++;
				}
			}
		}
		// hit a brick to force them out
		else {
			// get position of brick
			for (int x_pos = Constants.MARIO_POS_X - Constants.CLOSE_ITEM; 
				x_pos <= (Constants.MARIO_POS_X + Constants.CLOSE_ITEM) && !foundQuestionBrick; 
				x_pos++) {
				for (int y_pos = Constants.MARIO_POS_Y + Constants.CLOSE_ITEM; 
					y_pos >= (Constants.MARIO_POS_Y - Constants.CLOSE_ITEM) && !foundQuestionBrick; 
					y_pos--) {
					byte sceneobject = absEnv.levelscene[y_pos][x_pos];
					
					// get position of the question brick
					if (sceneobject == Constants.BRICK_QUESTION) {
						brickX = x_pos;
						brickY = y_pos;		
						foundQuestionBrick = true;
					}
					// get position of the first regular brick found
					else if (sceneobject == Constants.BRICK_REGULAR &&
							brickX == Constants.MARIO_POS_X && brickY == Constants.MARIO_POS_Y) {
						brickX = x_pos;
						brickY = y_pos;
					}					
				}
			}
					
			// set goal at brick
			x = brickX;
			y = brickY;
			
			// shift goal toward ground underneath the brick
			while (y < ((Environment.HalfObsHeight * 2) -1) && !absEnv.canStandOn(x, y + 1))
				y++;
			
			// if Mario is in between the ground underneath the brick 
			// and the brick itself (inclusive), goal will shift to the brick
			if (Constants.MARIO_POS_X == x && Constants.MARIO_POS_Y <= y && Constants.MARIO_POS_Y >= brickY)
				y = brickY;
		}
		
		return new Position(x, y);
	}
}