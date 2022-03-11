#!/bin/bash

set -ex

if [ -z "$1" ]
then
  echo "Please provide the tag 'dev' or 'latest'"
  exit 1
fi

tag=$1
workerVersion=1.2.6

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

gcloud config set core/project zeebe-io
gcloud config set compute/region europe-west1
gcloud config set compute/zone europe-west1-b

set +x; echo "${SA_CREDENTIALS}" > sa-credentials.json; set -x

gcloud auth activate-service-account jenkins-ci-cd@zeebe-io.iam.gserviceaccount.com --key-file=sa-credentials.json

rm sa-credentials.json

gcloud container clusters get-credentials zeebe-cluster

# deploy secrets (create new or overwrite)
kubectl create secret generic testbench-secrets --namespace="${namespace}" \
  --from-literal=contactPoint="${CONTACT_POINT}" \
  --from-literal=clientSecret="${CLIENT_SECRET}" \
  --from-literal=cloudClientSecret="${CLOUD_CLIENT_SECRET}" \
  --from-literal=internalCloudClientSecret="${INTERNAL_CLOUD_CLIENT_SECRET}" \
  --from-literal=internalCloudPassword="${INTERNAL_CLOUD_PASSWORD}" \
  --from-literal=slackWebhookUrl="${SLACK_WEBHOOK_URL}" \
  --save-config -o yaml --dry-run=client | kubectl apply -f -

# apply changes to testbench.yaml, if any
kubectl apply --namespace="${namespace}" -f "testbench${suffix}.yaml"

# apply changes to chaosWorker.yaml, if any
sed -i "s/WORKER_VERSION/$workerVersion/g" core/chaos-workers/deployment/*.yaml
kubectl apply --namespace="${namespace}" -f "core/chaos-workers/deployment/chaosWorker${suffix}.yaml"
kubectl apply --namespace="${namespace}" -f "core/chaos-workers/deployment/chaos-data-claim.yaml"

# trigger restart to load newest version of the image
kubectl rollout restart deployment testbench --namespace="${namespace}"
kubectl rollout restart deployment chaos-worker --namespace="${namespace}"

# wait for rollouts to complete
kubectl rollout status deployment testbench --timeout=180s --namespace="${namespace}"
kubectl rollout status deployment chaos-worker --timeout=180s --namespace="${namespace}"
