pipeline {
    agent {
        docker {
            registryUrl 'https://us-docker.pkg.dev'
            image 'verdant-bulwark-278/bzm-plugin-base-image/bzm-plugin-base-image:latest'
            registryCredentialsId 'push-to-gar-enc'
            args '-u root -v /var/run/docker.sock:/var/run/docker.sock -v $WORKSPACE:/build'
        }
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: "10"))
        ansiColor('xterm')
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        NEXUS_STAGING_CRED = credentials('blazerunner_nexus_staging_creds')
        API_CLIENT_CRED = credentials('blazerunner_api_client_creds')
        GPG_PASSPHRASE = credentials('blazerunner_api_client_gpg_passphrase')
    }

    stages {
        stage('Build API Client') {
            steps {
                script {
                    sh'''
                      sed 's/NEXUS-STAGING-USERNAME/${NEXUS_STAGING_CRED_USR}/' settings.xml
                      sed 's/NEXUS-STAGING-PASSWORD/${NEXUS_STAGING_CRED_PSW}/' settings.xml
                      sed 's/API-CLIENT-USERNAME/${API_CLIENT_CRED_USR}/' settings.xml
                      sed 's/API-CLIENT-PASSWORD/${API_CLIENT_CRED_PSW}/' settings.xml
                      cp settings.xml ~/.m2/settings.xml
                      '''
                }
            }
        }
        stage('Generating GPG Key') {
            steps {
                script {
                    sh'''
                    cat >gpgkey <<EOF
                    %echo Generating a basic OpenPGP key
                    Key-Type: DSA
                    Key-Length: 1024
                    Subkey-Type: ELG-E
                    Subkey-Length: 1024
                    Name-Real: Blazemeter
                    Name-Comment: Blazemeter PGP Key with Passphrase
                    Name-Email: sat@blazemeter.com
                    Expire-Date: 0
                    Passphrase: ${GPG_PASSPHRASE}
                    %commit
                    %echo done
                EOF
                    gpg --batch --generate-key gpgkey
                    KEY=`gpg --list-keys | grep Blazemeter -B 1 | head -1`
                    gpg --keyserver keyserver.ubuntu.com --send-keys $KEY
                    '''
                }
            }
        }
        stage('Release API Client') {
            steps {
                script {
                    sh'''
                    mvn release:prepare release:perform
                    echo "Client URL: https://repo1.maven.org/maven2/com/blazemeter/blazemeter-api-client/"
                    '''
                }
            }
        }
    }
}
