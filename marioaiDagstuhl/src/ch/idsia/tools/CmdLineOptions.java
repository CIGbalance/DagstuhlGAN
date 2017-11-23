package ch.idsia.tools;

import ch.idsia.mario.engine.GlobalOptions;
import ch.idsia.ai.agents.AgentsPool;

import java.util.Map;


/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 25, 2009
 * Time: 9:05:20 AM
 * Package: ch.idsia.tools
 */

/**
 * The <code>CmdLineOptions</code> class handles the commandline options received from actual
 * command line or through TCP interface. It sets up parameters from command line if there are any.
 * Defaults are used otherwise.
 *
 * @author  Sergey Karakovskiy
 * @version 1.0, Apr 25, 2009
 *
 * @see ch.idsia.utils.ParameterContainer
 * @see ch.idsia.tools.EvaluationOptions
 *
 * @since   iMario1.0
 */

public class CmdLineOptions extends EvaluationOptions
{
    // TODO: SK Move default options to xml, properties, beans, whatever.. //relevant?
    public CmdLineOptions(String[] args)
    {
        super();
        if (args.length > 1 && !args[0].startsWith("-") /*starts with a path to agent then*/)
        {
            this.setAgent(args[0]);

            String[] shiftedargs = new String[args.length - 1];
            System.arraycopy(args, 1, shiftedargs, 0, args.length - 1);
            this.setUpOptions(shiftedargs);
        }
        else
            this.setUpOptions(args);

        if (isEcho())
        {
            System.out.println("\nOptions have been set to:");
            for (Map.Entry<String,String> el : optionsHashMap.entrySet())
                System.out.println(el.getKey() + ": " + el.getValue());
        }
        GlobalOptions.GameVeiwerContinuousUpdatesOn = isGameViewerContinuousUpdates();        
    }

    public Boolean isToolsConfigurator() {
        return b(getParameterValue("-tc"));      }

    public Boolean isGameViewer() {
        return b(getParameterValue("-gv"));      }

    public Boolean isGameViewerContinuousUpdates() {
        return b(getParameterValue("-gvc"));      }

    public Boolean isEcho() {
        return b(getParameterValue("-echo"));      }
}
