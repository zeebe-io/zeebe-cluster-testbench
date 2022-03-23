# Extension Guide

This document describes how to extend the test bench. Extensions are indeed very welcome.
It would be great to have workers in different languages and a broad set of tests that cover the full product spectrum of Camunda Cloud.

## Adding Additional Tests

Additional tests can be added very easily by:
* Deploying additional (test) processes
* Deploying additional workers used in these processes

The sources for both may be added to the test bench repository, or (preferably) they live in a repository of their own.

The interface between test bench and the test would then be the [Run Test in Camunda Cloud](../README.md#run-test-in-camunda-cloud) process

![Run Test In Camunda Cloud](assets/run-test-in-camunda-cloud.png)

This process takes as parameters, among others, the _testProcessId_. This is the ID of the process you want to have called.

The called process will find the variables that are defined in `TestInputDTO`. This includes _authenticationDetails_ which
can be used to authenticate against the Zeebe cluster.

The called process is expected to return an output - see class `TestOutput` for the expected structure.

The called process can have sub processes and complex structures. In its simples form, the called process can have a single service task
that delegates to a test driver. A test driver is responsible for running a test within a cluster provided by Testbench.

Please have a look at [TestDriver](/testdriver/api/src/main/java/io/zeebe/clustertestbench/testdriver/api/TestDriver.java) which defines the interface of a test driver.

If you implement a test driver in Java/Kotlin/Scala, you can use these interfaces. In particular, you should be able to
unmarshall a `TestInputDTO` from the variables of the process instance, and you should be able to marshall a `TestOutputDTO`
to the response variables of a job.

## Testing Something Other than Zeebe

The current setup is squarely focused on Zeebe. In order to adapt if to other applications, you probably need to:

1. Build a generation that includes the version of your applications
2. Extract additional information about the cluster from Console API
3. Think about authentication

If you want to test against a specific version of your application, please have a look at `CreateGenerationInCamundaCloudHandler`.
The way this works today is that it takes two arguments: a _generationTemplate_ and a _zeebeImage_.
This worker creates a new generation with the desired Zeebe image - the images for the other applications are taken from the template.
This could easily be extended to also set a custom Tasklist, Optimize, etc. image.

We keep track of the Operate URL associated with the cluster, but we don't yet know the Tasklist URL or Optimize URL.

The first step to make this information available in the test process would be to change `GatherInformationAboutClusterInCamundaCloudHandler`.
This is the worker that reads out information about a cluster and writes this information into the process instance.
Under the hood this is using the `ClusterInfo` returned by the Console API.

One thing to note here is that the `ClusterInfo` object defined in this repo contains only the information we needed
so far. More information is available in the Console API and could easily be added to the objects to then be parsed
automatically.

Once you have the URL, you probably need to authenticate against these services somehow. In case you are implementing
worker in Java, it may help to look at `OAuthClient` which contains implementation for requesting service account and
user account tokens. These are then used in interceptors to request a token before REST calls are made

## Copy and Paste Goodness

Things that have already been implemented and might be useful to take inspiration from, or just to copy to save time:
* You can look at https://github.com/zeebe-io/zeebe-chaos/tree/master/chaos-workers which implements the extension mechanism described above. It deploys its own processes and workers to the test bench.
* Test bench has a CI pipeline for continuous deployment (`Jenkinsfile, prepare-deploy.sh, deploy.sh`). The main branch is deployed to the production environment. Additionally, developers can manually deploy from feature branches to a Dev cluster. This is used for testing before merging the feature branch.
* Zeebe has a [CI pipeline](https://github.com/camunda-cloud/zeebe/blob/22b0bf7d08f390bb2c288bb08bffc3f930c41fae/Jenkinsfile#L321) that triggers a test run on test bench, waits for the aggregated result and succeeds/fails the build depending on the result

## Outlook

We are looking for people interested in extending the test bench with more tests or more applications to test.

The further development of test bench will be shaped by the needs of such extensions.
For example, for Zeebe the cluster plan is of importance. But it might be less important for other applications in Camunda Cloud.

One thing that were already discussed in the context of chaos experiments:

We want to refactor the processes. In particular, we thought about getting rid of [Run All Tests in Camunda Cloud](../README.md#run-all-tests-in-camunda-cloud).
We want to replace it by a process that takes a list of _testProcessId_ and then uses a multi instance to run all tests in that list.
This would allow callers of the processes to specify which tests to run. We believe this will be a more flexible approach.
[#520](https://github.com/zeebe-io/zeebe-cluster-testbench/issues/520)
