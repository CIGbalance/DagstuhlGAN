package wox.serial;

public class EasyTest {
    public static void main(String[] args) {
        TestObject ob = new TestObject(5);
        System.out.println(ob.inc());
        Easy.save(ob, "test.xml");
        Object back = Easy.load("test.xml");
        System.out.println("Loaded object back in");
        Easy.save(back, "back.xml");
        System.out.println(((TestObject) back).inc());
    }
}
