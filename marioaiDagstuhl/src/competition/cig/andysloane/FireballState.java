package competition.cig.andysloane;

// not even started

public class FireballState extends SpriteState
{
	public boolean onGround = false;

	public static final float width = 4;
	public static final float height = 12;

	@Override
	public final float height() { return 12; }

	@Override
	public SpriteState clone() {
		FireballState e = new FireballState(x,y,false);
		e.xa = xa; e.ya = ya;
		e.facing = facing;
		e.onGround = onGround;
		return e;
	}

	FireballState(float _x, float _y, boolean predicted) {
		x=_x; y=_y; type=KIND_FIREBALL;
		xa = 0;
		ya = predicted ? -5 : -2.25f;
        facing = 0;
    }

	// returns false iff we should remove the enemy from the list
	public boolean move(WorldState ws) {
		return true;
	}

	@Override
	public void resync(float x, float y, float prev_x, float prev_y) {
		this.x = x;
		this.y = y;
		this.xa = x - prev_x;
		facing = this.xa == 0 ? 0 : (this.xa < 0) ? -1 : 1;
		this.ya = (y - prev_y) * 0.85f;
		if(!onGround) ya += 2;
	}


	// WOO LET'S COPY AND PASTE THIS SOME MORE!
    private boolean move(float xa, float ya, WorldState ws)
    {
        while (xa > 8) {
            if (!move(8, 0, ws)) return false;
            xa -= 8;
        }
        while (xa < -8) {
            if (!move(-8, 0, ws)) return false;
            xa += 8;
        }
        while (ya > 8) {
            if (!move(0, 8, ws)) return false;
            ya -= 8;
        }
        while (ya < -8) {
            if (!move(0, -8, ws)) return false;
            ya += 8;
        }

        boolean collide = false;
        if (ya > 0)
        {
            if (isBlocking(x + xa - width, y + ya, xa, 0, ws)) collide = true;
            else if (isBlocking(x + xa + width, y + ya, xa, 0, ws)) collide = true;
            else if (isBlocking(x + xa - width, y + ya + 1, xa, ya, ws)) collide = true;
            else if (isBlocking(x + xa + width, y + ya + 1, xa, ya, ws)) collide = true;
        }
        if (ya < 0)
        {
            if (isBlocking(x + xa, y + ya - height, xa, ya, ws)) collide = true;
            else if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya, ws)) collide = true;
            else if (collide || isBlocking(x + xa + width, y + ya - height, xa, ya, ws)) collide = true;
        }
        if (xa > 0)
        {
            if (isBlocking(x + xa + width, y + ya - height, xa, ya, ws)) collide = true;
            if (isBlocking(x + xa + width, y + ya - height / 2, xa, ya, ws)) collide = true;
            if (isBlocking(x + xa + width, y + ya, xa, ya, ws)) collide = true;
        }
        if (xa < 0)
        {
            if (isBlocking(x + xa - width, y + ya - height, xa, ya, ws)) collide = true;
            if (isBlocking(x + xa - width, y + ya - height / 2, xa, ya, ws)) collide = true;
            if (isBlocking(x + xa - width, y + ya, xa, ya, ws)) collide = true;
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

    private boolean isBlocking(float _x, float _y, float xa, float ya, WorldState ws)
    {
        int x = (int) (_x / 16);
        int y = (int) (_y / 16);
        if (x == (int) (this.x / 16) && y == (int) (this.y / 16)) return false;

        return ws.isBlocking(x, y, xa, ya);
    }

	@Override
    public WorldState collideCheck(WorldState ws, MarioState ms)
	{
		return ws;
	}

	/*

	// if shells hit one another, they both go poof
    public boolean shellCollideCheck(ShellState shell)
    {
        if (deadTime != 0) return false;

        float xD = shell.x - x;
        float yD = shell.y - y;

        if (xD > -16 && xD < 16)
        {
            if (yD > -height() && yD < shell.height)
            {
                xa = shell.facing * 2;
                ya = -5;
                flyDeath = true;
                deadTime = 100;
                return true;
            }
        }
        return false;
    }

    public SpriteState fireballCollideCheck(SpriteState fireball)
    {
        if (deadTime != 0) return false;

        float xD = fireball.x - x;
        float yD = fireball.y - y;

        if (xD > -16 && xD < 16)
        {
            if (yD > -height && yD < 8)
            {
                xa = fireball.facing * 2;
                ya = -5;
                flyDeath = true;
                deadTime = 100;
                return true;
            }
        }
        return false;
    }

    public SpriteState bumpCheck(int xTile, int yTile, MarioState ms)
    {
        if (deadTime != 0) return;

        if (x + width > xTile * 16 && x - width < xTile * 16 + 16 && yTile == (int) ((y - 1) / 16))
        {
            xa = -ms.facing * 2;
            ya = -5;
            flyDeath = true;
            deadTime = 100;
        }
    }
	*/

}

