package competition.cig.peterlawford.simulator;

import java.awt.Point;
import java.util.LinkedList;

import competition.cig.peterlawford.PeterLawford_SlowAgent;

import ch.idsia.mario.engine.level.Level;

public class TheoreticLevel extends BaseLevel {
	
	LinkedList<Point> skip_points = null;
	
	public TheoreticLevel() {		
	}
	public TheoreticLevel(TheoreticLevel in) {
		if (in.skip_points != null)
			skip_points = new LinkedList<Point>(in.skip_points);
	}
	
	@Override
	byte getBlock(int x,int y) {
		if (skip_points != null) {
			for(Point p : skip_points)
				if ((p.x == x) && (p.y == y)) {
					PeterLawford_SlowAgent.logger.info("*");
					return 0;
				}
		}
		return super.getBlock(x,y);
	}
	
	
	TheoreticLevel clearBlock(int x, int y) {
		TheoreticLevel lvl = new TheoreticLevel(this);
		if (lvl.skip_points == null) lvl.skip_points = new LinkedList<Point>();
		lvl.skip_points.add(new Point(x,y));
		return lvl;
	}
	
	
	
	
    public TheoreticLevel bump(int x, int y, boolean canBreakBricks)
    {
        byte block = getBlock(x, y);
/*
        if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BUMPABLE) > 0)
        {
            bumpInto(x, y - 1);
            level.setBlock(x, y, (byte) 4);
            level.setBlockData(x, y, (byte) 4);

            if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_SPECIAL) > 0)
            {
                if (!Mario.large)
                {
                    addSprite(new Mushroom(this, x * 16 + 8, y * 16 + 8));
                }
                else
                {
                    addSprite(new FireFlower(this, x * 16 + 8, y * 16 + 8));
                }
            }
            else
            {
                Mario.getCoin();
                addSprite(new CoinAnim(x, y));
            }
        }
*/
        if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BREAKABLE) > 0)
        {
//            bumpInto(x, y - 1);
            if (canBreakBricks)
            {
                PeterLawford_SlowAgent.logger.info("BumpInto-SetBlock");
                return clearBlock(x, y);
            }
//            else
//            {
 //               level.setBlockData(x, y, (byte) 4);
 //           }
        }
        return this;
    }
/*
    public void bumpInto(int x, int y)
    {
        byte block = level.getBlock(x, y);
        if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE) > 0)
        {
            Mario.getCoin();
            level.setBlock(x, y, (byte) 0);
            addSprite(new CoinAnim(x, y + 1));
        }

        for (Sprite sprite : sprites)
        {
            sprite.bumpCheck(x, y);
        }
    }
*/
	
	
//	private static final int MEMORY_SIZE = 16;
//
//	private static byte[][][] scnX = new byte[MEMORY_SIZE][][];
//	private static byte[][][]  bgX = new byte[MEMORY_SIZE][][];
//	private static byte[][][]  enemiesX = new byte[MEMORY_SIZE][][];
//
//	{
//		for (int i=0; i<MEMORY_SIZE; i++) {
//			enemiesX[i] = null;
//			scnX[i] = null;
//			bgX[i] = null;
//		}
//	}
//	static int[] offX = new int[MEMORY_SIZE];
//	static int[] offY = new int[MEMORY_SIZE];
//
//	static int ptr = 0;
//
//	static boolean pauseWorld = false;
//
//	static Environment env;
//
//	public static int fireballsOnScreen;
//
//	public void reset() {
//		fireballsOnScreen = 0;
//		pauseWorld = false;
//		ptr = 0;
//		env = null;
//
//		for (int i=0; i<MEMORY_SIZE; i++) {
//			offX[i] = 0; offY[i] = 0;
//			scnX[i] = null; bgX[i] = null;
//		}
//	}
//
//	public static float getHeight(float x, float y) {
//		// return value -1 implies infinite height
//		// 0 implies we are inside the ground
//
//		int xd = (int)(x/16);
//		int yd = (int)(y/16);
//		int j = (ptr+1) % MEMORY_SIZE;
//		int offset_x = xd-offX[j]+11;
//		if (offset_x < 0) offset_x = 0;
//		if (offset_x > 21) offset_x = 21;
//
//		boolean fFirst = true;
//		for (int offset_y = yd-offY[j]+11; offset_y < 22; offset_y++) {
//			byte scn_val = bgX[j][offset_y][offset_x];
//			if ((scn_val != 0) && (scn_val != 1)) {
//				if (!fFirst || (scn_val == -10)) {
//					return (offset_y+offY[j]-11)*16;
//				}
//			}
//
//			fFirst = false;
//		}
//		return -1;
//	}
//
//	public boolean isBlocking(int x, int y, float xa, float ya)
//	{
//		byte block = getBlock(x, y);
//
//		boolean blocking = ((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_BLOCK_ALL) > 0;
//		blocking |= (ya > 0) && ((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_BLOCK_UPPER) > 0;
//		blocking |= (ya < 0) && ((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_BLOCK_LOWER) > 0;
//
//		return blocking;
//	}
//
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
//
//
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
//
//
//	static boolean isMarioOverFireflower(float x, float y, boolean fDebug) {
//		int j = (ptr+1) % MEMORY_SIZE;
//		int nOffsetX = offX[j]; int nOffsetY = offY[j];
//		//		byte[][] scn = bgX[j];
//		int mario_x = (int)(x/16);
//		int mario_y = (int)(y/16);
//
//		int offY = ((int)(y/16))-nOffsetY+11;
//		int offX = ((int)(x/16))-nOffsetX+11;
//
////		if (fDebug)
////			SlowAgent.logger.info("FF:"+offX+","+offY+"\n");
//
//		if ((offX < 0) || (offX > 21) || (offY < 0) || (offY > 21))
//			return false;
//
//		float ff_x = -1;
//		float ff_y = -1;
//
////		byte val = enemiesX[j][offY][offX];		
//
//		for (int i=(offY == 0)?0:-1; (i<=1)&&(offY+i<22); i++) {
//			ff_y = (mario_y+i)*16+8;
//
//			if (fDebug)
//			SlowAgent.logger.info(
//					enemiesX[j][offY+i][offX-1]+"."+
//					enemiesX[j][offY+i][offX]+"."+
//					enemiesX[j][offY+i][offX+1]+".");
//			
//			if (enemiesX[j][offY+i][offX] == Visualizer.FIREFLOWER) { 
//				ff_x = mario_x*16+8;
//			} else {
//				if ((offX-1 >= 0) && 
//						(enemiesX[j][offY+i][offX-1] == Visualizer.FIREFLOWER)) {
//					ff_x = (mario_x-1)*16+8;
//				} else {
//					if ((offX+1 < 22) && 
//							(enemiesX[j][offY+i][offX+1] == Visualizer.FIREFLOWER)) {
//						ff_x = (mario_x+1)*16+8;				
//					}
//				}
//			}
//		}
//
//		if (fDebug && (ff_x != -1)) {
//			System.out.println("Found fireflower at "+ff_x);
//			SlowAgent.logger.info("Found fireflower at "+ff_x+"\n");
//		}
//
//		float xMarioD = x - ff_x;
//		if (xMarioD > -16 && xMarioD < 16) {
//			//			if (yMarioD > -16 && yMarioD < mario.height) {	// Fireflower height = 16
//			if (fDebug) SlowAgent.logger.info("FIREFLOWER!\n");
//			return true;
//			//			}
//		}
//
//		return false;
//	}
//
//
//	/*
//	 * This version relies on having ZLevel-0 data in the LevelScene grid
//	 */
//	static byte getBlock(int x,int y) {
//		int j = (ptr+1) % MEMORY_SIZE;
//		int nOffsetX = offX[j]; int nOffsetY = offY[j];
//		byte[][] scn = bgX[j];
//
//		int offY = y-nOffsetY+11;
//		int offX = x-nOffsetX+11;
//
//		if ((offX < 0) || (offX > 21) || (offY < 0) || (offY > 21))
//			return 0;
//
//		byte val = scn[offY][offX];
//
//		switch(val) {
//		case Visualizer.COIN: return 0;	// coin
//		case 0: 	// nothing
//		case 46: 	// bullet shooter stand (old-school)
//
//		case -127: 	// ground
//		case -111: 	// interior ground
//
//		case -106: 	// background underneath platform right
//		case -107: 	// background underneath platform
//		case -108: 	// background underneath platform left
//
//		case -110: 	// wall XXX|
//		case -112: 	// wall |XXX
//
//		case -122: 	// platform
//		case -123: 	// platform
//		case -124: 	// platform
//		case -76: 	// platform underneath another platform
//		case -74: 	// platform underneath another platform
//
//		case -125: 
//		case -126: 
//		case -128: 
//		case -109: 	// interior ground
//
//		case 14: 	// bullet shooter
//		case 30: 	// bullet shooter stand
//
//		case 4: 	// punched coinbox
//		case 16: 	// coinbox or brick
//		case 21: 	// mystery box
//
//		case 9: 	// rock
//
//		case 10:	// pipe - top-left corner
//		case 11: 	// pipe - top-right corner
//		case 26: 	// pipe
//		case 27: 	// pipe
//			return val;
//
//		default:
//			System.err.println("Unrecognized value: "+val+" @ "+offY+","+offX);
//			throw new java.lang.NullPointerException();
//		}
//	}
//
//
//
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
//
//	public static void updateImage(Environment env) {
//
//		float nMarioX = env.getMarioFloatPos()[0];
//		float nMarioY = env.getMarioFloatPos()[1];
//
//		TheoreticLevel.env = env;                            
//
//		enemiesX[ptr] = env.getEnemiesObservation(0);
//		scnX[ptr] = env.getCompleteObservation();
//		bgX[ptr] = env.getLevelSceneObservation(0);
//		offX[ptr] = (int)nMarioX/16;
//		offY[ptr] = (int)nMarioY/16;
//		ptr--;
//		if (ptr < 0) ptr = MEMORY_SIZE-1;
//
//		//		SlowAgent.DisplayPathInfo path = new SlowAgent.DisplayPathInfo(Color.GREEN);
//		//		float y_bottom = nMarioY;
//		//		path.push(new float[]{nMarioX, y_bottom});
//		//		path.push(new float[]{nMarioX, getHeight(nMarioX, y_bottom)});
//		//		SlowAgent.lines.add(path);
//		//	        System.out.println("MARIO:"+nOffsetX+", "+nOffsetY);
//	}
}
