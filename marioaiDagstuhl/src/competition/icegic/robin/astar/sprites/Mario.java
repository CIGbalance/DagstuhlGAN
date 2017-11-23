package competition.icegic.robin.astar.sprites;

import competition.icegic.robin.astar.LevelScene;
import competition.icegic.robin.astar.level.Level;
import ch.idsia.mario.engine.GlobalOptions;
import ch.idsia.mario.engine.Scene;


public class Mario extends Sprite implements Cloneable
{
    public boolean large = false;
    public boolean fire = false;
    public static int coins = 0;
    public static int lives = 1024;
    public int damage = 0; // counts +1 everytime mario is hurt
    public static int numberOfAttempts = 0;
    public static String levelString = "none";
    public int status = STATUS_RUNNING;
    private final int FractionalPowerUpTime = 0;

    public Sprite carried = null;
    
    public void resetStatic()
    {
        large = true;
        fire = false;
        coins = 0;
        lives = 65536;
        levelString = "none";
        numberOfAttempts = 0;
    }

    @Override
	public Object clone() throws CloneNotSupportedException
    {
    	return super.clone();
    }
    
    public void setMode(MODE mode)
    {
        large = (mode == MODE.MODE_LARGE);
        fire = (mode == MODE.MODE_FIRE);
    }


    public static enum MODE {MODE_SMALL, MODE_LARGE, MODE_FIRE}

    public static void resetCoins()
    {
        coins = 0;
        ++numberOfAttempts;
    }

    public static final int KEY_LEFT = 0;
    public static final int KEY_RIGHT = 1;
    public static final int KEY_DOWN = 2;
    public static final int KEY_JUMP = 3;
    public static final int KEY_SPEED = 4;
    public static final int KEY_UP = 5;
    public static final int KEY_PAUSE = 6;
    public static final int KEY_DUMP_CURRENT_WORLD = 7;
    public static final int KEY_LIFE_UP = 8;
    public static final int KEY_WIN = 9;

    public static final int STATUS_RUNNING = 2;
    public static final int STATUS_WIN = 1;
    public static final int STATUS_DEAD = 0;


    private static float GROUND_INERTIA = 0.89f;
    private static float AIR_INERTIA = 0.89f;

    public boolean[] keys;
    public boolean[] cheatKeys;
    private float runTime;
    boolean wasOnGround = false;
    public boolean onGround = false;
    private boolean mayJump = false;
    private boolean ducking = false;
    public boolean sliding = false;
    public int jumpTime = 0;
    private float xJumpSpeed;
    private float yJumpSpeed;
    private boolean canShoot = false;

    int width = 4;
    int height = 24;

    public LevelScene world;
    public int facing;
    private int powerUpTime = 0; // exclude pause for rendering changes

    public int xDeathPos, yDeathPos;

    public int deathTime = 0;
    public int winTime = 0;
    private int invulnerableTime = 0;

    //private static Mario instance;

    public Mario(LevelScene world)
    {
        kind = KIND_MARIO;
        //Mario.instance = this;
        this.world = world;
        keys = Scene.keys;      // SK: in fact, this is already redundant due to using Agent
        cheatKeys = Scene.keys; // SK: in fact, this is already redundant due to using Agent
        x = 32;
        y = 0;

        facing = 1;
        setLarge(true, true);
    }
     

    void setLarge(boolean large, boolean fire)
    {
        if (fire) large = true;
        if (!large) fire = false;
        
        this.large = large;
        this.fire = fire;
    }

    public void move()
    {
        if (winTime > 0)
        {
            winTime++;

            xa = 0;
            ya = 0;
            return;
        }

        if (deathTime > 0)
        {
            deathTime++;
            if (deathTime < 11)
            {
                xa = 0;
                ya = 0;
            }
            else if (deathTime == 11)
            {
                ya = -15;
            }
            else
            {
                ya += 2;
            }
            x += xa;
            y += ya;
            return;
        }

        //if (world.paused) 
        //	System.out.println("Sim World Paused!");
        if (powerUpTime != 0)
        {
            if (powerUpTime > 0)
            {
                powerUpTime--;
            }
            else
            {
                powerUpTime++;
            }

            if (powerUpTime == 0)             
            {
            	if (world.paused) System.out.println("Sim World Unpaused!");
            	world.paused = false;
            }
            return;
        }

        if (invulnerableTime > 0) invulnerableTime--;
        wasOnGround = onGround;
        float sideWaysSpeed = keys[KEY_SPEED] ? 1.2f : 0.6f;
        //        float sideWaysSpeed = onGround ? 2.5f : 1.2f;

        if (onGround)
        {
            if (keys[KEY_DOWN] && large)
            {
                ducking = true;
            }
            else
            {
                ducking = false;
            }
        }

        if (xa > 2)
        {
            facing = 1;
        }
        if (xa < -2)
        {
            facing = -1;
        }

        if (keys[KEY_JUMP] || (jumpTime < 0 && !onGround && !sliding))
        {
            if (jumpTime < 0)
            {
                xa = xJumpSpeed;
                ya = -jumpTime * yJumpSpeed;
                jumpTime++;
            }
            else if (onGround && mayJump)
            {
                xJumpSpeed = 0;
                yJumpSpeed = -1.9f;
                jumpTime = 7;
                ya = jumpTime * yJumpSpeed;
                onGround = false;
                sliding = false;
            }
            else if (sliding && mayJump)
            {
                xJumpSpeed = -facing * 6.0f;
                yJumpSpeed = -2.0f;
                jumpTime = -6;
                xa = xJumpSpeed;
                ya = -jumpTime * yJumpSpeed;
                onGround = false;
                sliding = false;
                facing = -facing;
            }
            else if (jumpTime > 0)
            {
                xa += xJumpSpeed;
                ya = jumpTime * yJumpSpeed;
                jumpTime--;
            }
        }
        else
        {
            jumpTime = 0;
        }

        if (keys[KEY_LEFT] && !ducking)
        {
            if (facing == 1) sliding = false;
            xa -= sideWaysSpeed;
            if (jumpTime >= 0) facing = -1;
        }

        if (keys[KEY_RIGHT] && !ducking)
        {
            if (facing == -1) sliding = false;
            xa += sideWaysSpeed;
            if (jumpTime >= 0) facing = 1;
        }

        if ((!keys[KEY_LEFT] && !keys[KEY_RIGHT]) || ducking || ya < 0 || onGround)
        {
            sliding = false;
        }
        
        if (keys[KEY_SPEED] && canShoot && this.fire && world.fireballsOnScreen<2)
        {
        	//System.out.println("Adding fireball!");
            world.addSprite(new Fireball(world, x+facing*6, y-20, facing));
        }

        world.paused = GlobalOptions.pauseWorld;
        canShoot = !keys[KEY_SPEED];

        mayJump = (onGround || sliding) && !keys[KEY_JUMP];

        runTime += (Math.abs(xa)) + 5;
        if (Math.abs(xa) < 0.5f)
        {
            runTime = 0;
            xa = 0;
        }

        if (sliding)
        {
            ya *= 0.5f;
        }

        onGround = false;
        move(xa, 0);
        move(0, ya);

        //System.out.println("Mario Sim Speed: "+xa);
        if (y > world.level.height * 16 + 16)
        {
            die();
        }

        if (x < 0)
        {
            x = 0;
            xa = 0;
        }

        if (x > world.level.xExit * 16)
        {
            win();
        }

        if (x > world.level.width * 16)
        {
            x = world.level.width * 16;
            xa = 0;
        }

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
            ya += 3;
        }

        if (carried != null)
        {
            carried.x = x + facing * 8;
            carried.y = y - 2;
            if (!keys[KEY_SPEED])
            {
            	//System.out.println("Releasing shell!");
                carried.release(this);
                carried = null;
            }
        }
        //System.out.println("Mariopos: "+x+" "+y);
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
            sliding = true;
            if (isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;
            else sliding = false;
            if (isBlocking(x + xa + width, y + ya - height / 2, xa, ya)) collide = true;
            else sliding = false;
            if (isBlocking(x + xa + width, y + ya, xa, ya)) collide = true;
            else sliding = false;
        }
        if (xa < 0)
        {
            sliding = true;
            if (isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
            else sliding = false;
            if (isBlocking(x + xa - width, y + ya - height / 2, xa, ya)) collide = true;
            else sliding = false;
            if (isBlocking(x + xa - width, y + ya, xa, ya)) collide = true;
            else sliding = false;
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
                jumpTime = 0;
                this.ya = 0;
            }
            if (ya > 0)
            {
                y = (int) ((y - 1) / 16 + 1) * 16 - 1;
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

        boolean blocking = world.level.isBlocking(x, y, xa, ya);

        byte block = world.level.getBlock(x, y);

        if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE) > 0)
        {
            getCoin();
            world.level.setBlock(x, y, (byte) 0);
        }

        if (blocking && ya < 0)
        {
            world.bump(x, y, large);
            if (world.verbose > 0) System.out.println("Sim Mario bumps a crate!");
        }
        //System.out.println("Sim Mario blockcheck: pos: " + _x + " "+_y + " discrete: "+x + " "+y+
        //		" block: "+block+" blocking?: " + (blocking? "true":"false") + "behaviourvalue: "
        //		+ ((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE));
        return blocking;
    }

    public void stomp(Enemy enemy)
    {
    	if (world.verbose > 0) System.out.println("Prestomp!");
        if (deathTime > 0 || world.paused) return;

        float targetY = enemy.y - enemy.height / 2;
        if (world.verbose > 0) System.out.println("STOMP! targetY: " + targetY + " enemyy: " + enemy.y + " height: " + enemy.height);
        move(0, targetY - y);

        xJumpSpeed = 0;
        yJumpSpeed = -1.9f;
        jumpTime = 8;
        ya = jumpTime * yJumpSpeed;
        if (world.verbose > 0) System.out.println("Stomp ya: "+ya+ " marioY: " + this.y);
        onGround = false;
        sliding = false;
        invulnerableTime = 1;
        world.enemiesJumpedOn++;
    }

    public void stomp(Shell shell)
    {
        if (deathTime > 0 || world.paused) return;

        if (keys[KEY_SPEED] && shell.facing == 0)
        {
            carried = shell;
            shell.carried = true;
            world.otherTricks+=2;
        }
        else
        {
            float targetY = shell.y - shell.height / 2;
            move(0, targetY - y);

            xJumpSpeed = 0;
            yJumpSpeed = -1.9f;
            jumpTime = 8;
            ya = jumpTime * yJumpSpeed;
            onGround = false;
            sliding = false;
            invulnerableTime = 1;
            world.otherTricks++;
        }
    }

    public void getHurt()
    {
    	if (world.verbose > 1) System.out.print("[hurt!]");
    	damage++;
        //if (deathTime > 0 || world.paused) return;
        if (invulnerableTime > 0) return;

        if (large)
        {
            world.paused = true;
            powerUpTime = -3 * FractionalPowerUpTime;
            if (fire)
            {
                setLarge(true, false);
            }
            else
            {
                setLarge(false, false);
            }
            invulnerableTime = 32;
        }
        else
        {
            die();
        }
    }

    private void win()
    {
        xDeathPos = (int) x;
        yDeathPos = (int) y;
        world.paused = true;
        winTime = 1;
        status = Mario.STATUS_WIN;
    }

    public void die()
    {
    	if (world.verbose > 1) System.out.println("[die!]");
    	damage+=2;
    	xDeathPos = (int) x;
        yDeathPos = (int) y;
        world.paused = true;
        deathTime = 1;
        status = Mario.STATUS_DEAD;       
    }


    public void getFlower()
    {
        if (deathTime > 0 || world.paused) return;

        if (!fire)
        {
            world.paused = true;
            powerUpTime = 3 * FractionalPowerUpTime;
            world.mario.setLarge(true, true);
            world.powerUpsCollected++;
        }
        else
        {
            getCoin();
        }
    }

    public void getMushroom()
    {
        if (deathTime > 0 || world.paused) return;

        if (!large)
        {
            world.paused = true;
            powerUpTime = 3 * FractionalPowerUpTime;
            world.mario.setLarge(true, false);
            world.powerUpsCollected++;
        }
        else
        {
            getCoin();
        }
    }

    public void kick(Shell shell)
    {
        //if (deathTime > 0 || world.paused) return;

        if (keys[KEY_SPEED])
        {
            carried = shell;
            shell.carried = true;
        }
        else
        {
            invulnerableTime = 1;
        }
    }

    public void stomp(BulletBill bill)
    {

    	if (world.verbose > 5) System.out.println("Simstomping Bullet Bill!");
        if (deathTime > 0 || world.paused) return;
        float targetY = bill.y - bill.height / 2;
        move(0, targetY - y);
        if (world.verbose > 0) System.out.println("STOMP! targetY: " + targetY + " enemyy: " + bill.y + " height: " + bill.height);
        
        xJumpSpeed = 0;
        yJumpSpeed = -1.9f;
        jumpTime = 8;
        ya = jumpTime * yJumpSpeed;
        onGround = false;
        sliding = false;
        invulnerableTime = 1;
        world.enemiesJumpedOn++;
    }

    public byte getKeyMask()
    {
        int mask = 0;
        for (int i = 0; i < 7; i++)
        {
            if (keys[i]) mask |= (1 << i);
        }
        return (byte) mask;
    }

    public void setKeys(byte mask)
    {
        for (int i = 0; i < 7; i++)
        {
            keys[i] = (mask & (1 << i)) > 0;
        }
    }

    public static void get1Up()
    {
        lives++;
    }
    
    public void getCoin()
    {
        coins++;
        world.coinsCollected++;
        if (coins % 100 == 0)
            get1Up();
    }

    public int getStatus() {
        return status;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public boolean mayJump() {
        return mayJump;
    }
}