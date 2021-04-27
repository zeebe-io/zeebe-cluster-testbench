#!/bin/bash

# This script demonstrates the use of the external-tool-integration process.
# It checks periodically whether any jobs for a certain business key are available.
# If there are none, it will wait more time. If there are, it reads out the variables
# and completes the job

if [ -z "$1" ]
then
  echo "Please provide as first argument the business Key of the process to wait for"
  exit 1
fi

businessKey=$1

waiting=1

echo "Waiting for result of $businessKey"

while [ $waiting -eq 1 ]; do

    zbctl activate jobs "$businessKey" --insecure > activationresponse.txt 2>error.txt

    key=$(jq -r '.jobs[0].key' < activationresponse.txt)

    if [ -z "$key" ]; then
        echo "Still waiting"
        sleep 5m
    else
        waiting=0
    fi

done

echo "Job Completed"
zbctl complete job "$key" --insecure

# example: extract aggrgated test result
key=$(jq -r '.jobs[0].key' < activationresponse.txt)

echo "Job key is: $key"

variables=$(jq -r '.jobs[0].variables' < activationresponse.txt)

echo "Job variables are: $variables"

testResult=$(echo "$variables" | jq -r '.aggregatedTestResult')

echo "Test result is: $testResult"

if [ "$testResult" == "FAILED" ]; then
  echo "Test failed"
  exit 1
else
  echo "Test passed or skipped"
  exit 0
fi
