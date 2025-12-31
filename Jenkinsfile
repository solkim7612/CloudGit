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
                        echo "🚀 카나리 배포 시작!"
                        
                        // 1. Blue는 건드리지 않음 (이미 구버전 4개가 떠 있다고 가정)
                        // 만약 Blue가 0개라면 여기서 scale up을 하면 안됨 (최신 이미지를 받아버리므로)
                        
                        // 2. Green(신버전) 초기화 및 이미지 업데이트
                        sh "./kubectl scale deployment my-calc-green --replicas=0 -n metallb-system"
                        // Green이 최신 이미지를 가져오도록 강제 재시작
                        sh "./kubectl rollout restart deployment/my-calc-green -n metallb-system"
                        
                        // 3. 카나리 투입 (Green 1개 = 약 20% 트래픽)
                        echo "--> Green(Canary) 1개를 투입합니다..."
                        sh "./kubectl scale deployment my-calc-green --replicas=1 -n metallb-system"
                        
                        // 4. 관찰 시간 (20초 동안 섞이는지 확인하세요!)
                        sleep 20
                        
                        // 5. 배포 확정 (Green을 메인으로)
                        echo "--> 테스트 통과! Green으로 전면 교체합니다."
                        sh "./kubectl scale deployment my-calc-green --replicas=4 -n metallb-system"
                        sh "./kubectl scale deployment my-calc-blue --replicas=0 -n metallb-system"
                    }
                }
            }
        }

    }
}
