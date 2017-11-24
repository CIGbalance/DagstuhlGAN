
# This generator program expands a low-dimentional latent vector into a 2D array of tiles.
# Usage: generator.py architecture.json weights.h5 < z.jsons > levels.jsons
# Each line of input should be an array of z vectors (which are themselves arrays of floats -1 to 1)
# Each line of output is an array of levels (which are arrays-of-arrays of integer tile ids)

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
		zs = numpy.array(json.loads(line))
		levels = model.predict(zs).argmin(-1)
		print(json.dumps(level.tolist()))
