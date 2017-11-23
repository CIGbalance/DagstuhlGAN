__author__ = "Tom Schaul"

from pybrain.rl.experiments.episodic import EpisodicExperiment
from tasks.mariotask import MarioTask
from agents.networkagent import MLPMarioAgent, SimpleMLPMarioAgent
from agents.mdrnnagent import MdrnnAgent, SimpleMdrnnAgent
from pybrain.tools.xml.networkwriter import NetworkWriter


def main():
    agent = SimpleMLPMarioAgent(10, inGridSize = 3,
                                )
    print agent.name
    NetworkWriter.writeToFile(agent.module, "../temp/MarioNetwork-"+agent.name+".xml")
    
    task = MarioTask(agent.name, timeLimit = 200)
    exp = EpisodicExperiment(task, agent)
    res = 0
    cumul = 0
    for seed in [0]:
        for difficulty in [0,3,5,10]:
            task.env.levelSeed = seed
            task.env.levelDifficulty = difficulty  
            
            exp.doEpisodes(1)
            print 'Difficulty: %d, Seed: %d, Fitness: %.2f' % (difficulty, seed, task.reward)
            cumul += task.reward
            if task.reward < 4000:
                break
            res += 1
    print res
    print agent.module.inputbuffer*1.
    
    
    
if __name__ == "__main__":
    main()

