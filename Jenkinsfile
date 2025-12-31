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

        stage('Canary Deploy') {
            steps {
                withKubeConfig([credentialsId: "${KUBECONFIG_ID}"]) {
                    script {
                        echo "Canary 배포 시작: 기존(Blue) 4개 / 신규(Green) 1개로 트래픽 분산"
                        sh "./kubectl scale deployment my-calc-blue --replicas=4 -n metallb-system"
                        sh "./kubectl scale deployment my-calc-green --replicas=0 -n metallb-system"
                        sleep 5

                        sh "./kubectl scale deployment my-calc-green --replicas=1 -n metallb-system"
                        echo "카나리 버전(Green)이 투입되었습니다. 접속 테스트를 진행하세요!"
                        
                        sleep 15
                        
                        echo "테스트 통과! Green으로 전면 교체합니다."
                        sh "./kubectl scale deployment my-calc-green --replicas=4 -n metallb-system"
                        sh "./kubectl scale deployment my-calc-blue --replicas=0 -n metallb-system"
                    }
                }
            }
        }

    }
}
