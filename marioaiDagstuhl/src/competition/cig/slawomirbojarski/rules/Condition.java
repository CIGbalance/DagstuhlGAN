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
import java.io.Serializable;

/**
 * All constants representing conditions and ternary values
 * are contained here.  These are mainly used to index into 
 * arrays of conditions and comparing ternary values.
 * <p>
 * <b>Note:</b> Value of DONT_CARE should be distinct
 * from all condition values (ternary and non-ternary).
 * 
 * @author Slawomir Bojarski
 */
@SuppressWarnings("serial")
public class Condition implements Serializable {
	
	/** constants */
	
	// base array of conditions used for cloning
	private static final Condition[] TEMPLATE = {
		new Condition(Condition.DONT_CARE, 0, Action.numActions() - 1), // 0: PREV_ACTION
		new Condition(Condition.DONT_CARE, 0, 2), // 1: MARIO_SIZE [0 = small, 1 = large, 2 = fire]
		new Condition(), // 2: IS_ENEMY_CLOSE_UPPER_LEFT
		new Condition(), // 3: IS_ENEMY_CLOSE_UPPER_RIGHT
		new Condition(), // 4: IS_ENEMY_CLOSE_LOWER_LEFT
		new Condition(), // 5: IS_ENEMY_CLOSE_LOWER_RIGHT
		new Condition(), // 6: BRICKS_PRESENT
		new Condition(), // 7: POWERUPS_PRESENT
		new Condition(), // 8: DEAD_END
		new Condition(), // 9: TUNNEL
		new Condition(), // 10: MAY_MARIO_JUMP
		new Condition(), // 11: MAY_MARIO_SHOOT
		new Condition(), // 12: IS_MARIO_ON_GROUND
		new Condition(), // 13: IS_MARIO_CARRYING
		new Condition(), // 14: IS_OBSTACLE_AHEAD
		new Condition(), // 15: IS_OBSTACLE_BEHIND
		new Condition(), // 16: IS_PIT_AHEAD
		new Condition(), // 17: IS_PIT_BEHIND
		new Condition(), // 18: IS_PIT_BELOW
	};
	
	// the first 'RULE_CONDITIONS' number 
	// of conditions are rule conditions
	//
	// ***Note***
	// has to be less than or equal to
	// the total number of conditions
	private static final int RULE_CONDITIONS = 10;
	
	public static final int // possible conditions - used for array indexing
							PREV_ACTION = 0,
							MARIO_SIZE = 1,
							IS_ENEMY_CLOSE_UPPER_LEFT = 2,
							IS_ENEMY_CLOSE_UPPER_RIGHT = 3,
							IS_ENEMY_CLOSE_LOWER_LEFT = 4,
							IS_ENEMY_CLOSE_LOWER_RIGHT = 5,
							BRICKS_PRESENT = 6,
							POWERUPS_PRESENT = 7,
							DEAD_END = 8,
							TUNNEL = 9,
							
							/** rule conditions end here */ 
							
							MAY_MARIO_JUMP = 10,
							MAY_MARIO_SHOOT = 11,
							IS_MARIO_ON_GROUND = 12,
							IS_MARIO_CARRYING = 13,							
							IS_OBSTACLE_AHEAD = 14,
							IS_OBSTACLE_BEHIND = 15,
							IS_PIT_AHEAD = 16,
							IS_PIT_BELOW = 17,
							IS_PIT_BEHIND = 18,
	
							// possible condition values
							DONT_CARE = -1,
							FALSE = 0, 
							TRUE = 1;
	
	/** variables */
	public int min, max, // valid inclusive range of values (not including DONT_CARE)
				value; // current value of condition
	
	/**
	 * Constructor
	 */
	public Condition() {
		this(Condition.DONT_CARE, 0, 1); // default to ternary condition
	}
	
	/**
	 * Constructor that initializes all variables.
	 * 
	 * @param value
	 * 			value for this condition
	 * @param min
	 * 			minimum value in valid range
	 * @param max
	 * 			maximum value in valid range
	 */
	public Condition( int value, int min, int max ) {
		this.value = value;
		this.min = min;
		this.max = max;
	}
	
	/**
	 * Returns a clone of this object.
	 */
	public Object clone() {
		return new Condition(value, min, max);
	}
	
	/**
	 * Returns the number of possible conditions.
	 * 
	 * @return 
	 * 		number of possible conditions
	 */
	public static int numConditions() {		
		return Condition.TEMPLATE.length;
	}
	
	/**
	 * Returns all possible conditions.
	 * 
	 * @return
	 * 		an array of all possible conditions 
	 */
	public static Condition[] allConditions() {
		Condition[] newcond = new Condition[Condition.TEMPLATE.length];
		
		for (int i = 0; i < newcond.length; i++)
			newcond[i] = (Condition) TEMPLATE[i].clone();		
		
		return newcond;
	}
	
	/**
	 * Returns only the conditions used in rules.
	 * 
	 * @return
	 * 		an array of conditions used in rules
	 */
	public static Condition[] ruleConditions() {
		Condition[] newcond = new Condition[RULE_CONDITIONS];
		
		for (int i = 0; i < RULE_CONDITIONS; i++)
			newcond[i] = (Condition) TEMPLATE[i].clone();		
		
		return newcond;
	}
}
