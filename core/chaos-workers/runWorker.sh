#!/bin/bash
set -euox pipefail

# DEBUG: PRINTS TOPOLOGY
zbctl status

# Registers worker - will run `handleJob.sh` for each new job


zbctl --insecure create worker chaos-experiments \
      --concurrency 1 \
      --maxJobsActive 1 \
      --timeout $(( 1 * 60 * 60  ))s \
      --handler handleJob.sh
