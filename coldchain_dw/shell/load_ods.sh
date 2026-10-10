#!/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
DATA_FILE="${PROJECT_DIR}/data/iot_coldchain_clean.tsv"
HDFS_TMP="/tmp/iot_coldchain_clean.tsv"
HDFS_ODS="/data_warehouse/ods_coldchain/ods_sensor_log"

if [ ! -f "${DATA_FILE}" ]; then
    echo "no tsv: ${DATA_FILE}"
    exit 1
fi

wc -l "${DATA_FILE}"

hdfs dfs -mkdir -p "${HDFS_ODS}"
hdfs dfs -mkdir -p /tmp
hdfs dfs -put -f "${DATA_FILE}" "${HDFS_TMP}"

hive -f "${PROJECT_DIR}/hql/01_create_databases.hql"
hive -f "${PROJECT_DIR}/hql/02_create_ods.hql"

hive -e "
USE ods_coldchain;
LOAD DATA INPATH '${HDFS_TMP}' OVERWRITE INTO TABLE ods_sensor_log;
SELECT COUNT(*) AS ods_cnt FROM ods_sensor_log;
SELECT * FROM ods_sensor_log LIMIT 3;
"
