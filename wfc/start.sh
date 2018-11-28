inputDir=overworld
ox=28
oy=14
fx=2
fy=2

border="True"
b="."

for (( cbatch=1; cbatch<=100; cbatch+=1 ))
do
    sleep 2s
    seed=$RANDOM
    cat <<EOS | qsub -
#!/bin/sh
#$ -j y
#$ -pe smp 2
#$ -l h_rt=00:30:00
#$ -l h_vmem=2G
#$ -M v.volz@qmul.ac.uk
#$ -m bea
module load python/2.7.15

cd /data/autoScratch/weekly/$USER
#$ -cwd

python $HOME/DagstuhlGAN/wfc/wfc.py --inputDir ${HOME}/DagstuhlGAN/wfc/training_levels/${inputDir} -ox $ox -oy $oy -fx $fx -fy $fy --seed $seed -b "$b" > log_${inputDir}_${ox}_${oy}_${fx}_${fy}_${seed}_${border}.txt
EOS
done
