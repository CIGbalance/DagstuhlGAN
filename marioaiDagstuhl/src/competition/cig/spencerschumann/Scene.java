package competition.cig.spencerschumann;

import ch.idsia.mario.environments.Environment;
import java.util.ArrayList;

/**
 *
 * @author Spencer Schumann
 */
public class Scene {

    public ArrayList<Edge> floors = new ArrayList<Edge>();
    public ArrayList<Edge> walls = new ArrayList<Edge>();
    public ArrayList<Edge> ceilings = new ArrayList<Edge>();
    public ArrayList<BumpableEdge> bumpables = new ArrayList<BumpableEdge>();
    public ArrayList<Edge> enemyEmitters = new ArrayList<Edge>();
    public long constructTime;

    // The extreme top left corner of the scene, in world coordinates
    public float originX;
    public float originY;

    @Override
    public Scene clone() {
        // TODO: I should really be calling s.clone() here; see Object.clone().
        Scene s = new Scene();
        s.update(this);
        //s.pos = pos.clone();
        return s;
    }

    public void update(Scene scene) {
        // TODO: no deep copy of edges; will this cause problems?
        clearEdges();
        add(scene);
        originX = scene.originX;
        originY = scene.originY;
        //marioHeight = scene.marioHeight;
        // TODO: copy the other mario attributes
    }

    public void clearEdges() {
        floors.clear();
        walls.clear();
        ceilings.clear();
        bumpables.clear();
        enemyEmitters.clear();
    }

    private Scene() {
    }

    //private SanitizedScene(Environment observation, PosTracker pos) {
    //    this.pos = pos;
    //    setMarioState(observation);
    //}

    public Scene(float originX, float originY) {
        this.originX = originX;
        this.originY = originY;
    }

    public Scene(Environment observation, byte[][] scene) {
        long startTime = System.nanoTime();

        float [] marioPos = observation.getMarioFloatPos();
        originX = (float) Math.floor(marioPos[0] / 16.0f) * 16.0f - observation.HalfObsWidth * 16.0f;
        originY = (float) Math.floor(marioPos[1] / 16.0f) * 16.0f - observation.HalfObsHeight * 16.0f;

        boolean[][] visited = new boolean[scene.length][scene[0].length];
        int x, y;
        for (y = 0; y < scene.length; y++) {
            for (x = 0; x < scene[y].length; x++) {
                byte tile = scene[y][x];
                if (tile == Tiles.COIN) {
                    // TODO
                } else if (!visited[y][x]) {
                    if (Tiles.isWall(tile)) {
                        Scene block = new Scene(originX, originY);
                        block.expandWall(scene, visited, x, y);
                        add(block);
                    } else if (tile == Tiles.LEDGE) {
                        Scene ledge = new Scene(originX, originY);
                        ledge.expandLedge(scene, visited, x, y);
                        add(ledge);
                    }
                }
            }
        }
        // TODO: update bumpables and EnemyEmitters, if not already done
        constructTime = System.nanoTime() - startTime;
    }

    // Expand vectorized block from an initial starting point, and mark
    // all tiles that are part of this block as visited
    private void expandWall(byte[][] scene, boolean[][] visited, int x, int y) {
        if (visited[y][x]) {
            return;
        }
        visited[y][x] = true;
        // left side
        if (x > 0) {
            if (Tiles.isWall(scene[y][x - 1])) {
                expandWall(scene, visited, x - 1, y);
            } else {
                walls.add(new Edge(originX + x * 16.0f, originY + y * 16.0f,
                        originX + x * 16.0f, originY + (y + 1) * 16.0f));
            }
        }
        // right side
        if (x < scene[y].length - 1) {
            if (Tiles.isWall(scene[y][x + 1])) {
                expandWall(scene, visited, x + 1, y);
            } else {
                walls.add(new Edge(originX + (x + 1) * 16.0f, originY + y * 16.0f,
                        originX + (x + 1) * 16.0f, originY + (y + 1) * 16.0f));
            }
        }
        // top side
        if (y > 0) {
            if (Tiles.isWall(scene[y - 1][x])) {
                expandWall(scene, visited, x, y - 1);
            } else {
                floors.add(new Edge(originX + x * 16.0f, originY + y * 16.0f,
                        originX + (x + 1) * 16.0f, originY + y * 16.0f));
            }
        }
        // bottom side
        if (y < scene.length - 1) {
            if (Tiles.isWall(scene[y + 1][x])) {
                expandWall(scene, visited, x, y + 1);
            } else {
                ceilings.add(new Edge(originX + x * 16.0f, originY + (y + 1) * 16.0f,
                        originX + (x + 1) * 16.0f, originY + (y + 1) * 16.0f));
            }
        }
        coalesce();
    }

    // Expand ledge
    private void expandLedge(byte[][] scene, boolean[][] visited, int x, int y) {
        if (visited[y][x]) {
            return;
        }
        visited[y][x] = true;
        int startx = x;
        int endx = x;
        // Find left side of ledge
        while (startx > 0 && scene[y][startx - 1] == Tiles.LEDGE) {
            startx--;
            visited[y][startx] = true;
        }
        // Find right side of ledge
        while (endx < scene[y].length - 1 && scene[y][endx + 1] == Tiles.LEDGE) {
            endx++;
            visited[y][endx] = true;
        }
        floors.add(new Edge(originX + startx * 16.0f, originY + y * 16.0f,
                originX + (endx + 1) * 16.0f, originY + y * 16.0f));
    }

    // Coalesce adjacent edges of the same type into one
    private void coalesce() {
        // TODO
        // Note: should I have a contiguous ceiling, or break it up for
        // each special?
        coalesce(walls);
        coalesce(ceilings);
        coalesce(floors);
        coalesce(enemyEmitters);
        // NOTE: bumpables shouldn't be coalesced.
    }

    private void coalesce(ArrayList<Edge> edges) {
        // Super stupid way for now.
        // TODO: optimize.  Without coalesce, everything runs in about
        // 60 microseconds max.  With coalesce, times get up to around 6
        // milliseconds.  That's a large chunk of the allotted 40 ms to
        // be wasting on this.
        boolean foundOne = true;
        while (foundOne) {
            foundOne = false;
            for (Edge a : edges) {
                for (Edge b : edges) {
                    if (a == b) {
                        continue;
                    }
                    foundOne = true;
                    if (a.x1 == b.x1 && a.y1 == b.y1) {
                        // Overlapping?  Something is wrong...
                        throw new RuntimeException("Overlapping edges!");
                    } else if (a.x1 == b.x2 && a.y1 == b.y2) {
                        a.x1 = b.x1;
                        a.y1 = b.y1;
                    } else if (a.x2 == b.x1 && a.y2 == b.y1) {
                        a.x2 = b.x2;
                        a.y2 = b.y2;
                    } else {
                        foundOne = false;
                    }

                    if (foundOne) {
                        edges.remove(b);
                        break;
                    }
                }
                if (foundOne) {
                    break;
                }
            }
        }
    }

    // Add the edges in the subscene to this scene
    private void add(Scene subscene) {
        floors.addAll(subscene.floors);
        walls.addAll(subscene.walls);
        ceilings.addAll(subscene.ceilings);
        bumpables.addAll(subscene.bumpables);
        enemyEmitters.addAll(subscene.enemyEmitters);
    }
}