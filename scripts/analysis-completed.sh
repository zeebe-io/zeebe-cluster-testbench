#!/bin/bash

set -euox pipefail


source credentials

zbctl publish message "Analysis Completed" --correlationKey "$1"
