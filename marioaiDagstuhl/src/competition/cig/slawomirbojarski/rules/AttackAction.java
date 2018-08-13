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

import competition.cig.slawomirbojarski.Constants;
import competition.cig.slawomirbojarski.simulator.Position;

/** 
 * Represents the objective of taking the enemy out.  
 * There are several ways Mario can dispatch enemies.  
 * For instance, if the enemy is not of the spiky variety, 
 * he could jump on top of the enemy or, if Mario has 
 * fire power, he can shoot fireballs.  The last option 
 * available to Mario is to either throw or kick the shell 
 * of a Koopa enemy.  These shells become available when 
 * Mario jumps on top of a wingless Koopa.  When thrown, 
 * shells slide along the ground and take out ANY enemy 
 * that they come in contact with.
 * 
 * @author Slawomir Bojarski
 */
public class AttackAction extends Action {

	/**
	 * Constructor
	 */
	public AttackAction() {
		weightMap = new TreeMap<Integer, Integer>();
		
		// weights for enemies
		weightMap.put(Constants.ENEMY_GOOMBA, 5);
		weightMap.put(Constants.ENEMY_REDKOOPA, 5);
		weightMap.put(Constants.ENEMY_GREENKOOPA, 5);
		weightMap.put(Constants.ENEMY_SPIKY, 5);
		weightMap.put(Constants.ENEMY_WINGED_GOOMBA, 5);
		weightMap.put(Constants.ENEMY_WINGED_REDKOOPA, 5);
		weightMap.put(Constants.ENEMY_WINGED_GREENKOOPA, 5);
		weightMap.put(Constants.ENEMY_WINGED_SPIKY, 5);
		weightMap.put(Constants.ENEMY_FLOWER, 5);
		weightMap.put(Constants.ENEMY_BULLETBILL, 5);
		
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
		Position start = new Position(Constants.MARIO_POS_X, Constants.MARIO_POS_Y);
		Position enemyPos = new Position(Constants.MARIO_POS_X, Constants.MARIO_POS_Y);
		Position shellPos = new Position(Constants.MARIO_POS_X, Constants.MARIO_POS_Y);
		Position goal = new Position(Constants.MARIO_POS_X, Constants.MARIO_POS_Y);
//		float closestEnemyX = Float.MAX_VALUE;
		boolean foundShell = false;
		byte biggestThreat = Constants.NO_ENEMY; // for now, it's the closest enemy
		
		// find biggest threat
//		for (int i = 0; i < enemiesPos.length; i += 3) {
//			byte enemy = generalizeThreat((byte) enemiesPos[i]);
//			
//			// anything is more threatening than nothing
//			if (biggestThreat == Constants.NO_ENEMY) {
//				biggestThreat = enemy;
//			}
//			// threat greater than type GOOMBA?
//			else if (biggestThreat == Constants.ENEMY_GOOMBA) {
//				// anything not of type GOOMBA is more threatening 
//				if (enemy != Constants.ENEMY_GOOMBA)
//					biggestThreat = enemy;
//			}
//			// threat greater than type BULLETBILL?
//			else if (biggestThreat == Constants.ENEMY_BULLETBILL) {
//				// only types FLOWER and SPIKY are more threatening 
//				if (enemy == Constants.ENEMY_FLOWER || enemy == Constants.ENEMY_SPIKY)
//					biggestThreat = enemy;
//			}
//			// threat greater than type FLOWER?
//			else if (biggestThreat == Constants.ENEMY_FLOWER) {
//				// only types SPIKY are more threatening 
//				if (enemy == Constants.ENEMY_SPIKY)
//					biggestThreat = enemy;
//			}
//			// found the most threatening type, stop searching
//			else {
//				break;
//			}
//		}
		
		// find position of biggest threat (if any)
//		for (int i = 0; i < enemiesPos.length; i += 3) {
//			byte enemy = generalizeThreat((byte) enemiesPos[i]);
//			
//			// closest enemy of same type as biggest threat?
//			if (/* enemy == biggestThreat && */ Math.abs(enemiesPos[i + 1] - marioPos[0]) < Math.abs(closestEnemyX - marioPos[0])) {
//				enemyPos.x = Constants.MARIO_POS_X + ((int) ((enemiesPos[i + 1] - marioPos[0]) / 16.0));
//				enemyPos.y = Constants.MARIO_POS_Y + ((int) ((enemiesPos[i + 2] - marioPos[1]) / 16.0));
//								
//				biggestThreat = enemy;
//			}
//		}
		
		// find position of biggest threat (if any)
		for (int x_pos = Constants.MARIO_POS_X - Constants.CLOSE_ENEMY; x_pos <= Constants.MARIO_POS_X + Constants.CLOSE_ENEMY; x_pos++) {
			for (int y_pos = Constants.MARIO_POS_Y + Constants.CLOSE_ENEMY; y_pos >= Constants.MARIO_POS_Y - Constants.CLOSE_ENEMY; y_pos--) {
				byte enemy = absEnv.enemies[y_pos][x_pos];
				
				// is there an enemy here?
				if (absEnv.isRegularEnemy(x_pos, y_pos) || absEnv.isSpikyEnemy(x_pos, y_pos) ||
						enemy == Constants.ENEMY_BULLETBILL ||
						(enemy == Constants.ENEMY_FLOWER && y_pos > 0 && absEnv.levelscene[y_pos - 1][x_pos] != Constants.ANGRYFLOWERPOT_OR_CANON)) {
					// first enemy found?
					if (enemyPos.x == start.x && enemyPos.y == start.y) {
						biggestThreat = generalizeThreat(enemy);
						enemyPos.x = x_pos;
						enemyPos.y = y_pos;						
					}
					// is enemy closer?
					else if (Math.abs(x_pos - start.x) < Math.abs(enemyPos.x - start.x) && 
							Math.abs(y_pos - start.y) < Math.abs(enemyPos.y - start.y)) {
						biggestThreat = generalizeThreat(enemy);
						enemyPos.x = x_pos;
						enemyPos.y = y_pos;						
					}
				}
			}
		}
		
		// find position of nearby shell (if any)
		for (int x_pos = Constants.MARIO_POS_X - Constants.CLOSE_ITEM; x_pos < Constants.MARIO_POS_X + Constants.CLOSE_ITEM && !foundShell; x_pos++) {
			for (int y_pos = Constants.MARIO_POS_Y + Constants.CLOSE_ITEM; y_pos > Constants.MARIO_POS_Y - Constants.CLOSE_ITEM && !foundShell; y_pos--) {
				byte item = absEnv.enemies[y_pos][x_pos];
				
				// get position of first shell
				if (item == Constants.ITEM_SHELL) {
					shellPos.x = x_pos;
					shellPos.y = y_pos;
					foundShell = true;
				}
			}
		}
		
		// handle biggest threat
		if (enemyPos.x != start.x || enemyPos.y != start.y) {
			// if unattended shell nearby, grab it
			if (shellPos.x != start.x && shellPos.y != start.y && unattendedShell(shellPos)) {
				goal = shellPos;
			}
			// if holding a shell, throw it
			else if (absEnv.conditions[Condition.IS_MARIO_CARRYING].value == Condition.TRUE) {
				goal.y--; // small jump should release the SPEED key
				
				// enemy to the right?
				if (enemyPos.x > start.x)
					goal.x++;
				// enemy to the left?
				else if (enemyPos.x < start.x)
					goal.x--;
				// enemy below
				else
					goal.y++; // no need to jump
			}
			// try shooting
			else if (absEnv.conditions[Condition.MAY_MARIO_SHOOT].value == Condition.TRUE && 
					(biggestThreat == Constants.ENEMY_GOOMBA || 
					biggestThreat == Constants.ENEMY_FLOWER)) {
				// enemy to the right?
				if (enemyPos.x > start.x)
					goal.x++; // step toward enemy to shoot
				// enemy to the left?
				else if (enemyPos.x < start.x)
					goal.x--; // step toward enemy to shoot
			}
			// try jumping on the enemy
			else if ((biggestThreat == Constants.ENEMY_GOOMBA || biggestThreat == Constants.ENEMY_BULLETBILL)) {
				goal = enemyPos;
				goal.y--;
				
			}
			// enable shooting
			else {
				goal.y--; // small jump should release the SPEED key
			}
		}
		// no threats present
		else {
			// if unattended shell nearby, grab it
			if (shellPos.x != start.x && shellPos.y != start.y && unattendedShell(shellPos))
				goal = shellPos;
		}
		
		return goal;
	}
	
	/* ***********************
	 * Private Helper Methods
	 * ***********************/
	
	/**
	 * Generalizes the enemy type.
	 * <p>
	 * Threats from least to greatest:
	 * <li>no enemies present = type NO_ENEMY
	 * <li>enemy can be jumped or shot = type GOOMBA
	 * <li>enemy can be jumped, but not shot = type BULLETBILL
	 * <li>enemy cannot be jumped, only shot = type FLOWER
	 * <li>enemy cannot be jumped or shot = type SPIKY
	 * <p><br>
	 * <b>***Note***</b><br>
	 * Ror simplicity's sake, not caring if enemy has wings.
	 */
	private byte generalizeThreat( byte enemy ) {
		byte threatType = Constants.NO_ENEMY;
		
		switch (enemy) {
		case Constants.ENEMY_GOOMBA:
		case Constants.ENEMY_GREENKOOPA:
		case Constants.ENEMY_REDKOOPA:
		case Constants.ENEMY_WINGED_GOOMBA:
		case Constants.ENEMY_WINGED_GREENKOOPA:
		case Constants.ENEMY_WINGED_REDKOOPA:
			threatType = Constants.ENEMY_GOOMBA;
			break;
		case Constants.ENEMY_BULLETBILL:
			threatType = Constants.ENEMY_BULLETBILL;
			break;
		case Constants.ENEMY_FLOWER:
			threatType = Constants.ENEMY_FLOWER;
			break;
		case Constants.ENEMY_SPIKY:
		case Constants.ENEMY_WINGED_SPIKY:
			threatType = Constants.ENEMY_SPIKY;
			break;
		default: // enemy unknown, use extreme caution
			threatType = Constants.ENEMY_SPIKY;							
		}
		
		return threatType;
	}
	
	/**
	 * Returns whether the shell at the given position
	 * is guarded by any nearby enemies.
	 */
	private boolean unattendedShell( Position shellPos ) {		
		// search area near the shell
		for (int x = shellPos.x - Constants.CLOSE_ITEM; x < shellPos.x + Constants.CLOSE_ENEMY; x++) {
			for (int y = shellPos.y + Constants.CLOSE_ITEM; y > shellPos.y - Constants.CLOSE_ENEMY; y--) {
				int enemy = absEnv.enemies[y][x];
				
				// if enemy present, shell is considered attended
				if (absEnv.isRegularEnemy(x, y) || absEnv.isSpikyEnemy(x, y) || 
						enemy == Constants.ENEMY_BULLETBILL ||
						(enemy == Constants.ENEMY_FLOWER && y > 0 && absEnv.levelscene[y - 1][x] != Constants.ANGRYFLOWERPOT_OR_CANON))
					return false;
			}
		}
		
		return true;
	}
}
