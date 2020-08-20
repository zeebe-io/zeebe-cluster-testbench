// vim: set filetype=groovy:

@Library(["camunda-ci", "zeebe-jenkins-shared-library"]) _

def buildName = "${env.JOB_BASE_NAME.replaceAll("%2F", "-").replaceAll("\\.", "-").take(20)}-${env.BUILD_ID}"

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
    NEXUS = credentials("camunda-nexus")
  }

  parameters {
    booleanParam(name: 'RELEASE', defaultValue: false, description: 'Build a release from current commit?')
    string(name: 'RELEASE_VERSION', defaultValue: '0.X.0', description: 'Which version to release?')
    string(name: 'DEVELOPMENT_VERSION', defaultValue: '0.Y.0-SNAPSHOT', description: 'Next development version?')
  }

  stages {
    stage('Prepare') {
      steps {
        script {
            commit_summary = sh([returnStdout: true, script: 'git show -s --format=%s']).trim()
            displayNameFull = "#" + BUILD_NUMBER + ': ' + commit_summary

            if (displayNameFull.length() <= 45) {
              currentBuild.displayName = displayNameFull
            } else {
              displayStringHardTruncate = displayNameFull.take(45)
              currentBuild.displayName = displayStringHardTruncate.take(displayStringHardTruncate.lastIndexOf(" "))
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
      when { not { expression { params.RELEASE } } }
      steps {
        container('maven') {
          configFileProvider([configFile(fileId: 'maven-nexus-settings-zeebe', variable: 'MAVEN_SETTINGS_XML')]) {
            sh 'mvn install -B -s $MAVEN_SETTINGS_XML -Dsurefire.rerunFailingTestsCount=5'
          }
        }
      }

      post {
        always {
            junit testResults: "target/surefire-reports/TEST-*.xml", keepLongStdio: true
            
            jacoco(
                  execPattern: '**/*.exec',
                  classPattern: '**/target/classes',
                  sourcePattern: '**/src/main/java',
                  runAlways: true
            )
            zip zipFile: 'test-coverage-reports.zip', archive: true, glob: "**/target/site/jacoco/**"
        }
        failure {
            zip zipFile: 'test-reports.zip', archive: true, glob: "**/*/surefire-reports/**"
            archive "**/hs_err_*.log"
        }        
      }
    }

    stage('Upload') {
      when { not { expression { params.RELEASE } } }
      steps {
        container('maven') {
          configFileProvider([configFile(fileId: 'maven-nexus-settings-zeebe', variable: 'MAVEN_SETTINGS_XML')]) {
            sh 'mvn -B -s $MAVEN_SETTINGS_XML generate-sources source:jar javadoc:jar deploy -DskipTests'
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
                    currentBuild.description = "Aborted due to connection error"
                  build job: currentBuild.projectName, propagate: false, quietPeriod: 60, wait: false
              }
          }
      }
  }
}