package ch.idsia.mario.simulation;

import ch.idsia.tools.EvaluationInfo;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 7, 2009
 * Time: 2:13:59 PM
 * Package: .Simulation
 */
public interface Simulation
{
    public void setSimulationOptions(SimulationOptions simulationOptions);

//    public void setAgent(Agent agent);
//
//    public void setLevelType(int levelType);
//
//    public void setLevelDifficulty(int levelDifficulty);
//
//    public void setLevelLength(int levelLength);
//
//    public Agent getAgent ();
//
//    public EvaluationInfo simulateOneLevel();
//
//    public MarioComponent getMarioComponent();

    public EvaluationInfo simulateOneLevel();
}
