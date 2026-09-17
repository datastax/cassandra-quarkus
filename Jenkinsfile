#!groovy

def initializeEnvironment() {
  env.DRIVER_DISPLAY_NAME = 'CassandraⓇ Quarkus Extension'

  env.GIT_SHA = "${env.GIT_COMMIT.take(7)}"
  env.GITHUB_PROJECT_URL = "https://${GIT_URL.replaceFirst(/(git@|http:\/\/|https:\/\/)/, '').replace(':', '/').replace('.git', '')}"
  env.GITHUB_BRANCH_URL = "${GITHUB_PROJECT_URL}/tree/${env.BRANCH_NAME}"
  env.GITHUB_COMMIT_URL = "${GITHUB_PROJECT_URL}/commit/${env.GIT_COMMIT}"

  env.MAVEN_HOME = "${env.HOME}/.mvn/apache-maven-3.8.8"
  env.PATH = "${env.MAVEN_HOME}/bin:${env.PATH}"

  sh label: 'Display Java and environment information',script: '''#!/bin/bash -le
    . ${JABBA_SHELL}
    
    echo "Java version:"
    jabba use ${JABBA_NAME}
    java -version

    echo "Maven version:"
    mvn -v
    
    echo "Environment:"
    printenv | sort
  '''
}

def buildAndExecuteTests() {
  sh label: 'Build and execute tests in non-native mode with release profile', script: '''#!/bin/bash -le
    . ${JABBA_SHELL}
    jabba use ${JABBA_NAME}
    mvn -B -V install -Prelease -Dgpg.skip
  '''
}

def executeNativeTests() {
  sh label: 'Execute integration tests in native mode', script: '''#!/bin/bash -le
    . ${JABBA_SHELL}
    jabba use ${JABBA_NAME}
    mvn -B -V verify -Dnative -rf :cassandra-quarkus-integration-tests -Djacoco.skip=true
  '''
}

pipeline {
  agent none

  // Global pipeline timeout
  options {
    timeout(time: 1, unit: 'HOURS')
    buildDiscarder(logRotator(artifactNumToKeepStr: '10', // Keep only the last 10 artifacts
                              numToKeepStr: '50'))        // Keep only the last 50 build records
  }


  environment {
    OS_VERSION = 'ubuntu/focal64/java-driver'
    JABBA_SHELL = '/usr/lib/jabba/jabba.sh'
  }

  stages {
    stage ('default') {
      options {
        timeout(time: 1, unit: 'HOURS')
      }
      when {
        beforeAgent true
        allOf {
          not { buildingTag() }
        }
      }

      matrix {
        axes {
          axis {
            name 'JABBA_NAME'
            values 'openjdk@1.17',
                   'openjdk@21',
                   'openjdk@1.25',
                   'graalvm@21.0.7',
                   'graalvm@25.0.3'
          }
        }

        agent {
          label "${OS_VERSION}"
        }
        stages {
          stage('Initialize-Environment') {
            steps {
              initializeEnvironment()
            }
          }

          // We always run base tests whether we're dealing with Graal or not.  We want to
          // make sure Graal can also support the extension when running in pure Java mode.
          stage('Build-And-Execute-Tests') {
            steps {
              catchError {
                buildAndExecuteTests()
              }
            }
            post {
              always {
                /*
                 * Empty results are possible
                 *
                 *  - Build failures during mvn verify may exist so report may not be available
                 */
                junit testResults: '**/target/surefire-reports/TEST-*.xml', allowEmptyResults: true
                junit testResults: '**/target/failsafe-reports/TEST-*.xml', allowEmptyResults: true
              }
            }
          }

          stage('Native-Tests') {
            when {
              expression {
                return env.JABBA_NAME.startsWith('graalvm')
              }
            }
            steps {
              catchError {
                executeNativeTests()
              }
            }
          }
        }
      }
    }
  }
}
