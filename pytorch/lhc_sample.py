#This does a lhc sample of the latent vector space in order to get a rough estimate of the variability of different
#levels produced

#import torch
#import torchvision.utils as vutils
#from torch.autograd import Variable

#import sys
#import json
#import numpy
#import models.dcgan as dcgan

import random

batchSize = 64
nz = 32  # Dimensionality of latent vector

imageSize = 32
ngf = 64
ngpu = 1
n_extra_layers = 0
generator = dcgan.DCGAN_G(imageSize, nz, 1, ngf, ngpu, n_extra_layers)

generator.load_state_dict(torch.load('netG_epoch_24.pth', map_location=lambda storage, loc: storage))


best = numpy.array(es.best.get()[0])
latent_vector = torch.FloatTensor(x).view(batchSize, nz, 1, 1)
levels = generator(Variable(latent_vector, volatile=True))
levels.data = levels.data[:, :, :14, :28]
vutils.save_image(levels.data, 'generated_samples.png')
