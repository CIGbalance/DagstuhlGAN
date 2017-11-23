""" Mini-script to evolve some mdrnn-based agents. 
For each agent, store it in xml whenever it beats the previous best.
Based on the updated pybrain learner framework already.
"""

from agents.mdrnnagent import SimpleMdrnnAgent
from pybrain.tools.xml.networkwriter import NetworkWriter
from tasks.mariotask import MarioTask
from pybrain.rl.experiments.episodic import EpisodicExperiment
from pybrain.optimization.distributionbased.cmaes import CMAES
from random import random
from pybrain.tools.filehandling import pickleDumpDict, pickleReadDict
from time import sleep

bestscore = 205 
port = 4242
counter = 0

idd = int(random()*9000) + 1000
agent = SimpleMdrnnAgent()
        
def writeAgentNet(score, counter):    
    NetworkWriter.writeToFile(agent.module, "../temp/"+str(port)+"x/SimpleMdrnnANet-fit:"
                              +str(round(score,2))+'-after:'+str(counter)+'-'+str(idd)+".xml")

def oneLevel(difficulty = 0, timelimit = 200, seed = 0):
    try:
        sleep(0.02)
        agent.module.reset()
        task = MarioTask(agent.name, port = port)
        task.env.levelSeed = seed
        task.env.visualization = False
        task.env.levelDifficulty = difficulty
        task.env.timeLimit = timelimit
        task.reset()    
        EpisodicExperiment(task, agent).doEpisodes(1)
        res = task.reward
        print 'Difficulty: %d, Seed: %d, Timelimit: %d, Fitness: %.2f' % (difficulty, seed, 
                                                                          timelimit, res)
        return res
    except Exception, e:
        print
        print 'OOPS', e
        print 
        try:
            task.reset()
        except:
            pass
        return 0
        
def score(netp):
    """ the score is determined as follows:
    - progress on difficulty 0, timelimit 10
    - if > 100 then replace by
    - progress on difficulty 0, timelimit 40
    - if > 400 then replace by
    - progress on difficulty 0, timelimit 200
    - if > 4000 then replace by 10000 plus
    - progress on difficulty 3, timelimit 200
    - if > 4000 then replace by 20000 plus
    - progress on difficulty 5, timelimit 200
    - if > 4000 then replace by 30000 plus
    - progress on difficulty 10, timelimit 200        
    
    Whenever a network is evaluated with a new best-score, it is stored. """
    agent.module.params[:] = netp
    res = oneLevel(difficulty = 0, timelimit = 10)
    if res > 250:
        res = oneLevel(difficulty = 0, timelimit = 40)
    if res > 1250:
        res = oneLevel(difficulty = 0, timelimit = 200)
    if res > 4000:
        res = 10000 + oneLevel(difficulty = 3, timelimit = 200)
    if res > 14000:
        res = 20000 + oneLevel(difficulty = 5, timelimit = 200)
    if res > 14000:
        res = 30000 + oneLevel(difficulty = 10, timelimit = 200)
    
    global bestscore, counter
    print 'best', round(bestscore,2), 'after', counter, '-- this final:', round(res,2)
    counter += 1
    if res > bestscore:
        print 
        print 'NEW BEST:', res
        print
        bestscore = res
        writeAgentNet(res, counter)
        
    # CMA minimizes
    return -res

if __name__ == '__main__':
    netp = agent.module.params.copy()
    l = CMAES(score, netp.copy(), verbose = True, storeAllEvaluations = True, 
              maxEvaluations = 100000, maxLearningSteps = 1000)
    l.learn()    
    pickleDumpDict('allevals-'+str(idd), l._allEvaluations)
    