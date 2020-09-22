#!/bin/bash

set -ex

apt install kubectl

kubectl version --client
