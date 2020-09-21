#!/bin/bash -e

if test -z "${ISTIO_NAMESPACE}"
then
  echo "ISTIO_NAMESPACE is empty, using default istio-system"
  ISTIO_NAMESPACE=istio-system
else
  echo "ISTIO_NAMESPACE is ${ISTIO_NAMESPACE}"
fi

if !(kubectl get ns ${ISTIO_NAMESPACE}); then
  echo "istio not deployed, deploying..."
  istioctl install --skip-confirmation
  if [ $? -ne 0 ]; then
    echo "istio not installed on cluster or istioctl not installed. error"
    return 1
  fi
fi

if !(kubectl get secret -n ${ISTIO_NAMESPACE} tls-credential); then
  ./generate.sh cleanup
  ./generate.sh *.default.svc.cluster.local password

  kubectl create -n ${ISTIO_NAMESPACE} secret tls tls-credential \
  --key=3_application/private/\*.default.svc.cluster.local.key.pem --cert=3_application/certs/\*.default.svc.cluster.local.cert.pem
fi

cat <<EOF | kubectl apply --overwrite -f -
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: istio-ingressgateway
  namespace: ${ISTIO_NAMESPACE}
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

cat <<EOF | kubectl apply --overwrite -f -
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: istio-virtualservice
  namespace: ${ISTIO_NAMESPACE}
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
