import random
__author__ = "Sergey Karakovskiy, sergey at idsia fullstop ch"
__date__ = "$Apr 30, 2009 6:46:04 PM$"

from marioagent import MarioAgent

class ForwardRandomAgent(MarioAgent):
    """
    Very simple example of an agent, who does not respect the observations,
    but just generates random forward moves and jumps
    """
    observation = None

    def __init__(self):
        """Constructor"""
        pass

    def getAction(self):
        """Get observation, (possibly analyse it), sent an action back
        @param obs: observation from the environment
        @type obs: by default, this is assumed to be a numpy array of doubles
        """
        fwd =       "01000\r\n"
        fwdjump =   "01010\r\n"
        actions = [fwd, fwdjump]
        return actions[random.randint(0, len(actions) - 1)]

    def integrateObservation(self, mayMarioJump, isMarioOnGround, levelScene):
        """This method stores the observation inside the agent"""
        self.mayMarioJump, self.isMarioOnGround, self.levelScene = \
            mayMarioJump, isMarioOnGround, levelScene
        self.printLevelScene()

    def printObs(self):
        """for debug"""
        print self.observation

if __name__ != "__main__":
    print "Importing %s " % __name__;
else:
    print "This is the module to import rather than run"