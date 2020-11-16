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
  failureMsg="$1"
  startTime="$2"
  endTime="$3"
  failTime="$4"
  # Input is expected to be an array of values.
  # Possible inputs are for example: ("Namespace not found" "Other Error")
  # In order to convert them to an json array we join them first together as one string, with comma as separator.
  # Result would be: "Namespace not found, Other Error". This makes it possible to use the jq split function,
  # which converts the string to a correct json array: [ "Namespace not found", "Other Error" ].
  # Previous we just split them on whitespaces, but this lead to problems on inputs with whitespaces.
  # Because they then have be converted to: [ "Namespace", "not", "found", "Other", "Error" ].
  args=( "${@:5}" ) # get arguments as array
  printf -v joined '%s,' "${args[@]}"

  result=FAILED
  # generate json result
  jq -n \
     --arg result "$result" \
     --arg failures "$failureMsg" \
     --arg startTime "$startTime" \
     --arg endTime "$endTime" \
     --arg failTime "$failTime" \
     --arg meta "${joined%,}" \
     '{testResult: $result,
       testReport: {
         testResult: $result,
         failureMessages: $failures | split(","),
         failureCount: 1,
         metaData: {
             results: $meta | split(",")
         },
           startTime: $startTime | tonumber,
           endTime: $endTime | tonumber,
           timeOfFirstFailure: $failTime | tonumber
       }
      }'
}

createSuccessMessage() {
  startTime="$1"
  endTime="$2"
  # Input is expected to be an array of values.
  # Possible inputs are for example: ("Namespace not found" "Other Error")
  # In order to convert them to an json array we join them first together as one string, with comma as separator.
  # Result would be: "Namespace not found, Other Error". This makes it possible to use the jq split function,
  # which converts the string to a correct json array: [ "Namespace not found", "Other Error" ].
  # Previous we just split them on whitespaces, but this lead to problems on inputs with whitespaces.
  # Because they then have be converted to: [ "Namespace", "not", "found", "Other", "Error" ].
  args=( "${@:3}" ) # get arguments as array
  printf -v joined '%s,' "${args[@]}"

  result=PASSED
  # generate json result
  jq -n \
     --arg result "$result" \
     --arg startTime "$startTime" \
     --arg endTime "$endTime" \
     --arg results "${joined%,}" \
     '{testResult: $result,
         testReport: {
           testResult: $result,
           failureMessages: [],
           failureCount: 0,
           metaData: {
             results: $results | split(",")
           },
           startTime: $startTime | tonumber,
           endTime: $endTime | tonumber,
           timeOfFirstFailure: 0
         }
      }'
}


createSkippedMessage() {
  result=SKIPPED
  time="$1"

  # generate json result
  jq -n \
     --arg result "$result" \
     --arg time "$time" \
     '{
       testResult: $result,
       testReport: {
         testResult: $result,
         failureMessages: [],
         failureCount: 0,
         metaData: {
           results: [ "Skipped test. There were no experiments to run" ]
         },
         startTime: $time | tonumber,
         endTime: $time | tonumber,
         timeOfFirstFailure: 0
       }
      }'
}

# https://serverfault.com/questions/151109/how-do-i-get-the-current-unix-time-in-milliseconds-in-bash
nowMs() {
  # %s gives the seconds since begin of epoch
  # %N gives nanoseconds since begin of epoch
  # 3 before N trims it to 3 most significant digits (which are ms)
  date +%s%3N
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

  if [ ${#experiments[@]} -eq 0 ] # if experiments is empty we skip execution
  then
    time=$(nowMs)
    resultMsg=$(createSkippedMessage "$time")
    echo "$resultMsg"
    return 0
  fi

  metadata=()
  startTime=$(nowMs)
  firstFailedTime=0
  result="PASSED"

  # run all experiments for cluster plan
  for experiment in "${experiments[@]}"
  do
    if ! $runner "$experiment" &>> "$(generateLogFileName)";
    then
      result="$experiment failed"
      firstFailedTime=$(nowMs)
      break
    else
      metadata+=( "$experiment run successfully" )
    fi
  done

  endTime=$(nowMs)
  resultMsg=""

  if [ "$result" == "PASSED" ]
  then
    resultMsg=$(createSuccessMessage "$startTime" "$endTime" "${metadata[@]}")
  else
    resultMsg=$(createFailureMessage "$result" "$startTime" "$endTime" "$firstFailedTime" "${metadata[@]}")
  fi

  # standard output will be consumed by worker to complete job
  echo "$resultMsg"
}
