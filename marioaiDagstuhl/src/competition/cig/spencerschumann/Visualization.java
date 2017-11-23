package competition.cig.spencerschumann;

import javax.swing.*;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Image;

/**
 *
 * @author Spencer Schumann
 */
public class Visualization {
    private JFrame frame;
    private Edge targetFloor;
    private int[] planX;
    private int[] planY;
    private boolean planIsValid;

    private void createInternal() {
        frame = new JFrame("Monkey AI Visualization");
        frame.setBounds(400, 0, 400, 400);
        frame.setVisible(true);
    }

    private void updateInternal(Scene scene, MarioState ms, EnemySimulator enemySim) {
        if (!frame.isShowing()) return;
        int x, y;
        Image image = frame.createImage(frame.getWidth(), frame.getHeight());
        Graphics g = image.getGraphics();

        int mx = (int)(ms.x - scene.originX);
        int my = (int)(ms.y - scene.originY);
        int tx = 20 - mx + 16*11;
        int ty = 50 - my + 16*11;

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, frame.getWidth(), frame.getHeight());

        g.translate(tx, ty);

        g.setColor(Color.WHITE);
        for (Edge e : scene.floors) {
            g.drawLine((int)(e.x1 - scene.originX),
                    (int)(e.y1-1 - scene.originY),
                    (int)(e.x2-2 - scene.originX),
                    (int)(e.y2-1 - scene.originY));
        }
        g.setColor(Color.CYAN);
        for (Edge e : scene.walls) {
            g.drawLine((int)(e.x1-1 - scene.originX),
                    (int)(e.y1 - scene.originY),
                    (int)(e.x2-1 - scene.originX),
                    (int)(e.y2-2 - scene.originY));
        }
        g.setColor(Color.YELLOW);
        for (Edge e: scene.ceilings) {
            g.drawLine((int)(e.x1 - scene.originX),
                    (int)(e.y1-1 - scene.originY),
                    (int)(e.x2-2 - scene.originX),
                    (int)(e.y2-1 - scene.originY));
        }

        if (targetFloor != null) {
            g.setColor(new Color(100, 100, 255));
            g.fillRect((int)(targetFloor.x1 - scene.originX + 2),
                    (int)(targetFloor.y1+2 - scene.originY),
                    (int)(targetFloor.x2 - targetFloor.x1 - 4.0f), 3);
        }

        if (planX != null && planY != null) {
            Graphics g2 = g.create();
            if (planIsValid)
                g2.setColor(Color.GREEN);
            else
                g2.setColor(Color.PINK);
            g2.translate((int)-scene.originX,
                    (int)-scene.originY);
            g2.drawPolyline(planX, planY,
                    Math.min(planX.length, planY.length));
        }

        // Draw Mario
        // Mario's origin is somewhere at his feet.
        // TODO: if small, he is 12 high, else 24 high.
        // He is 8 wide, centered around x position.
        g.setColor(Color.RED);
        //g.drawLine(mx-8, my-8, mx+7, my+7);
        //g.drawLine(mx-8, my+7, mx+7, my-8);
        g.fillRect(mx - 4, my - (int)ms.height,
                8, (int)ms.height);


        // Fire power?  Horizontal line.
        /*if (sanitizedScene.marioMode == 2) {
        int halfY = my - (int)sanitizedScene.marioHeight / 2;
        g.drawLine(mx - 4, halfY, mx + 3, halfY);
        }*/

        /* sanitizedScene. */
        // Can jump? arrow at top.
        /*if (sanitizedScene.marioMayJump) {
        int top = my - (int) sanitizedScene.marioHeight;
        g.drawLine(mx - 4, top + 4, mx, top);
        g.drawLine(mx, top, mx + 3, top + 3);
        }*/

        // On ground? Add a foot-like line.
        /*if (sanitizedScene.marioOnGround) {
        g.drawLine(mx - 2, my - 2, mx + 1, my - 2);
        }*/

        // Carrying? Draw a circle in the middle.
        if (ms.carrying) {
            int halfY = my - (int)ms.height / 2;
            g.drawOval(mx-3, halfY-3, 5, 6);
        }

        // TODO: fireballs and such: do I even know where they are?

        // Enemies
        if (enemySim != null) {
            for (Enemy enemy: enemySim.enemies) {
                int ex = (int)(enemy.x - scene.originX);
                int ey = (int)(enemy.y - scene.originY);
                g.setColor(Color.MAGENTA);
                g.drawRect((int)(ex - enemy.width/2.0f),
                        (int)(ey - enemy.height),
                        (int)enemy.width-1, (int)enemy.height-1);
                if (enemy.safeTop) {
                    g.setColor(Color.WHITE);
                    g.drawLine((int)(ex - enemy.width/2.0f),
                            (int)(ey - enemy.height),
                            (int)(ex + enemy.width/2.0f - 1.0f),
                            (int)(ey - enemy.height));
                }
                g.drawString(Integer.toString(enemy.type), ex, ey);
            }
        }

        g.translate(-tx, -ty);
        g.setColor(Color.WHITE);
        g.drawString(Integer.toString((int)(scene.constructTime / 1000)), 30, 50);
        g.drawString(Integer.toString(tx) + "," + Integer.toString(ty), 30, 70);

        Graphics g2 = frame.getGraphics();
        g2.drawImage(image, 0, 0, null);
    }

    private void setPlanInternal(boolean isValid,
            Edge targetFloor, float[] projectedX, float[] projectedY) {
        this.targetFloor = targetFloor;
        planIsValid = isValid;
        planX = null;
        planY = null;
        if (projectedX == null || projectedY == null)
            return;
        int length = Math.min(projectedX.length, projectedY.length);
        if (length == 0)
            return;
        planX = new int[length];
        planY = new int[length];
        for (int i = 0; i < length; i++) {
            planX[i] = (int)projectedX[i];
            planY[i] = (int)projectedY[i];
        }
    }

    public void update(final Scene scene, final MarioState ms, final EnemySimulator enemySim) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // TODO: would it be safer to make a copy of the scene
                // and pass that to the event thread?
                updateInternal(scene, ms, enemySim);
            }
        });
    }

    public void setPlan(final boolean isValid, 
            final Edge targetFloor,
            final float[] projectedX, final float[] projectedY) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setPlanInternal(isValid, targetFloor, projectedX, projectedY);
            }
        });
    }

    public Visualization() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createInternal();
            }
        });
    }
}
