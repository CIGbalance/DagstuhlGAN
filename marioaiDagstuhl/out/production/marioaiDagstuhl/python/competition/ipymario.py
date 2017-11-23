__author__ = "Sergey Karakovskiy, sergey at idsia dot ch"
__date__ = "$Apr 30, 2009 1:46:32 AM$"

import sys

from experiments.episodicexperiment import EpisodicExperiment
from tasks.mariotask import MarioTask
from agents.forwardagent import ForwardAgent
from agents.forwardrandomagent import ForwardRandomAgent


#from pybrain.... episodic import EpisodicExperiment
#TODO: reset sends: vis, diff=, lt=, ll=, rs=, mariomode, time limit, pw,
# with creatures, without creatures HIGH.
# send creatures.

def main():
    agent = ForwardAgent()
    task = MarioTask(agent.name, initMarioMode = 2)
    exp = EpisodicExperiment(task, agent)
    print 'Task Ready'
    exp.doEpisodes(2)
    print 'mm 2:', task.reward

    task.env.initMarioMode = 1
    exp.doEpisodes(1)
    print 'mm 1:', task.reward
    
    task.env.initMarioMode = 0
    exp.doEpisodes(1)
    print 'mm 0:', task.reward

    task.env.initMarioMode = 0
    exp.doEpisodes(1)
    print 'mm 0:', task.reward
    
    task.env.initMarioMode = 0
    task.env.levelDifficulty = 5
    exp.doEpisodes(1)
    print 'mm 0, ld 5: ', task.reward
    
    task.env.initMarioMode = 1
    task.env.levelDifficulty = 5
    exp.doEpisodes(1)
    print 'mm 1, ld 5: ', task.reward

    task.env.initMarioMode = 2
    task.env.levelDifficulty = 5
    exp.doEpisodes(1)
    print 'mm 2, ld 5: ', task.reward

    
    print "finished"

#    clo = CmdLineOptions(sys.argv)
#    task = MarioTask(MarioEnvironment(clo.getHost(), clo.getPort(), clo.getAgent().name))
#    exp = EpisodicExperiment(clo.getAgent(), task)
#    exp.doEpisodes(3)

if __name__ == "__main__":
    main()
else:
    print "This is module to be run rather than imported."
