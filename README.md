# Zeebe Cluster Testbench

Test bench to run tests against a Zeebe cluster .

## Current State
This code base is in very early stages. Currently we are doing an exploration of ideas towards a PoC.

## Workflows
The testbench deploys several workflows to orchestrate the test execution. The work flows reference each other - a higher level workflow will call a lower level workflow. 
However, lower level workflows can also be called directly if only a certain test execution is wanted.

### Run All Tests
This is the master workflow to run all tests:

![run-all-tests workflow](assets/run-all-tests.png "Run all Tests workflow")

Currently it only has steps for the Simple test, but this could be extended in the future:

![run-all-tests workflow (vision)](assets/run-all-tests-vision.png "Run all Tests workflow (Vision)")

**Workflow ID:** `run-all-tests`
 
| Inputs | 
| ------ | 
| `dockerImage` - the Docker image of Zeebe that shall be tested | 
| `clusterPlans` - array of cluster plans in which Zeebe shall be testes | 
| _contact information to reach troubleshooter_ - some information to contact someone if the test failed and needs to be analysed |

| Outputs |
| ------- |
| `testResults` - array of test results |

#### Run Sequential Test in Clusterplan
This workflow runs the sequential test in a given clusterplan:

![run-sequential-test-in-clusterplan workflow](assets/run-sequential-test-in-clusterplan.png "Run Sequential Test in Clusterplan workflow")

**Notes**
* The _Notify Engineers_ step is a workaround until we have user tasks

**Workflow ID:** `run-sequential-test-in-clusterplan`
 
| Inputs |
| ------ |
| `dockerImage` - the Docker image of Zeebe that shall be tested |
| `clusterPlan` - cluster plan in which Zeebe shall be tested | 
| _contact information to reach troubleshooter_ - some information to contact someone if the test failed and needs to be analysed |

| Runtime Variables |
| ----------------- |
| `clusterId` - ID of the cluster in which Zeebe is tested |
| _clusterCredentials_ (tbd) Credentials to authenticate against the cluster |


| Outputs |
| ------- |
| `testResult` - test result |

## Service Tasks

| Service Task | ID / Job Type | Input | Output | 
| ------------ | ------------- | ----- | ------ | 
| Create Zeebe Cluster in Camunda cloud | `creae-zeebe-cluster-in-camunda-cloud` / `create-zeebe-cluster-in-camunda-cloud-job` | `dockerImage`, `clusterPlan` | `clusterId`, _clusterCredentials_ |   
| Run Sequential Test | `run-sequential-test` / `run-sequential-test-job` | _clusterCredentials_ | `testResult` 
| Record Test Result | `record-test-result` / `record-test-result-job` | `testResult`, _clusterCredentials_ |
| Notify Engineers | `notify-engineers` / `notify-engineers-job` | `dockerImage`, `clusterPlan`, `clusterId`, `testResult`, _contact information to reach troubleshooter_ |
| Destroy Zeebe Cluster in Camunda CLoud | `destroy-zeebe-cluster-in-camunda-cloud` / `destroy-zeebe-cluster-in-camunda-cloud-job` | `clusterId` |
 
## Messages
| Message | ID  | Correlation Key  | 
| ------- | --- | ---------------- |  
| Analysis Completed | `msg-analysis-completed` | `clusterId` | 
