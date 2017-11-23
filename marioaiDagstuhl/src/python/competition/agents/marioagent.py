__author__="Sergey Karakovskiy, sergey at idsia fullstop ch"
__date__ ="$May 2, 2009 7:54:12 PM$"

class MarioAgent:
#    class MarioAgent(Agent):
    """ An agent is an entity capable of producing actions, based on previous observations.
        Generally it will also learn from experience. It can interact directly with a Task.
    """

    def integrateObservation(self, obs):
        raise "Not implemented"

    def getAction(self):
        raise "Not implemented"

    def giveReward(self, reward):
        pass
    def _getName(self):
        if self._name is None:
            self._name = self.__class__.__name__
        return self._name

    def _setName(self, newname):
        """Change name to newname. Uniqueness is not guaranteed anymore."""
        self._name = newname

    _name = None
    name = property(_getName, _setName)

    def __repr__(self):
        """ The default representation of a named object is its name. """
        return "<%s '%s'>" % (self.__class__.__name__, self.name)
    def newEpisode(self):
        pass


