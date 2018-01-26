latVecLength=32
budget=1008

# parameters:
# - path: relative path where results are stored
readAll <- function(path){
  # first, determine the folder of each algorithm for which data is available
  runs <- dir(path, full.names = FALSE)
  runs_data = list()
  for(f in runs){
    print(f)
    file <- paste(path,f, sep="/")
    res <- readLines(file) #read file
    run_data = matrix(NA, nrow=budget, ncol=(latVecLength+1))
    for(i in 1:length(res)){
      line = res[i]
      line=gsub("\\[", "", line)
      line = gsub("\\]", "", line)
      line=gsub(":", ",", line)
      run_data[i,] = as.numeric(unlist(strsplit(line, split=", ")))
    }
    runs_data = c(runs_data, list(run_data))
  }
  save(runs_data, file="runs_data")
}

#path="."
#readAll(path)

processRuns = function(){
  best_inds = matrix(NA, nrow=length(runs_data), ncol=(latVecLength+1))
  for(i in 1:length(runs_data)){
    data = runs_data[[i]]
    best_inds[i,] = data[which.min(data[, latVecLength+1]),]
    if(best_inds[i,latVecLength+1]<=-15){
      print(best_inds[i,])
    }
  }
  plot(1:length(runs_data), best_inds[, latVecLength+1], main="best individuals", xlab="run", ylab="fitness")
  minY = min(best_inds[, latVecLength+1])
  plot(0, type="n", ylim=c(minY, 0), xlim=c(1,budget), main="all individuals", xlab="iteration", ylab="fitness")
  for(i in 1:length(runs_data)){
    data = runs_data[[i]]
    points(1:budget, data[,latVecLength+1], pch=".")
  }
  avgs = numeric(budget)
  sds = numeric(budget)
  for(i in 1:budget){
    vec = numeric(length(runs_data))
    for(j in 1:length(runs_data)){
      data = runs_data[[j]]
      vec[j]=data[i,latVecLength+1]
    }
    avgs[i]=mean(vec)
    sds[i]=sd(vec)
  }
  plot(1:budget, avgs, main="mean fitness over time", xlab="iteration", ylab="fitness", type="l")
  plot(1:budget, avgs, main="mean fitness over time with standard deviation", xlab="iteration", ylab="fitness", type="l", ylim=c(min(avgs-1.5*sds), max(avgs+1.5*sds)))
  lines(1:budget, avgs+1.5*sds, col="red")
  lines(1:budget, avgs-1.5*sds, col="red")
  avgs = numeric(budget)
  sds = numeric(budget)
  for(i in 1:budget){
    vec = numeric(length(runs_data))
    for(j in 1:length(runs_data)){
      data = runs_data[[j]]
      vec[j]=data[i,latVecLength+1]
    }
    vec[vec<=-1]=-1
    avgs[i]=mean(vec)
    sds[i]=sd(vec)
  }
  plot(1:budget, avgs, main="mean fitness over time (only progress)", xlab="iteration", ylab="fitness", type="l")
  plot(1:budget, avgs, main="mean fitness over time (only progress) with sd", xlab="iteration", ylab="fitness", type="l", ylim=c(min(avgs-1.5*sds), max(avgs+1.5*sds)))
  lines(1:budget, avgs+1.5*sds, col="red")
  lines(1:budget, avgs-1.5*sds, col="red")
}
pdf(file="cmaResultPlots.pdf")
processRuns()
dev.off()