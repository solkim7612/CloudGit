pipeline {
    agent any

    tools {
        jdk 'jdk11'
    }
    
    environment {
        // 1. 젠킨스에 등록한 Docker Hub Credential ID
        DOCKER_CRED = credentials('dockerhub-id')
        // 2. 환경변수 매핑 (Jib가 가져다 씀)
        DOCKER_USER = "${DOCKER_CRED_USR}"
        DOCKER_PASS = "${DOCKER_CRED_PSW}"
        
        KUBECONFIG_ID = 'kubeconfig-id'
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
                    // kubectl 권한 설정 (지난번과 동일)
                    sh 'curl -LO https://dl.k8s.io/release/v1.28.4/bin/linux/amd64/kubectl'
                    sh 'chmod +x kubectl'
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

        stage('Build Image & Push') {
            steps {
                script {
                    sh './gradlew jib'
                }
            }
        }

        stage('Deploy to K8s') {
            steps {
                withKubeConfig([credentialsId: "${KUBECONFIG_ID}"]) {
                    script {
                        sh "./kubectl apply -f deploy.yaml"
                        sh "./kubectl rollout restart deployment/my-calc-app -n metallb-system"
                    }
                }
            }
        }
    }
}
