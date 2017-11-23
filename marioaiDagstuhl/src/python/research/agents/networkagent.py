__author__ = "Tom Schaul"

from pybrain.structure.modules.sigmoidlayer import SigmoidLayer
from agents.marioagent import MarioAgent
from scipy import zeros, ravel, rand
from pybrain.tools.shortcuts import buildNetwork


class ModuleMarioAgent(MarioAgent):
    """ A MarioAgent that produces actions by evaluating a module on the level-scene input. 
    The module takes as input an 1x484 numpy array, and produces a 1x5 numpy array as output,
    with each entry corresponding to the probablility of taking the corresponding action
    (left, right, down, jump, speed/shoot). """
    
    stochOutput = False

    inGridSize = 22
    usedActions = 5
    
    useSpecialInfo = True
    
    def __init__(self, module, **args):
        self.setArgs(**args)
        assert module.outdim == self.usedActions
        self.module = module
        if self.useSpecialInfo:
            assert module.indim == self.inGridSize**2+2
            self.lastobs = zeros(self.inGridSize**2+2, dtype=bool)
        else:
            assert module.indim == self.inGridSize**2
            self.lastobs = zeros(self.inGridSize**2, dtype=bool)        
        
    def getAction(self):
        out = self.module.activate(self.lastobs)
        res = zeros(5, int)
        for i in range(5):
            if self.stochOutput:
                if rand() < out[i]:
                    res[i] = 1
            else:
                if out[i] > 0.5:
                    res[i] = 1
        return res
        
    def integrateObservation(self, obs):
        if len(obs) == 3:
            if self.useSpecialInfo:
                self.lastobs[-2:] = obs[:2]
            leftindex = max(0, 11-self.inGridSize/2)
            rightindex = min(22, 11+self.inGridSize/2+1)
            middle = obs[2][leftindex:rightindex, leftindex:rightindex]
            #boolmid = logical_not(logical_not(middle))*1.
            if self.useSpecialInfo:
                self.lastobs[:-2] = ravel(middle)
            else:
                self.lastobs[:] = ravel(middle)
            
                        
class SimpleModuleAgent(ModuleMarioAgent):
    """ Like parent class, but simplifying inputs to a 7x7 array, and outputs to 
    only 3 actions (right, jump, speed/shoot). """
        
    inGridSize = 7
    usedActions = 3
    
    def getAction(self):
        out = self.module.activate(self.lastobs)
        res = zeros(5, int)
        for i, v in zip([1,3,4], out):
            if self.stochOutput:
                if rand() < v:
                    res[i] = 1
            else:
                if v > 0.5:
                    res[i] = 1
        return res
        
        
class MLPMarioAgent(ModuleMarioAgent):
    """ Containing a Multi-layer Perceptron """
    
    def __init__(self, hidden, **args):
        self.setArgs(**args)
        if self.useSpecialInfo:
            net = buildNetwork(self.inGridSize**2+2, hidden, self.usedActions, outclass = SigmoidLayer)
        else:
            net = buildNetwork(self.inGridSize**2, hidden, self.usedActions, outclass = SigmoidLayer)
        ModuleMarioAgent.__init__(self, net)
        
        
class SimpleMLPMarioAgent(MLPMarioAgent, SimpleModuleAgent):
    """ Containing a Multi-layer Perceptron """
        