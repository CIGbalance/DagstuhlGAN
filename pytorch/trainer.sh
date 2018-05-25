#!/bin/bash
budget=50
jsons=15
dims=( 10 20 30 40 )

for (( json=1; json<=$jsons; json+=1 ))
do
    for dim in "${dims[@]}"
    do
        echo "python main.py --niter $budget --nz $dim --problem $json"
        python main.py --niter $budget --nz $dim --problem $json --experiment samples-$budget-$json-$dim
    done
done

