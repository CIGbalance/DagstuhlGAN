package basicMap;

public class Settings {
    public static final String WARN_MSG = "[WARN] ";

    public static final String DEBUG_MSG = "[DEBUG] ";
    public static final String ERROR_MSG = "[ERROR] ";
    public static final String INFO_MSG = "[INFO] ";
    public static final double MAX_VALUE = 1000000.0;

    public static final boolean ACCESSIBLE = true;

    public static final String CMD_SEPARATOR = " ";
    public static final String PY_NAME = "generator.py";



    public static void printWarnMsg(String msg) {
        System.out.println(WARN_MSG + msg);
    }

    public static void printDebugMsg(String msg) {
        System.out.println(DEBUG_MSG + msg);
    }

    public static void printInfoMsg(String msg) {
        System.out.println(INFO_MSG + msg);
    }

    public static void printErrorMsg(String msg) {
        System.out.println(ERROR_MSG + msg);
    }
}
