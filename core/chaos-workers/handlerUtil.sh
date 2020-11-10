#!/bin/bash
# This file contains utility functions


# reads complete standard input
readStandardIn() {
  cat - 
}

################################################################################
# EXTRACT INPUT ################################################################
################################################################################

extractClusterPlan() {
  variables=$1
  echo "$variables" | jq -r '.clusterPlan' | tr '[:upper:]' '[:lower:]' | sed -e 's/[[:space:]]//g'
}

extractClientId() {
  variables=$1
  echo "$variables" | jq -r '.authenticationDetails.clientId'
}


extractClientSecret() {
  variables=$1
  echo "$variables" | jq -r '.authenticationDetails.clientSecret'
}


extractAuthorizationServerUrl() {
  variables=$1
  echo "$variables" | jq -r '.authenticationDetails.authorizationURL'
}


extractZeebeAddress() {
  variables=$1
  echo "$variables" | jq -r '.authenticationDetails.contactPoint'
}

extractTargetNamespace() {
  variables=$1
  echo "$(echo "$variables" |  jq -r '.clusterId')-zeebe"
}


################################################################################
# RUN EXPERIMENTS ##############################################################
################################################################################

generateLogFileName() {
  echo "output-$(date +%Y%m%d).log"
}

runChaosExperiments() {
  runner=$1
  # run all experiments for cluster plan
  for experiment in $2
  do
    if ! $runner "$experiment" &>> "$(generateLogFileName)";
    then
      echo "{\"testResult\":\"FAILED\"}"
      return 1
    fi
  done

  # standard output will be consumed by worker to complete job
  echo "{\"testResult\":\"PASSED\"}"
}

