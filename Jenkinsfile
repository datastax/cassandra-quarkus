#!groovy

def initializeEnvironment() {
  env.DRIVER_DISPLAY_NAME = 'CassandraⓇ Quarkus Extension'

  env.GIT_SHA = "${env.GIT_COMMIT.take(7)}"
  env.GITHUB_PROJECT_URL = "https://${GIT_URL.replaceFirst(/(git@|http:\/\/|https:\/\/)/, '').replace(':', '/').replace('.git', '')}"
  env.GITHUB_BRANCH_URL = "${GITHUB_PROJECT_URL}/tree/${env.BRANCH_NAME}"
  env.GITHUB_COMMIT_URL = "${GITHUB_PROJECT_URL}/commit/${env.GIT_COMMIT}"

  env.MAVEN_HOME = "${env.HOME}/.mvn/apache-maven-3.6.0"
  env.PATH = "${env.MAVEN_HOME}/bin:${env.PATH}"
  env.JAVA_HOME = sh(label: 'Get JAVA_HOME',script: '''#!/bin/bash -le
    . ${JABBA_SHELL}
    jabba which ${JABBA_VERSION}''', returnStdout: true).trim()
  env.JAVA8_HOME = sh(label: 'Get JAVA8_HOME',script: '''#!/bin/bash -le
    . ${JABBA_SHELL}
    jabba which 1.8''', returnStdout: true).trim()


  sh label: 'Display Java and environment information',script: '''#!/bin/bash -le
    # Load CCM environment variables

    . ${JABBA_SHELL}
    jabba use ${JABBA_VERSION}

    java -version
    mvn -v
    printenv | sort
  '''
}

def buildQuarkusExtension(jabbaVersion) {
  withEnv(["BUILD_JABBA_VERSION=${jabbaVersion}"]) {
    sh label: 'Build driver', script: '''#!/bin/bash -le
      . ${JABBA_SHELL}
      jabba use ${BUILD_JABBA_VERSION}

      mvn -B -V install -DskipTests -Dmaven.javadoc.skip=true
    '''
  }
}

def executeTestsNative() {
  sh label: 'Execute tests Native', script: '''#!/bin/bash -le
    # Load CCM environment variables
    
    . ${JABBA_SHELL}
    jabba use ${GRAALVM_VERSION}

    if [ "${JABBA_VERSION}" != "1.8" ]; then
      SKIP_JAVADOCS=true
    else
      SKIP_JAVADOCS=false
    fi

    printenv | sort
    
    mvn -B -V verify -Dnative -Dmaven.javadoc.skip=${SKIP_JAVADOCS}
  '''
}

def executeCodeCoverage() {
  jacoco(
    execPattern: '**/target/jacoco.exec',
    classPattern: '**/classes',
    sourcePattern: '**/src/main/java'
  )
}

pipeline {
  agent none

  // Global pipeline timeout
  options {
    timeout(time: 30, unit: 'MINUTES')
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
        timeout(time: 30, unit: 'MINUTES')
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
          // Per-commit builds are only going to run against JDK8
          JABBA_VERSION = '1.8'
          GRAALVM_VERSION = 'graalvm@20.1.0'
        }

        stages {
          stage('Initialize-Environment') {
            steps {
              initializeEnvironment()
            }
          }

          stage('Build-Quarkus-Extension') {
            steps {
              buildQuarkusExtension(env.JABBA_VERSION)
            }
          }
          stage('Execute-Tests-Native') {
            steps {
              catchError {
                executeTestsNative()
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
        }
    }
  }
}