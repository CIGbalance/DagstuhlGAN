__author__ = "Sergey Karakovskiy, sergey at idsia fullstop ch; Tom Schaul"
__date__ = "$Apr 30, 2009 6:46:04 PM$"

import random
from scipy import array
from agents.forwardagent import ForwardAgent

class ForwardRandomAgent(ForwardAgent):
    """
    Very simple example of an agent, who does not respect the observations,
    but just generates random forward moves and jumps
    """
    def getAction(self):
        fwd = array([0, 1, 0, 0, 0])
        fwdjump = array([0, 1, 0, 1, 0])
        actions = [fwd, fwdjump]
        return actions[random.randint(0, len(actions) - 1)]
