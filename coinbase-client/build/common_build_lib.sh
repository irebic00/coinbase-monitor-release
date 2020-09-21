LOCAL_DOCKER_REGISTRY=localhost:5000/proj-coinbase
MICROSERVICE_NAME=
SERVICE_VERSION_TEMPLATE="%SERVICE_VERSION%"

TEST_RELEASE_NAME=""
TEST_NAMESPACE_NAME=""

# This check is here to prevent sourcing of the script if the repo root directory is not set
if [ -z ${REPOSITORY_ROOT_DIR} ]; then
  echo "[ERROR] REPOSITORY_ROOT_DIR variable not set"
  exit 1
fi

#####################################################
# Finds last commit id in this git rep..
# Result will be in COMMIT_ID variable
#####################################################
function find_last_commit_id() {
    git version &>/dev/null
    if [ $? -eq 0 ]; then
      COMMIT_ID=$(git rev-parse --short HEAD)
    else
      echo "[ERROR] Not in a git repo or git is not installed. Exiting..."
      exit 1
    fi
    echo "[INFO] Last commit ID is ${COMMIT_ID}"
}

#####################################################
# Reads VERSION file which should be at the top of the git repository to find microservice version.
# Result will be in MICROSERVICE_VERSION variable
#####################################################
function find_microservice_version() {
    local ms_version_string

    ms_version_string=$(grep microservice_version "${REPOSITORY_ROOT_DIR}/VERSION")
    if [ $? -ne 0 ]; then
      echo "[ERROR] Failed to read microservice version from ${REPOSITORY_ROOT_DIR}/VERSION"
      exit 1
    fi

    MICROSERVICE_VERSION=$(echo ${ms_version_string} | cut -d ' ' -f 2)
    echo "[INFO] Current microservice version is ${MICROSERVICE_VERSION}"
}



#####################################################
# Checks that all Dockerfiles are following best practices for
# writing Dockerfiles.
#####################################################
function dockerfile_linter() {
    echo "" && echo "[INFO] Linting Dockerfiles"
    docker inspect hadolint/hadolint &>/dev/null

    OLD_PWD=$(pwd)
    cd ${REPOSITORY_ROOT_DIR}
    DOCKERFILES=$(find ./ -iname "*Dockerfile*")
    WARNINGS_FOUND=false
    for DOCKERFILE in ${DOCKERFILES}
    do
      docker run --rm -i hadolint/hadolint < ${DOCKERFILE}
      [[ "$?" -ne 0 ]] && echo "[WARN] ${DOCKERFILE} did not follow above rules" \
      && echo "" && WARNINGS_FOUND=true
    done
    echo ""
    cd ${OLD_PWD}
    if ${WARNINGS_FOUND}; then
      echo "[ERROR] Fix above listed warnings in Dockerfiles to proceed"
      exit 1
    fi
}

#####################################################
# Reads VERSION file to find microservice name in case micrservice repository does not have a Helm chart
# Result will be in MICROSERVICE_NAME variable
#####################################################
function find_microservice_name_from_version_file() {
    local ms_name_string

    ms_name_string=$(grep microservice_name ${REPOSITORY_ROOT_DIR}/VERSION)
    if [ $? -ne 0 ]; then
      echo "[ERROR] Failed to read microservice name from ${REPOSITORY_ROOT_DIR}/VERSION"
      exit 1
    fi

    MICROSERVICE_NAME=$(echo ${ms_name_string} | cut -d ' ' -f 2)
    echo "[INFO] Microservice name is ${MICROSERVICE_NAME}"
}

#####################################################
# Reads Chart.yaml file in microservice chart directory and finds microservice name
# Result will be in MICROSERVICE_NAME variable
#####################################################
function find_microservice_name_from_chart() {
    local ms_name_string

    ms_name_string=$(grep name ${REPOSITORY_ROOT_DIR}/charts/*/Chart.yaml)
    if [ $? -ne 0 ]; then
      echo "[ERROR] Failed to read microservice name from chart ${REPOSITORY_ROOT_DIR}/charts/*/Chart.yaml"
      exit 1
    fi

    MICROSERVICE_NAME=$(echo ${ms_name_string} | cut -d ' ' -f 2)
    echo "[INFO] Microservice name is ${MICROSERVICE_NAME}"
}

#####################################################
# This function sets some common variables that are used by microservice build scripts.
# Provides: COMMIT_ID, MICROSERVICE_VERSION, MICROSERVICE_TAG, MICROSERVICE_NAME, MICROSERVICE_CHART_DIR
#
# This function can take argument no_chart. If that argument is provided to function, that will mean that this
# repository does not have a Helm chart associated with it and microservice name will be taken from VERSION file
#####################################################
function set_default_microservice_variables() {
  find_last_commit_id
  find_microservice_version

  MICROSERVICE_PRODUCTION_TAG="${MICROSERVICE_VERSION}"
  if [ ${USE_PRODUCTION_TAGS} -eq 0 ]; then
    MICROSERVICE_TAG="${MICROSERVICE_VERSION}-${TEST_USER_ID}.${COMMIT_ID}"
  else
    MICROSERVICE_TAG="${MICROSERVICE_VERSION}"
  fi
  echo "[INFO] Microservice tag is ${MICROSERVICE_TAG}"
  
  if [ "$1" == "no_chart" ]; then
    find_microservice_name_from_version_file
    echo "[INFO] This repository has no Helm chart"
  else
    find_microservice_name_from_chart
    MICROSERVICE_CHART_DIR="${REPOSITORY_ROOT_DIR}/charts/${MICROSERVICE_NAME}"
    echo "[INFO] Default microservice chart directory is ${MICROSERVICE_CHART_DIR}"
  fi
  MICROSERVICE_NAME_UPPERCASE=$(echo ${MICROSERVICE_NAME} | awk '{print toupper($0)}')
  MICROSERVICE_RELEASE_TAG="${MICROSERVICE_NAME_UPPERCASE}_${MICROSERVICE_PRODUCTION_TAG}_REL"
  echo "[INFO] Microservice release tag is ${MICROSERVICE_RELEASE_TAG}"
}

#####################################################
# Packages microservice chart directory.
# Removes index.yaml and any old chart from the directory before packaging.
# Name of the chart van be passed into function.
# If argument is passed chart name and chart directory will be set to that.
#####################################################
function package_microservice_chart() {
    if [ ! -z $1 ]; then
      MICROSERVICE_CHART_DIR="${REPOSITORY_ROOT_DIR}/charts/$1"
      echo "[INFO] Chart directory is ${MICROSERVICE_CHART_DIR}"
    fi
    
    if [ -f ${MICROSERVICE_CHART_DIR}/index.yaml ]; then
      echo "[INFO] Removing index.yaml file"
      rm -rf ${MICROSERVICE_CHART_DIR}/index.yaml
    fi
    
    find ${MICROSERVICE_CHART_DIR}/*.tgz &>/dev/null
    if [ $? -eq 0 ]; then
      echo "[INFO] Removing *.tgz files from ${MICROSERVICE_CHART_DIR}"
      rm -rf ${MICROSERVICE_CHART_DIR}/*.tgz
    fi
    
    if [ -f "${MICROSERVICE_CHART_DIR}/values.yaml" ]; then
      echo "[INFO] Modifying Helm chart values template"
      sed -i -e "s/${SERVICE_VERSION_TEMPLATE}/${MICROSERVICE_TAG}/g" "${MICROSERVICE_CHART_DIR}/values.yaml"
    fi
    sed -i -e "s/${SERVICE_VERSION_TEMPLATE}/${MICROSERVICE_TAG}/g" "${MICROSERVICE_CHART_DIR}/Chart.yaml"
    
    if [ -f "${MICROSERVICE_CHART_DIR}/requirements.yaml" ]; then
      echo "[INFO] This chart directory has requirements file. Running dependency update"
      helm dep update ${MICROSERVICE_CHART_DIR}
      if [ $? -ne 0 ]; then
        echo "[ERROR] Failed to update dependencies"
        if [ -f ${MICROSERVICE_CHART_DIR} ]; then
          echo "[INFO] Restoring Helm chart values template"
          sed -i -e "s/${MICROSERVICE_TAG}/${SERVICE_VERSION_TEMPLATE}/g" "${MICROSERVICE_CHART_DIR}/values.yaml"
        fi
        sed -i -e "s/${MICROSERVICE_TAG}/${SERVICE_VERSION_TEMPLATE}/g" "${MICROSERVICE_CHART_DIR}/Chart.yaml"
        exit 1
      fi
    fi

    helm package ${MICROSERVICE_CHART_DIR} -d ${MICROSERVICE_CHART_DIR}
    if [ $? -ne 0 ]; then
      echo "[ERROR] Failed to package Helm chart"
      if [ -f ${MICROSERVICE_CHART_DIR} ]; then
        echo "[INFO] Restoring Helm chart values template"
        sed -i -e "s/${MICROSERVICE_TAG}/${SERVICE_VERSION_TEMPLATE}/g" "${MICROSERVICE_CHART_DIR}/values.yaml"
      fi
      sed -i -e "s/${MICROSERVICE_TAG}/${SERVICE_VERSION_TEMPLATE}/g" "${MICROSERVICE_CHART_DIR}/Chart.yaml"
      exit 1
    fi

    if [ -f ${MICROSERVICE_CHART_DIR}/values.yaml ]; then
      echo "[INFO] Restoring Helm chart values template"
      sed -i -e "s/${MICROSERVICE_TAG}/${SERVICE_VERSION_TEMPLATE}/g" "${MICROSERVICE_CHART_DIR}/values.yaml"
    fi
    sed -i -e "s/${MICROSERVICE_TAG}/${SERVICE_VERSION_TEMPLATE}/g" "${MICROSERVICE_CHART_DIR}/Chart.yaml"
}