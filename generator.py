
# This generator program expands a low-dimentional latent vector into a 2D array of tiles.
# Usage: generator.py architecture.json weights.h5 < z.jsons > levels.jsons

import sys
import json
import numpy
from keras.models import model_from_json

if __name__ == '__main__':
	_, architecture_filename, weights_filename = sys.argv
	with open(architecture_filename) as f:
		model = model_from_json(f.read())
	model.load_weights(weights_filename, True)

	for line in sys.stdin.readlines():
		z = numpy.array([json.loads(line)])
		level = model.predict(z).argmin(-1)[0]
		print(json.dumps(level.tolist()))
