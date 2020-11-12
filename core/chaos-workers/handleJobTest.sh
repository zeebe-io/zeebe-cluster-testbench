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

@test "run experiment with noop runner and empty arry - return PASSED" {
  array=()
  result=$(runChaosExperiments noop "${array[@]}")
  echo "$result"
  [ "$result" == "{\"testResult\":\"PASSED\"}" ]
}

@test "run experiment with noop runner and values in array - return PASSED" {
  array=(1 2)
  result=$(runChaosExperiments noop "${array[@]}")
  echo "$result"
  [ "$result" == "{\"testResult\":\"PASSED\"}" ]
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
  expected="$(jq -nc '{testResult: "PASSED"}')"

  # when
  result=$(runChaosExperiments failFunction "${array[@]}")

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

