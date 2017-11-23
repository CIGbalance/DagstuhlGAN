__author__="Sergey Karakovskiy, sergey at idsia fullstop ch"
__date__ ="$May 2, 2009 7:54:12 PM$"

from pybrain.rl.agents.agent import Agent

class MarioAgent(Agent):
    """ An agent is an entity capable of producing actions, based on previous observations.
        Generally it will also learn from experience. It can interact directly with a Task.
    """
