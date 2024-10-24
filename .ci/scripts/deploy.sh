#!/bin/bash

set -ex

if [ -z "$1" ]
then
  echo "Please provide the tag 'dev' or 'latest'"
  exit 1
fi

tag=$1
# shellcheck disable=SC2034
workerVersion=1.3.2

if [[ ${tag} == *-dev ]]; then
  echo "Deploying :dev to 'dev' stage / using '-dev' suffixed files"
  suffix="-dev"
elif [[ ${tag} == *-prod ]]; then
  echo "Deploying :prod to 'prod' stage / using '-prod' suffixed files"
  suffix="-prod"
else
  echo "Unknown tag '${tag}'. Please provide a tag matching '*-dev' or '*-prod'"
  exit 1
fi
echo "suffix: ${suffix}"

# replace dots with dashes for dns compliance
namespace="testbench-${tag//\./-}"
echo "target namespace: ${namespace}"

if [ "${DRY_RUN:-false}" == "true" ]; then
  exit 0
fi

# deploy secrets (create new or overwrite)
kubectl create secret generic testbench-secrets --namespace="${namespace}" \
  --from-literal=contactPoint="${CONTACT_POINT}" \
  --from-literal=clientSecret="${CLIENT_SECRET}" \
  --from-literal=cloudClientSecret="${CLOUD_CLIENT_SECRET}" \
  --from-literal=internalCloudClientSecret="${INTERNAL_CLOUD_CLIENT_SECRET}" \
  --from-literal=internalCloudPassword="${INTERNAL_CLOUD_PASSWORD}" \
  --from-literal=slackWebhookUrl="${SLACK_WEBHOOK_URL}" \
  --from-literal=testbenchRestAddress="${TESTBENCH_REST_ADDRESS}" \
  --save-config -o yaml --dry-run=client | kubectl apply -f -

# apply changes to testbench.yaml, if any
kubectl apply --namespace="${namespace}" -f "testbench${suffix}.yaml"

# trigger restart to load newest version of the image
kubectl rollout restart deployment testbench --namespace="${namespace}"

# wait for rollouts to complete
kubectl rollout status deployment testbench --timeout=180s --namespace="${namespace}"
