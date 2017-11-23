package competition.icegic.erek;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: espeed
 * Date: Aug 16, 2009
 * Time: 2:15:13 PM
 * A class to test and evolve populations of genetic agents.
 */
public class GeneticEvolver {

    private ArrayList<ErekSpeedAgent> population = new ArrayList<ErekSpeedAgent>();

    public static final Random random = new Random();

    private int genNum = 0;

    private final int popSize = 100;

    private ErekTask task = new ErekTask();
    private double totalFitness = 0;

    private boolean stop = false;


    public static void main(String[] args)
    {
        GeneticEvolver gE = new GeneticEvolver();

        gE.init();

        while(!gE.stop)
        {
            System.out.println("Generation:" + gE.genNum);
            System.out.println("Seed:" + gE.task.seeds[0]);
            gE.simulate();
            gE.generateNewPopulation();
            gE.genNum++;
            gE.totalFitness = 0;
            gE.task = new ErekTask();
        }

    }

    private void init()
    {
        for(int i = 0; i < popSize; i++)
        {
            population.add(new ErekSpeedAgent());
        }

    }

    private void simulate()
    {
        for(ErekSpeedAgent agent : population)
        {
            double[] result = task.evaluate(agent);
            agent.setFitness(result[0]);
            totalFitness += result[0];
            System.out.println("Total:" + result[0] + " gene size:" + agent.gene.size());
        }
        System.out.println("Pop Total: " + totalFitness);
    }

    private void generateNewPopulation()
    {
        ArrayList<ErekSpeedAgent> newPopulation;

        newPopulation = selectFromPopTourn();

        //Get rid of old population early.

        ArrayList<ErekSpeedAgent> bests = applyElitism();
        stop = stopEvolution();
        //save old population
        ErekSpeedAgent best = population.get(population.size()-1);
        //best.save();

        try
        {
            FileOutputStream os = new FileOutputStream("bestofgeneration" + genNum);
            ObjectOutputStream out = new ObjectOutputStream(os);
            out.writeObject(best);
            out.close();
            System.out.println("Finished writing: bestofgeneration" + genNum);

        }
        catch(Exception ex)
        {
            System.err.println(ex);
        }
        population = null;

        // continue evolution
        long begin, start, stop, elapsed;

        begin = System.currentTimeMillis();
        newPopulation = recombinePopUni(newPopulation);
        stop = System.currentTimeMillis();
        elapsed = stop - begin;
        System.out.println("Time to recombine:" + elapsed);

        start = System.currentTimeMillis();
        mutate(newPopulation);
        stop = System.currentTimeMillis();
        elapsed = stop-start;
        System.out.println("Time to mutate:" + elapsed);
        newPopulation.addAll(bests);
        population = newPopulation;
        System.out.println("Finished generation");
        System.out.println("Total time:" + (stop-begin));
    }

    private ArrayList<ErekSpeedAgent> selectFromPopRoulette()
    {
        ArrayList<ErekSpeedAgent> ret = new ArrayList<ErekSpeedAgent>();

        for(ErekSpeedAgent agent: population)
        {
            agent.setWeightedFitness(agent.getFitness()/totalFitness);
        }

        for(int i = 0; i < popSize; i++)
        {
            double r =  random.nextDouble();
            double current=0;
            for(ErekSpeedAgent agent: population)
            {
                current += agent.getWeightedFitness();

                if(current > r)
                {
                    ret.add(agent);
                    break;
                }    }
        }

        return ret;
    }

    private ArrayList<ErekSpeedAgent> selectFromPopTourn()
    {
        ArrayList<ErekSpeedAgent> ret = new ArrayList<ErekSpeedAgent>();


        for(int i = 0; i < popSize; i++)
        {
            ArrayList<ErekSpeedAgent> tourn = new ArrayList<ErekSpeedAgent>();
            int tSize = popSize/4;
            for(int j = 0; j < tSize; j++)
            {
                int idx = random.nextInt(population.size());
                tourn.add(population.get(idx));
            }
            Collections.sort(tourn, new FitnessComparator());
            ret.add(tourn.get(tourn.size()-1));
        }

        return ret;
    }


    private ArrayList<ErekSpeedAgent> recombinePopUni(ArrayList<ErekSpeedAgent> pop)
    {
        ArrayList<ErekSpeedAgent> ret = new ArrayList<ErekSpeedAgent>();

        while(ret.size() < popSize)
        {
            ErekSpeedAgent agent1 = pop.get(random.nextInt(pop.size()));
            ErekSpeedAgent agent2 = pop.get(random.nextInt(pop.size()));

            ErekSpeedAgent new1 = new ErekSpeedAgent();
            ErekSpeedAgent new2 = new ErekSpeedAgent();

            Set<MapWrapper> aS1 = agent1.gene.keySet();
            Set<MapWrapper> aS2 = agent2.gene.keySet();




            for(MapWrapper map : aS1)
            {
                if(agent2.gene.containsKey(map))
                {
                    // Decide which one we want
                    boolean flip = random.nextBoolean();

                    if(flip)
                    {
                        new1.gene.put(map, agent1.gene.get(map));
                        new2.gene.put(map, agent2.gene.get(map));


                    }
                    else
                    {
                        new2.gene.put(map, agent1.gene.get(map));
                        new1.gene.put(map, agent2.gene.get(map));

                    }

                  

                }
                else
                {

                    new1.gene.put(map, agent1.gene.get(map));
                    new2.gene.put(map, agent1.gene.get(map));

                }
            }


            for(MapWrapper map : aS2)
            {
                if(agent1.gene.containsKey(map))
                {

                }
                else
                {

                    new1.gene.put(map, agent2.gene.get(map));
                    new2.gene.put(map, agent2.gene.get(map));

                }
            }

            ret.add(new1);
            ret.add(new2);

        }

        return ret;
    }


    private void mutate(ArrayList<ErekSpeedAgent> pop)
    {
        double rate;

        for(ErekSpeedAgent agent : pop)
        {
            rate = 5.0/((double)agent.gene.size());

            Set<MapWrapper> maps = agent.gene.keySet();

            for(MapWrapper map : maps)
            {
                // roll the dice.
                double roll = random.nextDouble();

                if(roll <= rate)
                {

                    agent.gene.put(map, agent.actionMap.get(
                            random.nextInt(agent.actionMap.size())).acts);
                    //System.out.println("Mutate " + agent.toString());
                }
            }
        }

    }


    private ArrayList<ErekSpeedAgent> applyElitism()
    {
        ArrayList<ErekSpeedAgent> ret = new ArrayList<ErekSpeedAgent>(3);

        Collections.sort(population, new FitnessComparator());

        for(int i = 1; i < 4; i++)
        {
            ret.add(population.get(population.size()-i));
        }

        return ret;
    }


    private boolean stopEvolution()
    {
        // Assumes the population is already sorted
        double var = population.get(population.size()-1).getFitness() - population.get(0).getFitness();

        return (var < 50);
    }

    private class FitnessComparator implements Comparator<ErekSpeedAgent>
    {
        public int compare(ErekSpeedAgent o1, ErekSpeedAgent o2) {
            return (int)(o1.getFitness() - o2.getFitness());
        }
    }
}
