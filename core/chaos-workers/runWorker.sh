#!/bin/bash
set -euox pipefail

# verify whether kube was correctly configured
ls -la ~/.kube
cat ~/.kube/config

# DEBUG: PRINTS TOPOLOGY
zbctl status

# Registers worker - will run `handleJob.sh` for each new job
zbctl create worker chaos-experiments --handler handleJob.sh
