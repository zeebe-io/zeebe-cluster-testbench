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
  # Input is expected to be an array of values.
  # Possible inputs are for example: ("Namespace not found" "Other Error")
  # In order to convert them to an json array we join them first together as one string, with comma as separator.
  # Result would be: "Namespace not found, Other Error". This makes it possible to use the jq split function,
  # which converts the string to a correct json array: [ "Namespace not found", "Other Error" ].
  # Previous we just split them on whitespaces, but this lead to problems on inputs with whitespaces.
  # Because they then have be converted to: [ "Namespace", "not", "found", "Other", "Error" ].
  args=( "$@" ) # get arguments as array
  printf -v joined '%s,' "${args[@]}"

  result=FAILED
  # generate json result
  jq -n \
     --arg result "$result" \
     --arg failures "${joined%,}" \
     '{testResult: $result, testReport: {testResult: $result, failureMessages: $failures | split(","), failureCount: 1, metaData: {}}}'
}

createSuccessMessage() {
  # Input is expected to be an array of values.
  # Possible inputs are for example: ("Namespace not found" "Other Error")
  # In order to convert them to an json array we join them first together as one string, with comma as separator.
  # Result would be: "Namespace not found, Other Error". This makes it possible to use the jq split function,
  # which converts the string to a correct json array: [ "Namespace not found", "Other Error" ].
  # Previous we just split them on whitespaces, but this lead to problems on inputs with whitespaces.
  # Because they then have be converted to: [ "Namespace", "not", "found", "Other", "Error" ].
  args=( "$@" ) # get arguments as array
  printf -v joined '%s,' "${args[@]}"

  result=PASSED
  # generate json result
  jq -n \
     --arg result "$result" \
     --arg results "${joined%,}" \
     '{testResult: $result, testReport: { testResult: $result, failureMessages: [], failureCount: 0, metaData: { results: $results | split(",") } } }'
}


createSkippedMessage() {
  result=SKIPPED
  # generate json result
  jq -n \
     --arg result "$result" \
     '{testResult: $result, testReport: { testResult: $result, failureMessages: [], failureCount: 0, metaData: { results: [ "Skipped test. There were no experiments to run" ] } } }'
}

################################################################################
# RUN EXPERIMENTS ##############################################################
################################################################################

generateLogFileName() {
  echo "output-$(date +%Y%m%d).log"
}

chaosRunner() {
  if [ -f "$1" ]
  then
    chaos run "$1"
  fi
}

runChaosExperiments() {
  # The runner function which will be executed; useful for testing
  runner=$1

  # expects as second argument an array of experiments to run
  # uses $@ to get all arguments and creates a sublist from index 2
  #
  # 0 is the name of the function
  # 1 is the runner function
  # 2+ are the names of the experiments to run
  experiments=( "${@:2}" )

  metadata=()

  # run all experiments for cluster plan
  for experiment in "${experiments[@]}"
  do
    if ! $runner "$experiment" &>> "$(generateLogFileName)";
    then
      resultMsg=$(createFailureMessage "$experiment failed")
      echo "$resultMsg"
      return 0 # if we return an error code the job worker would fail the job
    else
      metadata+=( "$experiment run successfully" )
    fi
  done

  # standard output will be consumed by worker to complete job
  if [ ${#metadata[@]} -eq 0 ] # if metadata is empty we haven't executed any experiments
  then
    resultMsg=$(createSkippedMessage)
    echo "$resultMsg"
  else
    resultMsg=$(createSuccessMessage "${metadata[@]}")
    echo "$resultMsg"
  fi

}
