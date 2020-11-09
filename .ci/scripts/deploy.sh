#!/bin/bash

set -ex

gcloud config set core/project zeebe-io
gcloud config set compute/region europe-west1
gcloud config set compute/zone europe-west1-b

set +x; echo ${SA_CREDENTIALS} > sa-credentials.json; set -x

gcloud auth activate-service-account jenkins-ci-cd@zeebe-io.iam.gserviceaccount.com --key-file=sa-credentials.json

rm sa-credentials.json 

gcloud container clusters get-credentials zeebe-cluster

# apply changes to testbench.yaml, if any
kubectl apply --namespace=testbench -f testbench.yaml

# apply changes to chaosWorker.yaml, if any
kubectl apply --namespace=testbench -f core/chaos-workers/chaosWorker.yaml

# trigger restart to load newest version of the image 
kubectl rollout restart deployment testbench --namespace=testbench
kubectl rollout restart deployment chaos-worker --namespace=testbench

# wait for pods getting started
kubectl wait --for=condition=Ready pod -l app=testbench --timeout=180s --namespace=testbench
kubectl wait --for=condition=Ready pod -l app=chaos-worker --timeout=180s --namespace=testbench
