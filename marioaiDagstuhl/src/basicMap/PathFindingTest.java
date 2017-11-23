package basicMap;

public class PathFindingTest {
    public static void main(String[] args) {
        String dir = System.getProperty("user.dir");
        System.out.println("Working Directory = " +
                dir);
        Map map = new Map(16,16, dir + "/marioaiDagstuhl/sample.txt");
        System.out.println(map.solvable());

    }
}
