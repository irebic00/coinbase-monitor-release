{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "istio-config.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart version as used by the chart label.
*/}}
{{- define "istio-config.version" -}}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}