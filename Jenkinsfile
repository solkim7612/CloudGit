pipeline {
    agent any

    environment {
        KUBECONFIG_ID = 'kubeconfig-id'
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
                        // 1. Helm 다운로드 및 준비 (성공했던 부분)
                        sh 'curl -LO https://get.helm.sh/helm-v3.13.2-linux-amd64.tar.gz'
                        sh 'tar -zxvf helm-v3.13.2-linux-amd64.tar.gz'
                        
                        // 2. [추가됨] kubectl 다운로드 및 권한 부여
                        // (안정적인 v1.28.4 버전을 받습니다)
                        sh 'curl -LO https://dl.k8s.io/release/v1.28.4/bin/linux/amd64/kubectl'
                        sh 'chmod +x kubectl'

                        // 3. Helm 저장소 추가 (경로 ./linux-amd64/helm)
                        sh './linux-amd64/helm repo add metallb https://metallb.github.io/metallb'
                        sh './linux-amd64/helm repo update'
                        
                        // 4. 네임스페이스 생성 (경로 ./kubectl 로 변경!)
                        sh "./kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | ./kubectl apply -f -"
                        
                        // 5. MetalLB 설치 (경로 ./linux-amd64/helm)
                        sh "./linux-amd64/helm upgrade --install metallb metallb/metallb --namespace ${NAMESPACE} --wait"
                    }
                }
            }
        }

        stage('Configure IP Pool') {
            steps {
                withKubeConfig([credentialsId: "${KUBECONFIG_ID}"]) {
                    script {
                        // 6. IP 설정 (여기서도 ./kubectl 사용 필수!)
                        // 주의: IP 대역(192.168.1.240-250)은 본인 환경에 맞게 확인하셨죠?
                        sh """
cat <<EOF | ./kubectl apply -f -
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
        
        stage('Verification') {
            steps {
                withKubeConfig([credentialsId: "${KUBECONFIG_ID}"]) {
                    script {
                         // 검증 단계에서도 ./kubectl 사용
                        echo "=== MetalLB Pod Status ==="
                        sh "./kubectl get pods -n ${NAMESPACE}"
                    }
                }
            }
        }
    }
}
