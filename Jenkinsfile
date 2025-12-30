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
                        // 1. Helm이 없으면 현재 작업 폴더에 다운로드 (가장 확실한 방법!)
                        sh 'curl -LO https://get.helm.sh/helm-v3.13.2-linux-amd64.tar.gz'
                        sh 'tar -zxvf helm-v3.13.2-linux-amd64.tar.gz'
                        
                        // 2. 압축 푼 폴더(linux-amd64) 안에 있는 helm 실행파일 사용
                        // (점 slash ./ 를 사용하여 현재 위치의 파일을 실행)
                        sh './linux-amd64/helm repo add metallb https://metallb.github.io/metallb'
                        sh './linux-amd64/helm repo update'
                        
                        // 3. 네임스페이스 생성
                        sh "kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -"
                        
                        // 4. MetalLB 설치 (경로 주의: ./linux-amd64/helm)
                        sh "./linux-amd64/helm upgrade --install metallb metallb/metallb --namespace ${NAMESPACE} --wait"
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
