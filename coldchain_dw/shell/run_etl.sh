#!/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
HQL_DIR="${PROJECT_DIR}/hql"

HIVE_OPTS="--hiveconf hive.exec.mode.local.auto=false --hiveconf mapreduce.framework.name=yarn"

hive $HIVE_OPTS -f "${HQL_DIR}/03_create_dim.hql"
hive $HIVE_OPTS -f "${HQL_DIR}/04_ods_to_dwd.hql"
hive $HIVE_OPTS -f "${HQL_DIR}/05_dwd_to_dws.hql"
hive $HIVE_OPTS -f "${HQL_DIR}/06_dws_to_ads.hql"
hive $HIVE_OPTS -f "${HQL_DIR}/07_analysis.hql"
