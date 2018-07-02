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
import java.util.ArrayList;
import java.util.List;

/** 
 * Container for x and y position pairs.
 * 
 * @author Slawomir Bojarski
 */
public class Position implements Comparable<Position> {
	public int x, y, 
				gScore, // distance along optimal path to the position
				hScore, // estimate of distance to a goal from the position
				fScore; // total distance from start to goal through the position
	public Position north, south, east, west, cameFrom;
	
	/**
	 * Constructor
	 */
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
		north = null;
		south = null;
		east = null;
		west = null;
		cameFrom = null;
		gScore = hScore = fScore = Integer.MAX_VALUE; // bad scores
	}
	
	/**
	 * Returns a list of valid neighbors.
	 */
	public List<Position> neighbors() {
		List<Position> n = new ArrayList<Position>();
		
		// add neighbors
		if (north != null) 
			n.add(north);
		if (south != null) 
			n.add(south);
		if (east != null) 
			n.add(east);
		if (west != null) 
			n.add(west);
		
		return n;
	}

	/**
	 * Returns whether this position
	 * is equal to another position.
	 */
	public boolean equals(Object other) {
		if (other instanceof Position)
			return this.compareTo((Position) other) == 0;
		
		return false;
	}
	
	/**
	 * Compares this position to another.
	 * 
	 * @return
	 * 		-1 if this position is less than the other, 
	 * 		 1 if this position is greater than the other,
	 * 		 0 otherwise.
	 */
	public int compareTo(Position other) {
		if (this.x < other.x)
			return -1;
		else if (this.x > other.x)
			return 1;
		else if (this.y < other.y)
			return -1;
		else if (this.y > other.y)
			return 1;
		else
			return 0;
	}
	
	/**
	 * Returns a string representation of this position.
	 */
	public String toString() {
		return "(" + x + "," + y + ")";
	}
}
