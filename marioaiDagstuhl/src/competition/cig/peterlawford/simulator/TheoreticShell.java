package competition.cig.peterlawford.simulator;

import competition.cig.peterlawford.PeterLawford_SlowAgent;
import competition.cig.peterlawford.visualizer.Visualizer;



public class TheoreticShell extends TheoreticEnemy {

    public boolean dead = false;
	
	int height = 12;
	//	int facing;
	//	boolean fIsDead;
	//	boolean onGround;
	int width = 4;

    private boolean avoidCliffs = false;

	//   public boolean dead = false;
//	private int deadTime = 0;
	boolean carried;

	//	ThoreticMario mario = this.mario;

	public TheoreticShell(TheoreticEnemies enemies, TheoreticLevel level, 
			float x, float y//, float xa, float ya
			//			,TheoreticMario mario) {
	) {
		super(enemies, level, null, Visualizer.SHELL, x, y, 0, -5, false);
		facing = 0;
		//	       ya = -5;
		carried = false;
		
		if ((enemies != null) && (enemies.fSecretDebug))
			PeterLawford_SlowAgent.logger.info("SHELL CREATED("+x+","+y+"\n");
	}

	public TheoreticShell(TheoreticEnemies enemies, TheoreticShell in
			//			TheoreticMario mario) {
	) {
		super(enemies, in);
		carried = in.carried;
		dead = in.dead;

	}


	@Override
	public boolean fireballCollideCheck(TheoreticFireball fireball)
	{
		if (deadTime != 0) return false;

		float xD = fireball.x - x;
		float yD = fireball.y - y;

		if (fDebug)
	   	System.out.println("TShell: fireball-collide("+fireball.x+","+fireball.y);

	   	if (xD > -16 && xD < 16)
		{
			if (yD > -height && yD < fireball.height)
			{
			   	if (fDebug) System.out.println("- TRUE");
				if (facing!=0) return true;

				xa = fireball.facing * 2;
				ya = -5;
				deadTime = 100;
				return true;
			}
		}
		return false;
	}    

	@Override
	public boolean move(TheoreticMario mario, Frame frame) {
		nXP = x; nYP = y; nYAP = ya;
		if (fDebug) System.out.println("TSHELL-move:"+carried+","+deadTime+","+ya);

		if (carried)
		{
			frame.checkShellCollide(this);
			return false;
		}

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

		float sideWaysSpeed = 11f;
		//        float sideWaysSpeed = onGround ? 2.5f : 1.2f;

		if (xa > 2)
			facing = 1;

		if (xa < -2)
			facing = -1;

		xa = facing * sideWaysSpeed;

		if (facing != 0)
			frame.checkShellCollide(this);



		if (!move(xa, 0))
		{
			facing = -facing;
		}
		onGround = false;
		move(0, ya);

		ya *= 0.85f;
		if (onGround)
		{
			xa *= TheoreticSprite.GROUND_INERTIA;
		}
		else
		{
			xa *= TheoreticSprite.AIR_INERTIA;
		}

		if (!onGround)
		{
			ya += 2;
		}

		if (fDebug) System.out.println(" &&shell"+
				":"+nXP+","+nYP+" => "+x+","+y+","+ya+" ");
		if ((enemies != null) && (enemies.fSecretDebug))
				PeterLawford_SlowAgent.logger.info("&&shell"+
				":"+nXP+","+nYP+" => "+x+","+y+","+ya+"\n");

		return false;
	}

	@Override
	public TheoreticEnemy collideCheck(TheoreticMario mario, Frame frame) {
		if (carried || dead || deadTime>0) return null;

		float xMarioD = mario.x - x;
		float yMarioD = mario.y - y;
		float w = 16;
		if (xMarioD > -16 && xMarioD < 16)
		{
			if (yMarioD > -height && yMarioD < mario.height)
			{
				if (mario.ya > 0 && yMarioD <= 0 && (!mario.onGround || !mario.wasOnGround))
				{
					if (fDebug) System.out.println("TCOL-shell!("+
							xMarioD+","+yMarioD+",w"+width+" h"+height);

					mario.stomp(this);
					if (facing != 0)
					{
						xa = 0;
						facing = 0;
					}
					else
					{
						facing = mario.facing;
					}
				}
				else
				{
					if (facing != 0)
					{
						mario.getHurt();
					}
					else
					{
						mario.kick(this);
						facing = mario.facing;
					}
				}
			}
		}
		
		return null;
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
		}
		if (xa > 0)
		{
			if (fDebug) System.out.println("shell-w="+width+","+height+","+xa+","+ya);

        	if (isBlocking(x + xa + width, y + ya - height, xa, ya)) {
        		if (fDebug) System.out.print("C1");
        		collide = true;
        	}
			if (isBlocking(x + xa + width, y + ya - height / 2, xa, ya)) {
				if (fDebug) System.out.print("C2");
				collide = true;
			}
			if (isBlocking(x + xa + width, y + ya, xa, ya)) {
				if (fDebug) System.out.print("C3");
				collide = true;
			}

			if (avoidCliffs && onGround && !level.isBlocking(
					(int) ((x + xa + width) / 16), (int) ((y) / 16 + 1), xa, 1)) {
        		if (fDebug) System.out.print("C4");
				collide = true;
			}
		}
		if (xa < 0)
		{
			if (isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
			if (isBlocking(x + xa - width, y + ya - height / 2, xa, ya)) collide = true;
			if (isBlocking(x + xa - width, y + ya, xa, ya)) collide = true;

			if (avoidCliffs && onGround && !level.isBlocking((int) ((x + xa - width) / 16), (int) ((y) / 16 + 1), xa, 1)) collide = true;
		}

		if (collide)
		{
			if ((enemies != null) && (enemies.fSecretDebug))
				PeterLawford_SlowAgent.logger.info("&&shell-move:collide\n");

			if (xa < 0)
			{
				if (fDebug) System.out.println("Tshell.collide-x"+width);
				x = (int) ((x - width) / 16) * 16 + width;
				this.xa = 0;
			}
			if (xa > 0)
			{
				if (fDebug) System.out.println("Tshell.collide+x"+width);
				x = (int) ((x + width) / 16 + 1) * 16 - width - 1;
				this.xa = 0;
			}
			if (ya < 0)
			{
				y = (int) ((y - height) / 16) * 16 + height;
				if (fDebug)  System.out.println("Shell:ya=>0");
				this.ya = 0;
			}
			if (ya > 0)
			{
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
		int x = (int) (_x / 16);
		int y = (int) (_y / 16);
		if (x == (int) (this.x / 16) && y == (int) (this.y / 16)) return false;

		boolean blocking = level.isBlocking(x, y, xa, ya);

		byte block = level.getBlock(x, y);

		//      if (blocking && ya == 0 && xa!=0)
		//          world.bump(x, y, true);
		if (blocking && fSecretDebug)
			PeterLawford_SlowAgent.logger.info("["+block+","+x+","+y+"]");

		return blocking;
	}
	/*
    public void bumpCheck(int xTile, int yTile)
    {
        if (x + width > xTile * 16 && x - width < xTile * 16 + 16 && yTile == (int) ((y - 1) / 16))
        {
            facing = -world.mario.facing;
            ya = -10;
        }
    }
	 */
	public void die()
	{
		if (fDebug) System.out.println("TShell::die");
		dead = true;

		carried = false;

		xa = -facing * 2;
		ya = -5;
		deadTime = 100;
	}
	
	@Override
    public boolean shellCollideCheck(TheoreticShell shell, TheoreticMario mario)
    {
    	if (fDebug) System.out.println("shell::shellCollideCheck");

    	if (deadTime != 0) return false;

        float xD = shell.x - x;
        float yD = shell.y - y;

        if (xD > -16 && xD < 16)
        {
            if (yD > -height && yD < shell.height)
            {
                if (mario.carried == shell || mario.carried == this)
                {
                    mario.carried = null;
                }

                die();
                shell.die();
                return true;
            }
        }
        return false;
    }
	 
	public void release(TheoreticMario mario)
	{
		carried = false;
		facing = mario.facing;
		x += facing * 8;
	}

	@Override
	public boolean fixupDefinite(byte nType, float x, float y, boolean fFinalize) {
		if (fSecretDebug)
			PeterLawford_SlowAgent.logger.info("SHELL::fixup("+nXP+","+nYP+"\n");
		
		if ((y == this.y) && ((x-nXP) == (nXP-this.x) && (Math.abs(x-nXP) == 4))) {
			if (!fFinalize) return true;
			this.x = x;
			xa = -xa; facing = -facing;
			return true;
		}

		if ((y == this.y) && (Math.abs(x-nXP) == 11)) {
			if (!fFinalize) return true;
			this.x = x;
			xa = x-nXP;
			facing = (xa > 0) ? 1 : -1;
			return true;
		}

		if ((y == this.y) && (Math.abs(x-this.x) == 22)) {
			if (!fFinalize) return true;
			this.x = x;
			facing = (x-nXP > 0) ? 1 : -1;
			return true;
		}

		if ((y == nYP) && (Math.abs(x-this.x) == 22)) {
			if (!fFinalize) return true;
			this.x = x; this.y = y;
			facing = (x-nXP > 0) ? 1 : -1;
			ya = nYAP;
			ya *= 0.85f;
			return true;
		}

		// We thought we were alive but we're dead
		if ((x == nXP+2) && (y != nYP) && (Math.abs(y-nYP) < 8)) {
			if (!fFinalize) return true;
			this.x = x; deadTime = 100; xa = 2;
			this.y = y; ya = y-nYP;
			ya *= 0.95;
			ya += 1;
			return true;
		}
		
		// Maybe we are off-screen and don't realize we're not falling or moving
		if ((y == nYP) && (x == nXP)) {
			if (!fFinalize) return true;
			this.x = nXP; this.y = nYP; 
			ya = nYAP;
			ya *= 0.85f;
//			xa = 0; ya = nYAP;
			facing = -facing;
			return true;
		}

		// We don't realize we're not falling but moving
		if ((y == nYP) && (this.x == x)) {
			if (!fFinalize) return true;			
			this.y = nYP; 
//			xa = 0; ya = nYAP;
			ya = nYAP;
			ya *= 0.85f;
			return true;
		}

//		boolean fXCollision = (Math.abs(x-nXP) > 11) ? false :
//			(16*(int)((x-width)/16) == x-width);
		boolean fXCollision = (Math.abs(x-nXP) > 11) ? false :
			(16*(int)((x-width)/16) == x-width) || (16*(int)((x+5)/16) == x+5);
			
		boolean fGroundCollision = (16*(int)((y+1)/16) == y+1);
		
		if (fXCollision && (y == this.y)) {
			if (!fFinalize) return true;			
			this.x = x; xa = -xa; facing = -facing;
			return true;
		}

		if (fXCollision && (y == nYP)) {
			if (!fFinalize) return true;			

			this.x = x; xa = -xa; facing = -facing;

			this.y = nYP;
			ya = nYAP;
			ya *= 0.85f;

			return true;
		}

		if (fGroundCollision && (x == this.x)) {
			if (!fFinalize) return true;			
			this.y = nYP;
			ya = nYAP;
			ya *= 0.85f;			
			return true;
		}
		
		return false;
	}

	@Override
	public boolean fixupProbable(byte nType, float x, float y, boolean fFinalize) {
		if (nType != this.nType) return false;

		if ((y-nYP > 0) && (y-nYP < 10) && (Math.abs(x-nXP) == 11)) {
			if (!fFinalize) return true;
			this.x = x;
			xa = x-nXP;
			facing = (xa > 0) ? 1 : -1;

			this.y = y; ya = y-nYP;
			ya *= 0.95;
			ya += 1;
			return true;
		}
		
		return false;
	}
/*
	@Override
	public boolean isThisMe(byte nType, float x, float y) {
		if ((nType != -1) && (nType != Visualizer.SHELL) &&
				(nType != 4) && (nType != 6)) return false;

		if ((Math.abs(x-this.x) < 2) && (Math.abs(y-this.y)<2))
			return true;

		return false;
	}
*/
}
