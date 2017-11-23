package competition.cig.peterlawford.simulator;

import competition.cig.peterlawford.PeterLawford_SlowAgent;
import competition.cig.peterlawford.visualizer.Visualizer;



public class TheoreticEnemy extends TheoreticSprite {

	public static final boolean DEBUG_REPOSITIONING = false;

	boolean fDebug;
	final boolean fSecretDebug;

	public boolean winged;
	public int facing = -1;

	boolean onGround = false;
	private boolean mayJump = false;

	public final boolean avoidCliffs;

	int width = 4;
	final int height;

	int deadTime = 0;

	final TheoreticLevel level;
	final TheoreticEnemies enemies;

	boolean fDirKnown = false;
	boolean flyDeath = false;
	boolean noFireballDeath = false;

	boolean fDirIdentified = false;
	boolean fInitializingMove = false;
	boolean fBeyondHorizonOnGround = true;

	
	public boolean fMatched = false;
	

	// nType == -1 means unknown type
	public TheoreticEnemy(
			TheoreticEnemies enemies, TheoreticLevel level, TheoreticMario mario,
			byte nType, float x, float y, float xa, float ya, boolean winged
	) {

		super(nType, x, y, xa, ya);

		this.enemies = enemies;
		fDebug = (enemies == null) ? false : enemies.fDebug;
		fSecretDebug = (enemies == null) ? false : enemies.fSecretDebug;

		this.level = level;

		this.winged = winged;
		avoidCliffs = isCliffAvoiding(nType);
		height = getHeight(nType);
		noFireballDeath = getFlameproof(nType);
	
		// Enemies will always start off moving left?
//		boolean fMoveLeft = (16*((int)((x+1.75-8)%16)) == x+1.75-8);
//		boolean fMoveRight = (16*((int)((x-1.75-8)%16)) == x-1.75-8);

		if ((nType >= 2) && (nType <= 10) && (nType != 8)) {
			if (!winged) onGround = ( 16*(int)(ya / 16) == ya );
			this.x += 1.75;
			fInitializingMove = true;
			move(mario, null);
			if ((this.x != x) || (this.y != y)) {
				PeterLawford_SlowAgent.logger.severe("TRUE:"+x+","+y+" GUESS:"+this.x+","+this.y);
				throw new java.lang.NullPointerException();
			}
			fInitializingMove = false;
		}
	}
	private static boolean isCliffAvoiding(byte nType) {
		if (nType == Visualizer.ENEMY_GOOMBA) return false;
		if (nType == Visualizer.ENEMY_FLYING_GOOMBA) return false;
		if (nType == Visualizer.ENEMY_GREEN_KOOPA) return false;
		if (nType == Visualizer.ENEMY_FLYING_GREEN_KOOPA) return false;
		if (nType == Visualizer.ENEMY_SPINY) return false;
		if (nType == Visualizer.ENEMY_FLYING_SPINY) return false;
		return true;
	}
	private static int getHeight(byte nType) {
		if (nType >= 4 && nType < 8) return 24;
		return 12;
	}
	private static boolean getFlameproof(byte nType) {
		if (nType == Visualizer.ENEMY_BULLET) return true;
		if (nType == Visualizer.ENEMY_FLYING_SPINY) return true;
		if (nType == Visualizer.ENEMY_SPINY) return true;
		return false;
	}

	public TheoreticEnemy(TheoreticEnemies enemies, TheoreticEnemy in) {

		super(in.nType, in.x, in.y, in.xa, in.ya);

		this.enemies = enemies;
		fDebug = (enemies == null) ? true : enemies.fDebug;
		fSecretDebug = (enemies == null) ? false : enemies.fSecretDebug;

		level = in.level;
		facing = in.facing;

		mayJump = in.mayJump;
		winged = in.winged;
		avoidCliffs = in.avoidCliffs;
		fDirKnown = in.fDirKnown;
		onGround = in.onGround;

		deadTime = in.deadTime;
		//		fIsDead = in.fIsDead;
		flyDeath = in.flyDeath;
		fDirIdentified = in.fDirIdentified;

		height = getHeight(nType);
		noFireballDeath = getFlameproof(nType);

		fBeyondHorizonOnGround = in.fBeyondHorizonOnGround;
	}
/*
	public boolean isThisMe(byte nType, float x, float y) {
		if ((nType != this.nType) && (this.nType != -1)) return false;

		// We're dead
		if ((x == nXP+2) && (y == nYP - 5))
			return true;

		if (winged) {
			if ( (Math.abs(x-this.x)>4) ||
					(Math.abs(y-this.y)>12) ) {
				// We've been stomped
				if ((nType == 4) || (nType == 6) && (y == this.y-5)) {
					return true;
				}
				return false;
			}
		} else {
			if ( (Math.abs(x-this.x)>4) ||
					((y-this.y>4) || (this.y-y>4)) ) {
				// We've been stomped
				if ((nType == 4) || (nType == 6) && (y == this.y-5)) {
					return true;
				}
				return false;
			}			
		}


		if (this.nType == -1) this.nType = nType;

		return true;
	}
*/
	private static final float sideWaysSpeed = 1.75f;

	boolean fIsBeyondHorizon = false;

	float nXAP;
	float nYAP;	// = (winged) ? -10 : 2;

	public boolean move(TheoreticMario mario, Frame frame) {
		if ((nType == Visualizer.SHELL) && (nXP == 0) && (nYP == 0)) {
			return false;
		}

		if (xOld == -1) {
			xOld = x; yOld =y;
		} else {
			xOld = nXP; yOld = nYP;
		}
		nXP = x; nYP = y; nYAP = ya; nXAP = xa;

		//        float sideWaysSpeed = onGround ? 2.5f : 1.2f;

		if (deadTime > 0) {

			deadTime--;

			if (deadTime == 0)
			{
				deadTime = 1;
				return true;
			}

			if (flyDeath) {
				x += xa;
				y += ya;
				ya = ya * 0.95f + 1;
			}
			//			ya += 1;

			if (xa > 30) throw new java.lang.NullPointerException();

			if (fDebug) 
				System.out.println(" &&dead"+
						((winged)?"W":"")+((mayJump)?"J":"")+((onGround)?"g":"")+
						((avoidCliffs)?"a":"")+
						":"+nXP+","+nYP+" => "+x+","+y+","+ya+" ");
			if (fSecretDebug) 
				PeterLawford_SlowAgent.logger.info(" &&dead"+
						((winged)?"W":"")+((mayJump)?"J":"")+((onGround)?"g":"")+
						((avoidCliffs)?"a":"")+
						":"+nXP+","+nYP+" => "+x+","+y+","+ya+"::"+deadTime+"\n");

			return false;
		}

		if (xa > 2)
		{
			facing = 1;
		}
		if (xa < -2)
		{
			facing = -1;
		}

		xa = facing * sideWaysSpeed;

		mayJump = (onGround);

		//        runTime += (Math.abs(xa)) + 5;

		if (mario == null) System.out.println("MERIO is NULL");
		if (mario != null) {
			// The sequence of events is:
			// 1. enemy initialization, 2. move enemies, 3. move mario
			// The collision grid is based on marios position before the move
//			float mario_x = (!fInitializingMove) ? mario.nXP : mario.x;
//			float mario_y = (!fInitializingMove) ? mario.nYP : mario.y;
			float mario_x = mario.x; float mario_y = mario.y;
			int nX = (int)(nXP/16)-(int)(mario_x/16) +11;
			int nY = (int)(nYP/16)-(int)(mario_y/16) +11;
			fIsBeyondHorizon = ((nX<0) || (nY<0) || (nX > 21) || (nY > 21));	
			if (fIsBeyondHorizon && fSecretDebug)
				PeterLawford_SlowAgent.logger.info("{"+nXP+","+nYP+"/"+nX+","+nY+"}");
		}

		if (!move(xa, 0)) facing = -facing;
		onGround = false;

		move(0, ya);

		ya *= winged ? 0.95f : 0.85f;
		xa *= ((onGround) ? GROUND_INERTIA : AIR_INERTIA);

		if (!onGround)
		{
			if (winged)
			{
				ya += 0.6f;
			}
			else
			{
				ya += 2;
			}
		}
		else if (winged)
		{
			ya = -10;
		}

		if (fDebug) 
			System.out.println(" &&"+
					((winged)?"W":"")+((mayJump)?"J":"")+((onGround)?"g":"")+
					((avoidCliffs)?"a":"")+
					":"+nXP+","+nYP+","+nXAP+","+nYAP+" => "+x+","+y+","+xa+","+ya+" ");
		if ((frame != null) && (frame.enemies.fSecretDebug))
			PeterLawford_SlowAgent.logger.info(" &&"+
					((winged)?"W":"")+((mayJump)?"J":"")+((onGround)?"g":"")+
					((avoidCliffs)?"a":"")+
					":"+nXP+","+nYP+","+nXAP+","+nYAP+" => "+x+","+y+","+xa+","+ya+"\n");

		return false;
	}

	private boolean move(float xa, float ya)
	{

		while (xa > 8)
		{
			if (!move(8, 0)) return false;
			xa -= 8;
		}
		while (xa < -8)
		{
			if (!move(-8, 0)) return false;
			xa += 8;
		}
		while (ya > 8)
		{
			if (!move(0, 8)) return false;
			ya -= 8;
		}
		while (ya < -8)
		{
			if (!move(0, -8)) return false;
			ya += 8;
		}

		boolean collide = false;
		if (ya > 0)
		{
			if (!winged && fIsBeyondHorizon && fBeyondHorizonOnGround) {
				collide = true;
			}
			if (isBlocking(x + xa - width, y + ya, xa, 0)) collide = true;
			else if (isBlocking(x + xa + width, y + ya, xa, 0)) collide = true;
			else if (isBlocking(x + xa - width, y + ya + 1, xa, ya)) collide = true;
			else if (isBlocking(x + xa + width, y + ya + 1, xa, ya)) collide = true;			
		}
		if (ya < 0)
		{
			if (isBlocking(x + xa, y + ya - height, xa, ya)) collide = true;
			else if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
			else if (collide || isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;

			if (winged && fIsBeyondHorizon) collide = false;		
		}
		if (xa > 0)
		{
			if (isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;
			if (isBlocking(x + xa + width, y + ya - height / 2, xa, ya)) collide = true;
			if (isBlocking(x + xa + width, y + ya, xa, ya)) collide = true;

			if (avoidCliffs && onGround && 
					!level.isBlocking((int) ((x + xa + width) / 16), (int) ((y) / 16 + 1), xa, 1)) collide = true;

			if (fIsBeyondHorizon) collide = false;
		}
		if (xa < 0)
		{
			if (isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
			if (isBlocking(x + xa - width, y + ya - height / 2, xa, ya)) collide = true;
			if (isBlocking(x + xa - width, y + ya, xa, ya)) collide = true;

			if (avoidCliffs && onGround && 
					!level.isBlocking((int) ((x + xa - width) / 16),
							(int) ((y) / 16 + 1), xa, 1)) {
				if (fSecretDebug) PeterLawford_SlowAgent.logger.info("CLIFF");
				collide = true;
			}

			if (fIsBeyondHorizon) {
				if (fSecretDebug) PeterLawford_SlowAgent.logger.info(" O-O-B ");
				collide = false;
			}
			if (collide && fDebug) System.out.println("Ecollide:-x:"+
					x+","+y+","+xa+","+ya+","+width+height);
		}

		if (collide)
		{
			if (xa < 0)
			{
				if (fSecretDebug) PeterLawford_SlowAgent.logger.info("<-X>");
				x = (int) ((x - width) / 16) * 16 + width;
				//				System.out.println("Tenem-coll:-x="+x+", w="+width+" h="+height+" xa="+xa);
				this.xa = 0;
			}
			if (xa > 0)
			{
				if (fSecretDebug) PeterLawford_SlowAgent.logger.info("<+X>");
				x = (int) ((x + width) / 16 + 1) * 16 - width - 1;
				//				System.out.println("Tenem-coll:x="+x);
				this.xa = 0;
			}
			if (ya < 0)
			{
				y = (int) ((y - height) / 16) * 16 + height;
				//                jumpTime = 0;
				this.ya = 0;
			}
			if (ya > 0)
			{
				//				System.out.println("TE-onground");
				y = (int) (y / 16 + 1) * 16 - 1;
				onGround = true;
			}
			return false;
		}
		else
		{
			x += xa;
			y += ya;
			return true;
		}
	}

	private boolean isBlocking(float _x, float _y, float xa, float ya)
	{
		//    	System.out.println("%"+_x+","+_y+"% ");

		int x = (int) (_x / 16);
		int y = (int) (_y / 16);
		if (x == (int) (this.x / 16) && y == (int) (this.y / 16)) return false;

		boolean blocking = level.isBlocking(x, y, xa, ya);

		byte block = level.getBlock(x, y);

		if (blocking && fDebug)
			System.out.print("["+block+","+x+","+y+"]");
		//if (blocking && fSecretDebug)
					if (fSecretDebug)
			PeterLawford_SlowAgent.logger.info("["+block+","+x+","+y+"]");

		return blocking;
	}


	public TheoreticEnemy collideCheck(TheoreticMario mario, Frame frame) {
		TheoreticEnemy result = null;

		if (deadTime != 0) return null;

		float xMarioD = mario.x - x;
		float yMarioD = mario.y - y;
		if (fDebug) System.out.print("E-check("+mario.y+"-"+y+"="+yMarioD+
				","+nType+","+mario.ya+","+mario.onGround+","+mario.wasOnGround+
				","+xMarioD+","+mario.x+","+x+","+height);

		float w = 16;
		if (xMarioD > -width*2-4 && xMarioD < width*2+4)
		{
			if (fDebug) System.out.print("^#");	
			if (yMarioD > -height && yMarioD < mario.height)
			{
				if (fDebug) System.out.print("v");	
				//				System.out.println("TCOL-E!(E:"+nType+","+x+","+y+" D:"+xMarioD+","+yMarioD+",w"+width+" h"+height);

				if (nType != Visualizer.ENEMY_SPINY && nType != 10 && nType != 12 &&
						mario.ya > 0 && 
						yMarioD <= 0 && (!mario.onGround || !mario.wasOnGround))
				{
					mario.stomp(this);
					if (winged)
					{
						if (fDebug) System.out.println("T-UNWING");
						if (fSecretDebug)
							PeterLawford_SlowAgent.logger.info("T-UNWING\n");
						winged = false;
						ya = 0;
					}
					else
					{
						if (fSecretDebug)
							PeterLawford_SlowAgent.logger.info("T-KILL\n");

						deadTime = 10;
						winged = false;

						if ((nType == Visualizer.ENEMY_RED_KOOPA) ||
								(nType == Visualizer.ENEMY_GREEN_KOOPA) ||
								(nType == Visualizer.ENEMY_FLYING_RED_KOOPA) ||
								(nType == Visualizer.ENEMY_FLYING_GREEN_KOOPA) ) {
							TheoreticShell shell = new TheoreticShell(enemies,
									level, x, y);
							//							iterEnemies.add(shell);
							shell.move(mario, frame);
							result = shell;
						}
					}
				}
				else
				{
					mario.getHurt();
				}
			}
		}

		return result;
	}

	public boolean shellCollideCheck(TheoreticShell shell, TheoreticMario mario)
	{
		if (fDebug) System.out.println("enemy::shellCollideCheck("+this.nType);
		if (fSecretDebug) 
			PeterLawford_SlowAgent.logger.info("enemy::shellCollideCheck("+x+","+y+"\n");

		if (deadTime != 0) return false;

		float xD = shell.x - x;
		float yD = shell.y - y;

		if (xD > -16 && xD < 16)
		{
			if (yD > -height && yD < shell.height)
			{
				if (fSecretDebug) 
					PeterLawford_SlowAgent.logger.info("enemy::shellCollideCheck(HIT\n");

				xa = shell.facing * 2;
				ya = -5;
				flyDeath = true;
				deadTime = 100;
				winged = false;
				return true;
			}
		}
		return false;
	}

	public boolean fireballCollideCheck(TheoreticFireball fireball)
	{
		if (deadTime != 0) return false;

		float xD = fireball.x - x;
		float yD = fireball.y - y;

		if (xD > -16 && xD < 16)
		{
			if (fSecretDebug) PeterLawford_SlowAgent.logger.info("fire-x");
			
			if (yD > -height && yD < fireball.height)
			{
				if (fDebug)
					System.out.println("Enemy "+nType+" was hit by fireball at "+
							x+","+y+","+noFireballDeath);
				if (fSecretDebug) 
					PeterLawford_SlowAgent.logger.info("enemy::fireballCollideCheck(HIT\n");

				if (noFireballDeath) return true;

				xa = fireball.facing * 2;
				ya = -5;
				flyDeath = true;
				deadTime = 100;
				winged = false;
				return true;
			}
		}
		return false;
	}

	public void bumpCheck(TheoreticMario mario, int xTile, int yTile)
	{
		if (deadTime != 0) return;

		if (x + width > xTile * 16 && x - width < xTile * 16 + 16 && yTile == (int) ((y - 1) / 16))
		{
			xa = -mario.facing * 2;
			ya = -5;
			flyDeath = true;
			//           if (spriteTemplate != null) spriteTemplate.isDead = true;
			deadTime = 100;
			winged = false;
			//          hPic = -hPic;
			//          yPicO = -yPicO + 16;
		}
	}


	public boolean fixupDefinite(byte nType, float x, float y, boolean fFinalize) {
		boolean result = fixupDefinite_i(nType, x, y, fFinalize);
		if (xa > 30) throw new java.lang.NullPointerException();
		return result;
	}
	private boolean fixupDefinite_i(byte nType, float x, float y, boolean fFinalize) {
		if (nType != this.nType) return false;

		//		if (fDebug)
		//		System.out.println("fixing "+this.nType+","+this.x+","+this.y+
		//				" => "+nType+","+x+","+y);

		if ((Math.abs(x-this.x) > 30) || (Math.abs(y-this.y) > 30))
			return false;

		if ((y==nYP) && (x == nXP - 1.75) && (deadTime > 0)) {
			if (!fFinalize) return true;
			if (DEBUG_REPOSITIONING) {
				System.out.print("Resurrecting!"+nType+","+x+","+y);
				if (fSecretDebug)
					PeterLawford_SlowAgent.logger.info("Resurrecting!"+nType+","+x+","+y+"\n");
			}
			deadTime = 0;
			this.x = x; this.y = y;
			xa = -1.75f; xa *= 0.89;
			this.ya = nYAP;
			ya *= winged ? 0.95f : 0.85f;
			return true;
			//			throw new java.lang.NullPointerException();			
		}

		
		boolean fFlyingKilledMoveXDir = (Math.abs(x-nXP) == 2);
		
		// We were killed
		if (fFlyingKilledMoveXDir && (y == nYP-5)) {
			if (!fFinalize) return true;
			if (DEBUG_REPOSITIONING) System.out.print("WE ARE DEAD2 ");
			this.x = x; this.y = y;
			ya = -3.75f;
			xa = x-nXP;
			if (xa != 0) facing = (xa>0)?1:-1;
			deadTime = 99; flyDeath = true;
			return true;
		}
		if (fFlyingKilledMoveXDir && (y == nYP+nYAP)) {
			if (!fFinalize) return true;
			if (DEBUG_REPOSITIONING) {
				System.out.print("WE ARE DEAD ");
				if (fSecretDebug)
					PeterLawford_SlowAgent.logger.info("WE ARE DEAD\n");
			}
			this.x = x; this.y = y;
			ya = nYAP;			
			ya = ya * 0.95f + 1;
			xa = 2;
			deadTime = 99; flyDeath = true;
			return true;
		}
		if (fFlyingKilledMoveXDir && (nYP-y > 5) && (nYP-y < 6)) {
			if (!fFinalize) return true;
			if (DEBUG_REPOSITIONING) {
				System.out.print("WE ARE DEAD3");
				if (fSecretDebug)
					PeterLawford_SlowAgent.logger.info("WE ARE DEAD3\n");
			}
			this.x = x; this.y = y;
			ya = -3.75f;
			xa = x-nXP;
			if (xa != 0) facing = (xa>0)?1:-1;
			deadTime = 99; flyDeath = true;
			return true;
		}


		boolean fStandardXStep = (Math.abs(x-nXP)==1.75);
		
		
		if (deadTime == 0) {
			if ( (Math.abs(y - nYP + nYAP) <= 0.00001) && (x == this.x)) {
				if (!fFinalize) return true;
				if (DEBUG_REPOSITIONING) {
					System.out.print("MINOR CORRECTION ");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("MINOR CORRECTION\n");
				}
				this.y = y; onGround = false;

				ya = nYAP;
				ya *= winged ? 0.95f : 0.85f;
				ya += winged ? 0.6 : 2;
				//			System.out.println("Simple fix: "+ya);
				return true;
			}

			// We thought we had collided horizontally while travelling on ground, but we hadn't
			if (fStandardXStep && (Math.abs(this.x-x) == 2*1.75) &&
					((y == this.y) || (y == nYP))) {
				if (!fFinalize) return true;
				if (DEBUG_REPOSITIONING) {
					System.out.print("Maybe reversing ");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("Maybe reversing\n");
				}
				this.x = x; this.y = y;
				xa = x-nXP;
				if (xa != 0) facing = (xa>0)?1:-1;
				xa *= 0.89;
				return true;
			}

			// We thought we had collided vertically, but we hadn't
			// (I.e. we didn't collide with the ground so we're falling
			if ((y == nYP + nYAP) && (x == this.x)) {
				if (!fFinalize) return true;
				if (DEBUG_REPOSITIONING) {
					System.out.print("FALLING ");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("FALLING\n");
				}
				onGround = false; fBeyondHorizonOnGround = false;
				this.y = y;
				ya = nYAP;
				ya *= winged ? 0.95f : 0.85f;
				ya += winged ? 0.6 : 2;
				//			System.out.println("FIXUP falling: ya:"+nYAP+" => "+ya);
				return true;
			}
			if ((y == nYP + nYAP) && fStandardXStep) {
				if (!fFinalize) return true;
				if (DEBUG_REPOSITIONING) {
					System.out.print("FALLING2");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("FALLING2\n");
				}
				onGround = false; fBeyondHorizonOnGround = false;
				this.x = x;
				xa = x-nXP;
				if (xa != 0) facing = (xa>0)?1:-1;
				xa *= 0.89;
				this.y = y;
				ya = nYAP;
				ya *= winged ? 0.95f : 0.85f;
				ya += winged ? 0.6 : 2;
				//			System.out.println("FIXUP falling: ya:"+nYAP+" => "+ya);
				return true;
			}
			if ((x == this.x) && (y == nYP+2)) {
				if (!fFinalize) return true;
				if (DEBUG_REPOSITIONING) {
					System.out.print("START FALLING ");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("START FALLING\n");
				}
				onGround = false; fBeyondHorizonOnGround = false;
				this.y = y;
				ya = 2;
				ya *= 0.85f;			
				ya += 2;
				return true;
			}

			// We thought we had collided horizontally but we hadn't
			if (fStandardXStep && (y == nYP)) {
				if (!fFinalize) return true;
				if (DEBUG_REPOSITIONING) {
					System.out.print("NO HORIZ COLLISION");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("NO HORIZ COLLISION\n");
				}
				this.x = x; this.y = y;
				xa = x-nXP;
				if (xa != 0) facing = (xa>0)?1:-1;
				xa *= 0.89;

				ya = nYAP;
				ya *= winged ? 0.95f : 0.85f;

				return true;
			}
			
			boolean fXCollision = (Math.abs(x-nXP) > (winged?16:11)) ? false :
				(16*(int)((x-width)/16) == x-width) || (16*(int)((x+5)/16) == x+5);
			if (fXCollision && DEBUG_REPOSITIONING && fSecretDebug)
					PeterLawford_SlowAgent.logger.info("X-COLLISION\n");
			
			boolean fGroundCollision = (16*(int)((y+1)/16) == y+1);
			boolean fStartFlying = (y == nYP-10);
				
			
			/* This is too likely to be a mistaken identity, moved to probable
		// We didn't realize we had collided with something in the x-dir
		if ((y == this.y) && fXCollision) {
			this.x = x;
			xa = 0;
			//			throw new java.lang.NullPointerException();
			return true;
		}
		if ((y == nYP+nYAP) && fXCollision) {
			System.out.print("FALLING X-COLLISION ");
			onGround = false; fBeyondHorizonOnGround = false;
			this.y = y; this.x = x;
			xa = 0; ya = nYAP; facing = -facing;
			ya *= 0.85f;			
			ya += 2;
			return true;
		}
			 */

			// We have wings but are at a point in our cycle when we're on the ground
			if (winged && (x == this.x) && (y == nYP)) {
				if (!fFinalize) return true;
				if (DEBUG_REPOSITIONING) {
					System.out.print("WINGED-STAY ON GROUND ");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("WINGED-STAY ON GROUND\n");
				}
				this.y = y; 
				onGround = true; // mayJump = true; // For the next cycle
				ya = -10;
				//			ya *= 0.89f;
				return true;			
			}

			// We were falling and then collided with the ground but didn't notice
			if ((Math.abs(y-nYP) <= 11) && (x == this.x)) {
				if ( (16*(int)((y+4)/16) == y+4) || (16*(int)((y+8)/16) == y+8) ||
						(16*(int)((y+1)/16) == y+1)) {
					if (!fFinalize) return true;
					if (DEBUG_REPOSITIONING) {
						System.out.print("FALLING Y-COL ");
						if (fSecretDebug)
							PeterLawford_SlowAgent.logger.info("FALLING Y-COL\n");
					}
					this.y = y; onGround = (!winged);
					if (nYAP < 0) {
						ya = winged ? 0.6f : 2f;
					} else {
						ya = nYAP;
						ya *= winged ? 0.95f : 0.85f;
					}
//					ya += winged ? 0.6 : 2;  (No longer falling)
					return true;
				}
			}

			// We were flying and then collided with the ground but didn't notice
			if (winged && (x == this.x) && fGroundCollision &&
					Math.abs(this.y-nYP) <= 11) {
				if (!fFinalize) return true;
				if (DEBUG_REPOSITIONING) {
					System.out.print("FLYING -> GROUND ");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("FLYING -> GROUND ");
				}
				this.y = y; onGround = true;
				ya = -10;
				ya *= winged ? 0.95f : 0.85f;
//				ya += winged ? 0.6 : 2;
				return true;
			}

			boolean fWrongXDir = ((x-nXP) == -(this.x-nXP));
			
			// We guessed the wrong direction
			if ( (y == this.y) && fWrongXDir) {
				if (!fFinalize) return true;
				if (DEBUG_REPOSITIONING) {
					System.out.print("WRONG DIR ");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("WRONG DIR\n");
				}
				this.x += 2*(x-nXP);
				xa = -xa; facing = -facing;
				return true;
			}
			// We guessed the wrong direction, and we were flying and hit the ground
			if (winged && fWrongXDir && fGroundCollision) {
				this.x += 2*(x-nXP);
				xa = -xa; facing = -facing;

				this.y = y; onGround = true;
				ya = -10;				
			}
			
			// We thought we couldn't fly but we can
			if ((x == this.x) && fStartFlying && !winged) {
				if (!fFinalize) return true;
				// We can fly!
				if (DEBUG_REPOSITIONING) {
					System.out.print("WE CAN FLY ");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("WE CAN FLY\n");
				}
				this.y = y;
				onGround = false; winged = true;
				ya = -10;
				ya *= winged ? 0.95f : 0.85f;
				ya += winged ? 0.6 : 2;
				// throw new java.lang.NullPointerException();
				return true;
			}
			if (fStandardXStep && fStartFlying && winged) {
				if (!fFinalize) return true;
				if (DEBUG_REPOSITIONING) {
					System.out.print("WE ARE FLYING");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("WE ARE FLYING\n");
				}
				this.x = x; this.y = y;
				xa = x-nXP;
				if (xa != 0) facing = (xa>0)?1:-1;
				xa *= 0.89;
				onGround = false;
				ya = -10;
				ya *= winged ? 0.95f : 0.85f;
				ya += winged ? 0.6 : 2;
				return true;
			}
			if ((x == this.x) && fStartFlying && winged) {
				if (!fFinalize) return true;
				if (DEBUG_REPOSITIONING) {
					System.out.print("WE ARE FLYING2 ");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("WE ARE FLYING2\n");
				}
				this.y = y;
				onGround = false;
				ya = -10;
				ya *= winged ? 0.95f : 0.85f;
				ya += winged ? 0.6 : 2;
				return true;
			}
			
			// We were on the ground and had an x-collision
			if (!winged && onGround && fXCollision && (y == nYP) && (this.y == y)) {
				if (!fFinalize) return true;
				if (DEBUG_REPOSITIONING) {
					System.out.print("GROUND X-COLL");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("GROUND X-COLL\n");
				}
				this.x = x; xa = 0;
				return true;
			}
			// We were falling and had an x-collision
			if (!onGround && fXCollision && (this.y == y)) {
				if (!fFinalize) return true;
				if (DEBUG_REPOSITIONING) {
					System.out.print("FALLING X-COLL");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("FALLING X-COLL\n");
				}
				this.x = x; xa = 0;
				return true;
			}
			// We were falling and had an x-collision and a ground collision
			if (!onGround && fXCollision && fGroundCollision) {
				if (!fFinalize) return true;
				if (DEBUG_REPOSITIONING) {
					System.out.print("FALLING X-COLL GROUND-COLL");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("FALLING X-COLL GROUND-COLL\n");
				}
				this.x = x; xa = 0;
				onGround = true; this.y = y;
				return true;
			}
			// We had an x-collision
			if (fXCollision && (this.y == y)) {
				if (!fFinalize) return true;
				if (DEBUG_REPOSITIONING) {
					System.out.print("GENERAL X-COLL");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("GENERAL X-COLL\n");
				}
				this.x = x; xa = 0;
				return true;
			}
			// We can fly and were falling and had an x-collision but no y-collision
			if (fXCollision && (y==nYP+nYAP)) {
				if (!fFinalize) return true;
				if (DEBUG_REPOSITIONING) {
					System.out.print("FALLING X-COLL");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("FALLING X-COLL\n");
				}
				onGround = false;
				this.x = x; xa = 0;
				this.y = y; ya = nYAP;
				ya *= winged ? 0.95f : 0.85f;
				ya += winged ? 0.6f : 2;
				return true;
			}
			
			// We thought we were on-ground but we were flying and we collided
			if (winged && fStartFlying && fXCollision) {
				if (!fFinalize) return true;
				if (DEBUG_REPOSITIONING) {
					System.out.print("WE ARE FLYING & COLLIDED");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("WE ARE FLYING & COLLIDED\n");
				}
				this.y = y; this.x =x;
				xa = 0; facing = -facing;
				ya= -10;
				ya *= winged ? 0.95f : 0.85f;
				ya += winged ? 0.6 : 2;
				return true;
			}
			if (winged && onGround && fXCollision && (this.y == y)) {
				if (!fFinalize) return true;
				if (DEBUG_REPOSITIONING) {
					System.out.print("WE ARE FLYING & COLLIDED2");
					if (fSecretDebug)
						PeterLawford_SlowAgent.logger.info("WE ARE FLYING & COLLIDED2\n");
				}
				this.x = x; xa = 0;
				return true;
			}
		}

		// We thought we were flying, but we were on the ground
		if (winged && (x == this.x) && (y == nYP)) {
			if (!fFinalize) return true;
			if (DEBUG_REPOSITIONING) {
				System.out.print("WE ARE NOT FLYING ");
				if (fSecretDebug)
					PeterLawford_SlowAgent.logger.info("WE ARE NOT FLYING\n");
			}
			this.y = y;
			onGround = true;
			ya= -10;
			ya *= winged ? 0.95f : 0.85f;
//			ya += winged ? 0.6 : 2;
			return true;
		}

		// Paused?
		if ((y == nYP) && (x == nXP)) {
			if (!fFinalize) return true;
			if (DEBUG_REPOSITIONING) {
				System.out.print("PAUSED? ");
				if (fSecretDebug)
					PeterLawford_SlowAgent.logger.info("PAUSED?\n");
			}
			this.x = nXP; this.y = nYP; 
			xa = nXAP; ya = nYAP;
			return true;
		}

		return false;
		//		System.out.println("Couldn't fix new:"+x+","+y+" guess:"+this.x+","+this.y+
		//				" old:"+nXP+","+nYP+" dY:"+nYAP+", W="+width+", H="+height);
		//		System.out.println(y+" VS "+(nYP + nYAP));

		//		throw new java.lang.NullPointerException();
	}

	public boolean fixupProbable(byte nType, float x, float y, boolean fFinalize) {
		if (nType != this.nType) return false;

		boolean fXCollision = (Math.abs(x-nXP) > (winged?16:11)) ? false :
			(16*(int)((x-width)/16) == x-width) || (16*(int)((x+5)/16) == x+5);

		if (!fXCollision && ((Math.abs(x-this.x) > 5) || (Math.abs(y-this.y) > 5)))
			return false;
		/*		
		if (winged && (y==this.y) && (Math.abs(x-nXP)<=16) && 
				(16*(int)((x-4)/16) == x-4)) {
			this.x = x; this.y = y; ya = 10; xa = 0;
			ya *= 0.89f;
			return true;
		}
		 */		
		if ((y == this.y) && fXCollision) {
			if (!fFinalize) return true;
			this.x = x;
			xa = 0;
			//			throw new java.lang.NullPointerException();
			return true;
		}
		if ((y == nYP+nYAP) && fXCollision) {
			if (!fFinalize) return true;
			if (DEBUG_REPOSITIONING) {
			System.out.print("FALLING X-COLLISION ");
			if (fSecretDebug)
				PeterLawford_SlowAgent.logger.info("FALLING X-COLLISION\n");
			}
			onGround = false; fBeyondHorizonOnGround = false;
			this.y = y; this.x = x;
			xa = 0; ya = nYAP; facing = -facing;
			ya *= 0.85f;			
			ya += 2;
			return true;
		}

		if (!fFinalize) return true;

		this.x = x; this.y = y;
		onGround = (y == nYP);
		xa = x - nXP; ya = y - nYP;
		if (xa != 0) facing = (xa>0)?1:-1;

		if (xa > 30) {
			System.out.println(x+"-"+nXP);
			throw new java.lang.NullPointerException();
		}

		ya *= winged ? 0.95f : 0.85f;
		xa *= ((onGround) ? GROUND_INERTIA : AIR_INERTIA);

		if (!onGround)
		{
			ya += (winged) ? 0.6f : 2;
		}
		else if (winged)
		{
			ya = -10;
		}

		if (fSecretDebug)
			PeterLawford_SlowAgent.logger.info("FIXUP PROBABLE\n");
		
		return true;
	}

}
