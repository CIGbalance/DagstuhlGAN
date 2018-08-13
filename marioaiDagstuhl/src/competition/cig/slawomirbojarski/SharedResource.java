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
import competition.cig.slawomirbojarski.rules.MarioRule;

/**
 * A shared resource used by agent and ECJ when running concurrently
 * in order perform the competition's learning task.  Both agent and
 * ECJ play roles of "producer" and "consumer" of information contained
 * in this object.  This in effect establishes a form of communication
 * between them.
 * <p>
 * <b>Note:</b> Used in Learning Track of CIG 2010 competition
 */
public class SharedResource {

	// signals whether agent is undergoing a learning evaluation:
	// when true, agent and ECJ need to communicate using this object
	// when false, agent is not learning and thus ECJ can stop if already running
	public static boolean learningMode = false;
	
	// highest scoring rule set found
	public static MarioRule[] bestRules = null;
	
	// highest fitness score
	public static float bestFitness = -1;
	
	// set of rules for the agent:
	// ECJ "produces" the rules whenever they are null,
	// agent "consumes" the rules when needed and sets them to null
	private static MarioRule[] rules = null;
	
	// fitness of previously consumed rule set:
	// agent "produces" fitness after evaluating a consumed rule set,
	// ECJ "consumes" fitness when needed and sets it to a sentinel value
	private static float fitness = -1;
	
	public synchronized void produceRules( MarioRule[] rules ) {
		// wait until rules are "consumed"
		while (SharedResource.rules != null) {
			try {
				wait(100);
			} catch (InterruptedException e) {
				// do nothing
			}
		}

		// "produce" rules
		SharedResource.rules = rules;
		notifyAll();
	}
	
	public synchronized MarioRule[] consumeRules() {
		MarioRule[] result;
		
		// wait until rule set is "produced"
		while (rules == null) {
			try {
				wait(100);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		
		// "consume" rule set
		result = rules;
		rules = null;
		notifyAll();
		return result;
	}
	
	public synchronized void produceFitness( float fitness ) {
		// wait until fitness score is "consumed"
		while (SharedResource.fitness != -1) {
			try {
				wait(100);
			} catch (InterruptedException e) {
				// do nothing
			}
		}

		// "produce" fitness score
		SharedResource.fitness = fitness;
		notifyAll();
	}
	
	public synchronized float consumeFitness() {
		float result;
		
		// wait until fitness score is "produced"
		while (fitness == -1) {
			try {
				wait(100);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		
		// "consume" fitness score
		result = fitness;
		fitness = -1;
		notifyAll();
		return result;
	}
}
