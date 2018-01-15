# This generator program expands a low-dimentional latent vector into a 2D array of tiles.
# Each line of input should be an array of z vectors (which are themselves arrays of floats -1 to 1)
# Each line of output is an array of 32 levels (which are arrays-of-arrays of integer tile ids)

import torch
import torchvision.utils as vutils
from torch.autograd import Variable

import sys
import json
import numpy
import models.dcgan as dcgan

import random

batchSize = 64
nz = 32 #Dimensionality of latent vector

imageSize = 32
ngf = 64
ngpu = 1
n_extra_layers = 0
generator = dcgan.DCGAN_G(imageSize, nz, 1, ngf, ngpu, n_extra_layers)

generator.load_state_dict(torch.load('netG_epoch_24.pth', map_location=lambda storage, loc: storage))


# generate_example = True

# #Testing the system to generate an exampel picture
# if generate_example:
#   for i in range(10):
#     fixed_noise = torch.FloatTensor(batchSize, nz, 1, 1).normal_(0, 1)

#     fake = generator(Variable(fixed_noise, volatile=True))
#     fake.data = fake.data[:,:,:14,:28] #Cut of rest to fit the 14x28 tile dimensions

#     fake.data[fake.data > 0.] = 1.
#     fake.data[fake.data < 0.] = -1.

#     vutils.save_image(fake.data, 'fake_samples_{0}.png'.format(i))


testing = False

if testing:  
  line = []
  for i in range (batchSize):
    line.append( [ random.uniform(-1.0, 1.0) ]*nz )

  #This is the format that we expect from sys.stdin
  print line 

  line = json.dumps(line)
  lv = numpy.array(json.loads(line))

  latent_vector = torch.FloatTensor( lv ).view(batchSize, nz, 1, 1) #torch.from_numpy(lv)# torch.FloatTensor( torch.from_numpy(lv) )

  #latent_vector = numpy.array(json.loads(line))
  levels = generator(Variable(latent_vector, volatile=True))

  levels.data = levels.data[:,:,:14,:28] #Cut of rest to fit the 14x28 tile dimensions

  levels.data[levels.data > 0.] = 1  #SOLID BLOCK
  levels.data[levels.data < 0.] = -1.  #EMPTY TILE

  #print(json.dumps(levels.data.tolist()))
  vutils.save_image(levels.data, 'fake_samples.png')

  exit()


print("READY") # Java loops until it sees this special signal
sys.stdout.flush() # Make sure Java can sense this output before Python blocks waiting for input
#for line in sys.stdin.readlines(): # Jacob: I changed this to make this work on Windows ... did this break on Mac?


for line in sys.stdin:
  lv = numpy.array(json.loads(line))
  latent_vector = torch.FloatTensor( lv ).view(batchSize, nz, 1, 1) 

  levels = generator(Variable(latent_vector, volatile=True))

  levels.data = levels.data[:,:,:14,:28] #Cut of rest to fit the 14x28 tile dimensions

  levels.data[levels.data > 0.] = 1  #SOLID BLOCK
  levels.data[levels.data < 0.] = 2  #EMPTY TILE

  print(json.dumps(levels.data.tolist()))
  sys.stdout.flush() # Make Java sense output before blocking on next input

