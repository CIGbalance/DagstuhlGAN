#!/bin/sh
#$ -j y
#$ -pe smp 2
#$ -cwd
#$ -l h_rt=24:00:00
#$ -l h_vmem=3G
#$ -m bea
#$ -t 1-100
#$ -tc 35

inputDir=mario1-1
ox=28
oy=14
fx=$1
fy=$1
seed=$RANDOM
border="True"
b="."

module load python/2.7.15

cd /data/autoScratch/weekly/$USER
#$ -cwd

python $HOME/DagstuhlGAN/wfc/wfc.py --inputDir ${HOME}/DagstuhlGAN/wfc/training_levels/${inputDir} -ox $ox -oy $oy -fx $fx -fy $fy --seed $seed -b "$b" > log_${inputDir}_${ox}_${oy}_${fx}_${fy}_${seed}_${border}.txt

