#!/bin/bash

set -eo pipefail

# this script is a little help when analyzing the logfiles of the chaos tests
# it looks for the chaos worker pod in the current workspace
# it then downloads the log and splits the log into individual runs
# it then checks each run for a failure
# all failures will appear as *.log-chunk_failed files in the current folder

chaosWorkerPod="$(kubectl get pods -o json -l app=chaos-worker | jq -r '.items[].metadata.name')"

kubectl cp "$chaosWorkerPod":/home/chaos/data/chaostoolkit.log chaostoolkit.log

awk '/#{10,}/{filename=NR".log-chunk"}; {print > filename}' chaostoolkit.log

failedChunks=$(grep -l "failing this experiment" -- *.log-chunk)

for chunk in $failedChunks
do
  mv "$chunk" "$chunk-failed"
done

rm ./*.log-chunk

rm chaostoolkit.log
