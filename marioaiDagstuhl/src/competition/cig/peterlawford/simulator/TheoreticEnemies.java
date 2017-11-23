package competition.cig.peterlawford.simulator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import competition.cig.peterlawford.PeterLawford_SlowAgent;
import competition.cig.peterlawford.visualizer.Visualizer;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TheoreticEnemies {
	public LinkedList<TheoreticEnemy> enemies = new LinkedList<TheoreticEnemy>();
	TheoreticLevel level;

	final boolean fDebug;
	final boolean fSecretDebug;

	public TheoreticEnemies(TheoreticLevel level) {
		this.level = level;
		this.fDebug = PeterLawford_SlowAgent.DEBUG;
		this.fSecretDebug = true;
	}
	public TheoreticEnemies(TheoreticEnemies in, boolean fDebug, boolean fSecretDebug) {
		this.fDebug = fDebug;
		this.fSecretDebug = fSecretDebug;

		level = in.level;
		for (TheoreticEnemy e : in.enemies) {
			boolean fProcessed = false;
			if (e instanceof TheoreticFlowerEnemy) {
				enemies.add(new TheoreticFlowerEnemy(this,
						(TheoreticFlowerEnemy)e));
				fProcessed = true;
			}
			if (e instanceof TheoreticBulletBill) {
				enemies.add(new TheoreticBulletBill(this,
						(TheoreticBulletBill)e));
				fProcessed = true;
			}
			if (e instanceof TheoreticShell) {
				enemies.add(new TheoreticShell(this,
						(TheoreticShell)e));
				fProcessed = true;
			}
			if (e instanceof TheoreticFireball) {
				enemies.add(new TheoreticFireball(this,
						(TheoreticFireball)e));
				fProcessed = true;
			}
			if (e instanceof TheoreticMushroom) {
				enemies.add(new TheoreticMushroom(this,
						(TheoreticMushroom)e));
				fProcessed = true;
			}
			if (!fProcessed) {
				enemies.add(new TheoreticEnemy(this,e));
			}
		}
	}

	@Override
	public boolean equals(Object in) {
		if (in == null) return false;
		TheoreticEnemies e = (TheoreticEnemies)in;
		if (e.enemies.size() != enemies.size()) {
			if (fDebug) 
				System.err.print("("+e.enemies.size()+"!="+enemies.size()+")");
			return false;
		}

		ListIterator<TheoreticEnemy> iterSelf = enemies.listIterator();
		ListIterator<TheoreticEnemy> iterOther = e.enemies.listIterator();
		while (iterSelf.hasNext()) {
			TheoreticEnemy self = iterSelf.next();
			TheoreticEnemy other = iterOther.next();
			if ((self.deadTime != 0) && (other.deadTime != 0) && 
					(self.nType == other.nType))
				continue;
			if ((self.x != other.x) ||
					(self.y != other.y) ||
					(self.xa != other.xa) ||
					(self.ya != other.ya))
				return false;
		}
		return true;
	}






	public void processEnemyInfo2(Environment env, TheoreticMario mario) {
		float[] posEnemies = env.getEnemiesFloatPos();

		boolean fDiffers = false;
		ListIterator<TheoreticEnemy> iterE = enemies.listIterator();

		boolean fFirstInIterator = true;

		for (int i=0; i<posEnemies.length; i+=3) {
			TheoreticEnemy e = (iterE.hasNext()) ? iterE.next() : null;
			while ( (e != null) && (e.nType == Visualizer.FIREBALL) )
				e = ((iterE.hasNext()) ? iterE.next() : null);
			if (e == null) {
				// Add an entry on to the end of enemies
				fDiffers = true;	
			} else {
				// if the types are different or
				// the coords can't be synchronized,
				// then we either have a new item, or we've erased an old item

				boolean fFailedMatch = false;
				if (e.nType != ((byte)posEnemies[i])) {
					fFailedMatch = true;
				} else {
					if (
							(e.x != posEnemies[i+1]) ||
							(e.y != posEnemies[i+2]) ) {
						if (e.fixupDefinite((byte)posEnemies[i],
								posEnemies[i+1], posEnemies[i+2], true)) {
							fFailedMatch = false;
						} else {
							fFailedMatch = true;
						}					
					}
				}

				if (fFailedMatch) {
					// check these cases:
					// old can match with new+1, new can match with old+1
					boolean fItemAdded = false;
					boolean fItemRemoved = false;
					if ((i+3<posEnemies.length) && (e.nType == posEnemies[i+3])) {
						if ((e.x == posEnemies[i+4]) && (e.y == posEnemies[i+5])) {
							fItemAdded = true;
						} else {
							if (e.fixupDefinite((byte)posEnemies[i+3],
									posEnemies[i+4], posEnemies[i+5], false)) {	
								fItemAdded = true;
							}
						}
					}
					if (iterE.hasNext()) {
						TheoreticEnemy e2 = iterE.next(); iterE.previous();
						if (e2.nType == posEnemies[i]) {
							if ((e2.x == posEnemies[i+1]) && (e2.y == posEnemies[i+2])) {
								fItemRemoved = true;
							} else {
								if (e2.fixupDefinite((byte)posEnemies[i],
										posEnemies[i+1], posEnemies[i+2], false)) {	
									fItemRemoved = true;
								}
							}
						}
					}

					//					if (!fItemAdded && !fItemRemoved) {
					//						System.out.println("I dont know what to do!");
					//						throw new java.lang.NullPointerException();
					if (fDiffers) {
						if (!fItemAdded && !fItemRemoved && fFirstInIterator)
							fItemAdded = true;
						if (!fItemAdded && !fItemRemoved)
							throw new java.lang.NullPointerException();
						if (fItemAdded && fItemRemoved)
							throw new java.lang.NullPointerException();

						if(fItemAdded) {

						}
					}
				}
			}

		}

	}

	private void dumpGiven(float[] posEnemies) {
if (true) return;
		for (int i=0; i<posEnemies.length; i+=3) {
			System.out.print("["+(byte)posEnemies[i]+","+posEnemies[i+1]+","+
					posEnemies[i+2]+"] ");
		}
		System.out.println();
	}
	private void dumpTheoretic() {
		if (true)return;
		for (TheoreticEnemy e: enemies)
			System.out.print("["+e.nType+","+e.x+","+e.y+"] ");
		System.out.println();
	}

	public void dumpIfDifferent(float[] posEnemies) {
		boolean fDiffers = false;
		ListIterator<TheoreticEnemy> iterE = enemies.listIterator();
		for (int i=0; i<posEnemies.length; i+=3) {
			TheoreticEnemy e = (iterE.hasNext()) ? iterE.next() : null;
			while ( (e != null) && (e.nType == Visualizer.FIREBALL) )
				e = ((iterE.hasNext()) ? iterE.next() : null);
			if (e == null) {
				System.out.print("?????????????????????????????\t!=\t");
				System.out.println(
						"["+(byte)posEnemies[i]+","+posEnemies[i+1]+","+
						posEnemies[i+2]+"] ");
				fDiffers = true;	
			} else {
				if ( (e.nType != ((byte)posEnemies[i])) ||
						(e.x != posEnemies[i+1]) ||
						(e.y != posEnemies[i+2]) ) {
					System.out.print("["+e.nType+","+e.x+","+e.y+"]\t!=\t");
					System.out.println(
							"["+(byte)posEnemies[i]+","+posEnemies[i+1]+","+
							posEnemies[i+2]+"] ");
					fDiffers = true;

					if ((i+3<posEnemies.length) && (e.nType == posEnemies[i+3]) )
						iterE.previous();
				}
			}
		}


		if (fDiffers) {
			System.out.print("guess:("+enemies.size()+")");
			dumpTheoretic();
			System.out.print("given:");

			//		for (int i=posEnemies.length-3; i>=0; i-=3) {
			dumpGiven(posEnemies);
		}
	}


	private TheoreticEnemy createEnemy(byte nType, float x, float y, TheoreticMario mario,
			Environment env) {
		//		byte nType = (byte)posEnemies[i];
		///		float x = posEnemies[i+1];
		//		float y = posEnemies[i+2];
		//				new_enemies.add(new TheoreticComparable());
		switch(nType) {
		case Visualizer.ENEMY_GOOMBA:
		case Visualizer.ENEMY_RED_KOOPA:
		case Visualizer.ENEMY_GREEN_KOOPA:
		case Visualizer.ENEMY_SPINY:
			return new TheoreticEnemy(this, level, mario,
					nType, x, y, 0, 0, false);
		case Visualizer.ENEMY_FLYING_GOOMBA:
		case Visualizer.ENEMY_FLYING_RED_KOOPA:
		case Visualizer.ENEMY_FLYING_GREEN_KOOPA:
		case Visualizer.ENEMY_FLYING_SPINY:
			return new TheoreticEnemy(this, level, mario,
					nType, x, y, 0, 0, true);
		case Visualizer.ENEMY_PIRANHA_PLANT:
			return new TheoreticFlowerEnemy(this, level, mario, x, y);
		case Visualizer.ENEMY_BULLET:
			System.out.println("Surprise bullet!");
			return new TheoreticBulletBill(this, x, y, (x>mario.x)?-1:1);
		case Visualizer.SHELL:
			return new TheoreticShell(this, level, x, y);
		case 14:
			int xf = (int)(x/16); int yf = (int)(y/16);
			System.out.println("WHAT IS 14? "+x+","+y+
					"("+xf+","+yf+")"+
					"["+(xf-((int)(mario.x/16))+11)+","+(yf-((int)(mario.y/16))+11)+"]");
			PeterLawford_SlowAgent.viz.ansiViz(env, PeterLawford_SlowAgent.block_eval);
			return new TheoreticMushroom(this, level, x, y);
		default:
			System.out.println("Unrecognized enemy type: "+nType+", "+x+","+y+
					" M:"+mario.x+","+mario.y);					
			throw new java.lang.NullPointerException();
		}		
	}


	public void processEnemyInfo(Environment env, TheoreticMario mario) {
		float[] posEnemies = env.getEnemiesFloatPos();

		//		System.out.println("MARIO:"+mario.x+","+mario.y);

		//mario.dump();

		//		if (fDebug && SlowAgent.DEBUG) {
//		if (fSecretDebug) {
//			boolean fDiffers = false;
//			ListIterator<TheoreticEnemy> iterE = enemies.listIterator();
//			for (int i=0; i<posEnemies.length; i+=3) {
//				TheoreticEnemy e = (iterE.hasNext()) ? iterE.next() : null;
//				while ( (e != null) && (e.nType == Visualizer.FIREBALL) )
//					e = ((iterE.hasNext()) ? iterE.next() : null);
//				if (e == null) {
//					//					System.out.print("?????????????????????????????\t!=\t");
//					//					System.out.println(
//					//							"["+(byte)posEnemies[i]+","+posEnemies[i+1]+","+
//					//							posEnemies[i+2]+"] ");
//					fDiffers = true;	
//				} else {
//					if ( (e.nType != ((byte)posEnemies[i])) ||
//							(e.x != posEnemies[i+1]) ||
//							(e.y != posEnemies[i+2]) ) {
//						//						System.out.print("["+e.nType+","+e.x+","+e.y+"]\t!=\t");
//						//						System.out.println(
//						//								"["+(byte)posEnemies[i]+","+posEnemies[i+1]+","+
//						//								posEnemies[i+2]+"] ");
//						fDiffers = true;
//
//						if ((i+3<posEnemies.length) && (e.nType == posEnemies[i+3]) )
//							iterE.previous();
//					}
//				}
//			}
//
//			if (fDiffers) mario.dump();
//			/*
//			if (fDiffers) {
//				System.out.print("guess:("+enemies.size()+")");
//				for (TheoreticEnemy e: enemies)
//					System.out.print("["+e.nType+","+e.x+","+e.y+"] ");
//				System.out.print("\tgiven:");
//
//				//		for (int i=posEnemies.length-3; i>=0; i-=3) {
//				for (int i=0; i<posEnemies.length; i+=3) {
//					System.out.print("["+(byte)posEnemies[i]+","+posEnemies[i+1]+","+
//							posEnemies[i+2]+"] ");
//				}
//				System.out.println();
//			} */
//		}
//		if ((enemies.size() > 0) || (posEnemies.length > 0)) {
//			SlowAgent.logger.info("guess:("+enemies.size()+")");
//			for (TheoreticEnemy e: enemies)
//				SlowAgent.logger.info("["+e.nType+","+e.x+","+e.y+"] ");
//			SlowAgent.logger.info("\tgiven:");
//
//			//		for (int i=posEnemies.length-3; i>=0; i-=3) {
//			for (int i=0; i<posEnemies.length; i+=3) {
//				SlowAgent.logger.info("["+(byte)posEnemies[i]+","+posEnemies[i+1]+","+
//						posEnemies[i+2]+"] ");
//			}
//			SlowAgent.logger.info("\n");
//		}


//		// Clone the enemies list
//		LinkedList<TheoreticEnemy> temp = 
//			new LinkedList<TheoreticEnemy>(enemies);
//		{
//			// Remove fireballs
//			ListIterator<TheoreticEnemy> iterEnemies = temp.listIterator();
//			while (iterEnemies.hasNext()) {
//				TheoreticEnemy e = iterEnemies.next();
//				if (e instanceof TheoreticFireball) {
//					iterEnemies.remove();
//					//					continue;
//				}
//			}
//		}


		ListIterator<TheoreticEnemy> iterTrueEnemies = 
			enemies.listIterator(enemies.size());
		if (!enemies.isEmpty() && !iterTrueEnemies.hasPrevious())
			throw new java.lang.NullPointerException();

//		//		LinkedList<TheoreticComparable> new_enemies =
//		//			new LinkedList<TheoreticComparable>();
//		LinkedList<TheoreticEnemy> new_enemies =
//			new LinkedList<TheoreticEnemy>();
//		//		for (int i=0; i<posEnemies.length; i+=3) {
//
//		//		int nEmptySlot = enemies.size();
		
		
/*		
		if ((posEnemies.length > 0) || !enemies.isEmpty()) {
		System.out.print("GIVEN:");
		dumpGiven(posEnemies);
		System.out.print("ASSUMED:");
		dumpTheoretic();
		}
*/		
//		LinkedList<TheoreticEnemy> lOldEnemies = null;
//		boolean[] maskMatchedEnemies = new boolean[enemies.size()];
		
		
		for (TheoreticEnemy e : enemies)
			e.fMatched = false;
		
		
		{
			
			boolean fNoMismatches = true;
		TheoreticEnemy e = null;
		for (int i=posEnemies.length-3; i>=0; i-=3) {
			byte true_nType = (byte)posEnemies[i];
			float true_x = posEnemies[i+1];
			float true_y = posEnemies[i+2];
			//			TheoreticComparable comp = new TheoreticComparable(
			//					(byte)posEnemies[i], posEnemies[i+1], posEnemies[i+2]);
			//			while ((nEmptySlot > 0) && 
			//					(enemies.get(nEmptySlot-1).nType == Visualizer.FIREBALL))
			//				nEmptySlot--;


			///////////////////////////////
			// Begin the search
			///////////////////////////////
			boolean fFoundMatch = false;
//			TheoreticEnemy e = iterTrueEnemies.previous();

			// Skip over fireballs
			if (iterTrueEnemies.hasPrevious()) {
				e = iterTrueEnemies.previous();
				while (iterTrueEnemies.hasPrevious() && (e instanceof TheoreticFireball)) {
					e = iterTrueEnemies.previous();
				}			
				if (e instanceof TheoreticFireball)
					e = null;				
			} else {
				e = null;
			}
			
			if (e != null) {
			// Exact matches, in-order
			{
//				System.out.print(".");
				
				if ((e.nType == true_nType) &&
						(e.x == true_x) && (e.y == true_y)) {
					fFoundMatch = true;
					e.fMatched = true;
					continue;
					//				iterEnemies.remove();
				}
			}

			if (fNoMismatches) {
				fNoMismatches = false;

//				System.out.print("GIVEN:");
//				dumpGiven(posEnemies);
//				System.out.print("ASSUMED:");
//				dumpTheoretic();
}
			
			if (e != null)
//				System.out.println("TRUE:"+true_nType+","+true_x+","+true_y+" VS GUESS:"+
//						e.nType+","+e.x+","+e.y);
				

			// Non-exact but definite matches, in-order
			{
				byte nOldNtype = e.nType; float nX = e.x; float nY = e.y;

				if (e.fixupDefinite(true_nType,
						true_x, true_y, true)) {					
					//					if (fDebug)
//					System.out.println(nOldNtype+","+nX+","+nY+" ==> "+
//							true_nType+","+true_x+","+true_y);

					if ((e.x != true_x) || (e.y != true_y)) {
						PeterLawford_SlowAgent.logger.severe("ENEMY INFO NOT UPDATED! (\n"+
								"should be "+true_x+","+true_y+", not "+e.x+","+e.y);
						throw new java.lang.NullPointerException();
					}

					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info(nOldNtype+","+nX+","+nY+" ==> "+
								true_nType+","+true_x+","+true_y);
					if ( (Math.abs(true_x-mario.x) < 16*9) &&
							(Math.abs(true_y-mario.y) < 16*9) ) {
						//						SlowAgent.viz.ansiViz(env, SlowAgent.block_eval);
						//						SlowAgent.logger.severe("Out-of-sync");
						//						throw new java.lang.NullPointerException();
					}

//					System.out.println("Def. match, in-order("+e.nType+","+e.x+","+e.y);
					fFoundMatch = true;
					e.fMatched = true;
					continue;
					//				iterEnemies.remove();
					//				break;
				}
			}


///			if (lOldEnemies == null)
//				lOldEnemies = new LinkedList<TheoreticEnemy>(enemies);
			
			
			// Exact matches, out-of-order
			{
				ListIterator<TheoreticEnemy> iterSearchForEnemy = 
					enemies.listIterator(enemies.size());
				TheoreticEnemy e2 = null;
				while (iterSearchForEnemy.hasPrevious()) {
					e2 = iterSearchForEnemy.previous();
					if (e2 instanceof TheoreticFireball) continue;
					if (e2.nType != true_nType) continue;
					if (e2.x != true_x) continue;
					if (e2.y != true_y) continue;
				}

				if ((e2 != null) && (e2.nType == true_nType) &&
						(e2.x == true_x) && (e2.y == true_y)) {
//					System.out.println("Exact match, out-of-order(True="+
//							true_nType+","+true_x+","+true_y);

					fixupOrdering(iterSearchForEnemy,
							iterTrueEnemies, e2);
					fFoundMatch = true;
					continue;
				}
			}

			// Definite matches, out-of-order
			{
				ListIterator<TheoreticEnemy> iterSearchForEnemy = 
					enemies.listIterator(enemies.size());
				TheoreticEnemy e2 = null;
				while (iterSearchForEnemy.hasPrevious()) {
					e2 = iterSearchForEnemy.previous();
					if (e2 instanceof TheoreticFireball) continue;
					if (e2.nType != true_nType) continue;
					if (e2.fixupDefinite(true_nType, true_x, true_y, false)) continue;
				}

				if ((e2 != null) && (e2.fixupDefinite(true_nType, true_x, true_y, true))) {
//					System.out.println("Definite match, out-of-order(True="+
//							true_nType+","+true_x+","+true_y);

					fixupOrdering(iterSearchForEnemy,
							iterTrueEnemies, e2);
					fFoundMatch = true;					
					continue;
				}
			}

			}

			//			// Exact matches
			//			ListIterator<TheoreticEnemy> iterEnemies = temp.listIterator();
			//			while (iterEnemies.hasNext()) {
			//				TheoreticEnemy e = iterEnemies.next();
			//				//				if (e instanceof TheoreticFireball) {
			//				//					iterEnemies.remove();
			//				//					continue;
			//				//				}
			//				if ((e.nType == (byte)posEnemies[i]) &&
			//						(e.x == posEnemies[i+1]) && (e.y == posEnemies[i+2])) {
			//					fFoundMatch = true;
			//					iterEnemies.remove();
			//					break;
			//				}
			//			}
			//			if (fFoundMatch) {
			//				nEmptySlot--;
			//				continue;
			//			}
			//
			//			// Non-exact but definite matches
			//			iterEnemies = temp.listIterator();
			//			while (iterEnemies.hasNext()) {
			//				TheoreticEnemy e = iterEnemies.next();
			//				/*				if (e.isThisDefinitelyMe((byte)posEnemies[i],
			//						posEnemies[i+1], posEnemies[i+2])) {
			//					e.fixupDefinite((byte)posEnemies[i], posEnemies[i+1], posEnemies[i+2]); */
			//				byte nOldNtype = e.nType; float nX = e.x; float nY = e.y;
			//
			//				if (e.fixupDefinite((byte)posEnemies[i],
			//						posEnemies[i+1], posEnemies[i+2], true)) {					
			//					//					if (fDebug)
			//					System.out.println(nOldNtype+","+nX+","+nY+" ==> "+
			//							(byte)posEnemies[i]+","+posEnemies[i+1]+","+posEnemies[i+2]);
			//
			//					if ((e.x != posEnemies[i+1]) || (e.y != posEnemies[i+2])) {
			//						SlowAgent.logger.severe("ENEMY INFO NOT UPDATED!\n");
			//						throw new java.lang.NullPointerException();
			//					}
			//
			//					if (fSecretDebug)
			//						SlowAgent.logger.info(nOldNtype+","+nX+","+nY+" ==> "+
			//								(byte)posEnemies[i]+","+posEnemies[i+1]+","+posEnemies[i+2]);
			//					if ( (Math.abs(posEnemies[i+1]-mario.x) < 16*9) &&
			//							(Math.abs(posEnemies[i+2]-mario.y) < 16*9) ) {
			//						//						SlowAgent.viz.ansiViz(env, SlowAgent.block_eval);
			//						//						SlowAgent.logger.severe("Out-of-sync");
			//						//						throw new java.lang.NullPointerException();
			//					}
			//
			//					fFoundMatch = true;
			//					iterEnemies.remove();
			//					break;
			//				}
			//			}
			//			if (fFoundMatch) {
			//				nEmptySlot--;
			//				continue;
			//			}
			//
			//			// Resynchronize
			//			iterEnemies = temp.listIterator();
			//			while (iterEnemies.hasNext()) {
			//				TheoreticEnemy e = iterEnemies.next();
			//				if (e.fixupProbable((byte)posEnemies[i],
			//						posEnemies[i+1], posEnemies[i+2], true)) {
			//					if (fDebug)
			//						System.out.println(e.x+","+e.y+" == "+
			//								posEnemies[i+1]+","+posEnemies[i+2]);
			//					fFoundMatch = true;
			//					iterEnemies.remove();
			//					break;
			//				}
			//			}
			//			if (fFoundMatch) {
			//				nEmptySlot--;
			//				continue;
			//			}


			// All new enemies should be added to the front of the list
			//			if (!fFoundMatch) {
			//			System.out.println("adding at "+nEmptySlot);

			//			nEmpiterTrueEnemies.previousIndex();
//			System.out.println("Adding new enemy "+true_nType+","+true_x+","+true_y+
//					" :: "+e);
			//			enemies.add(nEmptySlot, createEnemy(true_nType,
			//					true_x, true_y, mario, env) );
			TheoreticEnemy e_new = createEnemy(true_nType,
					true_x, true_y, mario, env);
			if (iterTrueEnemies.hasNext() && (e != null)) {
			iterTrueEnemies.next();
			iterTrueEnemies.add(e_new);
			iterTrueEnemies.previous();
			} else {
				if (e == null) {
				enemies.addFirst(e_new);	
				} else {
				iterTrueEnemies.add(e_new);
//				iterTrueEnemies.next();
//				iterTrueEnemies.previous(); // so that when we get 'prev' we will re-get current
				}
			}
			e_new.fMatched = true;
//			System.out.print("post-add:");
			dumpTheoretic();
			
			/*			
			byte nType = (byte)posEnemies[i];
			float x = posEnemies[i+1];
			float y = posEnemies[i+2];
			//				new_enemies.add(new TheoreticComparable());
			switch(nType) {
			case Visualizer.ENEMY_GOOMBA:
			case Visualizer.ENEMY_RED_KOOPA:
			case Visualizer.ENEMY_GREEN_KOOPA:
			case Visualizer.ENEMY_SPINY:
				enemies.add(nEmptySlot, new TheoreticEnemy(this, level, mario,
						nType, x, y, 0, 0, false));
				break;
			case Visualizer.ENEMY_FLYING_GOOMBA:
			case Visualizer.ENEMY_FLYING_RED_KOOPA:
			case Visualizer.ENEMY_FLYING_GREEN_KOOPA:
			case Visualizer.ENEMY_FLYING_SPINY:
				enemies.add(nEmptySlot, new TheoreticEnemy(this, level, mario,
						nType, x, y, 0, 0, true));
				break;
			case Visualizer.ENEMY_PIRANHA_PLANT:
				enemies.add(nEmptySlot, new TheoreticFlowerEnemy(this, level, mario, x, y));
				break;
			case Visualizer.ENEMY_BULLET:
				System.out.println("Surprise bullet!");
				enemies.add(nEmptySlot, new TheoreticBulletBill(this, x, y, (x>mario.x)?-1:1));
				break;
			case Visualizer.SHELL:
				enemies.add(nEmptySlot, new TheoreticShell(this, level, x, y));
				break;
			case 14:
				int xf = (int)(x/16); int yf = (int)(y/16);
				System.out.println("WHAT IS 14? "+x+","+y+
						"("+xf+","+yf+")"+
						"["+(xf-((int)(mario.x/16))+11)+","+(yf-((int)(mario.y/16))+11)+"]");
				SlowAgent.viz.ansiViz(env, SlowAgent.block_eval);
				enemies.add(nEmptySlot, new TheoreticMushroom(this, level, x, y));
				break;
			default:
				System.out.println("Unrecognized enemy type: "+nType+", "+x+","+y+
						" M:"+mario.x+","+mario.y);					
				throw new java.lang.NullPointerException();
			}
			 */
			//			}
		}
		}
		
		// Clean up the dead
//		ListIterator<TheoreticEnemy> iter = temp.listIterator();
//		cleanUpTheDead(mario);

		cleanUpTheDead2(mario);
		
//		System.out.print("Finally:");
//		dumpTheoretic();

		//		if (!temp.isEmpty()) {
		//			throw new java.lang.NullPointerException();
		//		}
	}

	
	private void cleanUpTheDead2(TheoreticMario mario) {
		ListIterator<TheoreticEnemy> iter = enemies.listIterator();
		while (iter.hasNext()) {
			TheoreticEnemy e = iter.next();
			if (e instanceof TheoreticFireball) continue;
			if (e.fMatched) continue;
			
			if ((e.deadTime != 0) || (e.nType == 13) || (e.deadTime > 0) || 
					(mario.x - e.x > 230)) {	// 240?
				iter.remove();
//				iter.remove();
				if (fDebug)
					System.out.println("Removing dead or shell "+e.nType+", "+e.x+","+e.y);
			} else {
				//				if (fDebug)
				System.out.println("Unable to match "+
						e.nType+", "+e.x+","+e.y+","+e.deadTime);				
				if ((mario.status != Mario.STATUS_DEAD) &&
						(mario.status != Mario.STATUS_WIN)) {
					//					throw new java.lang.NullPointerException();				
					PeterLawford_SlowAgent.logger.severe("Unable to match "+
							e.nType+", "+e.x+","+e.y+","+e.deadTime);
//					iter.remove();
					throw new java.lang.NullPointerException();
				} else {
					iter.remove();					
				}
			}
		}		
	}
	
	private void cleanUpTheDead(TheoreticMario mario) {
		ListIterator<TheoreticEnemy> iter = enemies.listIterator();
		while (iter.hasNext()) {
			TheoreticEnemy e = iter.next();
			if (e.nType == 25) {
				iter.remove();
				continue;
			}
			if ((e.deadTime != 0) || (e.nType == 13) || (e.deadTime > 0) || 
					(mario.x - e.x > 230)) {	// 240?
				enemies.remove(e);
				iter.remove();
				if (fDebug)
					System.out.println("Removing dead or shell "+e.nType+", "+e.x+","+e.y);
			} else {
				//				if (fDebug)
				System.out.println("Unable to match "+
						e.nType+", "+e.x+","+e.y+","+e.deadTime);				
				if ((mario.status != Mario.STATUS_DEAD) &&
						(mario.status != Mario.STATUS_WIN)) {
					//					throw new java.lang.NullPointerException();				
					PeterLawford_SlowAgent.logger.severe("Unable to match "+
							e.nType+", "+e.x+","+e.y+","+e.deadTime);
					enemies.remove(e);
					iter.remove();
					throw new java.lang.NullPointerException();
				}
			}
		}		
	}

	private void fixupOrdering(ListIterator<TheoreticEnemy> iterSearchForEnemy,
			ListIterator<TheoreticEnemy> iterTrueEnemies,
			TheoreticEnemy e2) {
		int nPosToRemove = iterSearchForEnemy.previousIndex();
		int PosToInsertAfter = iterTrueEnemies.previousIndex();
		while (iterTrueEnemies.previousIndex() > nPosToRemove)
			iterTrueEnemies.previous();
		while (iterTrueEnemies.previousIndex() < nPosToRemove)
			iterTrueEnemies.next();

		// This is for debugging purposes only
		TheoreticEnemy eCut = iterTrueEnemies.next();
		if (e2 != eCut) {
			System.out.println("eCut="+eCut.nType+","+eCut.x+","+eCut.y);
			throw new java.lang.NullPointerException();					
		}

		iterTrueEnemies.remove();

		while (iterTrueEnemies.previousIndex() > PosToInsertAfter)
			iterTrueEnemies.previous();
		while (iterTrueEnemies.previousIndex() < PosToInsertAfter)
			iterTrueEnemies.next();

		iterTrueEnemies.add(e2);

		System.out.print("After mod: ");
		dumpTheoretic();

	}

	public boolean isOutOfBounds(Environment env, float x, float y) {
		if ((x==0) || (y==0)) return true;
		if (y > 255) return true;

		if (x - env.getMarioFloatPos()[0] > (15.5)*11) return true;
		if (env.getMarioFloatPos()[0] - x > 15*11) return true;
		if (y - env.getMarioFloatPos()[1] > (15.5)*11) return true;
		if (env.getMarioFloatPos()[1] - y > 15*11) return true;

		return false;
	}

	private byte getWiggleY(Environment env, int Dx, int My, float y, int nYold) {
		int EY = (int)(y/16);
		int nYW = EY - My +11;
		if ((nYW == nYold) || (nYW < 0) || (nYW >= 22))
			return -1;
		return env.getEnemiesObservation()[nYW][Dx];
	}
	private byte getWiggleX(Environment env, int Dy, int Mx, float x, int nXold) {
		int EX = (int)(x/16);
		int nXW = EX - Mx +11;
		if ((nXW == nXold) || (nXW < 0) || (nXW >= 22))
			return -1;
		return env.getEnemiesObservation()[Dy][nXW];
	}
	private byte getWiggleXY(Environment env,
			int Mx, int My, float x, float y, int nXold, int nYold) {
		int EX = (int)(x/16); int nXW = EX - Mx +11;
		int EY = (int)(y/16); int nYW = EY - My +11;
		if ((nXW == nXold) || (nXW < 0) || (nXW >= 22) ||
				(nYW == nYold) || (nYW < 0) || (nYW >= 22))
			return -1;
		return env.getEnemiesObservation()[nYW][nXW];
	}

	private byte getDiamondWiggle(Environment env, int nXCalcOld, int nYCalcOld, 
			int Mx, int My, float x, float y, float nOffset) {
		byte temp;
		temp = getWiggleX(env, nYCalcOld, Mx, x+nOffset, nXCalcOld);
		if ((temp != -1) && (temp != Visualizer.MARIO)) return temp;
		temp = getWiggleX(env, nYCalcOld, Mx, x-nOffset, nXCalcOld);
		if ((temp != -1) && (temp != Visualizer.MARIO)) return temp;
		temp = getWiggleY(env, nXCalcOld, My, y+nOffset, nYCalcOld);
		if ((temp != -1) && (temp != Visualizer.MARIO)) return temp;
		temp = getWiggleY(env, nXCalcOld, My, y-nOffset, nYCalcOld);
		if ((temp != -1) && (temp != Visualizer.MARIO)) return temp;
		return -1;
	}

	private byte getRectWiggle(Environment env, int nXCalcOld, int nYCalcOld,
			int Mx, int My, float x, float y, float xOff, float yOff ) {
		byte temp;
		temp = getWiggleXY(env, Mx, My, x+xOff,  y+yOff, nXCalcOld, nYCalcOld);		
		if ((temp != -1) && (temp != Visualizer.MARIO)) return temp;
		temp = getWiggleXY(env, Mx, My, x+xOff,  y-yOff, nXCalcOld, nYCalcOld);		
		if ((temp != -1) && (temp != Visualizer.MARIO)) return temp;
		temp = getWiggleXY(env, Mx, My, x-xOff,  y+yOff, nXCalcOld, nYCalcOld);		
		if ((temp != -1) && (temp != Visualizer.MARIO)) return temp;
		temp = getWiggleXY(env, Mx, My, x-xOff,  y-yOff, nXCalcOld, nYCalcOld);		
		if ((temp != -1) && (temp != Visualizer.MARIO)) return temp;
		return -1;
	}

	public byte getEnemyType(Environment env, float x, float y) {
		/*			int nX = (int)((x-env.getMarioFloatPos()[0])/16)+11;
			int nY = (int)((y-env.getMarioFloatPos()[1])/16)+11; */
		if ((x==0) || (y==0)) return -1;
		if (y > 255) return -1;

		//			x+=3;
		//			y += 15;
		float xMario = env.getMarioFloatPos()[0];
		float yMario = env.getMarioFloatPos()[1];

		//			xMario += 1;
		//			yMario += 16;
		//			yMario += 1;

		int MarioXInMap = (int)xMario/16;
		int MarioYInMap = (int)yMario/16;
		int EnemyXInMap = (int)(x/16);
		int EnemyYInMap = (int)(y/16);
		int nX = EnemyXInMap - MarioXInMap +11;
		int nY = EnemyYInMap - MarioYInMap +11;

		//			int EXp4 = (int)(x/16);
		//			int EYp4 = (int)(y/16);

		//			System.out.print("("+nX+","+nY+")");
		if ((nX >= 0) && (nX < 22) && (nY >= 0) && (nY < 22)) {
			System.out.print("[["+
					MarioXInMap+","+MarioYInMap+"|"+
					EnemyXInMap+","+EnemyYInMap+"|"+
					nY+","+nX+","+
					env.getEnemiesObservation()[nY][nX]+","+
					env.getLevelSceneObservation()[nY][nX]+"]]");
			byte result = env.getEnemiesObservation()[nY][nX];
			if (result == -1) {
				//					System.out.println("\t"+x+","+y+"\tMARIO:"+xMario+","+yMario);
				/*
					System.out.println("Enemy no change in x if -"+
							((x-EnemyXInMap*16)-1));
					System.out.println("Mario no change in x if -"+
							((xMario-MarioXInMap*16)-2));
					System.out.println("Mx:"+MarioXInMap*16+" Ex:"+EnemyXInMap*16);

					/*					System.out.println("Guess 1: add 6 to enemy.y");
					y += 6;
					nY = (int)(y/16) - MarioYInMap +11;
					if (nY >= 22) {
						System.out.println("OUT-OF-BOUNDS");
					} else */ 
				{
					byte temp = getDiamondWiggle(env, nX, nY,
							MarioXInMap, MarioYInMap, x, y, 1);
					if (temp != -1) return temp;
				}

				{
					byte temp = getRectWiggle(env, nX, nY,
							MarioXInMap, MarioYInMap, x, y, 1, 1 );					
					if (temp != -1) return temp;
				}

				{
					int bX = (int)((x-4)/16);
					int nBX = bX - MarioXInMap +11;
					if ((nBX >= 0) && (env.getEnemiesObservation()[nY][nBX] == 8))
						return 8;
					bX = (int)((x+4)/16);
					nBX = bX - MarioXInMap +11;
					if ((nBX < 22) && (env.getEnemiesObservation()[nY][nBX] == 8))
						return 8;
				}

				System.out.println(
						" v:"+((nY+1>21)?'-':env.getEnemiesObservation()[nY+1][nX])+
						" >:"+((nX+1>21)?'-':env.getEnemiesObservation()[nY][nX+1])+
				"]]");

				if (nX+1 < 22) { 
					result = env.getEnemiesObservation()[nY][nX+1];
					if ((result >= 2) && (result <= 10)) return result;
				}
				if (nY+1 < 22) {
					result = env.getEnemiesObservation()[nY+1][nX];
					if (result == 12) return result;
				}
				int EYm4 = (int)((y-4)/16);
				int nYm4 = EYm4 - MarioYInMap +11;
				byte ym4result = -1;
				if ((nYm4 != nY) && (nYm4 > 0)) {
					ym4result = env.getEnemiesObservation()[nYm4][nX];
					if ((env.getLevelSceneObservation()[nY][nX] == 0) &&
							(result == 2) || (result == 6) || (result == 9))
						return ym4result;
					if ((result == 3) || (result == 5) || 
							(result == 7) || (result == 10))
						return ym4result;
				}

				int EXm4 = (int)((x-4)/16);
				int nXm4 = EXm4 - MarioXInMap +11;
				if ((nXm4 != nX) && (nXm4 > 0)) {
					result = env.getEnemiesObservation()[nY][nXm4];							
					if ((result >= 2) && (result <= 10)) return result;
				}

				{
					byte temp = getDiamondWiggle(env, nX, nY,
							MarioXInMap, MarioYInMap, x, y, 2);
					if (temp != -1) return temp;
				}

				// The enemy may be dead at this point
				if ((ym4result >= 2) && (ym4result <= 10)) return ym4result;
				/*						int EXp5 = (int)((x+5)/16);
						int nXp5 = EXp5 - MarioXInMap +11;
						if ((nXp5 != nX) && (nXp5 > 0)) {
							result = env.getEnemiesObservation()[nY][nXp5];							
							if ((result >= 2) && (result <= 10)) return result;
						}
				 */						
				{
					byte temp = getDiamondWiggle(env, nX, nY,
							MarioXInMap, MarioYInMap, x, y, 3);
					if (temp != -1) return temp;
				}

				{
					byte temp = getRectWiggle(env, nX, nY,
							MarioXInMap, MarioYInMap, x, y, 2, 2 );					
					if (temp != -1) return temp;
				}

				System.out.println("Ex="+x+", Ey="+y);
				System.out.println("!!!!!!!!!!!!!!!!!!!!!");
				System.out.println("!!!!!!!!!!!!!!!!!!!!!");
				System.out.println("!!!!!!!!!!!!!!!!!!!!!");
				//					throw new java.lang.NullPointerException();
				return -1;
			}
			return result;
		}
		return -1;
	}	

	public void move(TheoreticMario mario, Frame frame) {
		//		System.out.println("MOVING "+enemies.size()+" ENEMIES");

		float xCam = mario.x - 160;
		float yCam = 0;
		if (xCam < 0) xCam = 0;
		//	        if (xCam > level.width * 16 - 320) xCam = level.width * 16 - 320;

		ListIterator<TheoreticEnemy> iter = enemies.listIterator();
		//		for (TheoreticEnemy enemy : enemies.enemies) {
		while (iter.hasNext()) {
			TheoreticEnemy enemy = iter.next();

			float xd = enemy.x - xCam;
			float yd = enemy.y - yCam;
			if ((xd < -64 || xd > 320 + 64 || yd < -64 || yd > 240 + 64) &&
					(enemy.nType != 13))
			{
				//				System.out.println("Removing enemy at "+enemy.x+","+enemy.y);
				iter.remove();
				continue;
			}

			//			if ( (enemy.x < mario.x - (12*16)) ||
			//					(enemy.y > 22*16) ) {
			//				iter.remove();
			//			} else {
			if (enemy.move(mario, frame)) {
//				System.out.println(" -=- Removing enemy -=-"+enemy.x+","+enemy.y);
				iter.remove();
			} else {
				//			enemy.path.push(new float[]{enemy.x, enemy.y});
			}
			//			}
		}
	}
	public boolean isMushroomPresent() {
		boolean result = false;
		for (TheoreticEnemy e : enemies)
			if (e instanceof TheoreticMushroom)
				result = true;
		return result;
	}

	public void dump() {
		for (TheoreticEnemy e : enemies) {
			System.out.print("["+e.nType+","+e.x+","+e.y+"] ");
		}
		System.out.println();
	}
}
