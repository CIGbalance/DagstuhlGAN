package ch.idsia.maibe.tasks;

import ch.idsia.ai.agents.Agent;
import ch.idsia.tools.CmdLineOptions;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 8, 2009
 * Time: 11:20:41 AM
 * Package: ch.idsia.maibe.tasks
 */

public interface Task
{
    public float[] evaluate (Agent controller);

    public void setOptions (CmdLineOptions options);

    public CmdLineOptions getOptions ();

    void doEpisodes(int amount, boolean verbose);

    boolean isFinished();

    void reset();
}
