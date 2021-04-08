// vim: set filetype=groovy:

@Library(["camunda-ci", "zeebe-jenkins-shared-library"]) _

final def buildName = "${env.JOB_BASE_NAME.replaceAll("%2F", "-").replaceAll("\\.", "-").take(20)}-${env.BUILD_ID}"

pipeline {

    agent {
        kubernetes {
            cloud 'zeebe-ci'
            label "zeebe-ci-build_${buildName}"
            defaultContainer 'jnlp'
            yamlFile '.ci/podSpecs/distribution.yml'
        }
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 15, unit: 'MINUTES')
    }

    environment {
        NEXUS = credentials('camunda-nexus')
        DOCKER_GCR = credentials('zeebe-gcr-serviceaccount-json')
        SA_CREDENTIALS = credentials('zeebe-jenkins-deploy-serviceaccount-json')
    }

    parameters {
        booleanParam(name: 'DEPLOY_TO_DEV', defaultValue: false, description: 'Click here if you want to test a feature in the development environment prior to merge');
        booleanParam(name: 'RELEASE', defaultValue: false, description: 'Build a release from current commit?')
        string(name: 'RELEASE_VERSION', defaultValue: '1.x.0', description: 'Which version to release?')
        string(name: 'DEVELOPMENT_VERSION', defaultValue: '1.y.0-SNAPSHOT', description: 'Next development version?')
    }

    stages {
        stage('Prepare') {
            steps {
                script {
                    commit_summary = sh([returnStdout: true, script: 'git show -s --format=%s']).trim()
                    displayNameFull = '#' + BUILD_NUMBER + ': ' + commit_summary

                    if (displayNameFull.length() <= 45) {
                        currentBuild.displayName = displayNameFull
                    } else {
                        displayStringHardTruncate = displayNameFull.take(45)
                        currentBuild.displayName = displayStringHardTruncate.take(displayStringHardTruncate.lastIndexOf(' '))
                    }
                }
                container('maven') {
                    configFileProvider([configFile(fileId: 'maven-nexus-settings-zeebe', variable: 'MAVEN_SETTINGS_XML')]) {
                        sh '.ci/scripts/distribution/prepare.sh'
                    }
                }
            }
        }

        stage('Build') {
            steps {
                container('maven') {
                    configFileProvider([configFile(fileId: 'maven-nexus-settings-zeebe', variable: 'MAVEN_SETTINGS_XML')]) {
                        sh 'mvn install -B -s $MAVEN_SETTINGS_XML -Dsurefire.rerunFailingTestsCount=5'
                    }
                }
            }

            post {
                always {
                    junit testResults: '**/*/surefire-reports/TEST-*.xml', keepLongStdio: true
                    junit testResults: '**/*/failsafe-reports/TEST-*.xml', keepLongStdio: true

                    jacoco(
                        execPattern: '**/*.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java',
                        runAlways: true
                    )
                    zip zipFile: 'test-coverage-reports.zip', archive: true, glob: '**/target/site/jacoco/**'
                }
                failure {
                    zip zipFile: 'test-reports.zip', archive: true, glob: '**/*/surefire-reports/**'
                    zip zipFile: 'integration-test-reports.zip', archive: true, glob: '**/*/failsafe-reports/**'
                }
            }
        }

        stage('Verify scripts') {
            steps {
                container('maven') {
                    dir('core/chaos-workers') {
                        println("Check all chaos script`s with shellcheck (linter).")
                        sh 'shellcheck -x *.sh'
                        println("Scripts are fine.")
                        println("Run bash tests via bats.")
                        sh './*Test.sh'
                    }
                    dir('scripts') {
                        println("Check general script`s with shellcheck (linter).")
                        sh 'touch credentials'
                        sh 'shellcheck -x *.sh'
                        println("Scripts are fine.")
                    }
                }
            }
        }

        stage('Upload') {
            when {
                not { expression { params.RELEASE } }
                anyOf {
                    branch 'develop'
                    branch 'stable/*'
                    expression { params.DEPLOY_TO_DEV }
                }
            }
            steps {
                container('maven') {
                    configFileProvider([configFile(fileId: 'maven-nexus-settings-zeebe', variable: 'MAVEN_SETTINGS_XML')]) {
                        sh 'mvn -B -s $MAVEN_SETTINGS_XML generate-sources source:jar javadoc:jar deploy -DskipTests'
                    }
                }
            }
        }

        stage('Deploy') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'stable/*'
                    expression { params.DEPLOY_TO_DEV }
                }
            }

            environment {
                TAG = getTag()
                SECRET_STORE = getSecretStore()
            }

            steps {
                container('docker') {
                    /* this command is a little convoluted to avoid leaking of the secret;
                    * it is unclear why the built-in Jenkins mechanism doesn't work out of the box
                    */
                    sh 'set +x ; echo ${DOCKER_GCR} | docker login -u _json_key --password-stdin https://gcr.io ; set -x'

                    sh 'docker build -t gcr.io/zeebe-io/zeebe-cluster-testbench:${TAG} .'
                    sh 'docker push gcr.io/zeebe-io/zeebe-cluster-testbench:${TAG}'
                    withVault([vaultSecrets: [
                        [path: 'secret/common/ci-zeebe/zeebe-chaos-service-account', secretValues: [
                            [vaultKey: 'token'],
                        ]],
                    ]]) {

                        dir('core/chaos-workers/') {
                            sh 'docker build . -t gcr.io/zeebe-io/zeebe-cluster-testbench-chaos:${TAG} --build-arg TOKEN=${token}'
                            sh 'docker push gcr.io/zeebe-io/zeebe-cluster-testbench-chaos:${TAG}'
                        }
                    }
                }


                container('gcloud') {
                    sh '.ci/scripts/prepare-deploy.sh'
                    withVault(
                        [vaultSecrets:
                             [
                                 [path        : "${SECRET_STORE}",
                                  secretValues: [
                                      [envVar: 'CLIENT_SECRET', vaultKey: 'clientSecret'],
                                      [envVar: 'CLOUD_CLIENT_SECRET', vaultKey: 'cloudClientSecret'],
                                      [envVar: 'CONTACT_POINT', vaultKey: 'contactPoint'],
                                      [envVar: 'INTERNAL_CLOUD_CLIENT_SECRET', vaultKey: 'internalCloudClientSecret'],
                                      [envVar: 'INTERNAL_CLOUD_PASSWORD', vaultKey: 'internalCloudPassword'],
                                      [envVar: 'SHEETS_API_KEYFILE_CONTENT', vaultKey: 'sheetsApiKeyfileContent'],
                                      [envVar: 'SLACK_WEBHOOK_URL', vaultKey: 'slackWebhookUrl'],
                                  ]
                                 ],
                             ]
                        ]
                    ) {
                        sh '.ci/scripts/deploy.sh ${TAG}'
                    }
                }
            }
        }

        stage('Release') {
            when { expression { params.RELEASE } }

            environment {
                MAVEN_CENTRAL = credentials('maven_central_deployment_credentials')
                GPG_PASS = credentials('password_maven_central_gpg_signing_key')
                GPG_PUB_KEY = credentials('maven_central_gpg_signing_key_pub')
                GPG_SEC_KEY = credentials('maven_central_gpg_signing_key_sec')
                GITHUB_TOKEN = credentials('camunda-jenkins-github')
                RELEASE_VERSION = "${params.RELEASE_VERSION}"
                DEVELOPMENT_VERSION = "${params.DEVELOPMENT_VERSION}"
            }

            steps {
                container('maven') {
                    configFileProvider([configFile(fileId: 'maven-nexus-settings-zeebe', variable: 'MAVEN_SETTINGS_XML')]) {
                        sshagent(['camunda-jenkins-github-ssh']) {
                            sh 'gpg -q --import ${GPG_PUB_KEY} '
                            sh 'gpg -q --allow-secret-key-import --import --no-tty --batch --yes ${GPG_SEC_KEY}'
                            sh 'git config --global user.email "ci@camunda.com"'
                            sh 'git config --global user.name "camunda-jenkins"'
                            sh 'mkdir ~/.ssh/ && ssh-keyscan github.com >> ~/.ssh/known_hosts'
                            sh 'mvn -B -s $MAVEN_SETTINGS_XML -DskipTests source:jar javadoc:jar release:prepare release:perform -Prelease'
                            sh '.ci/scripts/github-release.sh'
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            // Retrigger the build if the node disconnected
            script {
                if (agentDisconnected()) {
                    currentBuild.result = 'ABORTED'
                    currentBuild.description = 'Aborted due to connection error'
                    build job: currentBuild.projectName, propagate: false, quietPeriod: 20, wait: false
                }
            }
        }

        changed {
            script {
                if ((env.BRANCH_NAME != 'develop' && !env.BRANCH_NAME.startsWith("stable")) || agentDisconnected()) {
                    return
                }
                if (hasBuildResultChanged()) {
                    slackSend(
                        channel: '#zeebe-testbench-ci',
                        message: "Zeebe Cluster Testbench on _${env.BRANCH_NAME}_ changed status to _${currentBuild.currentResult}_ for build ${currentBuild.absoluteUrl}"
                    )
                }
            }
        }
    }
}

def getTag() {
    return params.DEPLOY_TO_DEV ? '1.x-dev' : '1.x-prod'
}

def getSecretStore() {
    return params.DEPLOY_TO_DEV ? 'secret/common/ci-zeebe/testbench-1.x-secrets-dev' : 'secret/common/ci-zeebe/testbench-1.x-secrets-prod'
}
