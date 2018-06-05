#!/bin/bash
budget=5000
dims=( 10 20 30 40 )
seeds=( 5641 3854 8370 494 1944 9249 2517 2531 5453 2982 670 56 6881 1930 5812 )
for seed in "${seeds[@]}"
do
    for dim in "${dims[@]}"
    do
        echo "python main.py --niter $budget --nz $dim --json underground.json --seed $seed"
        python main.py --niter $budget --nz $dim --json underground.json --experiment underground-$dim-$budget --seed $seed
        echo "python main.py --niter $budget --nz $dim --json overworld.json --seed $seed"
        python main.py --niter $budget --nz $dim --json overworld.json --experiment overworld-$dim-$budget --seed $seed
    done
done

