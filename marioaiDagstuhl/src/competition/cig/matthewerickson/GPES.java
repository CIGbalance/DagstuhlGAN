package competition.cig.matthewerickson;

import ch.idsia.ai.EA;
import ch.idsia.ai.Evolvable;
import ch.idsia.ai.tasks.Task;

public class GPES implements EA {

    private GPAgent[] population;
    private final double[] fitness;
	private Task task;
	int numCrossbreeds;
	int numClones;
	int numMutations;
	int populationSize;
	int generation = 0;
	int phase;
	double bestFitness;
	GPAgent bestAgent;
    
	public GPES(Task task, GPAgent initial, int populationsize, int phase) {
		this.population = new GPAgent[populationsize];
		populationSize = populationsize;
        for (int i = 0; i < population.length; i++) {
            population[i] = (GPAgent) initial.getNewInstance();
            population[i].setName("p" + phase +"g" + generation + "i" + i);
        }
        this.fitness = new double[populationsize];
        this.task = task;
        this.phase = phase;
        numCrossbreeds = (int)(populationsize * 0.9);
        numClones = (int)(populationsize * 0.09);
        numMutations = (int)(populationsize * 0.01);
	}

	public double[] getBestFitnesses() {
		double[] maxFitnessArray = new double[1];
		maxFitnessArray[0] = bestFitness;
		return maxFitnessArray;
	}
	
	private double bestFitness()
	{
		double maxFitness = 0;
		for(int i=0; i<fitness.length; i++)
		{
		    if(fitness[i] > maxFitness)
		    {
		        maxFitness = fitness[i];
		    }
		}
		
		return maxFitness;
	}
	

	public Evolvable[] getBests() {
		Evolvable[] best = new Evolvable[1];
		best[0] = bestAgent;
		return best;
	}

	private GPAgent bestAgent()
	{
		double maxFitness = 0;
		GPAgent best = population[0];
		
	 	for(int i=0; i<fitness.length; i++)
		{
		    if(fitness[i] > maxFitness)
		    {
		    	maxFitness = fitness[i];
		    	best = population[i];
		    }
		}
		return best;
	}
	

	public void nextGeneration()  {
		
		generation++;
		
		for (int i=0; i<population.length; i++)
		{
		    fitness[i] = task.evaluate(population[i])[0];
		}
		
		bestFitness = bestFitness();
		bestAgent = (GPAgent)bestAgent();
		
		GPAgent[] nextGeneration = new GPAgent[populationSize];
		
		//Pick 90% crossbreeds
		for(int i=0; i<numCrossbreeds; i++)
		{
			GPAgent parent1 = (GPAgent)selectParent(10);
			GPAgent parent2 = (GPAgent)selectParent(10);
			nextGeneration[i] = parent1.newCrossbreedWith(parent2);
		}
		//Pick 9% clones
		for(int i=numCrossbreeds; i<numCrossbreeds + numClones; i++)
		{
			nextGeneration[i] = (GPAgent)selectParent(20).copy();
		}
		//Pick 1% mutation
		for(int i=numCrossbreeds + numClones; i<numCrossbreeds + numClones + numMutations; i++)
		{
			nextGeneration[i] = (GPAgent)selectParent(20).newMutant();
		}
		
		
		for(int i=0; i<nextGeneration.length; i++)
        {
        	nextGeneration[i].setName("p" + phase +"g" + generation + "i" + i);
        }
		
		population = nextGeneration;
		
	}
	
	private GPAgent selectParent(int tournamentSize)
	{
		//Use tournament selection to select a parent
		GPAgent fittestParent = population[0];
		double maxFitness = 0.0;
		double selectedFitness;
		int randomIndex;
		for(int i=0; i<tournamentSize; i++)
		{
			randomIndex = (int)(Math.random()*(double)population.length);
			selectedFitness = fitness[randomIndex];
			if(population[randomIndex].program.size() > 200)
			{
				//Penalize large programs
				selectedFitness = selectedFitness / 2;
			}
			
			if(selectedFitness > maxFitness)
			{
				maxFitness = selectedFitness;
				fittestParent = population[randomIndex];
			}
		}
		
		return fittestParent;
	}

}
