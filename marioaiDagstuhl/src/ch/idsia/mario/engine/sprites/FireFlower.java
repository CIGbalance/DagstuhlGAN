package ch.idsia.mario.engine.sprites;

import ch.idsia.mario.engine.Art;
import ch.idsia.mario.engine.LevelScene;


public class FireFlower extends Sprite
{
    private final int height;

    //private LevelScene world;
    public int facing;

    public boolean avoidCliffs = false;
    private int life;

    public FireFlower(LevelScene world, int x, int y)
    {
        kind = KIND_FIRE_FLOWER;
        sheet = Art.items;

        this.x = x;
        this.y = y;
        this.world = world;
        xPicO = 8;
        yPicO = 15;

        xPic = 1;
        yPic = 0;
        height = 12;
        facing = 1;
        wPic  = hPic = 16;
        life = 0;
    }

    @Override
    public void collideCheck()
    {
        float xMarioD = world.mario.x - x;
        float yMarioD = world.mario.y - y;
        float w = 16;
        if (xMarioD > -16 && xMarioD < 16)
        {
            if (yMarioD > -height && yMarioD < world.mario.height)
            {
                world.mario.getFlower();
                this.world.removeSprite(this);
            }
        }
    }

    @Override
    public void move()
    {
        if (life<9)
        {
            layer = 0;
            y--;
            life++;
        }
    }
}