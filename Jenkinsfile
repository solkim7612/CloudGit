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
                    sh './gradlew clean build -x test --no-daemon -Dorg.gradle.jvmargs="-Xmx512m"'
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
                        echo "🚀 카나리 배포 시작 (Blue는 유지, Green 투입)"
                        
                        // 1. Green 초기화 및 이미지 업데이트
                        sh "./kubectl scale deployment my-calc-green --replicas=0 -n metallb-system"
                        sh "./kubectl rollout restart deployment/my-calc-green -n metallb-system"
                        
                        // 2. 카나리 투입 (Green 1개)
                        echo "--> Green(Purple) 1개를 투입합니다. (Blue 1개 vs Green 1개)"
                        sh "./kubectl scale deployment my-calc-green --replicas=1 -n metallb-system"
                        
                        // [수정] 대기 시간을 60초로 늘림! (충분히 관찰하세요)
                        echo "--> 60초 동안 트래픽이 섞입니다. 터미널을 확인하세요!"
                        sleep 60
                        
                        // 3. 배포 확정
                        echo "--> Green으로 전면 교체합니다."
                        sh "./kubectl scale deployment my-calc-green --replicas=1 -n metallb-system"
                        sh "./kubectl scale deployment my-calc-blue --replicas=0 -n metallb-system"
                    }
                }
            }
        }

    }
}
