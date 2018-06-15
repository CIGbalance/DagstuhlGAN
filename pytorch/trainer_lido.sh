#!/bin/bash
#budget=5000
budget=50
#dims=( 10 20 30 40 )
dims=( 10 )
#seeds=( 5641 3854 8370 494 1944 9249 2517 2531 5453 2982 670 56 6881 1930 5812 )
seeds=( 5641 )
for seed in "${seeds[@]}"
do
    for dim in "${dims[@]}"
    do
        sleep 2s
    cat <<EOS | qsub -
#!/bin/bash
###### PBS options for a serial job #############################################

### Select the queue where to submit the job to,
### e.g. short,med,long,ultralong
### Default: short
#PBS -q long

### Expected run time of the job.
### The job will be terminated if this time is exceeded.
### This is a serial job, so the number of nodes is set to 1.
### It should run on a standard node
### Default: 1 core on 1 node for 10 minutes
#PBS -l walltime=48:00:00

### Define the name of the job.
### Default: name of the PBS script.
#PBS -N GANtrainer-overworlds-$dims-$seed

### Specify whether and on what event you want to use e-mail notification:
### [b]egin, [a]bort, [e]nd
### Default: no notification
#PBS -m bae

### e-mail address for job notification messages.
### If no full name is given (e.g. just the username), a .forward file must be
### configured in the user's home directory, otherwise the mail will be discarded.
### Default: username
### Attention: Be sure to specify an existing e-mail address
### ---------  instead of the template's address below !!!
#PBS -M vanessa.volz@udo.edu

### File to redirect standard output of the job to.
### Make sure to use a unique file name.
### If you do not care about standard output, use "PBS -o /dev/null"
### Default: Name of jobfile plus ".o" plus number of PBS job
#PBS -o output.GANtrainer-overworlds-$dims-$seed

### This option redirects stdout and stderr into the same output file
### (see PBS option -o).
#PBS -j oe

###### End of PBS options #######################################################


### The following command, if uncommented by deleting the hash sign in front of 'cat',
### saves the name of the compute node (to which the job is submitted by the batch system).
### This information may be useful when debugging.
### This information can also be retrieved while the job is being executed via "qstat -f jobid".
###
### Be sure to use a unique file name (!), though, to avoid concurrent write access
### which may happen when multiple jobs of yours are started simultaneously.
###
### SB, 2016-Nov-20: Be sure to escape the dollar sign (unless it is nested in single ticks), in
###                  order to avoid that this enviroment variable gets already expanded at the
###                  time the outer bash script is invoked. At that time $HOME is set such that,
###                  strictly speaing, there is no need to escape $ in $HOME, but the $PBS_*
###                  environment variables are not and would get expanded to an empty string.
#cat \${PBS_NODEFILE} > \${HOME}/pbs-machine.\${PBS_JOBID}

### Go to the application's working directory
cd $HOME/DagstuhlGAN/pytorch

### SB, 2016-Nov-20
### Make 'module' command available by parsing its definition and use non-system python
### (This can be skipped for now as this is currently already done in \${HOME}/.bashrc)
#. ${MODULESHOME}/init/bash
#module load python/2.7.11


### Start the application
echo $cbatch
test -d /data/volz/marioGAN || mkdir -p /data/volz/marioGAN
python python main.py --niter $budget --nz $dim --json overworlds.json --experiment /data/volz/marioGAN/overworld-$dim-$budget --seed $seed
wait
EOS

    done
done
    
