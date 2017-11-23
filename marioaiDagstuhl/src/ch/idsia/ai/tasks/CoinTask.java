package ch.idsia.ai.tasks;

import ch.idsia.ai.agents.Agent;
import ch.idsia.tools.EvaluationOptions;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 8, 2009
 * Time: 11:28:56 AM
 * Package: ch.idsia.ai.tasks;
 */
public class CoinTask implements Task {

    private EvaluationOptions options = new EvaluationOptions ();

    public double[] evaluate(Agent controller) {
        return new double[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setOptions(EvaluationOptions options) {
        this.options = options;
    }

    public EvaluationOptions getOptions() {
        return options;
    }
}
