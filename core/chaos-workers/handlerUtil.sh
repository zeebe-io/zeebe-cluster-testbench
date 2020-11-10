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
# OUTPUT #######################################################################
################################################################################

# EXAMPLE OUTPUT used in java client
#{
#	"testResult": "FAILED",
#	"startTime": 1604999828531,
#	"endTime": 1604999899482,
#	"timeOfFirstFailure": 1604999840540,
#	"failureCount": 2,
#	"failureMessages": ["Iteration 0 exceeded maximum time of PT10S; elapsedTime: PT12.008S; metaData:{workflowInstanceKey=9007199254741170}", "Iteration 7 exceeded maximum time of PT10S; elapsedTime: PT10.003S; metaData:{workflowInstanceKey=15762598695796879}"],
#	"metaData": {
#		"testParams": {
#			"steps": 3,
#			"iterations": 10,
#			"maxTimeForIteration": "PT10S",
#			"maxTimeForCompleteTest": "PT2M"
#		}
#	},
#	"duration": 70951
#}

createFailureMessage() {
  result=FAILED
  args=( "$@" ) # get arguments as array

  # generate json result
  jq -n \
     --arg result "$result" \
     --arg failures "${args[*]}" \
     '{testResult: $result, failureMessages: $failures | split(" "), failureCount: 1, metaData: {}}'
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
      resultMsg=$(createFailureMessage "$experiment failed")
      echo "$resultMsg"
      return 1
    fi
  done

  # standard output will be consumed by worker to complete job
  echo "{\"testResult\":\"PASSED\"}"
}
