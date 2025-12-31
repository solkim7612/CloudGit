pipeline {
    agent any

    tools {
        jdk 'jdk11' 
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Permission Grant') {
            steps {
                script {
                    sh 'chmod +x gradlew'
                }
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    sh './gradlew clean build'
                }
            }
        }
    }

    post {
        always {
            junit '**/build/test-results/test/*.xml'
        }
        success {
            echo '빌드와 테스트가 성공했습니다! '
        }
        failure {
            echo '테스트에 실패했습니다. 코드를 확인하세요. '
        }
    }
}
