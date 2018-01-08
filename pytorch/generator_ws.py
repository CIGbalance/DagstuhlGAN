# This generator program expands a low-dimentional latent vector into a 2D array of tiles.
# Each line of input should be an array of z vectors (which are themselves arrays of floats -1 to 1)
# Each line of output is an array of 32 levels (which are arrays-of-arrays of integer tile ids)

import torch
import torchvision.utils as vutils
from torch.autograd import Variable

import sys
import json
import numpy

#Fix correct level dimensions

generator = torch.load('mario_gan.pth', map_location=lambda storage, loc: storage)

batchSize = 32 
nz = 32 #Dimensionality of latent vector

generate_example = True

#Testing the system to generate an exampel picture
if generate_example:
  fixed_noise = torch.FloatTensor(batchSize, nz, 1, 1).normal_(0, 1)

  fake = generator(Variable(fixed_noise, volatile=True))
  fake.data = fake.data[:,:,:14,:28] #Cut of rest to fit the 14x28 tile dimensions

  fake.data[fake.data > 0.] = 1.
  fake.data[fake.data < 0.] = -1.

  vutils.save_image(fake.data, 'fake_samples.png')

print("READY") # Java loops until it sees this special signal
sys.stdout.flush() # Make sure Java can sense this output before Python blocks waiting for input
#for line in sys.stdin.readlines(): # Jacob: I changed this to make this work on Windows ... did this break on Mac?
for line in sys.stdin:
  zs = numpy.array(json.loads(line))
  levels = generator(Variable(sz, volatile=True))
  levels.data = levels.data[:,:,:14,:28] #Cut of rest to fit the 14x28 tile dimensions

  levels.data[levels.data > 0.] = 1  #SOLID BLOCK
  levels.data[levels.data < 0.] = 2  #EMPTY TILE

  print(json.dumps(levels.data.tolist()))
  sys.stdout.flush() # Make Java sense output before blocking on next input

