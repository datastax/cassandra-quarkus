#!groovy

def initializeEnvironment() {
  env.DRIVER_DISPLAY_NAME = 'CassandraⓇ Quarkus Extension'

  env.GIT_SHA = "${env.GIT_COMMIT.take(7)}"
  env.GITHUB_PROJECT_URL = "https://${GIT_URL.replaceFirst(/(git@|http:\/\/|https:\/\/)/, '').replace(':', '/').replace('.git', '')}"
  env.GITHUB_BRANCH_URL = "${GITHUB_PROJECT_URL}/tree/${env.BRANCH_NAME}"
  env.GITHUB_COMMIT_URL = "${GITHUB_PROJECT_URL}/commit/${env.GIT_COMMIT}"

  env.MAVEN_HOME = "${env.HOME}/.mvn/apache-maven-3.6.0"
  env.PATH = "${env.MAVEN_HOME}/bin:${env.PATH}"

  sh label: 'Display Java and environment information',script: '''#!/bin/bash -le
    . ${JABBA_SHELL}
    
    echo "Java version used for compilation:"
    jabba use ${JABBA_VERSION}
    java -version
    
    echo "Java version used for native image generation:"
    jabba use ${GRAALVM_VERSION}
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
    jabba use ${JABBA_VERSION}
    mvn -B -V install -Prelease -Dgpg.skip
  '''
}

def executeNativeTests() {
  sh label: 'Execute integration tests in native mode', script: '''#!/bin/bash -le
    . ${JABBA_SHELL}
    jabba use ${GRAALVM_VERSION}
    mvn -B -V verify -Dnative -rf :cassandra-quarkus-integration-tests -Djacoco.skip=true
  '''
}

def executeCodeCoverage() {
  jacoco(
    execPattern: '**/target/*.exec',
    classPattern: '**/classes',
    sourcePattern: '**/src/main/java'
  )
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
    OS_VERSION = 'ubuntu/bionic64/java-driver'
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

        agent {
          label "${OS_VERSION}"
        }
        environment {
          JABBA_VERSION = 'openjdk@1.11'
          GRAALVM_VERSION = '11.0.17-graalvm-ce-22.3.0'
        }

        stages {
          stage('Initialize-Environment') {
            steps {
              initializeEnvironment()
            }
          }

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

          stage('Execute-Code-Coverage') {
            steps {
              executeCodeCoverage()
            }
          }

          stage('Native-Tests') {
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
