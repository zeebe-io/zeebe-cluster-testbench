#!/bin/bash

# reads complete standard input
readStandardIn() {
  cat - 
}

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
  echo $(echo "$variables" |  jq -r '.clusterId')-zeebe
}
