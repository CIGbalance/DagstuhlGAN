from __future__ import print_function
import argparse
import os
import numpy as np
from math import log
import random


parser = argparse.ArgumentParser()
parser.add_argument('--experiment', default=None, help='Where to store samples and models')

opt = parser.parse_args()
print(opt)

if opt.experiment is None:
    opt.experiment = 'samples'
os.system('mkdir {0}'.format(opt.experiment))

input = np.genfromtxt("lvlexample.txt", delimiter=1, dtype='a')
x_dims = input.shape[1] #sample width
y_dims = input.shape[0] #sample height

filter_x = 2
filter_y = 2

output_x = 28
output_y = 14

patterns_x = output_x-filter_x +1
patterns_y = output_y-filter_y+1

def translate(input):
    translateMap = {}
    for x in np.nditer(input, op_flags=['readwrite']):
        tmp = np.array2string(x)
        if tmp not in translateMap:
            translateMap[tmp]=len(translateMap)
        x[...] = translateMap[tmp]
    return translateMap



def patternsFromSample(input, filter_x, filter_y):
    patternsMap = {}
    total = 0
    for x in range(0,x_dims-filter_x):
        for y in range(0, y_dims-filter_y):
            pattern = input[y,x:(x + filter_x)]
            for p in range(1, filter_y):
                pattern = np.append(pattern, input[y+p,x:(x + filter_x)])
            pattern = np.array2string(pattern)
            if pattern not in patternsMap:
                patternsMap[pattern]=1
            else:
                patternsMap[pattern]+=1
            total+=1
    for key, value in patternsMap.items():
        patternsMap[key]=float(value)/total
    return patternsMap

def patternSubset(pattern, offset_y, offset_x):
    p = np.array(pattern.replace("[","").replace("]","").split(" "),dtype=int)
    p.shape=(filter_y, filter_x)
    if offset_x<0 or offset_y<0:
        p = p[0:filter_y+offset_y:,0:filter_x+offset_x]
    else:
        p = p[offset_y:,offset_x:]
    return p

def buildPropagator(patternsMap, filter_y, filter_x):
    coefficientMatrix = [[[0] * len(patternsMap.keys()) for i in range(((2*(filter_y-1)+1)*(2*(filter_x-1)+1)))] for j in range(len(patternsMap.keys()))]
    for index1, p1 in enumerate(patternsMap):
        index = 0
        for i in range(-1 * filter_y + 1, filter_y):
            for j in range(-1 * filter_x + 1, filter_x):
                psub1 = patternSubset(p1, i, j)
                for index2, p2 in enumerate(patternsMap):
                    psub2 = patternSubset(p2, -i, -j)
                    if np.all(np.equal(psub1, psub2)):
                        coefficientMatrix[index1][index][index2] = 1
                index += 1
    return coefficientMatrix

def buildLevel(patternsMap, patterns_y, patterns_x):
    level = [[[1] * len(patternsMap.keys()) for i in range(patterns_x)] for j in range(patterns_y)]
    return np.array(level)

def buildWave(patterns_y, patterns_x):
    wave = [[0] * patterns_x for i in range(patterns_y)]
    return np.array(wave)


def findLowestEntropy(patternsMap, level, patterns_y, patterns_x):
    cell = [-1]*2
    minEntropy = float("inf")
    for y in range(0,patterns_y):
        for x in range(0,patterns_x):
            valid_patterns = sum(level[y][x])
            if valid_patterns == 0:
                return None
            elif valid_patterns==1:
                continue
            pattern_probs = np.array([a*b for a,b in zip(patternsMap.values(),level[y][x])])
            pattern_probs = pattern_probs[pattern_probs!=0]
            entropy = -1*sum([a*b for a,b in zip(pattern_probs,[log(v, 2) for v in pattern_probs])]) + random.uniform(0, 0.000001)
            if entropy < minEntropy:
                minEntropy=entropy
                cell = [y,x]
    return cell

def observe(patternsMap, level, patterns_y, patterns_x, wave):
    cell = findLowestEntropy(patternsMap, level, patterns_y, patterns_x)
    if cell == None:
        return None
    elif cell == [-1]*2:
        return True
    pattern_probs = [a * b for a, b in zip(patternsMap.values(), level[cell[0]][cell[1]])]
    #print(pattern_probs)
    pattern = min(np.where(np.cumsum(pattern_probs) > random.uniform(0, sum(pattern_probs)))[0])
    #print(pattern)
    ##could produce errors if value is exact max value. ignore for now
    level[cell[0]][cell[1]] = [0]*len(patternsMap.keys())
    level[cell[0]][cell[1]][pattern]=1
    wave[cell[0]][cell[1]]=1
    #print(level[cell[0]][cell[1]])
    #print(wave[cell[0]][cell[1]])
    return False

def propagate(level,coefficientMatrix, wave, patterns_y, patterns_x, filter_y, filter_x):
    y=0
    x=0
    while np.sum(wave)!=0:
        if wave[y][x]==1:#cell flagged for update
            wave[y][x]=0
            index=0
            availPatternsOrig = level[y][x]
            for i in range(-1 * filter_y + 1, filter_y):
                for j in range(-1 * filter_x + 1, filter_x):
                    neighbour = [y+i, x+j]
                    if neighbour[0]<0 or neighbour[1]<0 or neighbour[0]>=patterns_y or neighbour[1]>=patterns_x:##only cells inside level
                        index+=1
                        continue
                    if i== 0 and j == 0:
                        index+=1
                        continue
                    allowed_patterns = [0]*len(availPatternsOrig)
                    for p in np.where(availPatternsOrig == True)[0]:
                        allowed_patterns = [max(a, b) for a, b in zip(allowed_patterns, coefficientMatrix[p][index])]
                    if sum(allowed_patterns)==0:
                        print(p)
                        break
                    #if sum(allowed_patterns)==1:
                    #    print("test")
                    if np.sum(level[neighbour[0]][neighbour[1]])>np.sum(allowed_patterns):
                        wave[neighbour[0]][neighbour[1]]=1
                    level[neighbour[0]][neighbour[1]] = [min(a,b) for a,b in zip(level[neighbour[0]][neighbour[1]],allowed_patterns)]
                    index+=1
        x+=1
        if x >= patterns_x:
            x=0
            y+=1
        if y>=patterns_y:
            x=0
            y=0
            #print(np.sum(wave))
            #print(np.sum(level))
            #print(np.sum(level,axis=2))
            #print(wave)
            #print("---")
            #print(allSum)
            allSum=0
    return 0

def convertToLevel(level, translateMap):
    patterns = [[-1]*patterns_x for i in range(patterns_y)]
    for y in range(patterns_x):
        for x in range(patterns_y):
            patterns[y][x] = np.where(level[y][x]==1)
    reverseTranslate = {}
    for k,v in
    output =  [[-1]*output_x  for i in range(output_y)]


translateMap = translate(input)
#print(translateMap)
input = input.astype(int)
#print(input)
patternsMap = patternsFromSample(input, filter_x, filter_y)
print(patternsMap)
#print(list(patternsMap.keys())[0])
coefficientMatrix=buildPropagator(patternsMap, filter_y, filter_x)
print(coefficientMatrix)
level = buildLevel(patternsMap, patterns_y, patterns_x)
wave = buildWave(patterns_y, patterns_x)
done = False
while not done:
    done = observe(patternsMap,level,patterns_y,patterns_x, wave)
    propagate(level, coefficientMatrix,wave,patterns_y,patterns_x,filter_y,filter_x)
    print(np.sum(level, axis=2))


#cell = findLowestEntropy(patternsMap,level, y_dims, x_dims)
#print(cell)
#print(patternSubset(list(patternsMap.keys())[0],-1,-1))
