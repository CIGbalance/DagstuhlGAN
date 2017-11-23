package competition.cig.peterlawford.simulator;

import competition.cig.peterlawford.PeterLawford_SlowAgent;
import competition.cig.peterlawford.visualizer.Visualizer;



public class TheoreticFireball extends TheoreticEnemy {

	    private float runTime;
	    private boolean onGround = false;

	    private int width = 4;
	    int height = 24;

	    public int facing;

	    public boolean avoidCliffs = false;
	    public int anim;

	    public boolean dead = false;
//	    private int deadTime = 0;

	    public TheoreticFireball(
	    		TheoreticEnemies enemies,TheoreticLevel level, TheoreticMario mario,
	    		 float x, float y, int facing)
	    {
//	        kind = KIND_FIREBALL;
	    	//super(Visualizer.ENEMY_BULLET, x, y, 0, 4*facing);
	    	super(enemies, level, mario, Visualizer.FIREBALL, x, y, 0, 0, false);
	    	
//	        this.x = x;
//	        this.y = y;

	        height = 8;
	        this.facing = facing;

	        ya = 4;
	        fDebug = enemies.fDebug;
	    }

	    public TheoreticFireball(
	    		TheoreticEnemies enemies, TheoreticFireball in) {
	    	super(enemies, in);
	        height = 8;
	        facing = in.facing;
	        ya = in.ya;
	        fDebug = enemies.fDebug;
	    }
	    
	    @Override
		public boolean move(TheoreticMario mario, Frame frame) {
	    	if (fDebug) System.out.print(" fireball("+x+","+y+","+ya+","+deadTime);
	    	if (fSecretDebug) PeterLawford_SlowAgent.logger.info(
	    			"fireball("+x+","+y+","+ya+","+deadTime);

	    	if (deadTime > 0)
	        {
	            return true;
	        }

	        if (facing != 0) anim++;

	        float sideWaysSpeed = 8f;
	        //        float sideWaysSpeed = onGround ? 2.5f : 1.2f;

	        if (xa > 2)
	        {
	            facing = 1;
	        }
	        if (xa < -2)
	        {
	            facing = -1;
	        }

	        xa = facing * sideWaysSpeed;

	        frame.checkFireballCollide(this);

	        runTime += (Math.abs(xa)) + 5;

//	        System.out.print("(XA="+xa+"w="+width+"h="+height+")");

	        if (!move(xa, 0))
	        {
	            die();
	        }
	        
	        onGround = false;
	        move(0, ya);
	        if (onGround) ya = -10;

	        ya *= 0.95f;
	        if (onGround)
	        {
	            xa *= GROUND_INERTIA;
	        }
	        else
	        {
	            xa *= AIR_INERTIA;
	        }

	        if (!onGround)
	        {
	            ya += 1.5;
	        }

	    	if (fDebug) System.out.print(" --> "+x+","+y+","+ya);
	    	if (fSecretDebug) PeterLawford_SlowAgent.logger.info(
	    			" --> "+x+","+y+","+ya+"\n");

	        return false;
	    }

	    private boolean move(float xa, float ya)
	    {
	    	if (fDebug) System.out.print("tmov-f("+xa+","+ya+","+
	    			avoidCliffs+","+onGround+","+height+","+width);
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
	            if (isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;
	            if (isBlocking(x + xa + width, y + ya - height / 2, xa, ya)) collide = true;
	            if (isBlocking(x + xa + width, y + ya, xa, ya)) collide = true;

	            if (avoidCliffs && onGround && !level.isBlocking((int) ((x + xa + width) / 16), (int) ((y) / 16 + 1), xa, 1)) collide = true;
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
	            if (xa < 0)
	            {
	                x = (int) ((x - width) / 16) * 16 + width;
	                this.xa = 0;
	            }
	            if (xa > 0)
	            {
	                x = (int) ((x + width) / 16 + 1) * 16 - width - 1;
	                this.xa = 0;
	            }
	            if (ya < 0)
	            {
	                y = (int) ((y - height) / 16) * 16 + height;
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
			if (blocking && fDebug)
				System.out.print("["+block+","+x+","+y+"]");

	        return blocking;
	    }

	    public void die()
	    {
//	       	System.out.println("TFireball::die");
	        	        dead = true;

	        xa = -facing * 2;
	        ya = -5;
	        deadTime = 100;
	    }

}
