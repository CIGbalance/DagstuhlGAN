package basicMap;

public class Node {
    private int xPos;
    private int yPos;
    private boolean accessible;

    private Node ancestor;
    private int costToGoal;
    private int costFromStart;

    public Node(int xPos, int yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.accessible = true;
    }

    public Node(int xPos, int yPos, boolean accessible) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.accessible = accessible;
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
}
