#!/bin/bash

set -eo pipefail

if [ -z "$1" ]
then
  echo "Please provide as first argument the cluster id for which you finished the analysis (without '-zeebe').
Example execution $0 'cluster-id'"
 exit 1
fi

source credentials

zbctl publish message "Analysis Completed" --correlationKey "$1"
