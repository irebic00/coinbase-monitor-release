# Coinbase Monitor

coinbase-monitor

- API version: 1.0.0

## Requirements
Prerequisites for this project:
- Docker
- Helm 3
- Minikube
- Maven
- Java 11
- Istio (istioctl tool)

All above tools need to be installed in your environment

## Getting Started
### Deployment
Follow these steps:
- In directory ./coinbase-monitor: mvn install
- Run from terminal: eval $(minikube docker-env)
- In directory ./coinbase-monitor/build: ./build-script -bv --package-helm-chart
- Continue using this terminal
- Deploy application: helm install test-release ./coinbase-monitor/charts/coinbase-monitor/<generated_chart_from_build_script> --create-namespace
- Deploy istio and corresponding ingress gatewy in directory ./istio-ingressgw: ./deploy_istio.sh

### Find NodePort:
- minikube service -n istio-system istio-ingressgateway --url
- kubectl get svc -n istio-system | grep ingressgateway 

Last step should list NodePort address and Service of ingressgw. Use address whose NodePort corresponds with istio service on port 443

## Documentation for API Endpoints


No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)


*Automatically generated by the [OpenAPI Generator](https://openapi-generator.tech)*

All URIs are relative to *http://127.0.0.1/*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------

## Author
Ivan Viseslav Rebic

