__author__ = "Tom Schaul"

from pybrain.rl.experiments.episodic import EpisodicExperiment
from tasks.mariotask import MarioTask
from agents.forwardagent import ForwardAgent
from agents.forwardrandomagent import ForwardRandomAgent
from time import sleep

def combinedScore(agent, task = None):
    """ Let the agent act on a number of levels of increasing difficulty. 
    Return the combined score."""
    if task == None:
        task = MarioTask(agentName = agent.name)
    exp = EpisodicExperiment(task, agent)
    res = 0
    for difficulty in range(1):
        for seed in range(1):
            task.env.levelSeed = seed
            task.env.levelDifficulty = difficulty  
            exp.doEpisodes(1)
            print 'Difficulty: %d, Seed: %d, Fitness: %.2f' % (difficulty, seed, task.reward)
            res += task.reward
    return res
    
def main():
    
    for i in range(10):
        sleep(1) # FIXME: move to client or implement better and more beautiful solution.
        agent1 = ForwardAgent()
        print agent1.name
        f = combinedScore(agent1)
        print "\nTotal:", f   
        
        sleep(1) # FIXME: move to client or implement better and more beautiful solution.
        
        agent2 = ForwardRandomAgent()
        print agent2.name
        f = combinedScore(agent2)
        print "\nTotal:", f    
    
if __name__ == "__main__":
    main()

