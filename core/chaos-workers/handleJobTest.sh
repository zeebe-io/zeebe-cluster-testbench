#!/usr/bin/env bats

# shellcheck source=handlerUtil.sh
. handlerUtil.sh

@test "read standard in" {
  result=$(echo "HELLO" | readStandardIn)
  [ "$result" == "HELLO" ]
}


variables="{
  \"authenticationDetails\":{
    \"audience\":\"33c00aed-cf34-4c8a-867d-161ee9c8943d.zeebe.ultrawombat.com\",
    \"authorizationURL\":\"https://login.cloud.ultrawombat.com/oauth/token\",
    \"clientId\":\"-M-bpgPX7bkW8ssgeuuQof5obhNQgr.O\",
    \"clientSecret\":\"~EfHvmjQFd4vIViilACpHSOz7IiJrMr~QgoNtDxlvhXbhlvkKut80.joW3On1zb4\",
    \"contactPoint\":\"33c00aed-cf34-4c8a-867d-161ee9c8943d.zeebe.ultrawombat.com:443\"
  },
\"clusterPlan\":\"Production - M\",
\"testResult\":\"PASSED\",
\"testParams\":{},
\"clusterId\":\"33c00aed-cf34-4c8a-867d-161ee9c8943d\"
}"


@test "extract cluster plan" {
  result=$(extractClusterPlan "$variables")
  echo "$result"
  [ "$result" == "production-m" ]
}

@test "extract client id" {
  result=$(extractClientId "$variables")
  echo "$result"
  [ "$result" == "-M-bpgPX7bkW8ssgeuuQof5obhNQgr.O" ]
}

@test "extract client secret" {
  result=$(extractClientSecret "$variables")
  echo "$result"
  [ "$result" == "~EfHvmjQFd4vIViilACpHSOz7IiJrMr~QgoNtDxlvhXbhlvkKut80.joW3On1zb4" ]
}

@test "extract authorization server url" {
  result=$(extractAuthorizationServerUrl "$variables")
  echo "$result"
  [ "$result" == "https://login.cloud.ultrawombat.com/oauth/token" ]
}

@test "extract zeebe address" {
  result=$(extractZeebeAddress "$variables")
  echo "$result"
  [ "$result" == "33c00aed-cf34-4c8a-867d-161ee9c8943d.zeebe.ultrawombat.com:443" ]
}


@test "extract target namespace" {
  result=$(extractTargetNamespace "$variables")
  echo "$result"
  [ "$result" == "33c00aed-cf34-4c8a-867d-161ee9c8943d-zeebe" ]
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
  array=(1 2)

  if runChaosExperiments failFunction "${array[@]}";
  then
    exit 1 # unexpected
  fi

  echo "expected"
}

@test "run experiment with failing runner and empty array - return PASSED" {
  array=(1 2)

  if runChaosExperiments failFunction "${array[@]}";
  then
    exit 1 # unexpected
  fi

  echo "expected"
}

@test "create failure message without args" {
  # given
  expected="$(jq -n '{testResult: "FAILED", failureMessages: [], failureCount: 1, metaData: {}}')"

  # when
  failureMsg=$(createFailureMessage)

  # then
  echo "$failureMsg"
  echo "expected: $expected"
  [ "$failureMsg" == "$expected" ]
}

@test "create failure message with number as one arg" {
  # given
  expected="$(jq -n '{testResult: "FAILED", failureMessages: ["2"], failureCount: 1, metaData: {}}')"

  # when
  failureMsg=$(createFailureMessage 2)

  # then
  echo "$failureMsg"
  echo "expected: $expected"
  [ "$failureMsg" == "$expected" ]
}

@test "create failure message with string as one arg" {
  # given
  expected="$(jq -n '{testResult: "FAILED", failureMessages: ["2"], failureCount: 1, metaData: {}}')"

  # when
  failureMsg=$(createFailureMessage "2")

  # then
  echo "$failureMsg"
  echo "expected: $expected"
  [ "$failureMsg" == "$expected" ]
}


@test "create failure message with multiple fail messages" {
  # given
  expected="$(jq -n '{testResult: "FAILED", failureMessages: ["2", "hallo"], failureCount: 1, metaData: {}}')"

  # when
  failureMsg=$(createFailureMessage 2 "hallo")

  # then 
  echo "actual: $failureMsg"
  echo "expected: $expected"
  [ "$failureMsg" == "$expected" ]
}

