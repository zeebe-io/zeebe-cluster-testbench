# Service Task Reference

| Service Task                                      | ID / Job Type                                                                                                 | Input                                                                                                                      | Output                                                                                                             | Header                                                           |
| ------------------------------------------------- | ------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------- |
| Map names to UUIDs                                | `map-names-to-uuids` / `map-names-to-uuids-job`                                                               | `channel`, `clusterPlan`, `region`, `generation`, `channelUUID`, `clusterPlanUUID`, `regionUUID`, `generationUUID`         | `channel`, `clusterPlan`, `region`, `generation`, `channelUUID`, `clusterPlanUUID`, `regionUUID`, `generationUUID` |                                                                  |
| Create Zeebe Cluster in Camunda Cloud             | `creae-zeebe-cluster-in-camunda-cloud` / `create-zeebe-cluster-in-camunda-cloud-job`                          | `channelUUID`, `clusterPlanUUID`, `regionUUID`, `generationUUID`                                                           | `clusterId`, `clusterName`, `authenticationDetails`                                                                |                                                                  |
| Query Zeebe Cluster State in Camunda Cloud        | `query-zeebe-cluster-state-in-camunda-cloud` / `query-zeebe-cluster-state-in-camunda-cloud-job`               | `clusterId`, `clusterName`                                                                                                 | `clusterStatus`                                                                                                    |                                                                  |
| Gather Information about Cluster in Camunda Cloud | `gather-information-about-cluster-in-camunda-cloud` / `gather-information-about-cluster-in-camunda-cloud-job` | `clusterId`, `clusterName`                                                                                                 | `operateURL`                                                                                                       |                                                                  |
| Warm Up Cluster                                   | `warm-up-cluster` / `warm-up-cluster-job`                                                                     | `authenticationDetails`                                                                                                    |                                                                                                                    |                                                                  |
| Run Sequential Test                               | `run-sequential-test` / `run-sequential-test-job`                                                             | `authenticationDetails`, `testParams`                                                                                      | `testResult`, `testReport`                                                                                         |                                                                  |
| Record Test Result                                | `record-test-result` / `record-test-result-job`                                                               | `channel`, `clusterPlan`, `region`, `generation`, `clusterId`, `clusterName`, `operateURL`, `testReport`, `testWorkflowId` |                                                                                                                    |                                                                  |
| Notify Engineers                                  | `notify-engineers` / `notify-engineers-job`                                                                   | `generation`, `clusterPlan`, `clusterName`, `operateURL`, `testReport`                                                     |                                                                                                                    |                                                                  |
| Destroy Zeebe Cluster in Camunda CLoud            | `destroy-zeebe-cluster-in-camunda-cloud` / `destroy-zeebe-cluster-in-camunda-cloud-job`                       | `clusterId`                                                                                                                |                                                                                                                    |                                                                  |
| Create Generation in Camunda Cloud                | `create-generation-in-camunda-cloud` / `create-generation-in-camunda-cloud-job`                               | `zeebeImage`, `generationTemplate`, `channel`                                                                              | `generation`, `generationUUID`                                                                                     |                                                                  |
| Delete Generation in Camunda Cloud                | `delete-generation-in-camunda-cloud` / `delete-generation-in-camunda-cloud-job`                               | `generationUUID`                                                                                                           |                                                                                                                    |                                                                  |
| Run Chaos Experiments                             | `run-chaos-experiments` / `chaos-experiments`                                                                 | `authenticationDetails`, `clusterPlan`                                                                                     | `testResult`, `testReport`                                                                                         |                                                                  |
| Aggregate Test Results                            | `aggregate-test-results` / `aggregate-test-results-job`                                                       | (defined in header field)                                                                                                  | `aggregatedTestResult`                                                                                             | `variableNames` (comma separated list of variables to aggregate) |
| Trigger Message Start Event                       | `trigger-message-start-event` / `trigger-message-start-event-job`                                             | (will pass all variables into the message)                                                                                 |                                                                                                                    | `messageName`                                                    |