package communication;

public class Commands {
    /**
     * Line separator for messages.
     */
    public static final String LINE_SEP = System.getProperty("line.separator");

    /**
     * Special character to separate message ID from actual message
     */
    public static final String TOKEN_SEP = "#";

    public static final String START_COMM = "START";


    public static final String START_FAILED = "START_FAILED";

    public static final String START_SUCCEED = "START_SUCCEED";

    public static final String END_COMM = "FINISH";

    public static final String END_FAILED = "END_FAILD";

    public static final String END_SUCCEED = "END_SUCCEED";
}
