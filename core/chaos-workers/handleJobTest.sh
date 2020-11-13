#!/usr/bin/env bats

# shellcheck source=handlerUtil.sh
. handlerUtil.sh

@test "read standard in" {
  result=$(echo "HELLO" | readStandardIn)
  [ "$result" == "HELLO" ]
}

ZEEBE_ADDRESS='address.zeebe.ultrawombat.com:443'
ZEEBE_CLIENT_ID='ID_CLIENT'
ZEEBE_CLIENT_SECRET='SECRET_CLIENT'
ZEEBE_AUTHORIZATION_SERVER_URL='https://login.cloud.ultrawombat.com/oauth/token'
variables=$(jq -n \
             --arg clientId "$ZEEBE_CLIENT_ID" \
             --arg clientSecret "$ZEEBE_CLIENT_SECRET" \
             --arg contactPoint "$ZEEBE_ADDRESS" \
             --arg authorizationURL "$ZEEBE_AUTHORIZATION_SERVER_URL" \
             --arg clusterId "${ZEEBE_ADDRESS%.zeebe.*}" \
             '{authenticationDetails: {authorizationURL: $authorizationURL, contactPoint: $contactPoint, clientId: $clientId, clientSecret: $clientSecret}, clusterPlan: "Production - M", clusterId: $clusterId}')

@test "extract cluster plan" {
  result=$(extractClusterPlan "$variables")
  echo "$result"
  [ "$result" == "production-m" ]
}

@test "extract client id" {
  result=$(extractClientId "$variables")
  echo "$result"
  [ "$result" == "ID_CLIENT" ]
}

@test "extract client secret" {
  result=$(extractClientSecret "$variables")
  echo "$result"
  [ "$result" == "SECRET_CLIENT" ]
}

@test "extract authorization server url" {
  result=$(extractAuthorizationServerUrl "$variables")
  echo "$result"
  [ "$result" == "https://login.cloud.ultrawombat.com/oauth/token" ]
}

@test "extract zeebe address" {
  result=$(extractZeebeAddress "$variables")
  echo "$result"
  [ "$result" == "address.zeebe.ultrawombat.com:443" ]
}

@test "extract target namespace" {
  result=$(extractTargetNamespace "$variables")
  echo "$result"
  [ "$result" == "address-zeebe" ]
}

noop() { :;}

@test "run experiment with noop runner and empty array - return SKIPPED" {
  # given
  array=()
  expected="$(jq -n '{testResult: "SKIPPED", testReport: { testResult: "SKIPPED", failureMessages: [], failureCount: 0, metaData: {results: [ "Skipped test. There were no experiments to run" ]}}}')"

  # when
  result=$(runChaosExperiments noop "${array[@]}")

  # then
  echo "actual: $result"
  echo "expected: $expected"
  [ "$result" == "$expected" ]
}

@test "run experiment with noop runner and values in array - return PASSED" {
  # given
  array=(1 2)
  expected="$(jq -n '{testResult: "PASSED", testReport: { testResult: "PASSED", failureMessages: [], failureCount: 0, metaData: {results: [ "1 run successfully", "2 run successfully" ]}}}')"

  # when
  result=$(runChaosExperiments noop "${array[@]}")

  # then
  echo "actual: $result"
  echo "expected: $expected"
  [ "$result" == "$expected" ]
}

failFunction() {
  return 1
}

@test "run experiment with failing runner and values in array - return FAILED" {
  # given
  array=(1 2)
  expected="$(jq -n '{testResult: "FAILED", testReport: {testResult: "FAILED", failureMessages: ["1 failed"], failureCount: 1, metaData: {}}}')"

  # when
  result=$(runChaosExperiments failFunction "${array[@]}")

  # then
  echo "actual: $result"
  echo "expected: $expected"
  [ "$result" == "$expected" ]
}

@test "run experiment with failing runner and empty array - return PASSED" {
  # given
  array=()
  expected="$(jq -n '{testResult: "SKIPPED", testReport: { testResult: "SKIPPED", failureMessages: [], failureCount: 0, metaData: {results: [ "Skipped test. There were no experiments to run" ]}}}')"

  # when
  result=$(runChaosExperiments failFunction "${array[@]}")

  # then
  echo "actual: $result"
  echo "expected: $expected"
  [ "$result" == "$expected" ]
}

@test "run experiments should skip if glob is null" {
  # given
  array=()
  expected="$(jq -n '{testResult: "SKIPPED", testReport: { testResult: "SKIPPED", failureMessages: [], failureCount: 0, metaData: {results: [ "Skipped test. There were no experiments to run" ]}}}')"

  # when
  shopt -s nullglob
  result=$(runChaosExperiments chaosRunner /tmp/*/files)
  shopt -u nullglob

  # then
  echo "actual: $result"
  echo "expected: $expected"
  [ "$result" == "$expected" ]
}

@test "chaos runner should not fail if file not exist" {
  # given
  array=()
  expected="$(jq -n '{testResult: "PASSED", testReport: { testResult: "PASSED", failureMessages: [], failureCount: 0, metaData: {results: [ "/tmp/*/files run successfully" ]}}}')"

  # when
  result=$(runChaosExperiments chaosRunner "/tmp/*/files")

  # then
  echo "actual: $result"
  echo "expected: $expected"
  [ "$result" == "$expected" ]
}

@test "create failure message without args" {
  # given
  expected="$(jq -n '{testResult: "FAILED", testReport: {testResult: "FAILED", failureMessages: [], failureCount: 1, metaData: {}}}')"

  # when
  failureMsg=$(createFailureMessage)

  # then
  echo "$failureMsg"
  echo "expected: $expected"
  [ "$failureMsg" == "$expected" ]
}

@test "create failure message with number as one arg" {
  # given
  expected="$(jq -n '{testResult: "FAILED", testReport: {testResult: "FAILED", failureMessages: ["2"], failureCount: 1, metaData: {}}}')"

  # when
  failureMsg=$(createFailureMessage 2)

  # then
  echo "$failureMsg"
  echo "expected: $expected"
  [ "$failureMsg" == "$expected" ]
}

@test "create failure message with string as one arg" {
  # given
  expected="$(jq -n '{testResult: "FAILED", testReport: {testResult: "FAILED", failureMessages: ["2"], failureCount: 1, metaData: {}}}')"

  # when
  failureMsg=$(createFailureMessage "2")

  # then
  echo "$failureMsg"
  echo "expected: $expected"
  [ "$failureMsg" == "$expected" ]
}

@test "create failure message with multiple fail messages" {
  # given
  expected="$(jq -n '{testResult: "FAILED", testReport: {testResult: "FAILED", failureMessages: ["2", "hallo"], failureCount: 1, metaData: {}}}')"

  # when
  failureMsg=$(createFailureMessage 2 "hallo")

  # then 
  echo "actual: $failureMsg"
  echo "expected: $expected"
  [ "$failureMsg" == "$expected" ]
}


@test "create success message with multiple result messages" {
  # given
  expected="$(jq -n '{ testResult: "PASSED", testReport: { testResult: "PASSED", failureMessages: [], failureCount: 0, metaData: {results: ["Run 1", "Run 2"]}}}')"

  # when
  failureMsg=$(createSuccessMessage "Run 1" "Run 2")

  # then
  echo "actual: $failureMsg"
  echo "expected: $expected"
  [ "$failureMsg" == "$expected" ]
}
