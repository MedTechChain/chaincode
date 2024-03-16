#!/usr/bin/env bash

set -euo pipefail
: ${DEBUG:="false"}

if [ "${DEBUG}" = "true" ]; then
   exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:8000 -jar /app/chaincode.jar
else
   exec java -jar /app/chaincode.jar
fi