# Zeebe Cluster Testbench

Test bench to run tests against a Zeebe cluster .

## Current State
This code base is in very early stages. Currently we are doing an exploration of ideas towards a PoC.

## Tests

### Sequential Test
In the sequential test a sequential workflow is executed several times in succession. There is at most one active workflow instance at any given time. In other words, there is no parallelism - neither inside the workflow being tested nor in the test driver that starts workflow instances.

A sequential test has the following parameters:
* _steps_ - number of steps within the workflow
* _iterations_ - number of times the workflow shall be executed
* _maxTimeForIteration_ - the maximum duration for one iteration 
* _maxTimeForCompleteTest_ - the maximum time for the entire test

This test will fail, if any of the following conditions occur:
* there is an exception during test execution
* a single iteration takes longer than _maxTimeForIteration_
* all iterations take longer than _maxTimeForCompleteTest_

In case of backpressure the iteration will be repeated. The time spent making requests that return backpressure responses and repeating those requests is included in the overall execution time, which must be smaller than _maxTimeForCompleteTest_ for the test to pass.

## Workflows
The testbench deploys several workflows to orchestrate the test execution. The work flows reference each other - a higher level workflow will call a lower level workflow. 
However, lower level workflows can also be called directly if only a certain test execution is wanted.

### Run All Tests in Camunda Cloud per Cluster Plan
This workflow runs all tests in a fresh cluster in Camunda Cloud in different cluster plans:

![run-all-tests-in-camunda-cloud-per-clusterplan workflow](assets/run-all-tests-in-camunda-cloud-per-clusterplan.png "Run all Tests in Camunda Cloud workflow")


**Workflow ID:** `run-all-tests-in-camunda-cloud-per-cluster-plan-process`
 
| Inputs | Description | Type |
| ------ | ----------- | ---- | 
| `dockerImage` | UUID of the generation for the cluster | `String` |
| `clusterPlans` | array of UUIDs of the cluster plans for the clusters | `List<String>` |
| `channelId` | UUID of the channel for the cluster | `String` |
| `regionId` | UUID of the region for the cluster | `String` |
| `sequentialTestParams` | Settings to parameterize the sequential test | `SequentialTestParameters` |

### Run All Tests in Camunda Cloud per Region
This workflow runs all tests in a fresh cluster in Camunda Cloud in different regions:

![run-all-tests-in-camunda-cloud-per-region workflow](assets/run-all-tests-in-camunda-cloud-per-region.png "Run all Tests in Camunda Cloud per Region workflow")


**Workflow ID:** `run-all-tests-in-camunda-cloud-per-region-process`
 
| Inputs | Description | Type |
| ------ | ----------- | ---- | 
| `dockerImage` | UUID of the generation for the cluster | `String` |
| `clusterPlan` | array of UUIDs of the cluster plans for the clusters | `String` |
| `channelId` | UUID of the channel for the cluster | `String` |
| `regions` | List of UUID of the regions for the clusters | `List<String>` |
| `sequentialTestParams` | Settings to parameterize the sequential test | `SequentialTestParameters` |

### Run All Tests in Camunda Cloud 
This workflow runs all tests in a fresh cluster in Camunda Cloud:

![run-all-tests-in-camunda-cloud workflow](assets/run-all-tests-in-camunda-cloud.png "Run all Tests in Camunda Cloud workflow")


Currently, it only has steps for the _sequential test_, but this could be extended in the future.

**Workflow ID:** `run-all-tests-in-camunda-cloud-process`
 
| Inputs | Description | Type |
| ------ | ----------- | ---- | 
| `dockerImage` | UUID of the generation for the cluster | `String` |
| `clusterPlan` | UUID of the cluster plan for the cluster | `String` |
| `channelId` | UUID of the channel for the cluster | `String` |
| `regionId` | UUID of the region for the cluster | `String` |
| `sequentialTestParams` | Settings to parameterize the sequential test | `SequentialTestParameters` |

| Outputs | Description | Type |
| ------- | ----------- | ---- |
| `sequentialTestResult` | Test result for sequential test | `List<TestResult>` |

#### Run Sequential Test in Camunda Cloud
This workflow runs the sequential test in a fresh cluster in Camunda Cloud:

![run-sequential-test-in-camunda-cloud workflow](assets/run-sequential-test-in-camunda-cloud.png "Run Sequential Test in Camunda Cloud workflow")

**Notes**
* The _Notify Engineers_ step is a workaround until we have user tasks

**Workflow ID:** `run-sequential-test-in-clusterplan`
 
| Inputs | Description | Type |
| ------ | ----------- | ---- |
| `dockerImage` | UUID of the generation for the cluster | `String` |
| `clusterPlan` | UUID of the cluster plan for the cluster | `String` |
| `channelId` | UUID of the channel for the cluster | `String` |
| `regionId` | UUID of the region for the cluster | `String` |
| `testParams` | Settings to parameterize the sequential test | `SequentialTestParameters` |

| Runtime Variables | Description | Type |
| ----------------- | ----------- | ---- |
| `clusterId` | ID of the cluster in which Zeebe is tested | `String` |
| `authenticationDetails` | Credentials to authenticate against the cluster | `CamundaCloudAutenticationDetails` |


| Outputs | Description | Type |
| ------- | ----------- | ---- |
| `testReport` | test report | `TestReport` |
| `testResult` | test result | `TestResult` |

## Service Tasks

| Service Task | ID / Job Type | Input | Output | Headers |
| ------------ | ------------- | ----- | ------ | ------- |
| Create Zeebe Cluster in Camunda cloud | `creae-zeebe-cluster-in-camunda-cloud` / `create-zeebe-cluster-in-camunda-cloud-job` | `dockerImage`, `clusterPlan`, `regionId`, `channelId` | `clusterId`, `authenticationDetails` |   
| Run Sequential Test | `run-sequential-test` / `run-sequential-test-job` | `authenticationDetails`, `testParams` | `testResult`, `testReport` 
| Record Test Result | `record-test-result` / `record-test-result-job` | `dockerImage`, `clusterPlan`, `clusterId`, `testReport` |
| Notify Engineers | `notify-engineers` / `notify-engineers-job` | `dockerImage`, `clusterPlan`, `clusterId`, `testReport` | | `channel` - Slack channel to post to, `testType` - test type (will be part of the error message
| Destroy Zeebe Cluster in Camunda CLoud | `destroy-zeebe-cluster-in-camunda-cloud` / `destroy-zeebe-cluster-in-camunda-cloud-job` | `clusterId` |
 
## Messages
| Message | ID  | Correlation Key  | 
| ------- | --- | ---------------- |  
| Analysis Completed | `msg-analysis-completed` | `clusterId` | 
