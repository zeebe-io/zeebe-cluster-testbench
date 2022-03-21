# Operator Handbook

## Deployment/Update Process

### Inside Camunda environment

Changes to `main` are automatically deployed to production.

### In Kubernetes

See `.ci/scripts/deploy.sh` for the steps necessary to deploy testbench.

## Initial Setup

### Summary

You need:

- Kubernetes cluster
- Zeebe cluster for test orchestration
- Camunda Cloud Organization account
- Camunda Cloud API credentials to create test clusters on demand
- Kubernetes service account, token and permissions to interact with running clusters on Kubernetes
  level
- Slack channel to receive notifications
- Slack app and webhook URL to send notifications to a slack channel

Setup and deployment:

- Look at `testbench.yaml` and `chaosWorker.yaml` and fill in the fields
- Setup a Kubernetes cluster to deploy into
- Set environment variables (see below)
- Deploy application `.ci/scripts/deploy.sh`
- Check in logs that self check was successful (happens at start up and connects to all external
  systems)
- Check in test orchestration cluster that processes were deployed

### Detailed Instructions

#### Test Orchestration Cluster

1. Login to Camunda Cloud
2. Create new cluster
3. Create new client for that cluster
4. Fill deployment descriptors as follows:

|               File               |             Field              |                                  Content                                   |
|----------------------------------|--------------------------------|----------------------------------------------------------------------------|
| testbench.yaml, chaosWorker.yaml | ZCTB_AUTHENTICATION_SERVER_URL | Cluster Client -> Connection Information -> ZEEBE_AUTHORIZATION_SERVER_URL |
| testbench.yaml, chaosWorker.yaml | ZCTB_CLIENT_ID                 | Cluster Client -> Connection Information -> ZEEBE_CLIENT_ID                |
| ENV_VARS                         | CLIENT_SECRET                  | Cluster Client -> Connection Information -> ZEEBE_CLIENT_SECRET            |
| ENV_VARS                         | CONTACT_POINT                  | Cluster Client -> Connection Information -> ZEEBE_ADDRESS                  |

#### Camunda Cloud API Client

1. Login to Camunda Cloud
2. Go to Organization Settings -> Cloud API
3. Create new client with all privileges
4. Fill deployment descriptors as follows:

|      File      |                Field                 |                                             Content                                             |
|----------------|--------------------------------------|-------------------------------------------------------------------------------------------------|
| testbench.yaml | ZCTB_CLOUD_API_URL                   | Depends on the stage (e.g. `https://api.cloud.ultrawombat.com/` for integration stage)          |
| testbench.yaml | ZCTB_CLOUD_AUDIENCE                  | Depends on stage (e.g. `api.cloud.ultrawombat.com` for integration stage)                       |
| testbench.yaml | ZCTB_CLOUD_AUTHENTICATION_SERVER_URL | Depends on stage (e.g. `https://login.cloud.ultrawombat.com/oauth/token` for integration stage) |
| testbench.yaml | ZCTB_CLOUD_CLIENT_ID                 | Cloud API -> Client -> Client Id                                                                |
| ENV_VARS       | CLOUD_CLIENT_SECRET                  | Cloud API -> Client -> Client Secret                                                            |

#### Slack App and Webhook

1. Create a Slack application (https://api.slack.com/start/overview)
2. Create an incoming webhook for your app (https://api.slack.com/messaging/webhooks)
3. Add a new webhook to your workspace for the desired channel (you may need approval from your
   workspace admin)
4. Invite your app to the channel it should publish to
5. Fill deployment descriptors as follows:

|   File   |       Field       |           Content            |
|----------|-------------------|------------------------------|
| ENV_VARS | SLACK_WEBHOOK_URL | The webhook URL for your app |

#### Chaos Experiments

In order to run our automated chaos experiments against a zeebe cluster, which runs in a different
kuebernetes cluster, we need access to that cluster. This means our chaos worker, which executes the
chaos experiments, needs access to it. This can be done by a service account and a corresponding
token which the application (the chaos worker) uses.

To create a service account, you can deploy the resources you find
under `/core/chaos-workers/deployment/service-account`.

Then you need to build the chaos worker docker image with the service account token. The
serviceaccount token can be received
via `kubectl -n zeebe-chaos describe secrets zeebe-chaos-sa-token-*`.

In the dockerfile of the chaos worker the token is used to setup the correct kube context etc.
Ideally you store the token in the team vault, where jenkins can access it and build the docker
image with the token as argument.

You can find more details on how to setup the service account and how it was previously
done [here](https://github.com/zeebe-io/zeebe/issues/4361#issuecomment-681869448).

### Deployment

- Create a Kubernetes cluster and namesapce
- Run steps similiar to those found in `.ci/scripts/deploy.sh`
- Check the log of the testbench pod for a successful self-test:

```
14:11:37.767 [main] INFO  io.zeebe.clustertestbench.bootstrap.BootstrapFromEnvVars - Bootstrapper starting
14:11:39.764 [main] INFO  io.zeebe.clustertestbench.bootstrap.Launcher - Selftest - Successfully established connection to test orchestration cluster
14:11:41.049 [main] INFO  io.zeebe.clustertestbench.bootstrap.Launcher - Selftest - Successfully established connection to cloud API
14:11:41.519 [main] INFO  io.zeebe.clustertestbench.bootstrap.Launcher - Selftest - Successfully established connection to Slack
14:11:42.422 [main] INFO  io.zeebe.clustertestbench.bootstrap.ProcessDeployer - Found processes to deploy:[processes\run-all-tests-in-camunda-cloud-per-cluster-plan.bpmn, processes\run-all-tests-in-camunda-cloud.bpmn, processes\run-sequential-test-in-camunda-cloud.bpmn]
14:11:42.422 [main] INFO  io.zeebe.clustertestbench.bootstrap.ProcessDeployer - Deploying run-all-tests-in-camunda-cloud-per-cluster-plan.bpmn
14:11:42.718 [main] INFO  io.zeebe.clustertestbench.bootstrap.ProcessDeployer - Deploying run-all-tests-in-camunda-cloud.bpmn
14:11:42.824 [main] INFO  io.zeebe.clustertestbench.bootstrap.ProcessDeployer - Deploying run-sequential-test-in-camunda-cloud.bpmn
14:11:42.923 [main] INFO  io.zeebe.clustertestbench.bootstrap.Launcher - Registering job worker MapNamesToUUIDsWorker for: map-names-to-uuids-job
14:11:42.934 [main] INFO  io.zeebe.clustertestbench.bootstrap.Launcher - Job worker opened and receiving jobs.
14:11:42.941 [main] INFO  io.zeebe.clustertestbench.bootstrap.Launcher - Registering job worker CreateClusterInCamundaCloudWorker for: create-zeebe-cluster-in-camunda-cloud-job
14:11:42.942 [main] INFO  io.zeebe.clustertestbench.bootstrap.Launcher - Job worker opened and receiving jobs.
14:11:42.942 [main] INFO  io.zeebe.clustertestbench.bootstrap.Launcher - Registering job worker SequentialTestLauncher for: run-sequential-test-job
14:11:42.943 [main] INFO  io.zeebe.clustertestbench.bootstrap.Launcher - Job worker opened and receiving jobs.
14:11:42.948 [main] INFO  io.zeebe.clustertestbench.bootstrap.Launcher - Registering job worker RecordTestResultWorker for: record-test-result-job
14:11:42.948 [main] INFO  io.zeebe.clustertestbench.bootstrap.Launcher - Job worker opened and receiving jobs.
14:11:42.949 [main] INFO  io.zeebe.clustertestbench.bootstrap.Launcher - Registering job worker NotifyEngineersWorker for: notify-engineers-job
14:11:42.950 [main] INFO  io.zeebe.clustertestbench.bootstrap.Launcher - Job worker opened and receiving jobs.
14:11:42.957 [main] INFO  io.zeebe.clustertestbench.bootstrap.Launcher - Registering job worker DeleteClusterInCamundaCloudWorker for: destroy-zeebe-cluster-in-camunda-cloud-job
14:11:42.957 [main] INFO  io.zeebe.clustertestbench.bootstrap.Launcher - Job worker opened and receiving jobs.
```

- Check that processes have been deployed to test orchestration cluster

