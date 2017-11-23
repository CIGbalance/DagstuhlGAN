package basicMap;

import static basicMap.Variables.ERROR_MSG;

public class Node {
    private int xPos;
    private int yPos;
    private boolean accessible;
    private int type = -1;

    private Node ancestor;
    private int costToGoal;
    private int costFromStart;

    public Node(int xPos, int yPos, int type) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.type = type;
        this.accessible = true;
    }

    public Node(int xPos, int yPos, int type, boolean accessible) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.type = type;
        this.accessible = accessible;
    }

    public boolean checkAccessible() {
        if (type == -1) {
            System.out.println(ERROR_MSG + "No type has been set.");
            this.accessible = false;
        } else {
            switch ()
        }
        return this.accessible;
    }

    public void setCostToGoal(int costToGoal) {
        this.costToGoal = costToGoal;
    }

    public void setCostFromStart(int costFromStart) {
        this.costFromStart = costFromStart;
    }

    public int getCostToGoal() {
        return costToGoal;
    }


    public int getCostFromStart() {
        return costFromStart;
    }

    public void setCoordinates(int xPos, int yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public void setXPosition(int xPos) {
        this.xPos = xPos;
    }

    public void setYPosition(int yPos) {
        this.yPos = yPos;
    }

    public int getXPosition() {
        return xPos;
    }

    public int getYPosition() {
        return yPos;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }

    public Node getAncestor() {
        return ancestor;
    }

    public void setAncestor(Node ancestor) {
        this.ancestor = ancestor;
    }

    public double getHeuristic(Node goal) {
        return Math.max(Math.abs(xPos - goal.xPos), Math.abs(yPos - goal.yPos));
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
