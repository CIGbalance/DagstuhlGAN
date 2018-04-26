# DagstuhlGAN

This project allows for the unsepervised learning of a Generative Adversarial Network (GAN) that
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

## Using the Code

