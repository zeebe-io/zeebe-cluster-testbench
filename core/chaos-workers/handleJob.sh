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

chaosRunner() {
  chaos run "$1"
}

runChaosExperiments chaosRunner "$clusterPlan"/*/experiment.json
