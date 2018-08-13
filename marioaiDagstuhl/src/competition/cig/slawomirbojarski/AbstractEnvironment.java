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
import java.util.Map;

import ch.idsia.mario.environments.Environment;

import competition.cig.slawomirbojarski.rules.Condition;
import competition.cig.slawomirbojarski.simulator.Position;

/** 
 * Provides an abstraction of the Mario environment.
 * 
 * @author Slawomir Bojarski
 */
public class AbstractEnvironment {
		
	/** instance variables */
	
	/* Position (0,0) is in upper-left corner.
	 * 
	 * First coordinate represents the row.
	 * Second coordinate represents the column. */
	public byte[][] levelscene, enemies;
	public float[] marioPos, enemiesPos;
	public Condition[] conditions;
	public int prevAction;

	/**
	 * Constructor
	 */
	public AbstractEnvironment( int prevAction, Condition[] conditions, byte[][] levelscene, byte[][] enemies, float[] marioPos, float[] enemiesPos ) {
		this.prevAction = prevAction;
		this.conditions = conditions;
		this.levelscene = levelscene;
		this.enemies = enemies;
		this.marioPos = marioPos;
		this.enemiesPos = enemiesPos;
	}
	
	/**
	 * Checks if enemies are present in the
	 * upper-left quarter of the environment,
	 * within a given range.
	 */
	public boolean isEnemyCloseUpperLeft( int range ) {
		for ( int y = Constants.MARIO_POS_Y; y >= Constants.MARIO_POS_Y - range && y >= 0; y-- ) {
			for ( int x = Constants.MARIO_POS_X; x >= Constants.MARIO_POS_X - range && x >= 0; x-- ) {
				int enemy = enemies[y][x];
				
				if (isRegularEnemy(x, y) || isSpikyEnemy(x, y) || 
						enemy == Constants.ENEMY_BULLETBILL ||
						(enemy == Constants.ENEMY_FLOWER && y > 0 && levelscene[y - 1][x] != Constants.ANGRYFLOWERPOT_OR_CANON)) // not hidden flower
					return true;
			}
		}
			
		return false;
	}
	
	/**
	 * Checks if enemies are present in the
	 * upper-right quarter of the environment,
	 * within a given range.
	 */
	public boolean isEnemyCloseUpperRight( int range ) {
		for ( int y = Constants.MARIO_POS_Y; y >= Constants.MARIO_POS_Y - range && y >= 0; y-- ) {
			for ( int x = Constants.MARIO_POS_X; x <= Constants.MARIO_POS_X + range && x < (2 * Environment.HalfObsWidth); x++ ) {
				int enemy = enemies[y][x];
				
				if (isRegularEnemy(x, y) || isSpikyEnemy(x, y) || 
						enemy == Constants.ENEMY_BULLETBILL  ||
						(enemy == Constants.ENEMY_FLOWER && y > 0 && levelscene[y - 1][x] != Constants.ANGRYFLOWERPOT_OR_CANON)) // not hidden flower
					return true;
			}
		}
			
		return false;
	}
	
	/**
	 * Checks if enemies are present in the
	 * lower-left quarter of the environment,
	 * within a given range.
	 */
	public boolean isEnemyCloseLowerLeft( int range ) {
		for ( int y = Constants.MARIO_POS_Y; y <= Constants.MARIO_POS_Y + range && y < (2 * Environment.HalfObsHeight); y++ ) {
			for ( int x = Constants.MARIO_POS_X; x >= Constants.MARIO_POS_Y - range && x >= 0; x-- ) {
				int enemy = enemies[y][x];
				
				if (isRegularEnemy(x, y) || isSpikyEnemy(x, y) || 
						enemy == Constants.ENEMY_BULLETBILL  ||
						(enemy == Constants.ENEMY_FLOWER && y > 0 && levelscene[y - 1][x] != Constants.ANGRYFLOWERPOT_OR_CANON)) // not hidden flower
					return true;
			}
		}
			
		return false;
	}
	
	/**
	 * Checks if enemies are present in the
	 * lower-right quarter of the environment,
	 * within a given range.
	 */
	public boolean isEnemyCloseLowerRight( int range ) {		
		for ( int y = Constants.MARIO_POS_Y; y <= Constants.MARIO_POS_Y + range && y < (2 * Environment.HalfObsHeight); y++ ) {
			for ( int x = Constants.MARIO_POS_X; x <= Constants.MARIO_POS_X + range && x < (2 * Environment.HalfObsWidth); x++ ) {
				int enemy = enemies[y][x];
				
				if (isRegularEnemy(x, y) || isSpikyEnemy(x, y) || 
						enemy == Constants.ENEMY_BULLETBILL  ||
						(enemy == Constants.ENEMY_FLOWER && y > 0 && levelscene[y - 1][x] != Constants.ANGRYFLOWERPOT_OR_CANON)) // not hidden flower
					return true;
			}
		}
			
		return false;
	}
	
	/**
	 * Checks if there is an obstacle within
	 * a given range in front of Mario.
	 */
	public boolean isObstacleAhead( int range ) {
		for ( int x = Constants.MARIO_POS_X + 1; x <= Constants.MARIO_POS_X + range; x++ ) {
			int sceneobject = levelscene[Constants.MARIO_POS_Y][x];
			
			if ( sceneobject != 0 && sceneobject != Constants.MARIO )
				return true;
		}			
			
		return false;
	}
	
	/**
	 * Checks if there is an obstacle within
	 * a given range behind of Mario.
	 */
	public boolean isObstacleBehind( int range ) {
		for ( int x = Constants.MARIO_POS_X - 1; x >= Constants.MARIO_POS_X - range; x-- ) {
			int sceneobject = levelscene[Constants.MARIO_POS_Y][x];
			
			if ( sceneobject != 0 && sceneobject != Constants.MARIO )
				return true;
		}			
			
		return false;
	}
	
	/**
	 * Checks if there is a pit within
	 * a given range in front of Mario.
	 */
	public boolean isPitAhead( int range ) {
		
		for (int x = Constants.MARIO_POS_X + 1; x <= Constants.MARIO_POS_X + range; x++)			
			if (isPitBelow(x, Constants.MARIO_POS_Y))
				return true;
			
		return false;
	}
	
	/**
	 * Checks if there is a pit within
	 * a given range behind of Mario.
	 */
	public boolean isPitBehind( int range ) {
		
		for (int x = Constants.MARIO_POS_X - 1; x >= Constants.MARIO_POS_X - range; x--)			
			if (isPitBelow(x, Constants.MARIO_POS_Y))
				return true;
			
		return false;
	}
	
	/**
	 * Checks if there is a pit 
	 * directly below the given location.
	 */
	public boolean isPitBelow( int x, int y ) {
		int x_pos = x;
		int level_element;
		
		for ( int y_pos = y; y_pos < (2 * Environment.HalfObsHeight); y_pos++ ) {
			level_element = levelscene[y_pos][x_pos];
			
			if ( level_element != 0 && level_element != Constants.MARIO )
				return false;
		}			
			
		return true;
	}
	
	/**
	 * Checks if the given location contains 
	 * something that Mario can stand on top of.
	 */
	public boolean canStandOn( int x, int y ) {
		byte sceneobject = levelscene[y][x];
		
		if (sceneobject != Constants.BORDER_FULL &&
				sceneobject != Constants.BORDER_HALF &&
				sceneobject != Constants.ANGRYFLOWERPOT_OR_CANON &&
				sceneobject != Constants.BRICK_REGULAR && 
				sceneobject != Constants.BRICK_QUESTION)
			return false;
		
		return true;
	}
	
	/**
	 * Checks if the given location contains 
	 * something that Mario can hide under.
	 */
	public boolean canHideUnder( int x, int y ) {
		byte sceneobject = levelscene[y][x];
		
		// can only hide under half-borders and bricks
		if (sceneobject != Constants.BORDER_HALF &&
				sceneobject != Constants.BRICK_REGULAR && 
				sceneobject != Constants.BRICK_QUESTION)
			return false;
		
		return true;
	}
	
	/**
	 * Checks if the given location is below ground.
	 */
	public boolean isBelowGround( int x, int y ) {
		byte sceneobject = levelscene[y][x];
		
		// not below ground if already in ground
		if (sceneobject == Constants.BORDER_FULL ||
					sceneobject == Constants.ANGRYFLOWERPOT_OR_CANON)
			return false;
		
		// not below ground if in pit
		if (isPitBelow(x, 0))
			return false;
			
		// below ground if there is no ground in between
		// this location and the bottom of the scene
		for (int i = (Environment.HalfObsHeight * 2) - 1; i > y; i--) {
			sceneobject = levelscene[i][x];
			
			if (sceneobject == Constants.BORDER_FULL ||
					sceneobject == Constants.ANGRYFLOWERPOT_OR_CANON)
				return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if the given location contains
	 * an enemy that is vulnerable to both 
	 * jumps and fire.
	 */
	public boolean isRegularEnemy( int x, int y ) {
		byte enemy = enemies[y][x];
		
		// regular enemies are ones that can be jumped on and fired upon
		if (enemy != Constants.ENEMY_GOOMBA && enemy != Constants.ENEMY_REDKOOPA && enemy != Constants.ENEMY_GREENKOOPA &&
				enemy != Constants.ENEMY_GOOMBA && enemy != Constants.ENEMY_REDKOOPA && enemy != Constants.ENEMY_GREENKOOPA)
			return false;
		
		return true;
	}
	
	/**
	 * Checks if the given location contains
	 * an enemy that is vulnerable to neither
	 * jumps nor fire.
	 */
	public boolean isSpikyEnemy( int x, int y ) {
		byte enemy = enemies[y][x];
		
		// spiky enemies are ones that can't be jumped or fired upon (jumping causes damage)
		if (enemy != Constants.ENEMY_SPIKY && enemy != Constants.ENEMY_WINGED_SPIKY)
			return false;
		
		return true;
	}
	
	/**
	 * Checks if there are bricks present
	 * within a given range of Mario.
	 */
	public boolean bricksPresent( int range ) {
		for (int x_pos = Constants.MARIO_POS_X - range; x_pos <= Constants.MARIO_POS_X + range; x_pos++) {
			for (int y_pos = Constants.MARIO_POS_Y + range; y_pos >= Constants.MARIO_POS_Y - range; y_pos--) {
				byte sceneobject = levelscene[y_pos][x_pos];
				
				if (sceneobject == Constants.BRICK_QUESTION || 
						sceneobject == Constants.BRICK_REGULAR ||
						(sceneobject == Constants.BORDER_FULL && 
							x_pos > 0 && x_pos < ((Environment.HalfObsWidth * 2) - 1) &&
							y_pos > 0 && y_pos < ((Environment.HalfObsHeight * 2) - 1) &&
							levelscene[y_pos][x_pos + 1] == Constants.NOTHING && 
							levelscene[y_pos][x_pos - 1] == Constants.NOTHING &&
							levelscene[y_pos + 1][x_pos] == Constants.NOTHING &&
							levelscene[y_pos - 1][x_pos] == Constants.NOTHING)) 
					return true;
			}
		}
			
		return false;
	}
	
	/**
	 * Checks if there are power-ups present
	 * within a given range of Mario.
	 */
	public boolean powerUpItemsPresent( int range ) {
		for (int x_pos = Constants.MARIO_POS_X - range; x_pos <= Constants.MARIO_POS_X + range; x_pos++) {
			for (int y_pos = Constants.MARIO_POS_Y + range; y_pos >= Constants.MARIO_POS_Y - range; y_pos--) {
				byte item = enemies[y_pos][x_pos];
				
				if (item == Constants.ITEM_MUSHROOM || item == Constants.ITEM_FIREFLOWER)
					return true;
			}
		}
			
		return false;
	}
	
	/**
	 * Check whether Mario surrounded on 3 sides by a dead-end.
	 */
	public boolean deadEnd() {
		Position ceiling = new Position(Constants.MARIO_POS_X, Constants.MARIO_POS_Y);
		Position floor = new Position(Constants.MARIO_POS_X, Constants.MARIO_POS_Y);
		Position wall = new Position(Constants.MARIO_POS_X, Constants.MARIO_POS_Y);
		
		// find closest wall to the right (may cause x to be levelscene.length -- off grid)
		while (wall.x < (Environment.HalfObsWidth * 2) && levelscene[wall.y][wall.x] != Constants.BORDER_FULL)			
			wall.x++;
		
		// if no wall to the right, there 'appears' to be no dead-end
		if (wall.x == Environment.HalfObsWidth * 2)
			return false;
		
		// find closest ceiling (may cause y to be -1 -- off grid)
		while (ceiling.y >= 0 && levelscene[ceiling.y][ceiling.x] != Constants.BORDER_FULL)
			ceiling.y--;
		// find closest floor
		while (floor.y < (Environment.HalfObsHeight * 2) && !(levelscene[floor.y][floor.x] == Constants.BORDER_FULL && !isPitBelow(floor.x, floor.y)))
			floor.y++;
				
		// check if wall is connected to ceiling and floor
		for (int y = ceiling.y; y <= floor.y; y++) {
			if (y == -1)
				continue;
			
			if (levelscene[y][wall.x] != Constants.BORDER_FULL)
				return false;
		}
			
		// check if ceiling is connected to wall
		for (int x = ceiling.x; ceiling.y != -1 && x < wall.x; x++)
			if (levelscene[ceiling.y][x] != Constants.BORDER_FULL)
				return false;
			
		// check if floor is connected to wall
		for (int x = floor.x; x < wall.x; x++)
			if (levelscene[floor.y][x] != Constants.BORDER_FULL)
				return false;
		
		return true;
	}
	
	/**
	 * Checks whether Mario has a ceiling above and and a floor below 
	 * that stretches across the to the right side of the screen.
	 */
	public boolean tunnel() {
		Position ceiling = new Position(Constants.MARIO_POS_X, Constants.MARIO_POS_Y);
		Position floor = new Position(Constants.MARIO_POS_X, Constants.MARIO_POS_Y);
		
		// find closest ceiling
		while (ceiling.y > 0 && levelscene[ceiling.y][ceiling.x] != Constants.BORDER_FULL)
			ceiling.y--;
		// find closest floor
		while (floor.y < (Environment.HalfObsHeight * 2) && !(levelscene[floor.y][floor.x] == Constants.BORDER_FULL && !isPitBelow(floor.x, floor.y)))
			floor.y++;
			
		// check if ceiling continues all the way to the right
		for (int x = ceiling.x; x < (Environment.HalfObsWidth * 2); x++)
			if (levelscene[ceiling.y][x] != Constants.BORDER_FULL)
				return false;
			
		// check if floor continues all the way to the right
		for (int x = floor.x; x < (Environment.HalfObsWidth * 2); x++)
			if (levelscene[floor.y][x] != Constants.BORDER_FULL)
				return false;
		
		return true;
	}
	
	/* *************************
	 * Penalty map calculations
	 * *************************/
	
	/**
	 * Returns a 2D array of penalties associated with enemies.
	 * Each value is associated with a location.
	 */
	public int[][] getEnemyPenalties( Map<Integer, Integer> weightMap ) {
		int[][] enemyPenalty = new int[Environment.HalfObsHeight * 2][Environment.HalfObsWidth * 2];
		
		if (Constants.VERBOSITY >= 2) {
			System.out.println("\nEnemy scene:");
			printScene(enemies);
		}	
		
		for (int i = 0; i < Environment.HalfObsHeight * 2; i++) {
			for (int j = 0; j < Environment.HalfObsWidth * 2; j++) {
				Integer penalty = weightMap.get((int) enemies[i][j]);
				
				// add penalty based on enemy in this location
				enemyPenalty[i][j] += penalty != null ? penalty : 0;
			}
		}
		
		if (Constants.VERBOSITY >= 2) {
			System.out.println("Enemy scene penalties:");
			printPenalties(enemyPenalty);
		}		
		
		return enemyPenalty;
	}
	
	/**
	 * Returns a 2D array of penalties associated with scene objects.
	 * Each value is associated with a location.
	 */
	public int[][] getScenePenalties( Map<Integer, Integer> weightMap ) {
		int[][] scenePenalty = new int[Environment.HalfObsHeight * 2][Environment.HalfObsWidth * 2];
		
		if (Constants.VERBOSITY >= 2) {
			System.out.println("\nLevel scene:");
			printScene(levelscene);
		}
				
		for (int y = 0; y < scenePenalty.length; y++) {
			for (int x = 0; x < scenePenalty[0].length; x++) {
				Integer penalty = weightMap.get((int) levelscene[y][x]);
				
				// add penalty based on scene object in this location
				scenePenalty[y][x] += penalty != null ? penalty : 0;			
				
				// if location is in a pit,
				// add pit penalty
				if (isPitBelow(x, y)) {
					penalty = weightMap.get(Constants.PIT);
					scenePenalty[y][x] += penalty != null ? penalty * (y + 1) / scenePenalty.length : 0;
				}
				// if location is below ground,
				// add hard obstacle penalty
				else if (isBelowGround(x, y)) {
					penalty = weightMap.get(Constants.BORDER_FULL);
					scenePenalty[y][x] += penalty != null ? penalty : 0;
				}
				// if location contains a brick, 
				// add a portion of the brick penalty around the perimeter
				else if (levelscene[y][x] == Constants.BRICK_REGULAR || 
						levelscene[y][x] == Constants.BRICK_QUESTION ||
						(levelscene[y][x] == Constants.BORDER_FULL && x > 0 && x < levelscene[y].length-1 &&
							!(levelscene[y][x + 1] == Constants.BORDER_FULL || // ground usually has more ground next to it...
								levelscene[y][x - 1] == Constants.BORDER_FULL))) { // unlike hard empty bricks
					for (int j = y - 1; j <= y + 1; j++) {
//						for (int i = x - 1; i <= x + 1; i++) {
							if (!(j == y /* && i == x */) && 
//									i >= 0 && i < scenePenalty[y].length &&
									j >= 0 && j < scenePenalty.length) {
								penalty = weightMap.get(Constants.BRICK_NEARBY);
								scenePenalty[j][x] += penalty != null ? penalty : 0;
							}
//						}
					}						
				}
				// if location is at least two cells
				// above ground, add jump penalty
				else if (scenePenalty[y][x] == 0 && (y <= (scenePenalty.length - 1) && !canStandOn(x, y + 1))) {
					penalty = weightMap.get(Constants.JUMP);
					scenePenalty[y][x] += penalty != null ? penalty : 0;
				}				
			}
		}
		
		if (Constants.VERBOSITY >= 2) {
			System.out.println("Level scene penalties:");
			printPenalties(scenePenalty);
		}		
		
		return scenePenalty;
	}
	
	/* *************************
	 * Private helper methods
	 * *************************/
	
	/**
	 * Print the given scene.
	 * <p>
	 * Only difference between this method and printPenalties
	 * is that this uses bytes, which hold smaller values.
	 */
	private void printScene( byte[][] scene ) {
		int maxSize = 4;
				
		for (int y_pos = 0; y_pos < scene.length; y_pos++) {
			for (int x_pos = 0; x_pos < scene[y_pos].length; x_pos++) {
				int spaces = maxSize - ("" + scene[y_pos][x_pos]).length();
				
				if (x_pos == 0)
					System.out.print("| ");
				
				for (; spaces > 0; spaces--)
					System.out.print(' ');
				
				System.out.print(scene[y_pos][x_pos]);
				
				if (x_pos != scene[y_pos].length - 1)
					System.out.print(" | ");
				else
					System.out.print(" |");
			}
			
			System.out.println();
		}	
	}
	
	/**
	 * Print the given penalties.
	 * <p>
	 * Only difference between this method and printScene
	 * is that this uses integers, which hold larger values.
	 */
	private void printPenalties( int[][] scene ) {
		int maxSize = 4;
		
		for (int y_pos = 0; y_pos < scene.length; y_pos++) {
			for (int x_pos = 0; x_pos < scene[y_pos].length; x_pos++) {
				int spaces = maxSize - ("" + scene[y_pos][x_pos]).length();
				
				if (x_pos == 0)
					System.out.print("| ");
				
				for (; spaces > 0; spaces--)
					System.out.print(' ');
				
				System.out.print(scene[y_pos][x_pos]);
				
				if (x_pos != scene[y_pos].length - 1)
					System.out.print(" | ");
				else
					System.out.println(" |");
			}
		}		
	}
}