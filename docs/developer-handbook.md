# Developer Handbook

- [Technical documentation](technical-documentation.md)
- [Operator handbook](operator-handbook.md)

## Stages
Testbench exists on 2 stages:
  - Dev stage - Used for development of the testbench
  - Prod stage - Used as the actual production environment for testing Zeebe clusters

All stages are clusters running on the Camunda Cloud int environment (ultrawombat).

## CI/CD Pipeline

- A new build can automatically be deployed to one of two stages:
  - Dev stage - any branch can be deployed when requested. This is done by setting the corresponding build parameter in Jenkins. Please coordinate with your colleagues as there is only one dev stage and each deploy will override it.
  - Prod stage - commits to develop and stable branches are automatically deployed to the prod stage. Other branches are ignored.
- If the develop build fails, the slack channel `#zeebe-testbench-ci` (on Camunda slack) will be notified (this often happens if the changes break the deploy process as the deploy steps are only executed for develop)
- Secrets are stored in Vault and also deployed automatically

### Dev Stage

- Image tag `*-dev`
- K8S namespace = `gke_zeebe-io_europe-west1-b_zeebe-cluster/testbench-1-x-dev`
- Secrets `secret/common/ci-zeebe/testbench-secrets-1.x-dev`

### Prod Stage

- Image tag `*-prod`
- K8S namespace = `gke_zeebe-io_europe-west1-b_zeebe-cluster/testbench-1-x-prod`
- Secrets `secret/common/ci-zeebe/testbench-secrets-1.x-prod`

## Permutation Testing
```
mvn eu.stamp-project:pitmp-maven-plugin:run
```

The permutation testing reports appear under `.[module]/target/pit-reports/[timestamp]/index.html`.

## Conventions

| Convention | Rationale |
|---------- | ---------|
| Verify all shell scripts with _shellcheck_ | Shellcheck tests shell scripts for common mistakes and vulnerabilities. It has been very valuable in making the scripts we use more secure |

## Best Practices

| Best Practice | Rationale |
|---------- | ---------|
| Test shell script workers with [bats](https://github.com/sstephenson/bats) | Tests are great. Shell scripts can be tested, too. Let's do this|


## Issue Discipline

- keep the list short (< 25 open issues)
- aim to complete current open issues before creating new ones

## Common Tasks

### Adding Workers

- When you add Java workers as part of the core code and testbench deployment, then make sure to register the workers in the launcher
- When you add workers that are to be run as a separate deployment, make sure to update `ci/scripts/deploy.sh` in three places
  - Deploy the pod
  - Restart the pod (see comment in file on why this is necessary)
  - Wait for the pod

### Adding Secrets

- Add the secret to the secrets files (one for dev and one for int)
- Resolve the secret in `Jenkinsfile` deploy stage

```Groovy
withVault(
    [ vaultSecrets:
        [
        [ path: "${SECRET_STORE}",
            secretValues: [
                [envVar: 'CLIENT_SECRET', vaultKey: 'clientSecret'],
                [envVar: 'CLOUD_CLIENT_SECRET', vaultKey: 'cloudClientSecret'],
```

- Add the secret to the `deploy.sh` script

```Bash
kubectl create secret generic testbench-secrets --namespace="${namespace}" \
  --from-literal=contactPoint="${CONTACT_POINT}" \
  --from-literal=clientSecret="${CLIENT_SECRET}" \
```

- Add the secret to the deployment artifacts (one for dev and one for int) where you want to use it

```yaml
spec:
  containers:
    - name: testbench
      image: gcr.io/zeebe-io/zeebe-cluster-testbench:1.x-prod
      imagePullPolicy: Always
      env:
        - name: ZCTB_CLIENT_SECRET
          valueFrom:
            secretKeyRef:
              name: testbench-secrets
              key: clientSecret
```
