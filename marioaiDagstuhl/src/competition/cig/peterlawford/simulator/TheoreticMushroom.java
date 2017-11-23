package competition.cig.peterlawford.simulator;

import competition.cig.peterlawford.PeterLawford_SlowAgent;
import competition.cig.peterlawford.visualizer.Visualizer;



public class TheoreticMushroom extends TheoreticEnemy
{
//    private static float GROUND_INERTIA = 0.89f;
//    private static float AIR_INERTIA = 0.89f;

//    private float runTime;
    private boolean onGround = false;
//    private boolean mayJump = false;
 //   private int jumpTime = 0;
//    private float xJumpSpeed;
//    private float yJumpSpeed;

    private int width = 4;
    int height = 12;

//    private LevelScene world;
    public int facing;

    public boolean avoidCliffs = false;
    private int life;

    public TheoreticMushroom(TheoreticEnemies enemies,TheoreticLevel level,
    		float x, float y)
    {
    	super(enemies, level, null, Visualizer.MUSHROOM, x, y, 0, 0, false);
//        kind = KIND_MUSHROOM;
//        sheet = Art.items;

 //       this.x = x;
//        this.y = y;
 //       this.world = world;
 //       xPicO = 8;
  //      yPicO = 15;

 //       yPic = 0;
        height = 12;
        facing = 1;
  //      wPic  = hPic = 16;
        life = 0;
        
        this.y += 1;
       move(null, null);
    }
    public TheoreticMushroom(
    		TheoreticEnemies enemies, TheoreticMushroom in) {
    	super(enemies, in);
        height = 12;
        facing = in.facing;
       life = in.life;
        fDebug = enemies.fDebug;
    }

    @Override
    public TheoreticEnemy collideCheck(TheoreticMario mario, Frame frame)
    {
        float xMarioD = mario.x - x;
        float yMarioD = mario.y - y;
        float w = 16;
 
        if (frame.enemies.fSecretDebug)
        PeterLawford_SlowAgent.logger.info("Tmush::collide("+
        		mario.x+" - "+x+
        		", "+mario.y+" - "+y+")"+height+","+mario.height+"\n");
        
        if (xMarioD > -16 && xMarioD < 16)
        {
            if (yMarioD > -height && yMarioD < mario.height)
            {
                if (frame.enemies.fSecretDebug)
            	PeterLawford_SlowAgent.logger.info("process");

                mario.getMushroom();
 //               spriteContext.removeSprite(this);
                return this;		// Signal to remove from service
            }
        }
        return null;
    }

    @Override
    public boolean move(TheoreticMario mario, Frame frame)
    {
    	nXP = x; nYP = y;
    	
        if (life<9)
        {
 //           layer = 0;
            y--;
            life++;
 
 //           if ((frame != null) && (mario != null) && 
 //           		(frame.enemies.fSecretDebug || mario.fDebug2))
 //           	System.out.println("TmushL"+
 //   					":"+nXP+","+nYP+" => "+x+","+y+","+xa+","+ya+"["+life+"] ");
           
            return false;
        }
        float sideWaysSpeed = 1.75f;
//        layer = 1;
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

 //       mayJump = (onGround);

//        xFlipPic = facing == -1;

 //       runTime += (Math.abs(xa)) + 5;



        if (!move(xa, 0)) facing = -facing;
        onGround = false;
        move(0, ya);

        ya *= 0.85f;
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
            ya += 2;
        }
        
        
  //      if ((frame != null) && (mario != null) && 
  //      		(frame.enemies.fSecretDebug || mario.fDebug2)) {
  //      	System.out.println("Tmush"+((onGround)?"g":"")+
//					":"+nXP+","+nYP+" => "+x+","+y+","+xa+","+ya+" ");
//            throw new java.lang.NullPointerException();
  //      }

        if (frame.enemies.fSecretDebug) 
        	PeterLawford_SlowAgent.logger.info("Tmush"+((onGround)?"g":"")+
					":"+nXP+","+nYP+" => "+x+","+y+","+xa+","+ya+" ");
        
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
 //               jumpTime = 0;
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

        return blocking;
    }

    @Override
    public void bumpCheck(TheoreticMario mario, int xTile, int yTile)
    {
        if (x + width > xTile * 16 && x - width < xTile * 16 + 16 && yTile==(int)((y-1)/16))
        {
            facing = -mario.facing;
            ya = -10;
        }
    }

}