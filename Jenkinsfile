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
                        echo "카나리 배포 시작 (Blue는 유지, Green 투입)"
                        
                        sh "./kubectl scale deployment my-calc-green --replicas=0 -n metallb-system"
                        sh "./kubectl rollout restart deployment/my-calc-green -n metallb-system"
                        
                        echo "--> Green(Purple) 1개를 투입합니다. (Blue 1개 vs Green 1개)"
                        sh "./kubectl scale deployment my-calc-green --replicas=1 -n metallb-system"
                        
                        echo "--> 60초 동안 트래픽이 섞입니다. 터미널을 확인하세요!"
                        sleep 60
                        
                        echo "--> Green으로 전면 교체합니다."
                        sh "./kubectl scale deployment my-calc-green --replicas=1 -n metallb-system"
                        sh "./kubectl scale deployment my-calc-blue --replicas=0 -n metallb-system"
                    }
                }
            }
        }

    }
    
    post {
        success {
            script {
                echo "[SUCCESS] 배포가 성공적으로 완료되었습니다!"
                echo "빌드 번호: ${env.BUILD_NUMBER}"
                echo "결과 확인: ${env.BUILD_URL}"
            }
        }
        failure {
            script {
                echo "[FAILURE] 배포 실패! 긴급 점검이 필요합니다."
                echo "빌드 번호: ${env.BUILD_NUMBER}"
                echo "로그 확인: ${env.BUILD_URL}console"
            }
        }
    }

}
