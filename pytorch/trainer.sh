#!/bin/bash
budget=5000
dims=( 5 10 20 30 40 )
seeds=( 5641 3854 8370 494 1944 9249 2517 2531 5453 2982 670 56 6881 1930 5812 )
for seed in "${seeds[@]}"
do
    for dim in "${dims[@]}"
    do
        echo "python main.py --niter $budget --nz $dim --json underground.json --seed $seed"
        python main.py --niter $budget --nz $dim --json underground.json --experiment /scratch/results/marioGAN/underground-$dim-$budget --seed $seed
        echo "python main.py --niter $budget --nz $dim --json overworld.json --seed $seed"
        python main.py --niter $budget --nz $dim --json overworld.json --experiment /scratch/results/marioGAN/overworld-$dim-$budget --seed $seed
        echo "python main.py --niter $budget --nz $dim --json overworlds.json --seed $seed"
        python main.py --niter $budget --nz $dim --json overworlds.json --experiment /scratch/results/marioGAN/overworlds-$dim-$budget --seed $seed
    done
done

