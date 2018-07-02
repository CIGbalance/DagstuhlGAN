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
 * Represents the objective of getting out of a dead-end.  
 * This involves Mario moving towards the left.
 * 
 * @author Slawomir Bojarski
 */
public class BackTrackAction extends Action {
			
	/**
	 * Constructor
	 */
	public BackTrackAction() {
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
		
		// weights for misc items
		weightMap.put(Constants.JUMP, 5);
		weightMap.put(Constants.PIT, 250);
	}

	/**
	 * Returns the position of the goal.
	 */
	protected Position getGoal() {
		int x = 0; // left side
		int y = (Environment.HalfObsHeight * 2) - 1; // bottom edge
		
		// goal can't be below ground
		while (y > 0 && absEnv.isBelowGround(x, y))
			y--;
		// goal can't be in the ground
		while (y > 0 && absEnv.levelscene[y][x] == Constants.BORDER_FULL || absEnv.levelscene[y][x] == Constants.ANGRYFLOWERPOT_OR_CANON)
			y--;
		// if couldn't set proper goal,
		// set y to Mario's level
		if (y == 0)
			y = Constants.MARIO_POS_Y;
				
		// goal is right side of screen on the same level as Mario
		return new Position(x, y);
	}
}
