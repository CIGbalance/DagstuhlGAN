package cmatest;

import ch.idsia.mario.engine.level.Level;
import ch.idsia.tools.EvaluationInfo;
import cmatest.marioobjectives.MarioLevelObjective;
import communication.Comm;
import communication.GANProcess;
import communication.MarioProcess;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static basicMap.Settings.PY_NAME;
import static basicMap.Settings.printErrorMsg;
import static communication.Commands.START_COMM;
import static communication.ZCreator.printVectorInGson;

public class MarioEvalFunction implements IObjectiveFunction {
    private GANProcess ganProcess;
    private MarioProcess marioProcess;

    public static void main(String[] args) throws IOException {
        MarioEvalFunction mef = new MarioEvalFunction();

    }

    // changing floor will change the reason for termination
    // (in conjunction with the target value)
    // see cma.options.stopFitness


    public MarioEvalFunction() throws IOException {
        // set up process for GAN
        ganProcess = new GANProcess();
        ganProcess.start();


        // set up mario game
        marioProcess = new MarioProcess();
        marioProcess.start();

        ganProcess.commSend(START_COMM);

        System.out.println("ganProcess Receive" + ganProcess.commRecv());
    }

    static double floor = 0.0;

    public void sendZVectorToGan(double[][] x) throws IOException {
        // TODO: 07/12/2017 add send v to Gan
        String gsonVector = printVectorInGson(x);
        // send it to GAN
        ganProcess.commSend(gsonVector);
    }


    public void getGsonLevelFromGAN() {
        String response = ganProcess.commRecv();
        if (response == null || response == "") {
            printErrorMsg("No level in Gson from GAN.");
        }
    }

    @Override
    public double valueOf(double[] x) {


        // Interprete x to a level
        // TODO: 07/12/2017 check  levelFromLatentVector
        Level level = MarioLevelObjective.levelFromLatentVector(x);

        // Do a simulation
        EvaluationInfo info = this.marioProcess.simulateOneLevel(level);
        // TODO: 07/12/2017 use info to get a scalar fitness

        double tot = floor;
        for (double a : x) tot += (5+a)*(5+a);
        return tot;
    }

    @Override
    public boolean isFeasible(double[] x) {
        return true;
    }



}
