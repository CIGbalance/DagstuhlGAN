__author__="Sergey Karakovskiy, sergey at idsia fullstop ch"
__date__ ="$May 12, 2009 11:46:44 PM$"

from task import Task

class EpisodicTask(Task):
    """
    Documentation
    """

    # tracking cumulative reward
    cumReward = 0

    # tracking the number of samples
    samples = 0

    def reset(self):
        """ reinitialize the environment """
        # Note: if a task needs to be reset at the start, the subclass constructor
        # should take care of that.
        self.env.reset()
        self.cumReward = 0
        self.samples = 0

    def isFinished(self):
        """ is the current episode over? """
        raise "Implement method isFinished()"

    def performAction(self, action):
        Task.performAction(self, action)
        self.addReward()
        self.samples += 1

    def addReward(self):
        """ a filtered mapping towards performAction of the underlying environment. """
        # by default, the cumulative reward is just the sum over the episode
        self.cumReward += self.getReward()

    def getTotalReward(self):
        """ the accumulated reward since the start of the episode """
        return self.cumReward

