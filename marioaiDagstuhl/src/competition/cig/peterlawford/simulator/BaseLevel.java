package competition.cig.peterlawford.simulator;

import java.awt.geom.Point2D;

import competition.cig.peterlawford.PeterLawford_SlowAgent;
import competition.cig.peterlawford.visualizer.Visualizer;

import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.environments.Environment;

public class BaseLevel {
	private static final int MEMORY_SIZE = 16;

	//	private static byte[][][] scnX = new byte[MEMORY_SIZE][][];
	//	private static byte[][][]  bgX = new byte[MEMORY_SIZE][][];
	//	private static byte[][][]  enemiesX = new byte[MEMORY_SIZE][][];

	static int offX = -1;
	static int offY = -1;

	private static byte[][] bg = null;
	private static byte[][] enemies = null;

	//	{
	//		for (int i=0; i<MEMORY_SIZE; i++) {
	//			enemiesX[i] = null;
	//			scnX[i] = null;
	//			bgX[i] = null;
	//		}
	//	}
	//	static int[] offX = new int[MEMORY_SIZE];
	//	static int[] offY = new int[MEMORY_SIZE];

	//	static int ptr = 0;

	static boolean pauseWorld = false;

	//	static Environment env;

	public static int fireballsOnScreen;

	public void reset() {
		fireballsOnScreen = 0;
		pauseWorld = false;
		//		ptr = 0;
		//		env = null;

		//		for (int i=0; i<MEMORY_SIZE; i++) {
		//			offX[i] = 0; offY[i] = 0;
		//			scnX[i] = null; bgX[i] = null;
		//		}
		offX = -1; offY = -1;
		bg = null; enemies = null;
	}

	public static float getHeight(float x, float y) {
		// return value -1 implies infinite height
		// 0 implies we are inside the ground

		int xd = (int)(x/16);
		int yd = (int)(y/16);
		//		int j = (ptr+1) % MEMORY_SIZE;
		int offset_x = xd-offX+11;
		if (offset_x < 0) offset_x = 0;
		if (offset_x > 21) offset_x = 21;

		boolean fFirst = true;
		for (int offset_y = yd-offY+11; offset_y < 22; offset_y++) {
			byte scn_val = bg[offset_y][offset_x];
			if ((scn_val != 0) && (scn_val != 1)) {
				if (!fFirst || (scn_val == -10)) {
					return (offset_y+offY-11)*16;
				}
			}

			fFirst = false;
		}
		return -1;
	}

	public boolean isBlocking(int x, int y, float xa, float ya)
	{
		byte block = getBlock(x, y);
		
		boolean blocking = ((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_BLOCK_ALL) > 0;
		blocking |= (ya > 0) && ((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_BLOCK_UPPER) > 0;
		blocking |= (ya < 0) && ((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_BLOCK_LOWER) > 0;

		return blocking;
	}

	//	static byte getBlock_i(int x,int y,
	//			byte[][] complete, byte[][] enemies, byte[][] scn,
	//			int nOffsetX, int nOffsetY
	//	) {
	//		if (y == 15) y = 14;	
	//		// For some reason we can't seem to see the last row of data
	//
	//		int offY = y-nOffsetY+11;
	//		int offX = x-nOffsetX+11;
	//		//		System.out.print("{"+offX+","+offY+"/"+x+","+y+"}");
	//		if ((offX < 0) || (offX > 21) || (offY < 0) || (offY > 21)) {
	//			//			System.out.println("!!!"+offX+","+offY+"!!!");
	//			return -1;
	//		}
	//
	//		byte result = complete[offY][offX];
	//		byte scn_result = scn[offY][offX];
	//		//		byte enemy_result = enemies[offY][offX];
	//
	//
	//		// ================================================================
	//		// New-style processing based on Aug. 13,2009 upstream modifications
	//		// ================================================================
	//
	//		//		if ((x == 192) && (y == 15)) {
	//		//			System.out.println("192-15:"+scn_result+","+result+"::"+offX+","+offY);
	//		//		}
	//
	//		//		if ((scn_result == -128) && (result == -128))
	//		//		System.out.print(scn_result+"/"+result+" x"+x+" y"+y);
	//
	//		if (scn_result == -10)	// ground
	//			//			return -127;
	//			return -127;
	//		if (scn_result == -11)	// platform
	//			return -123;
	//		if (scn_result == -12)	// rock
	//			return 9;
	//		if (scn_result == 0)
	//			return 0;
	//
	//		// TODO: what about scn_result == 19?
	//		if ((scn_result >= 16) && (scn_result <= 22)) return scn_result;
	//
	//		if (scn_result == Visualizer.MARIO) {
	//			//			System.err.println("MARIO:"+result+","+x+","+y);
	//			//			if (result == 0) return 0;
	//			throw new java.lang.NullPointerException();
	//		}
	//		// This is because Mario is considered part of the scenery,
	//		// and can cover up scenery.
	//		if (result == -11)
	//			return -123;
	//		/*		
	//		if (result == -11) {
	//			System.out.println("SCN="+scn_result+":"+offX+","+offY);
	//			throw new java.lang.NullPointerException();
	//		} */
	//
	//		//		if (true) return 0;
	//		System.err.println(scn_result+","+result);
	//		if (true)
	//			throw new java.lang.NullPointerException();
	//
	//		// ================= End new-style processing ===================
	//
	//
	//		if (result == Visualizer.FIREBALL) return -1;	// Fireballs cover scenery
	//		if (result == -1) return -1;	// sparkles cover scenery
	//
	//		// Mario is invisible
	//		if (result == 1) return -1;	// keep searching
	//
	//		// Pipes should be handled immediately
	//		// 10 is either a pipe or a flying spiny
	//		if (result == 10) {	// left hand side of pipe
	//			//			System.out.println("P?s:"+
	//			//					env.getLevelSceneObservation()[y-nOffsetY+11][x-nOffsetX+11]+","+
	//			//					env.getEnemiesObservation()[y-nOffsetY+11][x-nOffsetX+11]);
	//			if (env.getEnemiesObservation()[y-nOffsetY+11][x-nOffsetX+11] == 10)
	//				return 0;
	//			return result;
	//		}
	//
	//		// Pipes should be handled immediately
	//		if (result == 11) return result;	// right hand side of pipe
	//
	//		/*	
	//		if ((result == 9) && 
	//				(enemies[offY][offX] == -1)) {
	//			System.out.println("R?S?T:"+
	//					enemies[y-nOffsetY+11][x-nOffsetX+11]);
	//			return result;	// stepping stones should be handled immediately
	//		}
	//		 */
	//
	//		// bullet shooters are handled later and they can 'cover up' scenery, so 
	//		// mark as unknown (TODO: verify this)
	//		if (result == 14) {
	//			//			System.err.println("*14*");
	//			return 14;
	//		}
	//
	//		// bullets cause problems because they are not collidable but we can't see under them
	//		if (result == 8) {
	//			//			System.out.println("<"+result+","+
	//			//					env.getLevelSceneObservation()[y-nOffsetY+11][x-nOffsetX+11]+","+
	//			//					env.getEnemiesObservation()[y-nOffsetY+11][x-nOffsetX+11]+">");	
	//			return -1;
	//		}
	//
	//		// If there is an enemy underneath us then we will handle the sprite collision later
	//		if ((result >= 2) && (result <= 13)) {
	//			//			System.out.println("{"+result+","+
	//			//					env.getLevelSceneObservation()[y-nOffsetY+11][x-nOffsetX+11]+","+
	//			//					env.getEnemiesObservation()[y-nOffsetY+11][x-nOffsetX+11]+"}");
	//			return 0;
	//		}
	//
	//		return result;
	//	}


	//	static byte getBlock2(int x, int y) {
	//		int j = (ptr+1) % MEMORY_SIZE;
	//		int nOffsetX = offX[j]; int nOffsetY = offY[j];
	//		byte[][] scn = bgX[j];
	//
	//		if (y == 15) y = 14;	
	//		// For some reason we can't seem to see the last row of data
	//
	//		int offY = y-nOffsetY+11;
	//		int offX = x-nOffsetX+11;
	//		//		System.out.print("{"+offX+","+offY+"/"+x+","+y+"}");
	//		if ((offX < 0) || (offX > 21) || (offY < 0) || (offY > 21)) {
	//			return 0;
	//		}
	//
	//		//		byte result = complete[offY][offX];
	//		byte scn_result = scn[offY][offX];
	//
	//		if (scn_result == -10)	// ground
	//			//			return -127;
	//			return -127;
	//		if (scn_result == -11)	// platform
	//			return -123;
	//		if (scn_result == -12)	// rock
	//			return 9;
	//		if (scn_result == 0)
	//			return 0;
	//
	//		// TODO: what about scn_result == 19?
	//		if ((scn_result >= 16) && (scn_result <= 22)) return scn_result;	
	//
	//		return 0;
	//	}


	public Point2D.Float getFireflower(byte[][] enemies) {
		return null;
	}

	
	static boolean isMarioOverFireflower(float x, float y, int mario_height,
			boolean fDebug) {
		//		int j = (ptr+1) % MEMORY_SIZE;
		//		int nOffsetX = offX; int nOffsetY = offY;
		//		byte[][] scn = bgX[j];
		//	int mario_x = (int)(x/16);
		//		int mario_y = (int)(y/16);

		// Mario may not be in the center of the screen if we're predicting mario
		// ahead into the future

		// This is the location of mario within the current grid
		// (11,11) for time.now, maybe something else for time.future
		int screen_offset_y = ((int)(y/16))-offY+11;
		int screen_offset_x = ((int)(x/16))-offX+11;

		if (fDebug)
			PeterLawford_SlowAgent.logger.info("FF:"+screen_offset_x+","+screen_offset_y+"\n");

		if ((screen_offset_x < 0) || (screen_offset_x > 21) ||
				(screen_offset_y < 0) || (screen_offset_y > 21))
			return false;

		float ff_x = -1;
		float ff_y = -1;

		//		byte val = enemiesX[j][offY][offX];		
		//Math.max(-screen_offset_y, -2)
		for (int i=Math.max(-screen_offset_y, -2);(i<=1)&&(screen_offset_y+i<22); i++) {
			int guess_y = (screen_offset_y+i+offY-11)*16+15;

			if (fDebug)
				PeterLawford_SlowAgent.logger.info(
						enemies[screen_offset_y+i][screen_offset_x-1]+"."+
						enemies[screen_offset_y+i][screen_offset_x]+"."+
						enemies[screen_offset_y+i][screen_offset_x+1]+".");

			for (int j=-1; j<=1; j++) {
				if (screen_offset_x+j < 0) continue;
				if (screen_offset_x+j > 21) continue;
			if ( (enemies[screen_offset_y+i][screen_offset_x+j] == Visualizer.FIREFLOWER) &&
			(bg[screen_offset_y+i][screen_offset_x+j] == 0) ) { 
				ff_x = (screen_offset_x+j+offX-11)*16+8;
				ff_y = guess_y;
			}
			
			}
/*			else {
				if ((screen_offset_x-1 >= 0) && 
						(enemies[screen_offset_y+i][screen_offset_x-1] == Visualizer.FIREFLOWER)) {
					ff_x = (screen_offset_x-1+offX-11)*16+8;
					ff_y = guess_y;
				} else {
					if ((screen_offset_x+1 < 22) && 
							(enemies[screen_offset_y+i][screen_offset_x+1] == Visualizer.FIREFLOWER)) {
						ff_x = (screen_offset_x+1+offX-11)*16+8;	
						ff_y = guess_y;
					}
				}
			} */
		}

		if (fDebug && (ff_x != -1)) {
			System.out.println("Found fireflower at "+ff_x+","+ff_y+
					" M:"+x+","+y);
			PeterLawford_SlowAgent.logger.info("Found fireflower at "+ff_x+","+ff_y+
					" M:"+x+","+y+"\n");
		}

		float xMarioD = x-ff_x;
		float yMarioD = y-ff_y;

	    final int ff_height = 12;

		if (fDebug)
    	PeterLawford_SlowAgent.logger.info("fireflower::collideCheck("+xMarioD+","+yMarioD+
    			","+ff_height+","+mario_height+","+ff_x+","+ff_y);
		
		if (xMarioD > -16 && xMarioD < 16) {
			if (yMarioD > -ff_height && yMarioD < mario_height) {	// Fireflower height = 16
				if (fDebug) PeterLawford_SlowAgent.logger.info("FIREFLOWER!\n");
				return true;
			}
		}

		return false;
	}


	/*
	 * This version relies on having ZLevel-0 data in the LevelScene grid
	 */
	byte getBlock(int x,int y) {
		//		int j = (ptr+1) % MEMORY_SIZE;
		int nOffsetX = offX; int nOffsetY = offY;
		//		byte[][] scn = bgX[j];

		int offY = y-nOffsetY+11;
		int offX = x-nOffsetX+11;

		if ((offX < 0) || (offX > 21) || (offY < 0) || (offY > 21)) {
			return 0;
		}

		byte val = bg[offY][offX];

		switch(val) {
		case Visualizer.COIN: return 0;	// coin
		case 0: 	// nothing
		case 46: 	// bullet shooter stand (old-school)

		case -65: 	// I don't know what this is!
		case -81: 	// I don't know what this is!
		case -82: 	// I don't know what this is!
		case -83: 	// I don't know what this is!
		case -84: 	// I don't know what this is!
		case -97: 	// I don't know what this is!
		case -98: 	// I don't know what this is!
		case -99: 	// I don't know what this is!
		case -100: 	// I don't know what this is!
		case -102: 	// I don't know what this is!
		case -113: 	// I don't know what this is!
		case -114: 	// I don't know what this is!
		case -115: 	// I don't know what this is!
		case -116: 	// I don't know what this is!
			
		case -127: 	// ground
		case -111: 	// interior ground

		case -106: 	// background underneath platform right
		case -107: 	// background underneath platform
		case -108: 	// background underneath platform left

		case -110: 	// wall XXX|
		case -112: 	// wall |XXX

		case -122: 	// platform
		case -123: 	// platform
		case -124: 	// platform
		case -76: 	// platform underneath another platform
		case -74: 	// platform underneath another platform

		case -125: 
		case -126: 
		case -128: 
		case -109: 	// interior ground

		case 14: 	// bullet shooter
		case 30: 	// bullet shooter stand

		case 4: 	// punched coinbox
		case 16: 	// coinbox or brick
		case 21: 	// mystery box

		case 9: 	// rock

		case 10:	// pipe - top-left corner
		case 11: 	// pipe - top-right corner
		case 26: 	// pipe
		case 27: 	// pipe
			return val;

		default:
//			System.err.println("Unrecognized value: "+val+" @ "+offY+","+offX);
//			throw new java.lang.NullPointerException();
			return val;
		}
	}



	//	static byte getBlock_old(int x,int y) {
	//
	//		for (int i=0; i<MEMORY_SIZE; i++) {
	//			//			System.out.print("-"+i);
	//
	//			int j = (i+ptr+1) % MEMORY_SIZE;
	//			if (scnX[j] == null) break;
	//			byte result;
	//			result = getBlock_i(x,y,
	//					scnX[j], null, bgX[j],
	//					offX[j], offY[j]);
	//
	//			if (result != -1) return result;				
	//		}
	//
	//		//		System.out.println("*-1*");
	//		return 0;	
	//	}

	public static void updateImage(Environment env) {

		float nMarioX = env.getMarioFloatPos()[0];
		float nMarioY = env.getMarioFloatPos()[1];

		//		BaseLevel.env = env;                            

		enemies = env.getEnemiesObservationZ(0);
		bg = env.getLevelSceneObservationZ(0);
		offX = (int)(nMarioX/16);
		offY = (int)(nMarioY/16);

		PeterLawford_SlowAgent.logger.info("updateImage["+offX+","+offY+"]\n");

		//		enemiesX[ptr] = env.getEnemiesObservation(0);
		//		scnX[ptr] = env.getCompleteObservation();
		//		bgX[ptr] = env.getLevelSceneObservation(0);
		//		offX[ptr] = (int)nMarioX/16;
		//		offY[ptr] = (int)nMarioY/16;
		//		ptr--;
		//		if (ptr < 0) ptr = MEMORY_SIZE-1;

		//		SlowAgent.DisplayPathInfo path = new SlowAgent.DisplayPathInfo(Color.GREEN);
		//		float y_bottom = nMarioY;
		//		path.push(new float[]{nMarioX, y_bottom});
		//		path.push(new float[]{nMarioX, getHeight(nMarioX, y_bottom)});
		//		SlowAgent.lines.add(path);
		//	        System.out.println("MARIO:"+nOffsetX+", "+nOffsetY);
	}
}
