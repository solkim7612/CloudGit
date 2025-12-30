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
                        // helm 대신 /var/jenkins_home/helm 이라고 정확한 주소를 적어줍니다.
                        sh '/var/jenkins_home/helm repo add metallb https://metallb.github.io/metallb'
                        sh '/var/jenkins_home/helm repo update'
                        
                        sh "kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -"
                        
                        // 여기도 주소 변경
                        sh "/var/jenkins_home/helm upgrade --install metallb metallb/metallb --namespace ${NAMESPACE} --wait"
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
