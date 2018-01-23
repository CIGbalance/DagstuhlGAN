from __future__ import print_function
import argparse
import random
import torch
import torch.nn as nn
import torch.nn.parallel
import torch.backends.cudnn as cudnn
import torch.optim as optim
import torch.utils.data
import torchvision.datasets as dset
import torchvision.transforms as transforms
import torchvision.utils as vutils
from torch.autograd import Variable
import os
import numpy as np
import matplotlib.pyplot as plt

import math

import models.dcgan as dcgan
import models.mlp as mlp
import json

#python main.py --dataset mario --dataroot . --cuda
#TODO.   Use different loss function? Cross-entropy (together with softmax) instead of 
#Make images smaller so large empty parts doesn't dominate 

parser = argparse.ArgumentParser()
parser.add_argument('--dataset', required=True, help='cifar10 | lsun | imagenet | folder | lfw ')
parser.add_argument('--dataroot', required=True, help='path to dataset')
parser.add_argument('--workers', type=int, help='number of data loading workers', default=2)
parser.add_argument('--batchSize', type=int, default=64, help='input batch size')
parser.add_argument('--imageSize', type=int, default=64, help='the height / width of the input image to network')
parser.add_argument('--nc', type=int, default=3, help='input image channels')
parser.add_argument('--nz', type=int, default=100, help='size of the latent z vector')
parser.add_argument('--ngf', type=int, default=64)
parser.add_argument('--ndf', type=int, default=64)
parser.add_argument('--niter', type=int, default=5000, help='number of epochs to train for')
parser.add_argument('--lrD', type=float, default=0.00005, help='learning rate for Critic, default=0.00005')
parser.add_argument('--lrG', type=float, default=0.00005, help='learning rate for Generator, default=0.00005')
parser.add_argument('--beta1', type=float, default=0.5, help='beta1 for adam. default=0.5')
parser.add_argument('--cuda'  , action='store_true', help='enables cuda')
parser.add_argument('--ngpu'  , type=int, default=1, help='number of GPUs to use')
parser.add_argument('--netG', default='', help="path to netG (to continue training)")
parser.add_argument('--netD', default='', help="path to netD (to continue training)")
parser.add_argument('--clamp_lower', type=float, default=-0.01)
parser.add_argument('--clamp_upper', type=float, default=0.01)
parser.add_argument('--Diters', type=int, default=5, help='number of D iters per each G iter')
parser.add_argument('--noBN', action='store_true', help='use batchnorm or not (only for DCGAN)')
parser.add_argument('--mlp_G', action='store_true', help='use MLP for G')
parser.add_argument('--mlp_D', action='store_true', help='use MLP for D')
parser.add_argument('--n_extra_layers', type=int, default=0, help='Number of extra layers on gen and disc')
parser.add_argument('--experiment', default=None, help='Where to store samples and models')
parser.add_argument('--adam', action='store_true', help='Whether to use adam (default is rmsprop)')
opt = parser.parse_args()
print(opt)

if opt.experiment is None:
    opt.experiment = 'samples'
os.system('mkdir {0}'.format(opt.experiment))

opt.manualSeed = random.randint(1, 10000) # fix seed
print("Random Seed: ", opt.manualSeed)
random.seed(opt.manualSeed)
torch.manual_seed(opt.manualSeed)

cudnn.benchmark = True

if torch.cuda.is_available() and not opt.cuda:
    print("WARNING: You have a CUDA device, so you should probably run with --cuda")


if True:


    
    map_size = 32
    #opt.nz = 10
    opt.imageSize = 32

    opt.nz = 32   #dimensionality of latent vector

    smallExample = True

    if (smallExample):
        X = np.array ( json.load(open('example.json')) )
        opt.batchSize = 32
        z_dims = 10
    else:
        opt.batchSize = 32
        z_dims = 13
        X = np.array ( json.load(open('largeExamples.json')) )
        #X_train = np.zeros ( (X.shape[0], 14, 28) )

        i = 0
        for x in X:
            k = np.zeros( (14, 28), dtype = 'uint8')
            #print(len(X[i]))
            k[:len(X[i]),:] = X[i][:14][:]

            X[i] = k #X[i][:14][:]
            i = i+1
            #print(k)

        X = np.stack(X, axis=0)

    num_batches = X.shape[0] / opt.batchSize

#TODO: See how loss is with CIFAR
#TODO: Train with 1 feature map as input


#opt.batchSize = 30
    #print(np.amax(X))
    #exit()
    print ("SHAPE ",X.shape) 
    X_onehot = np.eye(z_dims, dtype='uint8')[X]
    #X_onehot = np.swapaxes(X_onehot,0,2)
    X_onehot = np.rollaxis(X_onehot, 3, 1)
    print ("SHAPE ",X_onehot.shape)    #(173, 14, 28, 16)

    #print (X_onehot[0,:, 0,0])
    #exit()
    #print(data.shape) #(173, 14, 28)
    X_train = np.zeros ( (X.shape[0], z_dims, map_size, map_size) )*2
    #print ("SHAPE ",X_train.shape)  
    #X_train = np.eye(z_dims, dtype='uint8')[X_train]
    X_train[:, 2, :, :] = 1.0  #Fill with empty space

    #Pad part of level so its a square
    X_train[:X.shape[0], :, :X.shape[1], :X.shape[2]] = X_onehot

    #X_train = X_onehot

#$X_onehot = to_categorical(X.ravel(), num_classes=MAP_TILES)\\\n",
#    "                .reshape((len(X),MAP_HEIGHT,MAP_WIDTH,MAP_TILES))\n",


ngpu = int(opt.ngpu)
nz = int(opt.nz)
ngf = int(opt.ngf)
ndf = int(opt.ndf)
nc = int(opt.nc)
n_extra_layers = int(opt.n_extra_layers)

# custom weights initialization called on netG and netD
def weights_init(m):
    classname = m.__class__.__name__
    if classname.find('Conv') != -1:
        m.weight.data.normal_(0.0, 0.02)
    elif classname.find('BatchNorm') != -1:
        m.weight.data.normal_(1.0, 0.02)
        m.bias.data.fill_(0)

if opt.noBN:
    print("A")
    netG = dcgan.DCGAN_G_nobn(opt.imageSize, nz, z_dims, ngf, ngpu, n_extra_layers)
elif opt.mlp_G:
    print("B")
    netG = mlp.MLP_G(opt.imageSize, nz, z_dims, ngf, ngpu)
else:
    print("C")
    netG = dcgan.DCGAN_G(opt.imageSize, nz, z_dims, ngf, ngpu, n_extra_layers)

netG.apply(weights_init)
if opt.netG != '': # load checkpoint if needed
    netG.load_state_dict(torch.load(opt.netG))
print(netG)

if opt.mlp_D:
    netD = mlp.MLP_D(opt.imageSize, nz, nc, ndf, ngpu)
else:
    netD = dcgan.DCGAN_D(opt.imageSize, nz, z_dims, ndf, ngpu, n_extra_layers)
    netD.apply(weights_init)

if opt.netD != '':
    netD.load_state_dict(torch.load(opt.netD))
print(netD)

input = torch.FloatTensor(opt.batchSize, z_dims, opt.imageSize, opt.imageSize)
noise = torch.FloatTensor(opt.batchSize, nz, 1, 1)
fixed_noise = torch.FloatTensor(opt.batchSize, nz, 1, 1).normal_(0, 1)
one = torch.FloatTensor([1])
mone = one * -1

def tiles2image(tiles):
    #print titles/z_dims
    return plt.get_cmap('rainbow')(tiles/float(z_dims))

    #return plt.get_cmap('rainbow')(tiles/float(z_dims))

def combine_images(generated_images):
    num = generated_images.shape[0]
    width = int(math.sqrt(num))
    height = int(math.ceil(float(num)/width))
    shape = generated_images.shape[1:]
    image = np.zeros((height*shape[0], width*shape[1],shape[2]), dtype=generated_images.dtype)
    for index, img in enumerate(generated_images):
        i = int(index/width)
        j = index % width
        image[i*shape[0]:(i+1)*shape[0], j*shape[1]:(j+1)*shape[1]] = img
    return image

if opt.cuda:
    netD.cuda()
    netG.cuda()
    input = input.cuda()
    one, mone = one.cuda(), mone.cuda()
    noise, fixed_noise = noise.cuda(), fixed_noise.cuda()

# setup optimizer
if opt.adam:
    optimizerD = optim.Adam(netD.parameters(), lr=opt.lrD, betas=(opt.beta1, 0.999))
    optimizerG = optim.Adam(netG.parameters(), lr=opt.lrG, betas=(opt.beta1, 0.999))
    print("Using ADAM")
else:
    optimizerD = optim.RMSprop(netD.parameters(), lr = opt.lrD)
    optimizerG = optim.RMSprop(netG.parameters(), lr = opt.lrG)

gen_iterations = 0
for epoch in range(opt.niter):
    
    #! data_iter = iter(dataloader)

    X_train = X_train[torch.randperm( len(X_train) )]

    i = 0
    while i < num_batches:#len(dataloader):
        ############################
        # (1) Update D network
        ###########################
        for p in netD.parameters(): # reset requires_grad
            p.requires_grad = True # they are set to False below in netG update

        # train the discriminator Diters times
        if gen_iterations < 25 or gen_iterations % 500 == 0:
            Diters = 100
        else:
            Diters = opt.Diters
        j = 0
        while j < Diters and i < num_batches:#len(dataloader):
            j += 1

            # clamp parameters to a cube
            for p in netD.parameters():
                p.data.clamp_(opt.clamp_lower, opt.clamp_upper)

            #! idx = random.randint(0, num_samples/opt.batchSize-1)

            #print(idx)

            #TODO Use random permutation..
            #TODO Add ranom noise? Easy to tell discrete and contnious images apart?

            #! data = X_train[idx*opt.batchSize:(idx+1)*opt.batchSize]
            

            data = X_train[i*opt.batchSize:(i+1)*opt.batchSize]
            #print(data)

            #data = data_iter.next()

            #data = map_data

            #print(data)
            #exit() #[torch.FloatTensor of size 64x3x64x64]
            #print(np.array(data).shape)
            #exit()

            i += 1

            # train with real
            
            #!real_cpu, _ = data
            real_cpu = torch.FloatTensor(data)

            #print (data.shape, real_cpu.shape,  np.argmax(real_cpu, axis = 1)[10] ) # tiles2image( np.argmax(real_cpu, axis = 1) ) )

            if (False):
                #im = data.cpu().numpy()
                print(data.shape)
                real_cpu = combine_images( tiles2image( np.argmax(data, axis = 1) ) )
                print(real_cpu)
                plt.imsave('{0}/real_samples.png'.format(opt.experiment), real_cpu)
                exit()

            #print(type(real_cpu))
            #exit()
            #real_cpu = real_cpu.mul(0.5).add(0.5)
            #print(real_cpu[10].shape)

            #m =tiles2image( np.argmax(X_onehot[10], axis = 0) )

            #print (m)

            #print (X.shape) #(173, 14, 28)
            
            #k = X[15] / float(z_dims)
            #plt.get_cmap('rainbow')
            #print (k)
            #real_cpu = tiles2image(X[15])
            #plt.imsave("sample_single.png", real_cpu)

            
            #print(real_cpu)

            #2 = air
            #0 = ground

            #plt.imshow(real_cpu)
            #print(real_cpu.shape)

            #exit()
            #real_cpu = real_cpu[:,:3,:,:]
            #print(real_cpu.shape)
            #for index, img in enumerate(real_cpu):
            #    plt.imsave('real_samples.png',img)
            #real_cpu = torch.FloatTensor(tiles2image(real_cpu[10]) )
            #print (real_cpu.shape)
            #real_cpu[:,np.argmax(real_cpu, axis=1),:,:]

            #real_cpu = plt.get_cmap(real_cpu[0])

            #vutils.save_image(real_cpu, 'real_samples.png')
           
            netD.zero_grad()
            #batch_size = num_samples #real_cpu.size(0)

            if opt.cuda:
                real_cpu = real_cpu.cuda()

            input.resize_as_(real_cpu).copy_(real_cpu)
            inputv = Variable(input)

            errD_real = netD(inputv)
            errD_real.backward(one)

            # train with fake
            noise.resize_(opt.batchSize, nz, 1, 1).normal_(0, 1)
            noisev = Variable(noise, volatile = True) # totally freeze netG
            fake = Variable(netG(noisev).data)
            inputv = fake
            errD_fake = netD(inputv)
            errD_fake.backward(mone)
            errD = errD_real - errD_fake
            optimizerD.step()

        ############################
        # (2) Update G network
        ###########################
        for p in netD.parameters():
            p.requires_grad = False # to avoid computation
        netG.zero_grad()
        # in case our last batch was the tail batch of the dataloader,
        # make sure we feed a full batch of noise
        noise.resize_(opt.batchSize, nz, 1, 1).normal_(0, 1)
        noisev = Variable(noise)
        fake = netG(noisev)
        errG = netD(fake)
        errG.backward(one)
        optimizerG.step()
        gen_iterations += 1

        print('[%d/%d][%d/%d][%d] Loss_D: %f Loss_G: %f Loss_D_real: %f Loss_D_fake %f'
            % (epoch, opt.niter, i, num_batches, gen_iterations,
            errD.data[0], errG.data[0], errD_real.data[0], errD_fake.data[0]))
        if gen_iterations % 50 == 0:   #was 500
            #!real_cpu = real_cpu.mul(0.5).add(0.5)
            #!real_cpu = plt.get_cmap(real_cpu)

            #print("SHAPE real ", real_cpu.shape)
            #real_cpu = combine_images(tiles2image( np.argmax(real_cpu, axis = 1) ))
            #plt.imsave('{0}/real_samples.png'.format(opt.experiment), real_cpu)

            #vutils.save_image(real_cpu, '{0}/real_samples.png'.format(opt.experiment))
            fake = netG(Variable(fixed_noise, volatile=True))
            
            if (True):
                im = fake.data.cpu().numpy()
                #print('SHAPE fake',type(im), im.shape)
                
                #print('SUM ',np.sum( im, axis = 1) )

                im = combine_images( tiles2image( np.argmax( im, axis = 1) ) )

                plt.imsave('{0}/mario_fake_samples_{1}.png'.format(opt.experiment, gen_iterations), im)
                torch.save(netG.state_dict(), '{0}/netG_epoch_{1}.pth'.format(opt.experiment, gen_iterations))

            #REMOVE
            #fake.data = fake.data.mul(0.5).add(0.5)
    
            #vutils.save_image(fake.data, '{0}/fake_samples_{1}.png'.format(opt.experiment, gen_iterations))

    # do checkpointing
    #torch.save(netG.state_dict(), '{0}/netG_epoch_{1}.pth'.format(opt.experiment, epoch))
    #torch.save(netD.state_dict(), '{0}/netD_epoch_{1}.pth'.format(opt.experiment, epoch))
