#!/usr/bin/env bash

set -e -u -x

current_directory="$(
  cd "$(dirname "$0")" >/dev/null 2>&1
  pwd -P
)/.."

find_jar() {
  find "${current_directory}/target" -type f -name "*.jar" ! -name "original*" -maxdepth 1 -print0 | while read -r -d $'\0' filename; do
    echo "${filename}"
    break
  done
}

jar=$(find_jar)
if [[ -z ${jar} || $(find "${jar}" -mmin +1 -print) ]]; then
  mvn -f "${current_directory}/pom.xml" clean install
fi

jar=$(find_jar)

java -jar "${jar}" \
  --mqtt-hostname "${MQTT_HOSTNAME}" \
  --tomorrow-io-api-key "${TOMORROW_IO_API_KEY}" \
  --influxdb-hostname "${INFLUXDB_HOSTNAME}" \
  --influxdb-token "${INFLUXDB_API_KEY}" \
  --influxdb-org "${INFLUXDB_ORG}" \
  --influxdb-bucket "${INFLUXDB_BUCKET}" \
  --weather-location "${WEATHER_LOCATION}" \
  "$@"
