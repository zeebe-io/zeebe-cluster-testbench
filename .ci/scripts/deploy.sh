#!/bin/bash

set -ex

if [ -z "$1" ]
then
  echo "Please provide the tag 'dev' or 'latest'"
  exit 1
fi


tag=$1

if [ "${tag}" = "latest" ]; then
  echo "Deploying :latest to 'int' stage / 'testbench' namespace"  
  suffix=""
elif [ "${tag}" = "dev" ]; then
  echo "Deploying :dev to 'dev' stage / 'testbench-dev' namespace"  
  suffix="-dev"
else 
  echo "Unknown tag '${tag}'. Please provide the tag 'dev' or 'latest'"
  exit 1
fi  

echo "suffix: ${suffix}"

namespace="testbench${suffix}"
echo "target namespace: ${namespace}"

gcloud config set core/project zeebe-io
gcloud config set compute/region europe-west1
gcloud config set compute/zone europe-west1-b

set +x; echo "${SA_CREDENTIALS}" > sa-credentials.json; set -x

gcloud auth activate-service-account jenkins-ci-cd@zeebe-io.iam.gserviceaccount.com --key-file=sa-credentials.json

rm sa-credentials.json 

gcloud container clusters get-credentials zeebe-cluster

# apply changes to testbench.yaml, if any
kubectl apply --namespace="${namespace}" -f "testbench${suffix}.yaml"

# apply changes to chaosWorker.yaml, if any
kubectl apply --namespace="${namespace}" -f "core/chaos-workers/chaosWorker${suffix}.yaml"

# trigger restart to load newest version of the image 
kubectl rollout restart deployment testbench --namespace="${namespace}"
kubectl rollout restart deployment chaos-worker --namespace="${namespace}"

# wait for pods getting started
kubectl wait --for=condition=Ready pod -l app=testbench --timeout=180s --namespace="${namespace}"
kubectl wait --for=condition=Ready pod -l app=chaos-worker --timeout=180s --namespace="${namespace}"
