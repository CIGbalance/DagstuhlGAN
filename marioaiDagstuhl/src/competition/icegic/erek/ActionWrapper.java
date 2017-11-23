package competition.icegic.erek;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: espeed
 * Date: Aug 16, 2009
 * Time: 1:56:04 AM
 * Holds an boolean array for actions
 */
public class ActionWrapper {

    public boolean[] acts;

    public ActionWrapper(int size)
    {
          acts = new boolean[size];
    }

    public void add(int idx, boolean val)
    {
        acts[idx] = val;
    }

    public ActionWrapper clone()
    {
        ActionWrapper ret = new ActionWrapper(acts.length);
        for(int i = 0; i<acts.length; i++)
        {
            ret.acts[i] = acts[i];
        }
        return ret;
    }
}
