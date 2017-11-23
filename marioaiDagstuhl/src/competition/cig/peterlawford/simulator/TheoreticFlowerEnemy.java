package competition.cig.peterlawford.simulator;

import competition.cig.peterlawford.PeterLawford_SlowAgent;

import ch.idsia.mario.engine.sprites.Sparkle;

public class TheoreticFlowerEnemy extends TheoreticEnemy {

	public TheoreticFlowerEnemy(TheoreticEnemies enemies,
			TheoreticLevel level, TheoreticMario mario,
			float x, float y//, float xa, float ya
			) {
		super(enemies, level, null, (byte)12, x, y, 0, -8, false);
		yStart = Integer.MAX_VALUE;	// (int)y;
//		ya = -8;
		fUnknownTiming = true;
		fUnknownStart = true;
		
		width = 2;
		
		
		this.y += 24;
		this.y = (float)Math.floor(this.y);
		this.y += 1;

		yStart = (int)this.y;

		ya = -8;
		ya = ya*0.9f + 0.1f;
        this.y-=1;
		
        if (enemies != null) fDebug = enemies.fDebug;
		for (int i=0; i<4; i++)
			move(mario, null);

	}

	public TheoreticFlowerEnemy(TheoreticEnemies enemies,
			TheoreticFlowerEnemy in) {
		super(enemies, in);
		yStart = in.yStart;
		jumpTime = in.jumpTime;
		fUnknownTiming = in.fUnknownTiming;
		fUnknownStart = in.fUnknownStart;
		
		width = 2;
	}

	boolean fUnknownTiming;
	boolean fUnknownStart;

	private int yStart;
	int jumpTime = 0;

	float nXAP = 0;
	float nYAP = 0;
	
	@Override
	public boolean move(TheoreticMario mario, Frame frame) {
		nXP = x;
		nYP = y;
		nYAP = ya;
		nXAP = xa;
		
        if (deadTime > 0)
        {
            deadTime--;

            if (deadTime == 0)
            {
                deadTime = 1;
                return true;
            }

            x += xa;
            y += ya;
            ya *= 0.95;
            ya += 1;

            return false;
        }
		
		if (y>=yStart)
		{
			y = yStart;

		 int xd = (int)(Math.abs(mario.x-x));
			// int xd = 100; 	 TODO: Add link to Mario

			jumpTime++;
			if (jumpTime>40 && xd>24)
			{
				ya = -8;
			}
			else
			{
				ya = 0;
			}
		}
		else
		{
			jumpTime = 0;
		}

		y+=ya;
		
		// *** WARNING! The following two calculations yield different results
		// Correct version:
		       ya*=0.9;
		       ya+=0.1f;   
		// Incorrect version:
		// ya = ya*0.9f + 0.1f;

		if (fDebug) System.out.println(" FF"+yStart+
				":"+nXP+","+nYP+" => "+x+","+y+","+ya+" ");  
		if (fSecretDebug) PeterLawford_SlowAgent.logger.info(" FF"+yStart+
				":"+nXP+","+nYP+" => "+x+","+y+","+ya+"\n");  
		return false;
	}
/*
	public boolean isThisMe(byte nType, float x, float y) {
		if (nType != this.nType) return false;
		if (x - this.x > 2)	// If it's dead than it may move in x-dir
			return false;
		return true;
	}

	public boolean isThisDefinitelyMe(byte nType, float x, float y) {
		if (nType != this.nType) return false;
		if (x - this.x > 2)	// If it's dead than it may move in x-dir
			return false;
		return true;
	}
*/
	@Override
	public boolean fixupDefinite(byte nType, float x, float y, boolean fFinalize) {

		// Paused?
		if ((y == nYP) && (x == nXP)) {
			if (!fFinalize) return true;
			this.x = nXP; this.y = nYP; 
			xa = nXAP; ya = nYAP;
			return true;
		}

		if (x == nXP) {
			if (!fFinalize) return true;
			this.y = y;
			ya = y-nYP;
		       ya*=0.9;
		       ya+=0.1f;   
			return true;
		}

		// We are dead
		if (x == nXP+2) {
			if (!fFinalize) return true;
			deadTime = 9;
			this.y = y; this.x = x;
			xa = 2;
			ya = nYAP;
            ya *= 0.95;
            ya += 1;
            
            return true;
		}
				
		
		return false;
	}
}
