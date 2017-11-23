package competition.cig.robinbaumgarten.astar.sprites;

import competition.cig.robinbaumgarten.astar.LevelScene;



public class BulletBill extends Sprite
{
    int height = 24;
    
    public int facing;

    public boolean avoidCliffs = false;
    public int anim;

    public boolean dead = false;
    private int deadTime = 0;


    public BulletBill(LevelScene world, float x, float y, int dir)
    {
        kind = Sprite.KIND_BULLET_BILL;     //added by SK
        this.x = x;
        this.y = y;
        this.world = world;
        height = 12;
        facing = 0;
        ya = -5;
        this.facing = dir;
    }

    public void collideCheck()
    {
        if (dead) return;

        float xMarioD = world.mario.x - x;
        float yMarioD = world.mario.y - y;
        if (xMarioD > -16 && xMarioD < 16)
        {
            if (yMarioD > -height && yMarioD < world.mario.height)
            {
                if (world.mario.ya > 0 && yMarioD <= 0 && (!world.mario.onGround || !world.mario.wasOnGround))
                {
                    world.mario.stomp(this);
                    //System.out.println("Sim bullet bill stomped on. Mario ya: "+world.mario.ya);
                    dead = true;

                    xa = 0;
                    ya = 1;
                    deadTime = 100;
                }
                else
                {
                    world.mario.getHurt();
                }
            }
        }
    }

    public void move()
    {
        if (deadTime > 0)
        {
            deadTime--;

            if (deadTime == 0)
            {
                deadTime = 1;
                
                spriteContext.removeSprite(this);
            }

            x += xa;
            y += ya;
            ya *= 0.95;
            ya += 1;

            return;
        }

        float sideWaysSpeed = 4f;

        xa = facing * sideWaysSpeed;
        move(xa, 0);
    }

    private boolean move(float xa, float ya)
    {
        x += xa;
        return true;
    }
    
    public boolean fireballCollideCheck(Fireball fireball)
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

    public boolean shellCollideCheck(Shell shell)
    {
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
}