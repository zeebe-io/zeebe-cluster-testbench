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

### Run All Tests
This is the master workflow to run all tests:

![run-all-tests workflow](assets/run-all-tests.png "Run all Tests workflow")

Currently it only has steps for the Simple test, but this could be extended in the future:

![run-all-tests workflow (vision)](assets/run-all-tests-vision.png "Run all Tests workflow (Vision)")

**Workflow ID:** `run-all-tests`
 
| Inputs | Description | Type |
| ------ | ----------- | ---- | 
| `dockerImage` | the Docker image of Zeebe that shall be tested | `String` |
| `clusterPlans` | array of cluster plans in which Zeebe shall be testes | `List<String>` |
| `sequentialTestParams` | Settings to parameterize the sequential test | `SequentialTestParameters` |

| Outputs | Description | Type |
| ------- | ----------- | ---- |
| `testResults` | array of test results | `List<TestResult>` |

#### Run Sequential Test in Clusterplan
This workflow runs the sequential test in a given clusterplan:

![run-sequential-test-in-clusterplan workflow](assets/run-sequential-test-in-clusterplan.png "Run Sequential Test in Clusterplan workflow")

**Notes**
* The _Notify Engineers_ step is a workaround until we have user tasks

**Workflow ID:** `run-sequential-test-in-clusterplan`
 
| Inputs | Description | Type |
| ------ | ----------- | ---- |
| `dockerImage` | the Docker image of Zeebe that shall be tested | `String` |
| `clusterPlan` | cluster plan in which Zeebe shall be tested | `String` |
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
| Create Zeebe Cluster in Camunda cloud | `creae-zeebe-cluster-in-camunda-cloud` / `create-zeebe-cluster-in-camunda-cloud-job` | `dockerImage`, `clusterPlan` | `clusterId`, `authenticationDetails` |   
| Run Sequential Test | `run-sequential-test` / `run-sequential-test-job` | `authenticationDetails`, `testParams` | `testResult`, `testReport` 
| Record Test Result | `record-test-result` / `record-test-result-job` | `dockerImage`, `clusterPlan`, `clusterId`, `testReport` |
| Notify Engineers | `notify-engineers` / `notify-engineers-job` | `dockerImage`, `clusterPlan`, `clusterId`, `testReport` | | `channel` - Slack channel to post to, `testType` - test type (will be part of the error message
| Destroy Zeebe Cluster in Camunda CLoud | `destroy-zeebe-cluster-in-camunda-cloud` / `destroy-zeebe-cluster-in-camunda-cloud-job` | `clusterId` |
 
## Messages
| Message | ID  | Correlation Key  | 
| ------- | --- | ---------------- |  
| Analysis Completed | `msg-analysis-completed` | `clusterId` | 
