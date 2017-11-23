__author__="Sergey Karakovskiy, sergey at idsia fullstop ch"
__date__ ="$May 1, 2009 3:05:27 AM$"

class Environment(object):
    """
    Environment class stores the observations and receives the actions
    """

    #the number of action values the environment accepts
    indim = 0
    # the number of sensor values the environment produces
    outdim = 0

    def __init__(self):
        """ The general interface for whatever we would like to model, learn about, predict, or simply interact in.
            We can perform actions, and access (partial) observations.
        """

    def getSensors(self):
        """ the currently visible state of the world (the observation may be
            stochastic - repeated calls returning different values)
            @rtype: by default, this is assumed to be a numpy array of doubles
            @note: This function is abstract and has to be implemented.
        """
        raise "Not implemented"

    def performAction(self, action):
        """ perform an action on the world that changes it's internal state (maybe stochastically)
            @param action: an action that should be executed in the Environment.
            @type action: by default, this is assumed to be a numpy array of doubles
            @note: This function is abstract and has to be implemented.
        """
        raise "Not implemented"

    def reset(self):
        """ Most environments will implement this optional method that allows for reinitialization.
        """
        pass