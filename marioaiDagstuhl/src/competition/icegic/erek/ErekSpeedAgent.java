package competition.icegic.erek;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

import java.util.HashMap;
import java.util.Set;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: espeed
 * Date: Aug 6, 2009
 * Time: 12:02:37 PM
 * My agent for the contest.
 */
public class ErekSpeedAgent extends BasicAIAgent implements Agent {

    public ArrayList<ActionWrapper> actionMap = generateActionMap();
    public HashMap<MapWrapper, boolean[]> gene;

    private ArrayList<MapWrapper> saveKeys;

    private ArrayList<boolean[]> saveVals;



    private double fitness;



    private double weightedFitness;


    // Generates a hashmap which contains every possible combination of buttons
    // This allows me to choose random states fairly easily for population generation
    // and mutation
    private ArrayList<ActionWrapper> generateActionMap() {
        ArrayList<ActionWrapper> map = new ArrayList<ActionWrapper>();

        // A simple array so that I can use foreach
        boolean[] vals = {true, false};

        // Determines the current integer mapping.
       // int count = 0;

        // Nested for loops go through each of the important buttons
        for (boolean val0 : vals) {
            ActionWrapper tList = new ActionWrapper(Environment.numberOfButtons);

            if(val0)
                continue;

            tList.add(Mario.KEY_LEFT, val0);

            for (boolean val1 : vals) {
                // If we're pressing left we don't want to go right
                // hence the boolean logic.
                if(val0&&val1)
                    continue;
                tList.add(Mario.KEY_RIGHT, val1);

                for (boolean val2 : vals) {
                    // If we're pressing left or right we don't want
                    // to press down, similar to before.
                    if((val0||val1)&&val2)
                        continue;
                    if(val2)
                        continue;
                    tList.add(Mario.KEY_DOWN, val2);
                    for (boolean val3 : vals) {
                        // Jump and Speed below it fit with any other combination
                        // So these loops are regular.
                        tList.add(Mario.KEY_JUMP, val3);
                        for (boolean val4 : vals) {
                            tList.add(Mario.KEY_SPEED, val4);

                            // I clone it so the rest of the for loops don't mess
                            // up what's in the HashMap.  There's a warning here
                            // due to generics and clone()
                            map.add(tList.clone());
                        }
                    }
                }
            }
        }


        return map;
    }


    public double getWeightedFitness() {
        return weightedFitness;
    }

    public void setWeightedFitness(double weightedFitness) {
        this.weightedFitness = weightedFitness;
    }
    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public ErekSpeedAgent() {
        super("ErekSpeedAgent");
        gene = new HashMap<MapWrapper, boolean[]>();
        reset();
    }

    public void reset() {
        action = new boolean[Environment.numberOfButtons];

    }


    public boolean[] getAction(Environment observation) {

        // this Agent requires observation.

        assert (observation != null);

        //System.out.println(gene.size());
        byte[][] levelScene = observation.getCompleteObservation();

        MapWrapper map = new MapWrapper();
        map.setMap(levelScene);

        try
        {
            gene.values().size();
        }
        catch(Exception ex)
        {
            restore();
        }
        if(!gene.containsKey(map))
        {
            //   for(byte[] ar : levelScene)
            //      System.out.println(Arrays.toString(ar));

            int ran = GeneticEvolver.random.nextInt(actionMap.size());
            gene.put(map, actionMap.get(ran).acts);

        }
        return gene.get(map);
    }

    public void save()
    {
          Set<MapWrapper> kSet = gene.keySet();
        ArrayList<MapWrapper> sKey = new ArrayList<MapWrapper>();
        ArrayList<boolean[]> sVal = new ArrayList<boolean[]>();
        for(MapWrapper key : kSet)
        {
             sKey.add(key);
            sVal.add(gene.get(key));
        }

        saveKeys = sKey;
        saveVals = sVal;
    }

    private void restore()
    {
        gene = null;
        HashMap<MapWrapper, boolean[]> ret = new HashMap<MapWrapper, boolean[]>();
        for(int i = 0; i < saveKeys.size(); i++)
        {
             ret.put(saveKeys.get(i), saveVals.get(i));
        }

        gene = ret;
    }
}