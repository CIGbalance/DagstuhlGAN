package competition.cig.spencerschumann;

import ch.idsia.mario.environments.Environment;

/**
 *
 * @author Spencer Schumann
 */
public class MarioState {

    public float x;
    public float y;
    public float vx;
    public float vy;
    public int mode;
    public float height;
    public boolean carrying;
    public boolean onGround;
    public boolean mayJump;
    public int jumpTime;

    private boolean first = true;

    public void update(Environment observation) {
        mode = observation.getMarioMode();
        if (mode > 0) {
            height = 24.0f;
        } else {
            height = 12.0f;
        }

        carrying = observation.isMarioCarrying();
        onGround = observation.isMarioOnGround();
        mayJump = observation.mayMarioJump();

        float [] pos = observation.getMarioFloatPos();
        if (first) {
            first = false;
            x = pos[0];
            y = pos[1];
        }
        float newVx = pos[0] - x;
        float newVy = pos[1] - y;
        vx = newVx;
        vy = newVy;
        x = pos[0];
        y = pos[1];

        // If cheat code is there...
        //if (true) {
            //vx = pos[2];
            //vy = pos[3];
            //jumpTime = (int)pos[4];
        //}
    }

    @Override
    public MarioState clone() {
        MarioState m = new MarioState();
        m.x = x;
        m.y = y;
        m.vx = vx;
        m.vy = vy;
        m.first = first;
        m.mode = mode;
        m.height = height;
        m.carrying = carrying;
        m.onGround = onGround;
        m.mayJump = mayJump;
        m.jumpTime = jumpTime;
        return m;
    }

    public boolean equals(MarioState other) {
        return x == other.x &&
                y == other.y &&
                //vx == other.vx &&
                //vy == other.vy &&
                first == other.first &&
                mode == other.mode &&
                height == other.height &&
                jumpTime == other.jumpTime &&
                carrying == other.carrying &&
                mayJump == other.mayJump &&
                onGround == other.onGround;
    }

    public void diff(final String msg, final MarioState other) {
        if (this.equals(other))
            return;

        System.out.print(msg + ": ");
        if (x != other.x)
            System.out.print(String.format("x:(%f,%f) ", x, other.x));
        if (y != other.y)
            System.out.print(String.format("y:(%f,%f) ", y, other.y));
        if (vx != other.vx)
            System.out.print(String.format("vx:(%f,%f) ", vx, other.vx));
        if (vy != other.vy)
            System.out.print(String.format("vy:(%f,%f) ", vy, other.vy));
        if (first != other.first)
            System.out.print(String.format("first:(%s,%s) ", Boolean.toString(first), Boolean.toString(other.first)));
        if (mode != other.mode)
            System.out.print(String.format("mode:(%d,%d) ", mode, other.mode));
        if (height != other.height)
            System.out.print(String.format("height:(%f,%f) ", height, other.height));
        if (jumpTime != other.jumpTime)
            System.out.print(String.format("jumpTime:(%d,%d) ", jumpTime, other.jumpTime));
        if (carrying != other.carrying)
            System.out.print(String.format("carrying:(%s,%s) ", Boolean.toString(carrying), Boolean.toString(other.carrying)));
        if (mayJump != other.mayJump)
            System.out.print(String.format("mayJump:(%s,%s) ", Boolean.toString(mayJump), Boolean.toString(other.mayJump)));
        if (onGround != other.onGround)
            System.out.print(String.format("onGround:(%s,%s) ", Boolean.toString(onGround), Boolean.toString(other.onGround)));
        System.out.println("");
    }
}
