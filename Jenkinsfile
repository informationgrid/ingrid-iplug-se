pipeline {
    agent any

    options {
        // each branch has 1 job running at a time, since tests conflict with elasticsearch port otherwise
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '5'))
    }

    environment {
        VERSION = readMavenPom().getVersion()
    }

    stages {
        // normal build if it's not the master branch and not the support branch, except if it's a SNAPSHOT-version
        stage('Build-SNAPSHOT') {
            when {
                not { branch 'master' }
                not {
                    allOf {
                        branch 'support/*'
                        expression { return !VERSION.endsWith("-SNAPSHOT") }
                    }
                }
            }
            steps {

                script {
                    /*
                        Start an elasticsearch cluster in a docker container
                        Attention: we need to assign the correct network where jenkins was created in
                                   we also should use the IP mask for the port mapping to only allow
                                   access to the right containers
                    */
                    docker.image('docker-registry.wemove.com/ingrid-elasticsearch-with-decompound:6.4.2').withRun('--name "elasticsearch_iplug-se_test" -e "cluster.name=ingrid" -e "http.host=0.0.0.0" -e "transport.host=0.0.0.0" -e "xpack.security.enabled=false" -e "xpack.monitoring.enabled=false" -e "xpack.ml.enabled=false" --network jenkinsnexussonar_devnet -p 127.0.0.1:18325:9300 -p 127.0.0.1:18326:9200') { c ->

                        withMaven(
                            // Maven installation declared in the Jenkins "Global Tool Configuration"
                            maven: 'Maven3',
                            // Maven settings.xml file defined with the Jenkins Config File Provider Plugin
                            // Maven settings and global settings can also be defined in Jenkins Global Tools Configuration
                            mavenSettingsConfig: '2529f595-4ac5-44c6-8b4f-f79b5c3f4bae'
                        ) {

                            echo "Project version: $VERSION"

                            /* Wait max 1 minute until elasticsearch service is up */
                            timeout(1) {
                                sh script: "wget --retry-connrefused --tries=60 --waitretry=1 -q http://elasticsearch_iplug-se_test:9200 -O /dev/null", returnStatus: true
                            }

                            // Run the maven build
                            sh 'mvn clean deploy -PrequireSnapshotVersion,docker,docker-$GIT_BRANCH -Dmaven.test.failure.ignore=true'

                        } // withMaven will discover the generated Maven artifacts, JUnit Surefire & FailSafe & FindBugs reports...
                    }
                }
            }
        }
        // release build if it's the master or the support branch and is not a SNAPSHOT version
        stage ('Build-Release') {
            when {
                anyOf { branch 'master'; branch 'support/*' }
                expression { return !VERSION.endsWith("-SNAPSHOT") }
            }
            steps {
                script {
                    /*
                        Start an elasticsearch cluster in a docker container
                        Attention: we need to assign the correct network where jenkins was created in
                                   we also should use the IP mask for the port mapping to only allow
                                   access to the right containers
                    */
                    docker.image('docker-registry.wemove.com/ingrid-elasticsearch-with-decompound:6.4.2').withRun('--name "elasticsearch_iplug-se_test" -e "cluster.name=ingrid" -e "http.host=0.0.0.0" -e "transport.host=0.0.0.0" -e "xpack.security.enabled=false" -e "xpack.monitoring.enabled=false" -e "xpack.ml.enabled=false" --network jenkinsnexussonar_devnet -p 127.0.0.1:18325:9300 -p 127.0.0.1:18326:9200') { c ->

                        withMaven(
                            maven: 'Maven3',
                            mavenSettingsConfig: '2529f595-4ac5-44c6-8b4f-f79b5c3f4bae'
                        ) {
                            echo "Release: $VERSION"
                            // check license
                            // check is release version
                            // deploy to distribution
                            // send release email
                            sh 'mvn clean deploy -Pdocker,release'
                        }
                    }
                }
            }
        }
        stage ('SonarQube Analysis'){
            steps {
                withMaven(
                    maven: 'Maven3',
                    mavenSettingsConfig: '2529f595-4ac5-44c6-8b4f-f79b5c3f4bae'
                ) {
                    withSonarQubeEnv('Wemove SonarQube') {
                        sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.4.0.905:sonar'
                    }
                }
            }
        }
    }
    post {
        changed {
            // send Email with Jenkins' default configuration
            script { 
                emailext (
                    body: '${DEFAULT_CONTENT}',
                    subject: '${DEFAULT_SUBJECT}',
                    to: '${DEFAULT_RECIPIENTS}')
            }
        }
    }
}
