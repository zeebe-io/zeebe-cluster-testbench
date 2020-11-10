#!/bin/bash
set -euox pipefail

# DEBUG: PRINTS TOPOLOGY
zbctl status

# Registers worker - will run `handleJob.sh` for each new job
zbctl create worker chaos-experiments --handler handleJob.sh
