package competition.cig.peterlawford.simulator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import competition.cig.peterlawford.PeterLawford_SlowAgent;

import ch.idsia.mario.engine.level.Level;

public class Frame {

	public static long marioTime = 0;
	public static long simTime = 0;

	public static long max_marioTime = 0;
	public static long max_simTime = 0;

	public final PeterLawford_SlowAgent agent;
	public final TheoreticMario mario;
	public final TheoreticEnemies enemies;
	private final TheoreticLevel level;

	//		public int nCycleId;

	private LinkedList<TheoreticShell> shellsToCheck = null;
	private LinkedList<TheoreticFireball> fireballsToCheck = null;

	public int fireballsOnScreen = 0;
	private int tick;
	public int getTick() { return tick; }
	public void decrementTick() { tick--; }
	
	private final boolean fIsReal;
	
	public Frame(PeterLawford_SlowAgent agent,
			TheoreticMario mario,
			TheoreticEnemies enemies,
			TheoreticLevel level,
			int fireballsOnScreen,
			int tick) {
		this.mario = mario;
		this.agent = agent;
		this.enemies = enemies;
		this.level = level;
		this.fireballsOnScreen = fireballsOnScreen;
		this.tick = tick;
		fIsReal = true;
		//			nCycleId = 0;
	}

	public Frame(Frame in, boolean fDebug) {
		this(in, fDebug, false);
	}

	public Frame(Frame in, boolean fDebug, boolean fSecretDebug) {
		agent = in.agent;
		// The enemies can see and react to mario
		enemies = new TheoreticEnemies(in.enemies, fDebug, fSecretDebug);
		mario = new TheoreticMario(in.mario, enemies, fSecretDebug);
		level = in.level;
		fireballsOnScreen = in.fireballsOnScreen;
		tick = in.tick;
		fIsReal = fSecretDebug;
		//			nCycleId = in.nCycleId;
	}

	public void checkShellCollide(TheoreticShell shell) {
		if (shellsToCheck == null) 
			shellsToCheck = new LinkedList<TheoreticShell>();
		shellsToCheck.add(shell);
		//		throw new java.lang.NullPointerException();
	}
	public void checkFireballCollide(TheoreticFireball fireball) {
		//		System.out.println("Registering fireball");
		if (fireballsToCheck == null) 
			fireballsToCheck = new LinkedList<TheoreticFireball>();
		fireballsToCheck.add(fireball);
		//		throw new java.lang.NullPointerException();
	}

	private LinkedList<TheoreticEnemy> fireCannon(float xCam, float yCam, int tick, 
			TheoreticEnemies enemies, float mario_x, TheoreticMario mario) {
		LinkedList<TheoreticEnemy> result = null;
		
        for (int x = (int) xCam / 16 - 1; x <= (int) (xCam + 320) / 16 + 1; x++)
            for (int y = (int) yCam / 16 - 1; y <= (int) (yCam + 240) / 16 + 1; y++)
            {
                int dir = 0;

                if (x * 16 + 8 > mario.x + 16) dir = -1;
                if (x * 16 + 8 < mario.x - 16) dir = 1;
/*
                SpriteTemplate st = level.getSpriteTemplate(x, y);

                if (st != null)
                {
                    if (st.lastVisibleTick != tick - 1)
                    {
                        if (st.sprite == null || !sprites.contains(st.sprite))
                        {
                            st.spawn(this, x, y, dir);
                        }
                    }

                    st.lastVisibleTick = tick;
                }
*/
                if (dir != 0)
                {
                     byte b = level.getBlock(x, y);
                    if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_ANIMATED) > 0)
                    {
                        if ((b % 16) / 4 == 3 && b / 16 == 0)
                        {
                            if ((tick - x * 2) % 100 == 0)
                            {
 //                               xCannon = x;
//                                for (int i = 0; i < 8; i++)
//                                {
 //                                   addSprite(new Sparkle(x * 16 + 8, y * 16 + (int) (Math.random() * 16), (float) Math.random() * dir, 0, 0, 1, 5));
 //                               }
 //                               addSprite(new BulletBill(this, x * 16 + 8 + dir * 8, y * 16 + 15, dir));
 
                            	
                            	if (result == null)
                            		result = new LinkedList<TheoreticEnemy>();
                            	TheoreticBulletBill bill = new TheoreticBulletBill(
            							enemies, x * 16 + 8 + dir * 8, y * 16 + 15, dir);
                            	bill.move(mario, this);
                            	result.addFirst(bill);
 
 //                           	if (fIsReal)
 //                               System.out.print("TCANNON:"+xCam+","+b+","+tick+
 //                               		","+mario_x);
                            	
                            	//                               hasShotCannon = true;
                            }
                        }
                    }
                }
            }
        
        return result;
	}
	
	public void move(boolean[] keys) {
		//			nCycleId++;

		if (mario.status == TheoreticMario.STATUS_WIN) {
			if (enemies.fSecretDebug) PeterLawford_SlowAgent.logger.info("MARIO HAS WON!");
			return;
		}
		

		long time_t = System.currentTimeMillis();

		mario.keys = keys;
		boolean fPause = mario.fPauseWorld;

		float xCam = mario.x - 160;
		float yCam = 0;

		if (xCam < 0) xCam = 0;
		//        if (xCam > level.width * 16 - 320) xCam = level.width * 16 - 320;

		fireballsOnScreen = 0;
		//		for (TheoreticEnemy e : enemies.enemies)
		//			if (e instanceof TheoreticFireball) fireballsOnScreen++;

		ListIterator<TheoreticEnemy> iterT = enemies.enemies.listIterator();
		//		for (TheoreticEnemy e : enemies.enemies) {
		while (iterT.hasNext()) {
			TheoreticEnemy e = iterT.next();
			float xd = e.x - xCam;
			float yd = e.y - yCam;
			if (xd < -64 || xd > 320 + 64 || yd < -64 || yd > 240 + 64) {
				iterT.remove();
			} else {
				if (e instanceof TheoreticFireball)
					fireballsOnScreen++;
			}
		}


		if (fireballsOnScreen > 2) {
			System.err.println("Too many fireballs on screen:"+fireballsOnScreen);
			throw new java.lang.NullPointerException();
		}

		LinkedList<TheoreticEnemy> new_enemies = null;
		if (!fPause) {
			tick++;
			new_enemies = fireCannon(xCam, yCam, tick, enemies, mario.x, mario);
			
			enemies.move(mario, this);
		} else {
			if (enemies.fSecretDebug) System.out.println("T-NOTICK");
		}

		{
			long time_m = System.currentTimeMillis();
			// Move mario last
			mario.move(enemies, this);
			time_m = System.currentTimeMillis() - time_m;
			marioTime += time_m;
			if (time_m > max_marioTime) max_marioTime = time_m;
		}

		if (!fPause) {
			//				for (TheoreticEnemy enemy : enemies.enemies) {

			// If mario stomps a shell before colliding with an enemy he's safe,
			// but the other way round he gets hurt.

			// because of this, new enemies MUST be added to the front of the list
//			LinkedList<TheoreticEnemy> new_enemies = null;
						Iterator<TheoreticEnemy> iterEnemies = enemies.enemies.iterator();
						while (iterEnemies.hasNext()) {
							TheoreticEnemy enemy = iterEnemies.next();
//			for (TheoreticEnemy enemy : enemies.enemies) {
				if (enemy instanceof TheoreticFireball) continue;

				TheoreticEnemy result = enemy.collideCheck(mario, this);
				if (result != null) {
					if (result == enemy) {
						//enemies.enemies.remove(result);
						iterEnemies.remove();
					} else {
					if (new_enemies == null) new_enemies = new LinkedList<TheoreticEnemy>();
					new_enemies.add(result);
					}
				}
			}	

			if (agent.level.isMarioOverFireflower(mario.x, mario.y, mario.height,
					enemies.fSecretDebug)) {
				if (enemies.fSecretDebug) System.out.println("MARIO IS OVER A FIREFLOWER!");
				mario.getFlower();
			}
			
			
			if (shellsToCheck != null) {
				if (enemies.fDebug)
					System.out.println("=== Checking for shell collision ===");
				for (TheoreticShell shell : shellsToCheck)
					for (TheoreticEnemy sprite : enemies.enemies) {
						if (sprite instanceof TheoreticFireball) continue;
						if ((sprite != shell) && !shell.dead)
							if (sprite.shellCollideCheck(shell, mario))
							{
								if (enemies.fDebug) System.out.println("SHELL-COLLISION");

								if ((mario.carried == shell) && !shell.dead)
								{
									mario.carried = null;
									shell.die();
								}
							}
					}

				shellsToCheck = null;
			}

			if (fireballsToCheck != null) {
				for (TheoreticFireball fireball : fireballsToCheck)
					for (TheoreticEnemy sprite : enemies.enemies) {
						if (sprite instanceof TheoreticFireball) continue;
						if (!fireball.dead) {
							if (sprite.fireballCollideCheck(fireball)) {
								//								System.out.println("somebody killed fireball");
								fireball.die();
							}
						}
					}
				fireballsToCheck= null;
			}

			if (new_enemies != null)
				enemies.enemies.addAll(0, new_enemies);

		} else {
			if (enemies.fDebug)
			System.out.println(" - skipping enemies due to pain - ");
		}

		mario.fPauseWorld = mario.fNextPauseWorld;

		simTime += (System.currentTimeMillis() - time_t);

	}

	@Override
	public boolean equals(Object o) {
		Frame f_other = (Frame)o;

		if ((mario.getX() != f_other.mario.getX()) ||
				(mario.getY() != f_other.mario.getY()) ||
				(mario.xa != f_other.mario.xa) ||
				(mario.ya != f_other.mario.ya)) {
//			if (mario.getX() != f_other.mario.getX()) System.err.print("Mx");
//			if (mario.getY() != f_other.mario.getY()) System.err.print("My");
//			if (mario.xa != f_other.mario.xa) System.err.print("Mxa");
//			if (mario.ya != f_other.mario.ya) System.err.print("Mya");
			return false;
		}
		if (enemies != null) {
			if (!enemies.equals(f_other.enemies)) return false;
		} else {
			if (f_other.enemies != null) return false;
		}

		return true;
	}
	
	public void dump() {
		mario.dump();
		enemies.dump();
	}
}
