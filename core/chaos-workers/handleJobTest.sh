#!/usr/bin/env bats

. ioHandlerUtil.sh

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


