#!/bin/sh
#$ -j y
#$ -pe smp 2
#$ -l h_rt=8:00:00
#$ -l h_vmem=2G
#$ -M v.volz@qmul.ac.uk
#$ -m bea

inputDir=overworld
ox=28
oy=14
fx=2
fy=2
seed=$RANDOM
border="True"
b="."


module load python/2.7.15

cd /data/autoScratch/weekly/$USER
#$ -cwd

python $HOME/DagstuhlGAN/wfc/wfc.py --inputDir ${HOME}/DagstuhlGAN/wfc/training_levels/${inputDir} -ox $ox -oy $oy -fx $fx -fy $fy --seed $seed -b "$b" > log_${inputDir}_${ox}_${oy}_${fx}_${fy}_${seed}_${border}.txt
