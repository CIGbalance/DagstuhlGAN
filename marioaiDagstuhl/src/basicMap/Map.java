package basicMap;

import java.io.*;
import java.util.*;

import static basicMap.Settings.*;

public class Map {
    public int width;
    public int height;
    public Node[][] grid;
    public double[][] gridCost;
    public ArrayList<Node> startPosSet;
    public ArrayList<Node> goalPosSet;

    public HashMap<Node, Double> pathCost;
    LinkedList<Node> route = new LinkedList<>();


    Random rdm = new Random();

    public Map(int width, int height, String filename) {
        this.width = width;
        this.height = height;
        this.grid = new Node[width][height];
        this.pathCost = new HashMap<Node, Double>();
        gridCost = new double[width][height];
        System.out.println(DEBUG_MSG + "Grid length is " + gridCost.length + ", [0].length is " + gridCost[0].length );
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                gridCost[i][j] = rdm.nextDouble();
            }
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));

            int lineCounter = 1;
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                int j=0;
                for (String token : line.split("")) {
                    int type = Integer.parseInt(token);
                    grid[j][i] = new Node(j,i, type);
                    System.out.println(DEBUG_MSG
                            + "(" + j + "," + i + ") is "
                            + grid[j][i].isAccessible());
                    j++;
                }
                i++;
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Check if coordinate format correct
    public boolean isLegalCoordinate(Vector2d pos) {
        if (pos == null) {
            return false;
        }
        return isLegalCoordinate((int) pos.x, (int) pos.y);
    }

    // Check if coordinate format correct
    public boolean isLegalCoordinate(int xPos, int yPos) {
        if (xPos<0 || yPos<0) {
            System.out.println(Settings.WARN_MSG + "Position (" + xPos + "," + yPos + ") not accessible.");
            return false;
        }
        if (xPos > width-1) {
            System.out.println(Settings.WARN_MSG + "Position (" + xPos + "," + yPos + ") not accessible.");
            return false;
        }
        if (yPos > height-1) {
            System.out.println(Settings.WARN_MSG + "Position (" + xPos + "," + yPos + ") not accessible.");
            return false;
        }
        return true;
    }

    // Get node by position
    public Node getNode(Vector2d pos) {
        int x = (int) pos.x;
        int y = (int) pos.y;
        if (isLegalCoordinate(x, y)) {
            return this.grid[x][y];
        }
        return null;
    }

    // Get node by position
    public Node getNode(int xPos, int yPos) {
        if (isLegalCoordinate(xPos, yPos)) {
            return this.grid[xPos][yPos];
        }
        return null;
    }

    // Check if can enter a position
    public boolean isAccessible(int xPos, int yPos) {
        if (isLegalCoordinate(xPos, yPos)) {
            return (this.grid[xPos][yPos].isAccessible() == ACCESSIBLE);
        } 
        return false;
    }

    public void initStartPosSet() {
        startPosSet = new ArrayList<>();
        if (this.grid != null) {
            boolean canAdd = true;
            int y=height-1;
            while (y>=0) {
                if (isAccessible(0,y)) {
                    System.out.println(DEBUG_MSG + "Position (" + 0 + "," + y + ") accessible.");

                    if (canAdd) {
                        startPosSet.add(getNode(0, y));
                        canAdd = false;
                    }
                } else {
                    canAdd = true;
                }
                y--;
            }
        } else {
            System.out.println(WARN_MSG + " grid is null");
        }
    }

    public void initGoalPosSet() {
        goalPosSet = new ArrayList<>();
        if (this.grid != null) {
            boolean canAdd = true;
            int y=height-1;
            while (y>=0) {
                if (isAccessible(width-1,y)) {
                    if (canAdd) {
                        goalPosSet.add(getNode(width-1, y));
                        canAdd = false;
                    }
                } else {
                    canAdd = true;
                }
                y--;
            }
        } else {
            System.out.println(WARN_MSG + " grid is null");
        }
    }

    public Node getGoalPos(int idx) {
        if (idx<0) {
            System.out.println(Settings.ERROR_MSG + " idx is negative." );
            return null;
        }
        if (goalPosSet == null) {
            System.out.println(Settings.ERROR_MSG + " goalPosSet is null." );
            return null;
        }
        if (goalPosSet.isEmpty()) {
            System.out.println(Settings.INFO_MSG + " No goal point available." );
            return null;
        }
        if (idx>goalPosSet.size()-1) {
            System.out.println(Settings.ERROR_MSG + " Out of bound: idx > goalPosSet.size()-1." );
            return null;
        }
        return this.goalPosSet.get(idx);
    }

    public Node getStartPos(int idx) {
        if (idx<0) {
            System.out.println(Settings.ERROR_MSG + " idx is negative." );
            return null;
        }
        if (startPosSet == null) {
            System.out.println(Settings.ERROR_MSG + " startPosSet is null." );
            return null;
        }
        if (startPosSet.isEmpty()) {
            System.out.println(Settings.INFO_MSG + " No starting point available." );
            return null;
        }
        if (idx>startPosSet.size()-1) {
            System.out.println(Settings.ERROR_MSG + " Out of bound: idx > startPosSet.size()-1." );
            return null;
        }
        return this.startPosSet.get(idx);
    }


    public LinkedList<Node> findPath(Node start, Node goal) {
        HashMap<Node, Double> gScore = initScoreMap(MAX_VALUE);
        HashMap<Node, Double> fScore = initScoreMap(MAX_VALUE);

        LinkedList<Node> openList = new LinkedList<>();
        LinkedList<Node> closedList = new LinkedList<>();

        // Init path trace
        HashMap<Node, Node> ancestors = new HashMap<>();
        gScore.replace(start, 0.0);
        fScore.replace(start, start.getHeuristic(goal)); // TODO: 23/11/2017
        openList.add(start);

        // Loop over
        Node current;
        while (!openList.isEmpty()) {
            current = openList.poll();
            if (current == null) {
                System.out.println(ERROR_MSG + " Current node is null.");
            }
            openList.remove(current);

            // Yeeeeeeeep
            if (current == goal) {
                while (current != null) {
                    route.add(0, current);
                    current = ancestors.get(current);
                }
                return route;
                //return reconstructPath(start, current);
            }

            closedList.add(current);

            // Find neighbours
            List<Node> neighbours = getNeighbours(current);
            for (Node neighbour: neighbours) {
                if (closedList.contains(neighbour)) {
                    continue;
                }
                double tentativeScore = gScore.get(current) + 1 + gridCost[neighbour.getXPosition()][neighbour.getYPosition()];

                boolean contains = openList.contains(neighbour);
                if (!contains || tentativeScore < gScore.get(neighbour)) {
                    gScore.put(neighbour, tentativeScore);
                    fScore.put(neighbour, tentativeScore + neighbour.getHeuristic(goal));

                    if (contains) {
                        openList.remove(neighbour);
                    }

                    openList.offer(neighbour);
                    ancestors.put(neighbour, current);
                }
            }
        }
        return null;
    }

    public HashMap<Node, Double> initScoreMap(Double defaultValue) {
        HashMap<Node, Double> map = new HashMap<>();
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                map.put(getNode(i,j), defaultValue);
            }
        }
        return map;
    }

    public LinkedList<Node> reconstructPath(Node start, Node goal) {
        LinkedList<Node> path = new LinkedList<>();
        Node current = goal;
        boolean done = false;
        while (!done) {
            path.addFirst(current);
            current = current.getAncestor();
            if (current.equals(start)) {
                done = true;
            }
        }
        return path;
    }

    private List<Node> getNeighbours(Node node) {
        if (node == null) {
            System.err.println(ERROR_MSG + "NULL node is passed to getNeighbours.");
        }
        int x = node.getXPosition();
        int y = node.getYPosition();
        List<Node> neighbour = new LinkedList<Node>();

        Node tmp;
        if (x > 0) {
            tmp = this.getNode((x - 1), y);
            if (tmp.isAccessible()) {
                neighbour.add(tmp);
            }
        }

        if (x < width-1) {
            tmp = this.getNode((x + 1), y);
            if (tmp.isAccessible()) {
                neighbour.add(tmp);
            }
        }

        if (y > 0) {
            tmp = this.getNode(x, (y - 1));
            if (tmp.isAccessible()) {
                neighbour.add(tmp);
            }
        }

        if (y < height-1) {
            tmp = this.getNode(x, (y + 1));
            if (tmp.isAccessible()) {
                neighbour.add(tmp);
            }
        }

        if (x < width-1 && y < height-1) {
            tmp = this.getNode((x + 1), (y + 1));
            if (tmp.isAccessible()) {
                neighbour.add(tmp);
            }
        }

        if (x > 0 && y > 0) {
            tmp = this.getNode((x - 1), (y - 1));
            if (tmp.isAccessible()) {
                neighbour.add(tmp);
            }
        }

        if (x > 0 && y < height-1) {
            tmp = this.getNode((x - 1), (y + 1));
            if (tmp.isAccessible()) {
                neighbour.add(tmp);
            }
        }

        if (x < width-1 && y > 0) {
            tmp = this.getNode((x + 1), (y - 1));
            if (tmp.isAccessible()) {
                neighbour.add(tmp);
            }
        }

        return neighbour;
    }

    public int solvable() {
        int nbSolution = 0;
        initGoalPosSet();
        initStartPosSet();
        System.out.println(INFO_MSG + "Number of starters: " + this.startPosSet.size());
        System.out.println(INFO_MSG + "Number of goals: " + this.goalPosSet.size());

        for (Node start: this.startPosSet) {
            for (Node goal: this.goalPosSet) {


                LinkedList<Node> path = findPath(start, goal);
                if (path != null) {
                    System.out.println(INFO_MSG + "Find route: ("
                            + start.getXPosition() + ","+ start.getYPosition()
                            + ")->("
                            + goal.getXPosition() + ","+ goal.getYPosition()
                            + ")");
                    nbSolution++;
                } else {
                    System.out.println(INFO_MSG + "NOT find route: ("
                            + start.getXPosition() + ","+ start.getYPosition()
                            + ")->("
                            + goal.getXPosition() + ","+ goal.getYPosition()
                            + ")");
                }
            }
        }
        return nbSolution;
    }

    public int calculateCoins() {
        int nbCoins = 0;
        for (int i=0; i<width; i++) {
            for (int j = 0; j < height; j++) {
                Node node = grid[i][j];
                if (node.getType() == 10) {
                    nbCoins++;
                }
            }
        }
        return nbCoins;
    }
}
