package ch.idsia.tools;

import ch.idsia.mario.engine.sprites.Mario;

import java.text.DecimalFormat;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 12, 2009
 * Time: 12:44:51 AM
 * Package: .Tools
 */
public class EvaluationInfo
{
    private static final int MagicNumberUndef = -42;
    public int levelType = MagicNumberUndef;
    public int marioStatus = MagicNumberUndef;
    public int livesLeft = MagicNumberUndef;
    public double lengthOfLevelPassedPhys = MagicNumberUndef;
    public int lengthOfLevelPassedCells = MagicNumberUndef;
    public int totalLengthOfLevelCells = MagicNumberUndef;
    public double totalLengthOfLevelPhys = MagicNumberUndef;
    public int timeSpentOnLevel = MagicNumberUndef;
    public int totalTimeGiven = MagicNumberUndef;
    public int numberOfGainedCoins = MagicNumberUndef;
//    public int totalNumberOfCoins = MagicNumberUndef;
    public int totalActionsPerfomed = MagicNumberUndef;
    public int jumpActionsPerformed = MagicNumberUndef;
    public int totalFramesPerfomed = MagicNumberUndef;
    // Number Of collisions with creatures
    // if large
    // if fire
    public String Memo = "";
    public int timeLeft = MagicNumberUndef;
    public String agentName = "undefinedAgentName";
    public String agentType = "undefinedAgentType";
    public int levelDifficulty = MagicNumberUndef;
    public int levelRandSeed = MagicNumberUndef;
    public int marioMode = MagicNumberUndef;
    public int killsTotal = MagicNumberUndef;

    public double computeBasicFitness()
    {
        // neglect totalActionsPerfomed;
        // neglect totalLengthOfLevelCells;
        // neglect totalNumberOfCoins;
        return (lengthOfLevelPassedPhys - timeSpentOnLevel + numberOfGainedCoins + marioStatus*5000)/5000;
    }

    public double computeDistancePassed()
    {
        return lengthOfLevelPassedPhys;
    }

    public int computeKillsTotal()
    {
        return this.killsTotal;
    }
    
    public double computeJumpFraction(){
        return (double)this.jumpActionsPerformed/this.totalActionsPerfomed;
    }

    //TODO: possible fitnesses adjustments: penalize for collisions with creatures and especially for  suicide. It's a sin.

    public double [] toDouble()
    {
        
        return new double[]
                {
                        marioStatus,
                        lengthOfLevelPassedPhys,
                        totalLengthOfLevelCells,
                        timeSpentOnLevel,
                        numberOfGainedCoins,
//                        totalNumberOfCoins,
                        totalActionsPerfomed,
                        totalFramesPerfomed,
                        computeBasicFitness()
                };
    }

    private DecimalFormat df = new DecimalFormat("0.00");

    public String toString()
    {

        String ret = "\nStatistics. Score:";
        ret += "\n                  Player/Agent type : " + agentType;
        ret += "\n                  Player/Agent name : " + agentName;
        ret += "\n                       Mario Status : " + ((marioStatus == Mario.STATUS_WIN) ? "Win!" : "Loss...");
        ret += "\n                         Level Type : " + levelType;
        ret += "\n                   Level Difficulty : " + levelDifficulty;
        ret += "\n                    Level Rand Seed : " + levelRandSeed;
        ret += "\n                         Lives Left : " + livesLeft;
        ret += "\nTotal Length of Level (Phys, Cells) : " + "(" + totalLengthOfLevelPhys + "," + totalLengthOfLevelCells + ")";
        ret += "\n                      Passed (Phys) : " + df.format(lengthOfLevelPassedPhys / totalLengthOfLevelPhys *100) + "% ( " + df.format(lengthOfLevelPassedPhys) + " of " + totalLengthOfLevelPhys + ")";
        ret += "\n                     Passed (Cells) : " + df.format((double)lengthOfLevelPassedCells / totalLengthOfLevelCells *100) + "% ( " + lengthOfLevelPassedCells + " of " + totalLengthOfLevelCells + ")";
        ret += "\n             Time Spent(Fractioned) : " + timeSpentOnLevel + " ( " + df.format((double)timeSpentOnLevel/totalTimeGiven*100) + "% )";
        ret += "\n              Time Left(Fractioned) : " + timeLeft + " ( " + df.format((double)timeLeft/totalTimeGiven*100) + "% )";
        ret += "\n                   Total time given : " + totalTimeGiven;
//        ret += "\nCoins Gained: " + numberOfGainedCoins/totalNumberOfCoins*100 + "%. (" + numberOfGainedCoins + " of " + totalNumberOfCoins + ")";
        ret += "\n                       Coins Gained : " + numberOfGainedCoins;
        ret += "\n             Total Actions Perfomed : " + totalActionsPerfomed;
        ret += "\n              Total Frames Perfomed : " + totalFramesPerfomed;
        ret += "\n               Simple Basic Fitness : " + df.format(computeBasicFitness());
        ret += "\nMemo: " + ((Memo.equals("")) ? "Empty" : Memo);
        return ret;
    }
}
