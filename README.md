# MarioGAN

This project allows for the unsupervised learning of a Generative Adversarial Network (GAN) that
understands the structure of Super Mario Bros. levels. The model is trained on actual Mario levels from
the [Video Game Level Corpus](https://github.com/TheVGLC/TheVGLC). The trained model is capable of generating
new level segments with the input of a latent vector, and these segments can be stitched together to
make complete levels. In order to find the best level segments within this latent space, the
evolutionary algorithm Covariance Matrix Adaptation Evolution Strategy (CMA-ES) is used to find latent
vectors producing level segments that either optimize some sort of tile distribution or result in a
particular level of performance by an artificial agent. The resulting system helps discover new levels
in the space of examples created by human experts.

For more information, please see the following publication. This publication should also be cited if
code from this project is used in any way:

```
@inproceedings{volz:gecco2018,
	title={Evolving Mario Levels in the Latent Space of a Deep Convolutional Generative Adversarial Network},
	author={Volz, Vanessa and Schrum, Jacob and Liu, Jialin and Lucas, Simon M. and Smith, Adam M. and Risi, Sebastian},
	year={2018},
	booktitle={Proceedings of the Genetic and Evolutionary Computation Conference (GECCO 2018)},
	month={July},
	numpages = {8},
	url = {http://doi.acm.org/10.1145/3205455.3205517},
	doi = {10.1145/3205455.3205517},
	publisher = {ACM},
	address = {New York, NY, USA},
	location={Kyoto, Japan}
}
```

## Installing

The framework is build on PyTorch. Installation instructions for PyTorch can be found here: https://pytorch.org/

The latent vectors are optimized with a Python version of CMA-ES, which can be installed with:

```
python -m pip install cma
```

## Using the Code

There are two separate aspects to this codebase:

1. The GAN code written in Python and trained with Pytorch
2. The Mario and CMA-ES code written in Java

### Training the GAN

An already trained Pytorch model is part of this repository. It is in 
[pytorch/netG_epoch_5000.pth](https://github.com/TheHedgeify/DagstuhlGAN/blob/master/pytorch/netG_epoch_5000.pth).
However, if you would like to re-train the GAN yourself from scratch you can run

```
python main.py
```

Once the GAN is trained (or if you use the included GAN), you can run the Java class viewer.MarioRandomLevelViewer
to generate level images by sending several randomly generated latent vectors to the GAN.
If you want to see what level is generated for a specific vector of your choice, then you can use
viewer.MarioLevelViewer, which takes a json array of length 32 as a command line parameter that represents
a latent vector for the GAN. If you want to actually play these levels, then you should launch
viewer.MarioLevelPlayer in the same way. Additionally, if MarioLevelViewer or MarioLevelPlayer are sent a 2D json array,
then each sub-array is interpreted as a separate latent vector for the GAN, and the segments are
stitched together into a larger level.

### Evolving Levels Based on Static Features

The trained GAN model can be used to generate Mario levels that optimize certain tile distributions. See gan_optimize.py for details.

### Evolving Levels Based on Agent Performance

The trained GAN model can also be used to evolve levels based on how a Java-based agent performs in them.
This approach uses the Java version of CMA-ES, though the Java code still executes the Python Pytorch model.
The Java class to execute is [cmatest.CMAMarioSolver](https://github.com/TheHedgeify/DagstuhlGAN/blob/master/marioaiDagstuhl/src/cmatest/CMAMarioSolver.java).
This code will evaluate the levels by playing them with Robin Baumgarten's A* Agent that won the 2009 Mario AI Competition.

The level is specified by a latent vector of modifiable size. The values need to be between -1 and 1 and should otherwise be mapped to that value range. To generate an image of the generated level, the class to execute is [cmatest.MarioLevelViewer](https://github.com/TheHedgeify/DagstuhlGAN/blob/master/marioaiDagstuhl/src/viewer/MarioLevelViewer.java). To do this, the class [cmatest.MarioEvalFunction](https://github.com/TheHedgeify/DagstuhlGAN/blob/master/marioaiDagstuhl/src/cmatest/MarioEvalFunction.java) has the function levelFromLatentVector that returns a level, which can be played by an agent (human or artifical) via the BasicSimulator [ch.idsia.mario.simulation.BasicSimulator](https://github.com/TheHedgeify/DagstuhlGAN/blob/master/marioaiDagstuhl/src/ch/idsia/mario/simulation/BasicSimulator.java).

Alternatively, the level can created from a .json file (as produced by the GAN) that describes the level with nested arrays and encodes the different tiles available according to the video level corpus. To do this, use function marioLevelsFromJson in [cmatest.MarioEvalFunction](https://github.com/TheHedgeify/DagstuhlGAN/blob/master/marioaiDagstuhl/src/cmatest/MarioEvalFunction.java)

### Play a Level Created By the GAN

In order to play a level created by the GAN, use [viewer.MarioLevelPlayer](https://github.com/TheHedgeify/DagstuhlGAN/blob/master/marioaiDagstuhl/src/viewer/MarioLevelPlayer.java). If a single latent vector is sent to the class as a command line parameter, then an individual level segment will be produced. However, if multiple latent vectors are sent to the GAN, then the different segments will be stitched together in sequence to form one long level.
