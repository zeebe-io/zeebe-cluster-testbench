#!/bin/bash -xeu

cd target

export PROJECT_NAME="Zeebe Cluster Testbench"
export GITHUB_TOKEN=${GITHUB_TOKEN_PSW}
export GITHUB_ORG=zeebe-io
export GITHUB_REPO=zeebe-cluster-testbench

# do github release - mainly for assigning the tag
curl -sL https://github.com/aktau/github-release/releases/download/v0.7.2/linux-amd64-github-release.tar.bz2 | tar xjvf - --strip 3

description='## Changes\n[summarize changes]\n\nRead more about the [release process](https://github.com/zeebe-io/zeebe-cluster-testbench/tree/master/docs/release-process.md).'

./github-release release --user "${GITHUB_ORG}" --repo "${GITHUB_REPO}" --tag "${RELEASE_VERSION}" --draft --name "${PROJECT_NAME} ${RELEASE_VERSION}" --description "${description}"

