package ch.idsia.tools;

import ch.idsia.mario.engine.GlobalOptions;
import ch.idsia.mario.environments.Environment;
import ch.idsia.mario.simulation.BasicSimulator;
import ch.idsia.mario.simulation.Simulation;
import ch.idsia.tools.tcp.Server;
import ch.idsia.tools.tcp.ServerAgent;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 6, 2009
 * Time: 8:12:18 PM
 * Package: .Tools
 */

public class Evaluator implements Runnable
{
    Thread thisThread = null;
    EvaluationOptions evaluationOptions;

    private List<EvaluationInfo> evaluationSummary = new ArrayList<EvaluationInfo>();

    private void evaluateServerMode()
    {
        Server server = new Server(evaluationOptions.getServerAgentPort(), Environment.numberOfObservationElements, Environment.numberOfButtons);
        evaluationOptions.setAgent(new ServerAgent(server, evaluationOptions.isFastTCP()));

        Simulation simulator = new BasicSimulator(evaluationOptions.getSimulationOptionsCopy());
        while (server.isRunning())
        {
            String resetData = server.recvUnSafe();
            if (resetData.startsWith("ciao"))
            {
                System.out.println("Evaluator: ciao received from client; restarting server");
                server.restartServer();
                continue;
            }
            if (resetData.startsWith("reset"))
            {
                resetData = resetData.split("reset\\s*")[1];
                evaluationOptions.setUpOptions(resetData.split("[\\s]+"));
                //TODO: Fix this in more general way
                ((ServerAgent)evaluationOptions.getAgent()).setFastTCP(evaluationOptions.isFastTCP());
                init(evaluationOptions);
                // Simulate One Level
                EvaluationInfo evaluationInfo;

                long startTime = System.currentTimeMillis();
                String startMessage = "Evaluation started at " + GlobalOptions.getDateTime(null);
//                LOGGER.println(startMessage, LOGGER.VERBOSE_MODE.ALL);

                simulator.setSimulationOptions(evaluationOptions);
                evaluationInfo = simulator.simulateOneLevel();

                evaluationInfo.levelType = evaluationOptions.getLevelType();
                evaluationInfo.levelDifficulty = evaluationOptions.getLevelDifficulty();
                evaluationInfo.levelRandSeed = evaluationOptions.getLevelRandSeed();
                evaluationSummary.add(evaluationInfo);
//                LOGGER.VERBOSE_MODE VM = (evaluationInfo.marioStatus == Mario.STATUS_WIN) ? LOGGER.VERBOSE_MODE.INFO : LOGGER.VERBOSE_MODE.ALL;
//                LOGGER.println("run finished with result : " + evaluationInfo, VM);

                String fileName = "";
                if (!this.evaluationOptions.getMatlabFileName().equals(""))
                    fileName = exportToMatLabFile();
                Collections.sort(evaluationSummary, new evBasicFitnessComparator());

//                LOGGER.println("Entire Evaluation Finished with results:", LOGGER.VERBOSE_MODE.ALL);
//                for (EvaluationInfo ev : evaluationSummary)
//                {
//                    LOGGER.println(ev.toString(), LOGGER.VERBOSE_MODE.ALL);
//                }
                long currentTime = System.currentTimeMillis();
                long elapsed = currentTime - startTime;
//                LOGGER.println(startMessage, LOGGER.VERBOSE_MODE.ALL);
//                LOGGER.println("Evaluation Finished at " + GlobalOptions.getDateTime(null), LOGGER.VERBOSE_MODE.ALL);
//                LOGGER.println("Total Evaluation Duration (HH:mm:ss:ms) " + GlobalOptions.getDateTime(elapsed), LOGGER.VERBOSE_MODE.ALL);
//                if (!fileName.equals(""))
//                    LOGGER.println("Exported to " + fileName, LOGGER.VERBOSE_MODE.ALL);
//                return evaluationSummary;
            }
            else
            {
                System.err.println("Evaluator: Message <" + resetData + "> is incorrect client behavior. Exiting evaluation...");
                server.restartServer();
            }
        }
    }

    public List<EvaluationInfo> evaluate()
    {
        if (this.evaluationOptions.isServerMode() )
        {
            this.evaluateServerMode();
            return null;
        }


        Simulation simulator = new BasicSimulator(evaluationOptions.getSimulationOptionsCopy());
        // Simulate One Level

        EvaluationInfo evaluationInfo;

        long startTime = System.currentTimeMillis();
        String startMessage = "Evaluation started at " + GlobalOptions.getDateTime(null);
//        LOGGER.println(startMessage, LOGGER.VERBOSE_MODE.ALL);

//        boolean continueCondition;
//        int i = 0;
//        do
//        {
//            LOGGER.println("Attempts left: " + (evaluationOptions.getNumberOfTrials() - ++i ), LOGGER.VERBOSE_MODE.ALL);
            evaluationInfo = simulator.simulateOneLevel();
                                                        
            evaluationInfo.levelType = evaluationOptions.getLevelType();
            evaluationInfo.levelDifficulty = evaluationOptions.getLevelDifficulty();
            evaluationInfo.levelRandSeed = evaluationOptions.getLevelRandSeed();
            evaluationSummary.add(evaluationInfo);
//            LOGGER.VERBOSE_MODE VM = (evaluationInfo.marioStatus == Mario.STATUS_WIN) ? LOGGER.VERBOSE_MODE.INFO : LOGGER.VERBOSE_MODE.ALL;
//            LOGGER.println("run  finished with result : " + evaluationInfo, VM);
//            continueCondition = !GlobalOptions.StopSimulationIfWin || !(evaluationInfo.marioStatus == Mario.STATUS_WIN);
//        }
//        while ((evaluationOptions.getNumberOfTrials() > i || evaluationOptions.getNumberOfTrials() == -1 ) && continueCondition);

        String fileName = "";
        if (!this.evaluationOptions.getMatlabFileName().equals(""))
           fileName = exportToMatLabFile();
        Collections.sort(evaluationSummary, new evBasicFitnessComparator());

//        LOGGER.println("Entire Evaluation Finished with results:", LOGGER.VERBOSE_MODE.ALL);
//        for (EvaluationInfo ev : evaluationSummary)
//        {
//             LOGGER.println(ev.toString(), LOGGER.VERBOSE_MODE.ALL);
//        }
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - startTime;
//        LOGGER.println(startMessage, LOGGER.VERBOSE_MODE.ALL);
//        LOGGER.println("Evaluation Finished at " + GlobalOptions.getDateTime(null), LOGGER.VERBOSE_MODE.ALL);
//        LOGGER.println("Total Evaluation Duration (HH:mm:ss:ms) " + GlobalOptions.getDateTime(elapsed), LOGGER.VERBOSE_MODE.ALL);
//        if (!fileName.equals(""))
//            LOGGER.println("Exported to " + fileName, LOGGER.VERBOSE_MODE.ALL);
        return evaluationSummary;
    }

//    public void verbose(String message, LOGGER.VERBOSE_MODE verbose_mode)
//    {
//        LOGGER.println(message, verbose_mode);
//    }

    public void getMeanEvaluationSummary()
    {
        //TODO: SK
    }

    public String exportToMatLabFile()
    {
        FileOutputStream fos;
        String fileName = this.evaluationOptions.getMatlabFileName() + ".m";
        try {

            fos = new FileOutputStream(fileName);              
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.newLine();
            bw.write("%% " + this.evaluationOptions.getAgent().getName());
            bw.newLine();
            bw.write("% BasicFitness ");            
            bw.newLine();
            bw.write("Attempts = [1:" + evaluationSummary.size() + "];");
            bw.newLine();
            bw.write("% BasicFitness ");
            bw.newLine();
            bw.write("BasicFitness = [");
            for (EvaluationInfo ev : evaluationSummary)
                bw.write(String.valueOf(ev.computeBasicFitness()) + " ");
            bw.write("];");
            bw.newLine();
            bw.write("plot(Attempts,BasicFitness, '.')");
            bw.close();
            return fileName;
        }
        catch (FileNotFoundException e)  {  e.printStackTrace(); return "Null" ;       }
        catch (IOException e) {     e.printStackTrace();  return "Null";      }
    }

    public void exportToPyPlot(String fileName)
    {
        //TODO:SK
    }

    public void reset()
    {
        evaluationSummary = new ArrayList<EvaluationInfo>();
    }

    public Evaluator(EvaluationOptions evaluationOptions)
    {                      
        init(evaluationOptions);
    }

    public void run()
    {
        evaluate();
    }

    public void start()
    {
        if (thisThread.getState() == Thread.State.NEW)
            thisThread.start();
    }

    public void init(EvaluationOptions evaluationOptions)
    {
        ToolsConfigurator.CreateMarioComponentFrame(
                evaluationOptions);
        
        GlobalOptions.pauseWorld = evaluationOptions.isPauseWorld();
        this.evaluationOptions = evaluationOptions;
        if (thisThread == null)
            thisThread = new Thread(this);
    }
}

class evBasicFitnessComparator implements Comparator
{
    public int compare(Object o, Object o1)
    {
        double ei1Fitness = ((EvaluationInfo)(o)).computeBasicFitness();
        double ei2Fitness = ((EvaluationInfo)(o1)).computeBasicFitness();
        if (ei1Fitness < ei2Fitness)
            return 1;
        else if (ei1Fitness > ei2Fitness)
            return -1;
        else
            return 0;
    }
}

class evCoinsFitnessComparator implements Comparator
{
    public int compare(Object o, Object o1)
    {
        int ei1Fitness = ((EvaluationInfo)(o)).numberOfGainedCoins;

        int ei2Fitness = ((EvaluationInfo)(o1)).numberOfGainedCoins;
        if (ei1Fitness < ei2Fitness)
            return 1;
        else if (ei1Fitness > ei2Fitness)
            return -1;
        else
            return 0;
    }
}

class evDistanceFitnessComparator implements Comparator
{
    public int compare(Object o, Object o1)
    {
        double ei1Fitness = ((EvaluationInfo)(o)).computeDistancePassed();
        double ei2Fitness = ((EvaluationInfo)(o1)).computeDistancePassed();
        if (ei1Fitness < ei2Fitness)
            return 1;
        else if (ei1Fitness > ei2Fitness)
            return -1;
        else
            return 0;
    }
}