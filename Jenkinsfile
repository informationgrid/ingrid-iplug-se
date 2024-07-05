pipeline {
    agent any
    triggers{ cron( getCronParams() ) }

    tools {
        jdk 'jdk17'
    }

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
                not { buildingTag() }
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
                    docker.image('docker.elastic.co/elasticsearch/elasticsearch:8.14.1').withRun('--name "elasticsearch_iplug-se_test" -e "ES_JAVA_OPTS=-Xms1536m -Xmx1536m" -e "cluster.name=ingrid" -e "discovery.type=single-node" -e "http.host=0.0.0.0" -e "transport.host=0.0.0.0" -e "xpack.security.enabled=false" --network jenkins-nexus-sonar_devnet -p 127.0.0.1:18326:9200') { c ->

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
                not { buildingTag() }
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
                    docker.image('docker.elastic.co/elasticsearch/elasticsearch:8.14.1').withRun('--name "elasticsearch_iplug-se_test" -e "ES_JAVA_OPTS=-Xms1536m -Xmx1536m" -e "cluster.name=ingrid" -e "discovery.type=single-node" -e "http.host=0.0.0.0" -e "transport.host=0.0.0.0" -e "xpack.security.enabled=false" --network jenkins-nexus-sonar_devnet -p 127.0.0.1:18326:9200') { c ->

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

        stage('Stage Tag Building') {
            when { buildingTag() }
            steps {
                script {
                    if (env.BUILD_NUMBER == '1') {
                        env.EXTRA_TAG = "${env.TAG_NAME}-release"
                    } else {
                        env.EXTRA_TAG = "${env.TAG_NAME}"
                    }
                }
                withMaven(
                    maven: 'Maven3',
                    mavenSettingsConfig: '2529f595-4ac5-44c6-8b4f-f79b5c3f4bae'
                ) {
                    echo "Scheduled Release: $VERSION"
                    // only build and create docker image
                    // use release tag if build number == 1
                    sh "mvn package docker:build -DpushImageTag -DdockerImageTags=${env.EXTRA_TAG} -Pdocker -DskipTests"
                }
            }
        }

        stage ('SonarQube Analysis'){
            when { branch 'develop' }
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


def getCronParams() {
    String tagTimestamp = env.TAG_TIMESTAMP
    long diffInDays = 0
    if (tagTimestamp != null) {
        long diff = "${currentBuild.startTimeInMillis}".toLong() - "${tagTimestamp}".toLong()
        diffInDays = diff / (1000 * 60 * 60 * 24)
        echo "Days since release: ${diffInDays}"
    }

    def versionMatcher = /\d\.\d\.\d(.\d)?/
    if( env.TAG_NAME ==~ versionMatcher && diffInDays < 180) {
        // every Sunday between midnight and 6am
        return 'H H(0-6) * * 0'
    }
    else {
        return ''
    }
}
