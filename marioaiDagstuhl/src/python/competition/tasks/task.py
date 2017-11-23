__author__ = "Sergey Karakovskiy, sergey at idsia fullstop ch"
__date__ = "$May 12, 2009 11:48:33 PM$"


class Task(object):
    """ A task is associating a purpose with an environment. It decides how to evaluate the
    observations, potentially returning reinforcement rewards or fitness values.
    Furthermore it is a filter for what should be visible to the agent.
    Also, it can potentially act as a filter on how actions are transmitted to the environment. """

    def __init__(self, environment):
        """ All tasks are coupled to an environment. """
        self.env = environment

    def performAction(self, action):
        """ a filtered mapping towards performAction of the underlying environment. """
        self.env.performAction(action)

    def getObservation(self):
        """ a filtered mapping to getSample of the underlying environment. """
        obs = self.env.getSensors()
        return obs


    def getReward(self):
        """ compute and return the current reward (i.e. corresponding to the last action performed) """
        raise "define method getReward()"
