OLD_PWD=$(pwd)
cd certificate-generator

kubectl get secret -n istio-system tls-credential &>/dev/null
if [ $? -ne 0 ]; then
  ./generate.sh cleanup
  ./generate.sh *.default.svc.cluster.local password

  kubectl create -n istio-system secret tls tls-credential \
  --key=3_application/private/\*.default.svc.cluster.local.key.pem --cert=3_application/certs/\*.default.svc.cluster.local.cert.pem
fi


kubectl get ns istio-system &>/dev/null
if [ $? -ne 0 ]; then
  echo "istio not deployed, deploying..."
  istioctl istall
  if [ $? -ne 0 ]; then
    echo "istio not installed on cluster or istioctl not installed. error"
    return 1
  fi
fi

cd ${OLD_PWD}


cat <<EOF | kubectl apply -f -
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: istio-ingressgateway
  namespace: istio-system
spec:
  selector:
    istio: ingressgateway # use istio default ingress gateway
  servers:
  - port:
      number: 443
      name: https
      protocol: HTTPS
    tls:
      mode: SIMPLE
      credentialName: tls-credential
    hosts:
    - '*'
EOF

cat <<EOF | kubectl apply -f -
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: istio-virtualservice
  namespace: istio-system
spec:
  hosts:
  - "*"
  gateways:
  - istio-ingressgateway
  http:
  - match:
      - uri:
          prefix: /
    retries:
      attempts: 0
    route:
    - destination:
        port:
          number: 8080
        host: coinbase-monitor.default.svc.cluster.local
  - match:
      - uri:
          regex: ^/subscription/[A-Fa-f0-9]{64}$
      - uri:
          exact: /subscription
      - uri:
          exact: /websocket/coinbase
    retries:
      attempts: 0
    route:
    - destination:
        port:
          number: 8080
        host: coinbase-monitor.default.svc.cluster.local
EOF

minikube service -n istio-system istio-ingressgateway --url
kubectl get svc -n istio-system | grep ingressgateway
