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

        stage('Blue/Green Deploy') {
            steps {
                withKubeConfig([credentialsId: "${KUBECONFIG_ID}"]) {
                    script {
                        def current_color = sh(script: "kubectl get svc my-calc-service -n metallb-system -o jsonpath='{.spec.selector.color}'", returnStdout: true).trim()
                        
                        echo "현재 활성화된 색상: ${current_color}"
                        
                        def target_color = (current_color == 'blue') ? 'green' : 'blue'
                        echo "배포할 목표 색상: ${target_color}"
                        
                        sh "./kubectl rollout restart deployment/my-calc-${target_color} -n metallb-system"
                        
                        sh "./kubectl rollout status deployment/my-calc-${target_color} -n metallb-system"
                        
                        sleep 5 
                        
                        sh "./kubectl patch service my-calc-service -n metallb-system -p '{\"spec\":{\"selector\":{\"color\":\"${target_color}\"}}}'"
                        
                        echo "배포 완료! 서비스가 [${current_color}] -> [${target_color}] 로 전환되었습니다."
                    }
                }
            }
        }      

    }
}
