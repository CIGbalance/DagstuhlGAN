from pybrain.tests.helpers import sortedProfiling
__author__ = "Tom Schaul"

from pybrain.rl.experiments.episodic import EpisodicExperiment
from tasks.mariotask import MarioTask
from agents.networkagent import MLPMarioAgent, SimpleMLPMarioAgent
from agents.mdrnnagent import MdrnnAgent, SimpleMdrnnAgent
from pybrain.tools.xml.networkwriter import NetworkWriter


def combinedScore(agent, task = None):
    """ Let the agent act on a number of levels of increasing difficulty. 
    Return the combined score."""
    if task == None:
        task = MarioTask(agent.name, timeLimit = 20)    
    exp = EpisodicExperiment(task, agent)
    res = 0
    for difficulty in range(5):
        for seed in range(2):
            task.env.levelSeed = seed
            task.env.levelDifficulty = difficulty  
            
            exp.doEpisodes(1)
            print 'Difficulty: %d, Seed: %d, Fitness: %.2f' % (difficulty, seed, task.reward)
            res += task.reward
    return res
    
def main():
    #agent1 = SimpleMLPMarioAgent(2)
    #agent1 = MLPMarioAgent(4)
    #agent1 = MdrnnAgent()
    
    agent1 = SimpleMdrnnAgent()
    print agent1.name
    NetworkWriter.writeToFile(agent1.module, "../temp/MarioNetwork-"+agent1.name+".xml")
    f = combinedScore(agent1)
    print "\nTotal:", f
    
    
    
if __name__ == "__main__":
    sortedProfiling('main()')
    #main()

