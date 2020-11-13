#!/bin/bash
set -oxu pipefail

# import util methods
# shellcheck source=handlerUtil.sh
. handlerUtil.sh

# called by worker to execute chaos experiments
# variables can be read from standard in
logFile=$(generateLogFileName)
touch "$logFile"

################################################################################
# EXTRACT INPUT ################################################################
################################################################################

variables=$(readStandardIn)

clusterPlan=$(extractClusterPlan "$variables")

ZEEBE_CLIENT_ID=$(extractClientId "$variables")
export ZEEBE_CLIENT_ID

ZEEBE_CLIENT_SECRET=$(extractClientSecret "$variables")
export ZEEBE_CLIENT_SECRET

ZEEBE_AUTHORIZATION_SERVER_URL=$(extractAuthorizationServerUrl "$variables")
export ZEEBE_AUTHORIZATION_SERVER_URL

ZEEBE_ADDRESS=$(extractZeebeAddress "$variables")
export ZEEBE_ADDRESS

NAMESPACE=$(extractTargetNamespace "$variables")
export NAMESPACE

################################################################################
kubens "$NAMESPACE" &>> "$logFile" \
  || (createFailureMessage "Namespace '$NAMESPACE' doesn't exist" && exit 0)
kubectl get pods &>> "$logFile"

################################################################################
# RUN EXPERIMENTS ##############################################################
################################################################################
# We need to forward everything to standard error,
# otherwise job will be completed with the corresponding output

cd "zeebe-chaos/chaos-experiments/camunda-cloud/" || exit 1

# deploy workers which are needed in some of our chaos experiments
# Be aware that we are not delete them here, since if the experiments fails we might want to check
# the logs of the workers AND they are deleted if we delete the namespace anyway.
kubectl apply -f worker.yaml &>> "$logFile"

# add scripts to path
PATH="$PATH:$(pwd)/scripts/"
export PATH

# Get latest state of the repo
git pull origin master &>> "$logFile"


# We using a glob to get all experiments for a clusterplan, but if no files exist
# the glob is replaced with itself. To avoid that we use nullglob, which will replace it with null.
#
# Shell expansion:  https://tldp.org/LDP/Bash-Beginners-Guide/html/sect_03_04.html
# Bash loop over a list of (non-existing) files https://www.endpoint.com/blog/2016/12/12/bash-loop-wildcards-nullglob-failglob
# Shopts: https://www.gnu.org/software/bash/manual/html_node/The-Shopt-Builtin.html
#
# This makes the implementation easier, since we get an empty array as parameter, which means we skip the execution and mark the job as skipped. 
shopt -s nullglob
runChaosExperiments chaosRunner "$clusterPlan"/*/experiment.json
shopt -u nullglob
