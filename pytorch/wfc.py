from __future__ import print_function
import argparse
import os
import numpy as np
from math import log
import random
import sys
from io import StringIO

#parser = argparse.ArgumentParser()
#parser.add_argument('--inputDir', default=None, help='InputFolder')

#opt = parser.parse_args()

border="."
#inputFolder=opt.inputDir
inputFolder="levels"

filter_x = 3
filter_y = 3

output_x = 100
output_y = 14

if border!="":
    output_x+=2
    output_y+=2
patterns_x = output_x-filter_x +1
patterns_y = output_y-filter_y+1

#def translate(input, translateMap):
#    for x in np.nditer(input, op_flags=['readwrite']):
#        tmp = np.array2string(x)
#        if tmp not in translateMap:
#            translateMap[tmp]=len(translateMap)
#        x[...] = translateMap[tmp]
#    return translateMap



def patternsFromSample(patternsMap, input, filter_x, filter_y, x_dims, y_dims):
    total = 0
    for x in range(0,x_dims-filter_x+1):
        for y in range(0, y_dims-filter_y+1):
            pattern = input[y,x:(x + filter_x)]
            for p in range(1, filter_y):
                pattern = np.append(pattern, input[y+p,x:(x + filter_x)])
            pattern= "".join(pattern)
            if pattern not in patternsMap:
                patternsMap[pattern]=1
            else:
                patternsMap[pattern]+=1
            total+=1
    return total

def processInput(inputFolder):
    patternsMap={}
    #translateMap={}
    total=0
    for file in os.listdir(inputFolder):
        if file.endswith(".txt"):
            input = np.genfromtxt(inputFolder+"/"+file, delimiter=1, dtype='a')
            x_dims = input.shape[1]  # sample width
            y_dims = input.shape[0]  # sample height
            print(input)
            if border != "":
                border_input = np.full((y_dims + 2, x_dims + 2), np.array(border), dtype='a')
                border_input[1:y_dims + 1, 1:x_dims + 1] = input
                input = border_input
                x_dims = input.shape[1]  # sample width
                y_dims = input.shape[0]  # sample height
                print(input)
            #translateMap = translate(input,translateMap)
            #input = input.astype(int)
            #print(input)
            total += patternsFromSample(patternsMap, input, filter_x, filter_y, x_dims, y_dims)
    for key, value in patternsMap.items():
        patternsMap[key]=float(value)/total

    #translateMap[np.array2string(np.array(" "))] = len(translateMap)
    #reverseTranslate = {}
    #for key, value in translateMap.items():
    #    reverseTranslate[value] = key
    return patternsMap#, patternsMap, reverseTranslate

def patternSubset(pattern, offset_y, offset_x):
    p = np.array(list(pattern))
    #p = np.array(pattern.replace("[","").replace("]","").split(" "),dtype=int)
    p.shape=(filter_y, filter_x)
    if offset_x<0 and offset_y<0:
        p = p[0:filter_y+offset_y:,0:filter_x+offset_x]
    elif offset_x<0 and offset_y>=0:
        p = p[offset_y:,0:filter_x+offset_x]
    elif offset_x>=0 and offset_y<0:
        p= p[0:filter_y+offset_y,offset_x:]
    elif offset_x>=0 and offset_y >=0:
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
                    if np.all(psub1==psub2):
                        coefficientMatrix[index1][index][index2] = 1
                index += 1
    return coefficientMatrix

def buildLevel(patternsMap, patterns_y, patterns_x):
    level = [[[1] * len(patternsMap.keys()) for i in range(patterns_x)] for j in range(patterns_y)]
    return np.array(level)

def buildWave(patterns_y, patterns_x):
    wave = [[1] * patterns_x for i in range(patterns_y)]
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
        sys.exit(-1)
    elif cell == [-1]*2:
        return True
    #print(cell)
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
                    prev_allowed = level[neighbour[0]][neighbour[1]]
                    allowed_patterns = [0] * len(prev_allowed)
                    for p in np.where(availPatternsOrig == True)[0]:
                        allowed_patterns = [max(a, b) for a, b in zip(allowed_patterns, coefficientMatrix[p][index])]
                    allowed_patterns = [min(a, b) for a, b in zip(prev_allowed,allowed_patterns)]
                    if sum(allowed_patterns)==0:
                        sys.exit(1)
                    #if sum(allowed_patterns)==1:
                    #    print("test")
                    if np.sum(prev_allowed)>np.sum(allowed_patterns):
                        wave[neighbour[0]][neighbour[1]]=1
                        level[neighbour[0]][neighbour[1]] = allowed_patterns
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
            #print(wave)
            #print("---")
            #print(allSum)
            allSum=0
    return 0

def convertToLevel(level, patternsMap):
    patterns = [[-1]*patterns_x for i in range(patterns_y)]
    for y in range(patterns_y):
        for x in range(patterns_x):
            if sum(level[y][x])==1:
                patterns[y][x] = patternsMap.keys()[np.where(level[y][x]==1)[0][0]]
    #print(patterns)
    output =  [[0]*output_x  for i in range(output_y)]
    for y in range(patterns_y):
        for x in range(patterns_x):
            pattern = patterns[y][x]
            if pattern != -1:
                p = np.array(list(pattern))
                # p = np.array(pattern.replace("[","").replace("]","").split(" "),dtype=int)
                p.shape = (filter_y, filter_x)
                for offset_y in range(filter_y):
                    for offset_x in range(filter_x):
                        output[y+offset_y][x+offset_x]=p[offset_y][offset_x]
    #print(output)
    #for y in range(output_y):
    #    for x in range(output_x):
    #        output[y][x]=reverseTranslate[output[y][x]].replace("'","")
    for xs in output:
        print("".join(map(str, xs)).replace("0"," "))


def doBorderStuff(patternsMap, border, level, wave):
    border_bottom = [0]*len(patternsMap.keys())
    border_top = [0]*len(patternsMap.keys())
    border_left = [0]*len(patternsMap.keys())
    border_right = [0]*len(patternsMap.keys())
    no_border = [1]*len(patternsMap.keys())
    any_border = [0]*len(patternsMap.keys())

    for i, pattern in enumerate(patternsMap):
        p = np.array(list(pattern))
        # p = np.array(pattern.replace("[","").replace("]","").split(" "),dtype=int)
        p.shape = (filter_y, filter_x)
        if np.all(p[0,:]==border):
            border_top[i]=1
            any_border[i]=1
        if np.all(p[filter_y-1,:]==border):
            border_bottom[i]=1
            any_border[i]=1
        if np.all(p[:,0]==border):
            border_left[i]=1
            any_border[i]=1
        if np.all(p[:,filter_x-1]==border):
            border_right[i]=1
            any_border[i]=1
    no_border = [a-b for a,b in zip(no_border,any_border)]
    for x in range(patterns_x):
        level[0][x] = [min(a,b) for a,b in zip(level[0][x],border_top)]
        level[patterns_y-1][x] = [min(a,b) for a,b in zip(level[patterns_y-1][x],border_bottom)]
    for y in range(patterns_y):
        level[y][0] = [min(a,b) for a,b in zip(level[y][0],border_left)]
        level[y][patterns_x-1] = [min(a,b) for a,b in zip(level[y][patterns_x-1],border_right)]
    for y in range(1,patterns_y-1):
        for x in range(1,patterns_x-1):
            level[y][x] = no_border
    print(wave)
    print(np.sum(level, axis=2))
    propagate(level, coefficientMatrix, wave, patterns_y, patterns_x, filter_y, filter_x)
    print(wave)
    print(np.sum(level, axis=2))

patternsMap= processInput(inputFolder)
print(patternsMap)
#print(list(patternsMap.keys())[0])
coefficientMatrix=buildPropagator(patternsMap, filter_y, filter_x)
#print(coefficientMatrix)
level = buildLevel(patternsMap, patterns_y, patterns_x)
wave = buildWave(patterns_y, patterns_x)
if border!="":
    doBorderStuff(patternsMap,border,level,wave)
done = False
while not done:
    done = observe(patternsMap,level,patterns_y,patterns_x, wave)
    propagate(level, coefficientMatrix,wave,patterns_y,patterns_x,filter_y,filter_x)
    convertToLevel(level, patternsMap)
    print("")
    #print(np.sum(level, axis=2))
    #print("......")

#convertToLevel(level,reverseTranslate, patternsMap)

#cell = findLowestEntropy(patternsMap,level, y_dims, x_dims)
#print(cell)
#print(patternSubset(list(patternsMap.keys())[0],-1,-1))
