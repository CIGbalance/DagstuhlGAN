package competition.cig.peterlawford.simulator;

import competition.cig.peterlawford.PeterLawford_SlowAgent;
import competition.cig.peterlawford.visualizer.Visualizer;



public class TheoreticBulletBill extends TheoreticEnemy {

    public boolean dead = false;

	public TheoreticBulletBill(TheoreticEnemies enemies, 
			float x, float y, int facing) {
		super(enemies, null, null, Visualizer.ENEMY_BULLET, x, y, facing*sideWaysSpeed, 0, false);
		width = 4;
//		facing = (x > mario.x) ? -1 : 1;
		this.facing = facing;
	}
	
	public TheoreticBulletBill(TheoreticEnemies enemies, TheoreticBulletBill in) {
		super(enemies, in);
		width = 4;
		dead = in.dead;
	}

    private static final float sideWaysSpeed = 4f;

    @Override
	public boolean move(TheoreticMario mario, Frame frame) {
//    	if (fDebug) System.out.println("bullet-move");
    	
       	nXP = x; nYP = y;

   	if (deadTime > 0) {
        deadTime--;

        if (deadTime == 0)
        {
        	// remove from queue
        	return true;
        }
            x += xa;
            y += ya;
            ya *= 0.95;
            ya += 1;

    		if (fDebug) System.out.println(" BBdead"+
    				":"+nXP+","+nYP+" => "+x+","+y+","+xa+","+ya);

            return false;
        }
    	
         xa = facing * sideWaysSpeed;
        move(xa, 0);
 
   		if (fDebug) System.out.println(" &BB&"+
				":"+nXP+","+nYP+" => "+x+","+y+" ");
   		return false;
    }

    private boolean move(float xa, float ya)
    {
        x += xa;
        return true;
    }

    @Override
	public boolean fireballCollideCheck(TheoreticFireball fireball)
    {
        if (deadTime != 0) return false;

        float xD = fireball.x - x;
        float yD = fireball.y - y;

        if (xD > -16 && xD < 16)
        {
            if (yD > -height && yD < fireball.height)
            {
                return true;
            }
        }
        return false;
    }      

    @Override
    public boolean shellCollideCheck(TheoreticShell shell, TheoreticMario mario)
    {
    	if (fDebug) System.out.println("bullet::shellCollideCheck");

    	if (deadTime != 0) return false;

        float xD = shell.x - x;
        float yD = shell.y - y;

        if (xD > -16 && xD < 16)
        {
            if (yD > -height && yD < shell.height)
            {
                dead = true;

                xa = 0;
                ya = 1;
                deadTime = 100;

                return true;
            }
        }
        return false;
    }      

    
    
    
    /*
	@Override
	public boolean isThisMe(byte nType, float x, float y) {
		if ((nType != this.nType) && (this.nType != -1)) return false;
		
		// We missed a cycle
		if ((x == nXP+8) && (y == nYP)) {
			return true;
		}
		
		// we're ahead a cycle
		if ((x == nXP) && (y == nYP)) {
			return true;
		}
		return super.isThisMe(nType, x, y);
	}
*/
 	
	@Override
    public TheoreticEnemy collideCheck(TheoreticMario mario, Frame frame)
    {
	       if (dead) return null;
    	
        float xMarioD = mario.x - x;
        float yMarioD = mario.y - y;
        float w = 16;
            if (xMarioD > -16 && xMarioD < 16) {
            	if (fDebug) System.out.println("TCOL-Ebill!("+
            			xMarioD+","+yMarioD+",w"+width+" h"+height);
            	if (frame.enemies.fSecretDebug)
            		PeterLawford_SlowAgent.logger.warning("TCOL-Ebill!("+
                			mario.x+"-"+x+"="+xMarioD+
                			","+mario.y+"-"+y+"="+yMarioD+",w"+width+" h"+height+"\n");
               if (yMarioD > -height && yMarioD < mario.height)
                {
                    if (mario.ya > 0 && yMarioD <= 0 && (!mario.onGround || !mario.wasOnGround))
                    {
                        mario.stomp(this);
                        dead = true;

                        xa = 0;
                        ya = 1;
                        deadTime = 100;
                   } else {
                   	if (frame.enemies.fSecretDebug)
                	   PeterLawford_SlowAgent.logger.warning("TBulletBill hurts Mario");
                        mario.getHurt();
                    }
               
            }
         }
            return null;
    }
	
	
	@Override
	public boolean fixupDefinite(byte nType, float x, float y, boolean fFinalize) {
		if ((y == this.y) && (x == this.x+4)) {
			if (!fFinalize) return true;
			this.x = x;
			return true;
		}
		if ((y == this.y) && Math.abs(x-this.x) == 8) {
			if (!fFinalize) return true;
			this.x = x; xa = x - nXP;
			if (xa != 0) facing = (xa > 0)?1:-1;
			return true;
		}
		// We were paused
		if ((y == this.y) && (x == nXP)) {
			if (!fFinalize) return true;
			this.x = x;
			return true;
		}
		return false;
	}

}
