package competition.cig.slawomirbojarski.simulator;

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
//import java.awt.Color;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
//import ch.idsia.mario.engine.GlobalOptions;
//import ch.idsia.mario.engine.MarioVisualComponent;
import ch.idsia.mario.engine.sprites.Mario;
//import ch.idsia.mario.engine.sprites.Sprite;
import ch.idsia.mario.environments.Environment;

import competition.cig.slawomirbojarski.AbstractEnvironment;
import competition.cig.slawomirbojarski.Constants;
import competition.cig.slawomirbojarski.rules.Condition;

/** 
 * Simulates a path through the environment
 * by using an A* algorithm, then returns
 * an appropriate action based on the first
 * section of the calculated path.
 * 
 * @author Slawomir Bojarski
 */
public class Simulator {
	
	/** instance variables */
	private Condition[] conditions;
	private Position goal;
	private Map<Integer, Integer> weightMap;
	private AbstractEnvironment absEnv;
//	private MarioVisualComponent visual;
//	private Sprite mario;
	
	/**
	 * Constructor
	 * 
	 * @param goal
	 * 			the goal position
	 * @param weightMap
	 * 			maps weights to level scenes and enemies
	 * @param absEnv
	 * 			the abstract environment
	 */
	public Simulator( Condition[] conditions, Position goal, Map<Integer, Integer> weightMap, AbstractEnvironment absEnv ) {
		this.conditions = conditions;
		this.goal = goal;
		this.weightMap = weightMap;
		this.absEnv = absEnv;
//		this.visual = GlobalOptions.marioVisualComponent;		
		
		// find Mario
//		for (Sprite s : visual.levelScene.sprites)
//			if (s instanceof Mario)
//				mario = s;
	}

	/**
	 * Returns what action needs to be performed 
	 * to start along an optimal path.
	 * 
	 * @return
	 * 			first action along optimal path
	 */
	public boolean[] simulate() {		
		long startTime = System.currentTimeMillis();
		Deque<Position> path = getPath();
		if (Constants.VERBOSITY >= 2) {
			System.out.println("Time to find optimal path: " + (System.currentTimeMillis() - startTime) + "ms");
		}
		
		Position start = path.peekFirst();
		Position localMaxUp = new Position(start.x, start.y);
		Position localMaxUp2 = new Position(start.x, start.y);
		Position localMaxDown = new Position(start.x, start.y);;
//		Position endJump = new Position(start.x, start.y);;
		Position leftRight = new Position(start.x, start.y);;
		Position upDown = new Position(start.x, start.y);;
		boolean[] action = new boolean[]{false, false, false, false, false};
				
		// if goal is same as start, do nothing
		if (start.equals(goal)) {
			action[Mario.KEY_SPEED] = true;
			return action;
		}
		
		// check path segment  
		// within a desired window
		for (Position current : path) {			
			// check if position is within Mario's jumping distance
			if (current.x <= (start.x + Constants.JUMP_DISTANCE_LONG) ||
					current.x >= (start.x - Constants.JUMP_DISTANCE_LONG) ||
					current.y <= (start.y + Constants.JUMP_DISTANCE_LONG) ||
					current.y >= (start.y - Constants.JUMP_DISTANCE_LONG)) {
				
				// is current position local max of the jump?
				if (current.y < localMaxUp.y)
					localMaxUp = current;
				// is current position local max further along the path?
				if (current.y <= localMaxUp2.y)
					localMaxUp2 = current;
				if (current.y > localMaxDown.y)
					localMaxDown = current;
				// is current position end of the jump?
//				if (!current.equals(start) && absEnv.canStandOn(current.south.x, current.south.y))
//					endJump = current;
				if (leftRight.x == start.x && (current.x < leftRight.x || current.x > leftRight.x))
					leftRight = current;
				if (current.cameFrom == start && (current.y < upDown.y || current.y > upDown.y))
					upDown = current;
				
				// did we find both a localMax and endJump?
//				if (!localMaxUp.equals(start) && !endJump.equals(start))
//					break; // stop. no need to check rest of path segment
			}
			// stop. path moved outside of desired window
			else {
				break;
			}
		}
		
		// are we even jumping?
		if (localMaxUp2.y < start.y) {
			if (conditions[Condition.MAY_MARIO_JUMP].value == Condition.TRUE || 
					conditions[Condition.IS_MARIO_ON_GROUND].value == Condition.FALSE)
				action[Mario.KEY_JUMP] = true;
			
			// long jump?
			if (Math.abs(localMaxUp2.x - start.x) > Constants.JUMP_DISTANCE_SHORT)
				action[Mario.KEY_SPEED] = true;
		}
		// don't jump, run instead
		else {
			action[Mario.KEY_SPEED] = true;
		}
		
		// go to the right? 
		if (localMaxUp.x > start.x)
			action[Mario.KEY_RIGHT] = true;
		// go to the left?
		else if (localMaxUp.x < start.x)
			action[Mario.KEY_LEFT] = true;
		// up and over a 'fence'
		else if (Math.abs(localMaxUp.x - localMaxUp2.x) <= 3) {
			// start going over 'fence'
			if (Math.abs(localMaxUp.y - start.y) <= 3) {
				// go right?
				if (leftRight.x < start.x)
					action[Mario.KEY_LEFT] = true;
				// go left?
				else if (leftRight.x > start.x)
					action[Mario.KEY_RIGHT] = true;
			}
			// move towards middle of grid cell
			else if (absEnv.marioPos[0] % 16 <= 4) {
				action[Mario.KEY_RIGHT] = true;
			}
			// move towards middle of grid cell
			else if (absEnv.marioPos[0] % 16 >= 12) {
				action[Mario.KEY_LEFT] = true;
			}
		}
		// go to the right? 
		else if (localMaxUp2.x > start.x)
			action[Mario.KEY_RIGHT] = true;
		// go to the left?
		else if (localMaxUp2.x < start.x)
			action[Mario.KEY_LEFT] = true;
		else {
			// go right?
			if (leftRight.x < start.x)
				action[Mario.KEY_LEFT] = true;
			// go left?
			else if (leftRight.x > start.x)
				action[Mario.KEY_RIGHT] = true;
		}
		
		// if goal is lower down and we're close,
		// slow down in order not to overshoot it
		if (goal.y > start.y && 
				Math.abs(goal.x - start.x) <= Constants.CLOSE_OBSTACLE &&
				Math.abs(goal.y - start.y) <= Constants.CLOSE_OBSTACLE &&
				localMaxUp.x <= goal.x) // not going up and around
			action[Mario.KEY_DOWN] = true;
		// if going straight down first
		else if (upDown.y > start.y)
			action[Mario.KEY_DOWN] = true;
		
		/** PAINTING ONTO VISUAL FOR DEBUGGING */
		
//		// paint locations of localMax and endJump
//		int[] pixelStartLoc = null;
//		int[] pixelGridLoc1 = null;
//		int[] pixelGridLoc2 = null;		
//		
//		// get pixel location of start
//		if (mario != null) 
//			pixelStartLoc = getPixelLocation(start);
//		
//		// set color and save previous color
//		Color prevColor = visual.thisVolatileImageGraphics.getColor();
//		visual.thisVolatileImageGraphics.setColor(Color.GREEN);
//		
//		// draw the goal
//		if (mario != null) 
//			pixelGridLoc1 = getPixelLocation(goal);		
//		visual.thisVolatileImageGraphics.drawRect(pixelGridLoc1[0], pixelGridLoc1[1], 16, 16);
//		visual.thisVolatileImageGraphics.drawString("G", pixelGridLoc1[0] + 4, pixelGridLoc1[1] + 12);
//		
//		// draw the path
//		for (Position p : path) {
//			// set current location
//			pixelGridLoc1 = getPixelLocation(p);
//			
//			// if we have a previous location,
//			// draw line from previous to current
//			if (pixelGridLoc2 != null)
//				visual.thisVolatileImageGraphics.drawLine(pixelGridLoc1[0] + 8, pixelGridLoc1[1] + 8, 
//															pixelGridLoc2[0] + 8, pixelGridLoc2[1] + 8);
//			// draw line from start location to current
//			else
//				visual.thisVolatileImageGraphics.drawLine(pixelStartLoc[0] + 8, pixelStartLoc[1] + 8, 
//															pixelGridLoc1[0] + 8, pixelGridLoc1[1] + 8);
//			
//			// set previous location
//			pixelGridLoc2 = pixelGridLoc1;
//		}
//		
//		// draw localMax and endJump
//		if (mario != null) {
//			pixelGridLoc1 = getPixelLocation(localMax);
//			pixelGridLoc2 = getPixelLocation(endJump);
//		}
//		visual.thisVolatileImageGraphics.setColor(Color.RED);
//		visual.thisVolatileImageGraphics.drawRect(pixelGridLoc2[0], pixelGridLoc2[1], 16, 16); // rectangle at endJump
//		visual.thisVolatileImageGraphics.drawLine(pixelStartLoc[0] + 8, pixelStartLoc[1] + 8, 
//													pixelGridLoc1[0] + 8, pixelGridLoc1[1] + 8); // line from start to localMax 
//		visual.thisVolatileImageGraphics.drawLine(pixelGridLoc1[0] + 8, pixelGridLoc1[1] + 8, 
//													pixelGridLoc2[0] + 8, pixelGridLoc2[1] + 8); // line from localMax to endJump
//				
//		// paint the stuff
//		visual.thisGraphics.drawImage(visual.thisVolatileImage, 0, 0, null);
//		
//		// reset color to previous color
//		visual.thisVolatileImageGraphics.setColor(prevColor);
		
		return action;
	}
	
	/**
	 * Returns an optimal path via an A* algorithm.
	 * 
	 * @return
	 * 			an optimal path
	 */
	private Deque<Position> getPath() {
		Set<Position> closedSet = new TreeSet<Position>(); // the positions of nodes already evaluated
		PriorityQueue<Position> openSet = new PriorityQueue<Position>(11, new ComparePositionByScore()); // the set of tentative positions to be evaluated
		int[][] enemyPenalty = absEnv.getEnemyPenalties(weightMap);
		int[][] scenePenalty = absEnv.getScenePenalties(weightMap);
		
		// create graph and set start position
		long startTime = System.currentTimeMillis();
		Position start = createGraph();
		if (Constants.VERBOSITY >= 2) {
			System.out.println("Time to construct graph: " + (System.currentTimeMillis() - startTime) + "ms");
		}
			
		// add Mario's starting position to the open set
		openSet.offer(start);
		
		// score the starting position
		start.gScore = 0;
		start.hScore = calculateHScore(start, goal);
		start.fScore = start.hScore;
		
		if (Constants.VERBOSITY >= 2) {
			System.out.println("Goal position: (" + goal.x + ", " + goal.y + ")");
		}
		
		while (! openSet.isEmpty()) {
			Position current = openSet.peek(); // position with lowest score
			
			if (Constants.VERBOSITY >= 3) {
				System.out.println("A* scores:");
				printAStarScores( openSet, closedSet, reconstructPath(current) );
			}
			
			// did we reach the goal?
			// or are we taking too much time?
			if (current.equals(goal) ||
					(Constants.LIMIT_TIME && (System.currentTimeMillis() - startTime) > Constants.TIME_LIMIT))
				return reconstructPath(current); // return path
			
			// move position from open to closed set
			closedSet.add(openSet.poll());
			
			for (Position neighbor : current.neighbors()) {
				// skip neighbor if in closed set
				if (closedSet.contains(neighbor))
					continue;
				
				// tentative gScore
				int gScore = current.gScore + 10 + enemyPenalty[neighbor.y][neighbor.x] + scenePenalty[neighbor.y][neighbor.x];
				
				// neighbor is better if it isn't already in the open set 
				// or if its tentative gScore is better than its current gScore  
				if (!openSet.contains(neighbor) || gScore < neighbor.gScore) {
					// remove if in open set,
					// will add back to open set
					// with new score values
					if (openSet.contains(neighbor))
						openSet.remove(neighbor);
					
					neighbor.cameFrom = current;
					neighbor.gScore = gScore;
					neighbor.hScore = calculateHScore(neighbor, goal);
					neighbor.fScore = neighbor.gScore + neighbor.hScore;
					
					openSet.offer(neighbor);
				}
			}
		}
		
		// couldn't find a path, so
		// return a path going nowhere
		return reconstructPath(start);
	}
		
	/**
	 * Constructs a graph by setting  
	 * links to neighboring positions.
	 * 
	 * @return
	 * 			the starting position in the graph
	 */
	private Position createGraph() {
		Position[][] positions = new Position[Environment.HalfObsWidth * 2][Environment.HalfObsHeight * 2]; // (x, y)
		
		// create all positions
		for (int i = 0; i < positions.length; i++)
			for (int j = 0; j < positions[i].length; j++)
				positions[i][j] = new Position(i, j);

		// link positions together
		for (int i = 0; i < positions.length; i++) {
			for (int j = 0; j < positions[i].length; j++) {
				Position current = positions[i][j];
				
				// add west link if not on left edge
				if (i != 0)
					current.west = positions[i - 1][j];
				
				// add north link if not on top edge
				if (j != 0)
					current.north = positions[i][j - 1];
				
				// add east link if not on right edge
				if (i != (positions.length -1))
					current.east = positions[i + 1][j];
				
				// add south link if not on bottom edge
				if (j != (positions[i].length - 1))
					current.south = positions[i][j + 1];				
			}
		}
			
		return positions[Constants.MARIO_POS_X][Constants.MARIO_POS_Y];
	}
	
	/**
	 * Returns a score representing the estimated distance 
	 * to the given goal from the given current position.
	 * 
	 * @param current
	 * 			the current position
	 * @param goal
	 * 			the goal
	 * @return
	 * 			a score representing the estimated distance 
	 * 			to the goal from the current position
	 */
	private int calculateHScore( Position current, Position goal ) {
		int HScore = 0;
		
		// score based on number of grid cells
		// passed on direct path to goal, which
		// doesn't take into account obstacles
		HScore += Math.abs(goal.x - current.x);
		HScore += Math.abs(goal.y - current.y);
		
		return HScore * 10;
	}
	
	/* ***********************
	 * Private Helper Methods
	 * ***********************/
	
	/**
	 * Prints a 2D array with A* scores and
	 * displays the current best path.
	 */
	private void printAStarScores( PriorityQueue<Position> openSet, Set<Position> closedSet, Deque<Position> path ) {
		Position[][] positions = new Position[Environment.HalfObsHeight * 2][Environment.HalfObsWidth * 2]; // (y, x)
		int maxSize = 4;
		
		// add positions from set set
		for (Position p : openSet)
			positions[p.y][p.x] = p;
		
		// add positions from closed set
		for (Position p : closedSet)
			positions[p.y][p.x] = p;
		
		for (int i = 0; i < (Environment.HalfObsHeight * 2); i++) {
			for (int j = 0; j < (Environment.HalfObsWidth * 2); j++) {	
				Position p = positions[i][j];
				int spaces = p == null ? maxSize : maxSize - ("" + p.fScore).length();
				
				// first column?
				if (j == 0)
					System.out.print("|");
				
				// add spaces for even columns
				for (; spaces > 0; spaces--)
					System.out.print(' ');
				
				// show score if there is a position
				if (p == null)
					System.out.print(" ");
				else if (path.contains(p))
					System.out.print("[" + p.fScore);
				else
					System.out.print(" " + p.fScore);			
				
				// last column?
				if (j == (Environment.HalfObsWidth * 2) - 1) {
					// position on path?
					if (p != null && path.contains(p))
						System.out.println("]|");
					// not on path
					else
						System.out.println(" |");
				}
				// column somewhere in the middle
				else {
					// position on path?
					if (p != null && path.contains(p))
						System.out.print("]|");
					// not on path
					else
						System.out.print(" |");					
				}
			}
		}
	}
	
	/**
	 * Reconstructs a path by following back-links.
	 * 
	 * @param goal
	 * 			the goal position, used to start reconstruction of path
	 * @return
	 * 			a path with the goal in the last position
	 */
	private Deque<Position> reconstructPath( Position goal ) {
		Deque<Position> path = new LinkedList<Position>();
		Position current = goal;
		
		while (current.cameFrom != null) {
			// add current position to end of path
			path.offerFirst(current);
			
			// follow the back-link
			current = current.cameFrom; 
		}
		
		// add start position to path
		path.offerFirst(current);
		
		return path;
	}
	
//	private int[] getPixelLocation( Position gridPos ) {
//		int marioX = (int) mario.x - (mario.xPicO / 2);
//		int marioY = (int) mario.y - (mario.yPicO / 2);
//		int gridX = marioX + (gridPos.x - Constants.MARIO_POS_X) * 16 - (marioX % 16);
//		int gridY = marioY + (gridPos.y - Constants.MARIO_POS_Y) * 16 - (marioY % 16);
//		
//		return new int[]{gridX, gridY};
//	}
	
	/* ***********************
	 * Private Nested Classes
	 * ***********************/
	
	/**
	 * Comparator that compares two positions by their fScores.
	 */
	private class ComparePositionByScore implements Comparator<Position> {

		public int compare( Position first, Position second ) {
			if (first.fScore < second.fScore)
				return -1;
			else if (first.fScore > second.fScore)
				return 1;
			else
				return first.compareTo(second);
		}
	}
}