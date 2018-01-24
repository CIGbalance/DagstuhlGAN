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
import cma
import random
import math
import matplotlib.pyplot as plt

batchSize = 64
nz = 32  # Dimensionality of latent vector

imageSize = 32
ngf = 64
ngpu = 1
n_extra_layers = 0

features = 10

generator = dcgan.DCGAN_G(imageSize, nz, features, ngf, ngpu, n_extra_layers)
generator.load_state_dict(torch.load('netG_epoch_5000.pth', map_location=lambda storage, loc: storage))



GROUND = 0
ENEMY = 5
PIPE = 6 #7, 8 9

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


def combine_images(generated_images):
    num = generated_images.shape[0]
    width = int(math.sqrt(num))
    height = int(math.ceil(float(num)/width))
    shape = generated_images.shape[1:]
    image = numpy.zeros((height*shape[0], width*shape[1],shape[2]), dtype=generated_images.dtype)
    for index, img in enumerate(generated_images):
        i = int(index/width)
        j = index % width
        image[i*shape[0]:(i+1)*shape[0], j*shape[1]:(j+1)*shape[1]] = img
    return image


batchSize = 1

def gan_maximse_title_type(x):
    x = numpy.array(x)
    latent_vector = torch.FloatTensor(x).view(batchSize, nz, 1,
                                              1)  # torch.from_numpy(lv)# torch.FloatTensor( torch.from_numpy(lv) )
    levels = generator(Variable(latent_vector, volatile=True))
    levels.data = levels.data[:, :, :14, :28]
    im = levels.data.cpu().numpy()
    im = numpy.argmax( im, axis = 1)

    num_titles =  (len (im[im == PIPE]))
    return 100.0 - num_titles


def gan_fitness_function(x):
    x = numpy.array(x)
    # print(x)

    latent_vector = torch.FloatTensor(x).view(batchSize, nz, 1,
                                              1)  # torch.from_numpy(lv)# torch.FloatTensor( torch.from_numpy(lv) )
    levels = generator(Variable(latent_vector, volatile=True))
    levels.data = levels.data[:, :, :14, :28]
    #return solid_blocks_fraction(levels.data, 0.2)
    return solid_blocks_fraction(levels.data, 0.4)*ground_blocks_fraction(levels.data,0.8)


def ground_blocks_fraction(data, frac):
    ground_count = sum(data[0, GROUND, 13, :] > 0)
    #print(ground_count)
    #print(ground_count- frac*28)
    return math.sqrt(math.pow(ground_count - frac*28, 2))

def solid_blocks_fraction(data, frac):
    solid_block_count = len(data[data > 0.])
    return math.sqrt(math.pow(solid_block_count - frac*14*28, 2))


es = cma.CMAEvolutionStrategy(nz * [0], 0.5)
#cma.CMAEvolutionStrategy(4 * [1], 1, {'seed':234})
#'BoundaryHandler': 'BoundTransform  # or BoundPenalty, unused when ``bounds in (None, [None, None])``',
#'bounds': '[None, None]  # lower (=bounds[0]) and upper domain boundaries, each a scalar or a list/vector',

es.optimize(gan_maximse_title_type)

# es.result_pretty()
best = numpy.array(es.best.get()[0])
print ("BEST ", best)
print("Fitness", gan_fitness_function(best))


# levels.data[levels.data > 0.] = 1  #SOLID BLOCK
# levels.data[levels.data < 0.] = 2  #EMPTY TILE
latent_vector = torch.FloatTensor(best).view(batchSize, nz, 1, 1)
levels = generator(Variable(latent_vector, volatile=True))

im = levels.data.cpu().numpy()
im = im[:,:,:14,:28] #Cut of rest to fit the 14x28 tile dimensions
im = numpy.argmax( im, axis = 1)
#print(json.dumps(levels.data.tolist()))
print("Saving to file ")
im = ( plt.get_cmap('rainbow')( im/float(features) ) )
plt.imsave('fake_sample.png', combine_images(im) )

#vutils.save_image(levels.data, 'generated_samples.png')

cma.plot()
# raw_input("Press Enter to continue...")
