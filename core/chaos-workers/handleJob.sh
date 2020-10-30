#!/bin/bash
set -oxu pipefail

# called by worker to execute chaos experiments
# variables can be read from standard in
logFile="output-$(date +%Y%m%d).log"
touch "$logFile"

################################################################################
# EXTRACT INPUT ################################################################
################################################################################

variables=$(cat -) # reads complete std in
# read -r variables reads only one line

clusterPlan=$(echo "$variables" | jq -r '.clusterPlan' | tr '[:upper:]' '[:lower:]')

ZEEBE_CLIENT_ID=$(echo "$variables" | jq -r '.authenticationDetails.clientId')
export ZEEBE_CLIENT_ID

ZEEBE_CLIENT_SECRET=$(echo "$variables" | jq -r '.authenticationDetails.clientSecret')
export ZEEBE_CLIENT_SECRET

ZEEBE_AUTHORIZATION_SERVER_URL=$(echo "$variables" | jq -r '.authenticationDetails.authorizationURL')
export ZEEBE_AUTHORIZATION_SERVER_URL

ZEEBE_ADDRESS=$(echo "$variables" | jq -r '.authenticationDetails.contactPoint')
export ZEEBE_ADDRESS

NAMESPACE=$(echo "$variables" | jq -r '.authenticationDetails.audience' | cut -d'.' -f 1)-zeebe
export NAMESPACE

################################################################################

kubens $NAMESPACE &>> "$logFile" || (echo "{\"testResult\":\"FAILED\"}" && exit 1)
kubectl get pods &>> "$logFile"

################################################################################
# RUN EXPERIMENTS ##############################################################
################################################################################
# We need to forward everything to standard error,
# otherwise job will be completed with the corresponding output

cd "zeebe-chaos/chaos-experiments/camunda-cloud/" || exit 1

# Get latest state of the repo
git pull origin master &>> "$logFile"

# add scripts to path
PATH="$PATH:$(pwd)/scripts/"
export PATH

# run all experiments for cluster plan
for experiment in "$clusterPlan"/*/experiment.json
do
  chaos run "$experiment" &>> "$logFile"
done

################################################################################
# OUTPUT #######################################################################
################################################################################
# standard output will be consumed by worker to complete job

echo "{\"testResult\": \"PASSED\"}"