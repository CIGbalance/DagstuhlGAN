__author__ = "Tom Schaul"

from agents.networkagent import ModuleMarioAgent, SimpleModuleAgent
from pybrain.structure.networks.multidimensional import MultiDimensionalRNN
from pybrain.structure.networks.feedforward import FeedForwardNetwork
from pybrain.structure.connections.full import FullConnection
from pybrain.structure.networks.swiping import SwipingNetwork
from pybrain import MDLSTMLayer, IdentityConnection
from pybrain import ModuleMesh, LinearLayer, TanhLayer, SigmoidLayer
from scipy import product



class MarioMdrnnNetwork(MultiDimensionalRNN):
    insize = 1
    outputs = 5
    hsize = 2
    mariopos = None
                 
    def __init__(self, dims, **args):
        """ The one required argument specifies the sizes of each dimension (minimum 2) """
        assert len(dims) == 2
        SwipingNetwork.__init__(self, dims = dims, **args)
        if self.mariopos == None:
            self.mariopos = (dims[0]/2, dims[1]/2)
        
        pdims = product(dims)
        # the input is a 2D-mesh (as a view on a flat input layer)
        inmod = LinearLayer(self.insize*pdims, name = 'input')
        inmesh = ModuleMesh.viewOnFlatLayer(inmod, dims, 'inmesh')
        
        # the output is a 2D-mesh (as a view on a flat sigmoid output layer)
        outmod = self.outcomponentclass(self.outputs*pdims, name = 'output')
        outmesh = ModuleMesh.viewOnFlatLayer(outmod, dims, 'outmesh')
        
        if self.componentclass is MDLSTMLayer:
            c = lambda: MDLSTMLayer(self.hsize, 2, self.peepholes).meatSlice()
            hiddenmesh = ModuleMesh(c, (self.size, self.size, 4), 'hidden', baserename = True)
        else:
            hiddenmesh = ModuleMesh.constructWithLayers(self.componentclass, self.hsize, tuple(list(dims)+[self.swipes]), 'hidden')
        
        self._buildSwipingStructure(inmesh, hiddenmesh, outmesh)
        
        
        o = LinearLayer(self.outputs)
        self.addConnection(IdentityConnection(outmesh[self.mariopos], o))
        self.outmodules = []
        self.addOutputModule(o)
        
        
        # add the identity connections for the states
        for m in self.modules:
            if isinstance(m, MDLSTMLayer):
                tmp = m.stateSlice()
                index = 0
                for c in list(self.connections[m]):
                    if isinstance(c.outmod, MDLSTMLayer):
                        self.addConnection(IdentityConnection(tmp, c.outmod.stateSlice(), 
                                                              outSliceFrom = self.hsize*(index), 
                                                              outSliceTo = self.hsize*(index+1)))
                        index += 1
 
        # special inputs
        self.addInputModule(LinearLayer(2, name = 'specialin'))
        self.addConnection(FullConnection(self['specialin'], o))
 
        self.sortModules()
        

class MdrnnAgent(ModuleMarioAgent):
    """ A MarioAgent, deciding on its actions using a special Multi-dimensional RNN. """
    
    hidden =3
    
    def __init__(self, **args):
        self.setArgs(**args)
        net = MarioMdrnnNetwork((self.inGridSize,self.inGridSize), 
                                outputs = self.usedActions,
                                hsize = self.hidden,
                                symmetricdimensions = False,
                                symmetricdirections = False,
                                )
        fnet = net.convertToFastNetwork()
        if fnet != None:
            net = fnet
        ModuleMarioAgent.__init__(self, net)
    
class SimpleMdrnnAgent(MdrnnAgent, SimpleModuleAgent):
    """ Like parent class, but simplifying inputs to a 7x7 array, and outputs to 
    only 3 actions (right, jump, speed/shoot). """
    