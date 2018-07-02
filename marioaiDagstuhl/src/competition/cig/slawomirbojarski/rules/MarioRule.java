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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ec.EvolutionState;
import ec.util.MersenneTwisterFast;

/** 
 * Represents a rule to be used in a rule-based system.
 * Each rule contains an array of conditions and an action.
 * 
 * The class inherits from ECJ's abstract rule class to 
 * provide the ability to be used in ECJ to learn the rules.
 * 
 * @author Slawomir Bojarski
 */
@SuppressWarnings("serial")
public class MarioRule extends ec.rule.Rule {
		
	/** static variables */
	public static float dontcare_prob = 0.4f;
	public static float mutate_prob = 0.1f;
	
	/** instance variables */
	public Condition[] conditions;
	public Action action;
	
	/**
	 * Constructor
	 */
	public MarioRule() {
		conditions = Condition.ruleConditions();
		setDefaultAction();
	}
	
	/**
	 * Constructor
	 * 
	 * @param initial_conditions
	 * 			set of conditions for this rule
	 * @param initial_action
	 * 			action to take based on this rule
	 */
	public MarioRule( Condition[] initial_conditions, int action_index ) {
		conditions = initial_conditions;
		action = Action.getActionType(action_index);
	}
	
	/**
	 * Set the condition at given index to given value.
	 * 
	 * @param index
	 * 		index of condition
	 * @param value
	 * 		new value for condition
	 */
	public void setCondition( int index, int value ) {
		conditions[index].value = value;
	}
	
	/**
	 * Set all conditions to given value.
	 * 
	 * @param value
	 * 		new value for all conditions
	 */
	public void setAllConditions( int value ) {
		
		// set each condition to value
		for (int i = 0; i < conditions.length; i++) {
			conditions[i].value = value;
		}
	}
	
	/**
	 * Set the button at given index to given value.
	 * 
	 * @param index
	 * 		the index of button
	 * @param value
	 * 		the new value for button
	 */
	public void setAction( int index ) {
		action = Action.getActionType(index);
	}
	
	/**
	 * Set the action to the empty action.
	 */
	public void setDefaultAction() {
		action = Action.getActionType(Action.PROGRESS);
	}
	
	/**
	 * Compare the other conditions to the conditions in this rule and
	 * return an evaluation of how many exact and general matches were found.
	 * 
	 * @param other
	 * 		the conditions that are being matched with this rule
	 * @return
	 * 		an evaluation of how many exact and general matches were found
	 */
	public RuleEval matchConditions( Condition[] other ) {
		int generalMatches = 0;
		int strictMatches = 0;		
		
		// if there are more rule conditions than matching conditions, 
		// make sure extra rule conditions are all don't cares
		if (conditions.length > other.length) {
			int difference = conditions.length - other.length;
			
			for (int i = conditions.length - difference; i < conditions.length; i++)
				if (conditions[i].value != Condition.DONT_CARE)
					return new RuleEval(strictMatches, generalMatches);
		}
		// else make sure each matching condition matches the corresponding rule
		// condition, unless the corresponding rule condition is a don't care
		else {
			for (int i = 0; i < conditions.length; i++) {
				if (conditions[i].value == other[i].value) {
					generalMatches++;
					strictMatches++;
				}
				else if (conditions[i].value == Condition.DONT_CARE) {
					generalMatches++;
				}					
			}
		}
		
		return new RuleEval(strictMatches, generalMatches);
	}	

	/**
	 * Resets the rule with random conditions and action.
	 */
	public void reset( EvolutionState state, int thread ) {
		MersenneTwisterFast rand = state.random[thread];
		
		setAllConditions(Condition.DONT_CARE);
		setDefaultAction();
		
		// randomly set conditions
		for (Condition c : conditions) {
			if (rand.nextBoolean(dontcare_prob)) {
				c.value = Condition.DONT_CARE;
			}
			else {
				int range = (c.max - c.min) + 1;
				c.value = c.min + rand.nextInt(range);
			}
		}
				
		// randomly set action
		action = Action.getActionType(rand.nextInt(Action.numActions()));
	}
		
	/**
	 * Mutates the conditions and buttons in action with a set probability.
	 * <p>
	 * <b>Note:</b> This is needed because ECJ calls reset on default, 
	 * which would cause the rule to be random.
	 */
	public void mutate( final EvolutionState state, final int thread ) {
		MersenneTwisterFast rand = state.random[thread];
		List<Action> differentActions = new ArrayList<Action>(); 
		
		// check each condition
		for (Condition c : conditions) {
			// is this condition being mutated?
			if (rand.nextBoolean(mutate_prob)) {
				// if DONT_CARE, set to a value in valid range
				if ( c.value == Condition.DONT_CARE ) {
					int range = (c.max - c.min) + 1;
					c.value = c.min + rand.nextInt(range);
				}
				// set to any other value in valid range or DONT_CARE
				else {
					int range = (c.max - c.min) + 1;
					int newval = c.min + rand.nextInt(range);
					
					// check if new value is different
					if (newval == c.value)
						newval = Condition.DONT_CARE;
					
					c.value = newval;
				}
			}
		}
		
		// build list of different actions
		for (int i = 0; i < Action.numActions(); i++) {
			Action current = Action.getActionType(i);
			
			if (i != Action.indexOf(action))
				differentActions.add(current);
		}
		
		// set action to a random different action
		action = differentActions.get(rand.nextInt(differentActions.size()));
    }
	
	/**
	 * Compares this rule to the other object and 
	 * returns whether this rule is less than, 
	 * greater than, or equal to the other object.
	 */
	public int compareTo( Object o ) {
		int actionIndex, otherActionIndex;
		MarioRule other;
		
		// compare class types
		if (!getClass().equals(o.getClass()))
			return 1;
		
		// cast other as a Rule
		other = (MarioRule) o;
		
		// compare number of conditions
		if (conditions.length < other.conditions.length)
			return -1;
		else if (conditions.length > other.conditions.length)
			return 1;
		
		// compare conditions
		for (int i = 0; i < conditions.length; i++)
			if (conditions[i].value < other.conditions[i].value)
				return -1;
			else if (conditions[i].value > other.conditions[i].value)
				return 1;

		// compare actions
		actionIndex = Action.indexOf(action);
		otherActionIndex = Action.indexOf(other.action);
		if (actionIndex < otherActionIndex)
			return -1;
		else if (actionIndex > otherActionIndex)
			return 1;
		
		return 0;
	}
	
	/**
	 * Returns whether this rule is equal to the other object.
	 */
	public boolean equals( Object other ) {
		return compareTo(other) == 0;
	}

	/**
	 * Returns the hash code for this rule.
	 */
	public int hashCode() {
		return conditions.hashCode() + action.hashCode();
	}
	
	/**
	 * Create a clone of the rule.
	 * <p>
	 * <b>Note:</b> This is needed because ECJ makes heavy use of cloning, thus the need for deep copies.
	 */
	public Object clone() {
		Condition[] clonecond = new Condition[conditions.length];
		
		for (int i = 0; i < clonecond.length; i++)
			clonecond[i] = (Condition) conditions[i].clone();		
		
		return new MarioRule(clonecond, Action.indexOf(action));
	}	
	
	/**
	 * Print the rule in such a way that readRuleFromString can parse it.
	 * <p>
	 * <b>Note:</b> This is needed in order to be able to read/write the rule to files.
	 */
	public String toString() {
		String result = "";
				
		for (int i = 0; i < conditions.length; i++)
			result += conditions[i].value + " "; // add condition and a space as delimiter
		
		result += "| " + Action.indexOf(action); // add delimiter between conditions from action
		
		return result;	
	}
	
	/**
	 * Parse the given string and set the conditions and action for this rule.
	 * <p>
	 * <b>Note:</b> This is needed in order to be able to read/write the rule to files.
	 */
	public void readRuleFromString( final String string, final EvolutionState state ) { 
		 StringTokenizer majorTokens = new StringTokenizer(string,"|");
		 StringTokenizer conditionTokens = new StringTokenizer(majorTokens.nextToken()," ");
		 StringTokenizer actionTokens = new StringTokenizer(majorTokens.nextToken()," ");
		 
		 for (int i = 0; i < conditions.length && conditionTokens.hasMoreTokens(); i++)
			 conditions[i].value = Integer.parseInt(conditionTokens.nextToken());
		 
		 action = Action.getActionType(Integer.parseInt(actionTokens.nextToken()));
	}
		
	/**
	 * Encapsulates the number of exact and general
	 * matches resulting from a comparison between
	 * other conditions and the conditions for this rule.
	 */
	public static class RuleEval {
		public int exactMatches;
		public int generalMatches;
		
		public RuleEval( int exact, int general ) {
			exactMatches = exact;
			generalMatches = general;
		}
	}
}