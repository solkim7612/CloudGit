pipeline {
    agent any

    environment {
        KUBECONFIG_ID = 'kubeconfig-id' // 아까 만든 Credential ID
        NAMESPACE = 'metallb-system'
    }

    stages {
        stage('Clone Repository') {
            steps {
                checkout scm
            }
        }

        stage('Install MetalLB') {
            steps {
                withKubeConfig([credentialsId: "${KUBECONFIG_ID}"]) {
                    script {
                        // 1. MetalLB 저장소 추가
                        sh 'helm repo add metallb https://metallb.github.io/metallb'
                        sh 'helm repo update'
                        
                        // 2. 네임스페이스 생성 및 설치
                        sh "kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -"
                        sh "helm upgrade --install metallb metallb/metallb --namespace ${NAMESPACE} --wait"
                    }
                }
            }
        }

        stage('Configure IP Pool') {
            steps {
                withKubeConfig([credentialsId: "${KUBECONFIG_ID}"]) {
                    script {
                        // 3. IP 대역 설정 (L2 모드)
                        sh """
cat <<EOF | kubectl apply -f -
apiVersion: metallb.io/v1beta1
kind: IPAddressPool
metadata:
  name: first-pool
  namespace: ${NAMESPACE}
spec:
  addresses:
  - 192.168.1.240-192.168.1.250
---
apiVersion: metallb.io/v1beta1
kind: L2Advertisement
metadata:
  name: example
  namespace: ${NAMESPACE}
EOF
                        """
                    }
                }
            }
        }
    }
}
