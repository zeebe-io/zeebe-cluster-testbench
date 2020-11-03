#!/bin/bash
set -euox pipefail

# verify whether kube was correctly configured
ls -la ~/.kube
cat ~/.kube/config

# DEBUG: PRINTS TOPOLOGY
zbctl status

# DEBUG: DEPLOY WORKFLOW
# scriptPath=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
# zbctl deploy "$scriptPath/../../workflows/chaos-test.bpmn"

# DEBUG: CREATE NEW WORKFLOW INSTANCE
# zbctl create instance chaos-test --variables "{
#  \"authenticationDetails\":{\"audience\":\"33c00aed-cf34-4c8a-867d-161ee9c8943d.zeebe.ultrawombat.com\",\"authorizationURL\":\"https://login.cloud.ultrawombat.com/oauth/token\",\"clientId\":\"-M-bpgPX7bkW8ssgeuuQof5obhNQgr.O\",\"clientSecret\":\"~EfHvmjQFd4vIViilACpHSOz7IiJrMr~QgoNtDxlvhXbhlvkKut80.joW3On1zb4\",\"contactPoint\":\"33c00aed-cf34-4c8a-867d-161ee9c8943d.zeebe.ultrawombat.com:443\"},
#\"testReport\":{\"testResult\":\"PASSED\",\"startTime\":1603438963772,\"endTime\":1603438989088,\"timeOfFirstFailure\":0,\"failureCount\":0,\"failureMessages\":[],\"metaData\":{\"testParams\":{\"steps\":3,\"iterations\":10,\"maxTimeForIteration\":\"PT10S\",\"maxTimeForCompleteTest\":\"PT2M\"}},\"duration\":25316},
#\"clusterPlan\":\"Production-M\",
#\"testResult\":\"PASSED\",
#\"testParams\":{\"maxTimeForCompleteTest\":\"PT2M\",\"maxTimeForIteration\":\"PT10S\",\"iterations\":10,\"steps\":3}
#}"

# Registers worker - will run `handleJob.sh` for each new job
zbctl create worker chaos-experiments --handler handleJob.sh