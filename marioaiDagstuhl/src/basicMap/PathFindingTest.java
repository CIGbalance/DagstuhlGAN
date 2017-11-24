package basicMap;

public class PathFindingTest {
    public static void main(String[] args) {
        String dir = System.getProperty("user.dir");
        System.out.println("Working Directory = " +
                dir);
        Map map = new Map(16,16, dir + "/marioaiDagstuhl/sample.txt");
        System.out.println(map.solvable());



//        JSONObject obj = new JSONObject(" .... ");
//        String pageName = obj.getJSONObject("pageInfo").getString("pageName");
//
//        JSONArray arr = obj.getJSONArray("posts");
//        for (int i = 0; i < arr.length(); i++)
//        {
//            String post_id = arr.getJSONObject(i).getString("post_id");
//    ......
//        }
    }
}
