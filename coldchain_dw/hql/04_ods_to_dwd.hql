USE dwd_coldchain;

DROP TABLE IF EXISTS dwd_temp_detail;
CREATE TABLE IF NOT EXISTS dwd_temp_detail (
    device_id           INT,
    event_time          STRING,
    dt                  STRING,
    hour                INT,
    weekday             INT,
    is_weekend          INT,
    location            STRING,
    location_type       STRING,
    coldchain_temp      DOUBLE,
    humidity            DOUBLE,
    battery             INT,
    status              STRING,
    sensor_type         STRING,
    firmware_version    STRING,
    error_code          STRING,
    is_overheat         INT,
    is_overcold         INT,
    final_alert         INT,
    alert_type          STRING,
    temp_level          STRING,
    temp_deviation      DOUBLE
)
PARTITIONED BY (pt STRING)
STORED AS TEXTFILE;

SET hive.exec.dynamic.partition=true;
SET hive.exec.dynamic.partition.mode=nonstrict;

INSERT OVERWRITE TABLE dwd_temp_detail PARTITION (pt)
SELECT
    o.device_id,
    o.event_time,
    o.dt,
    o.hour,
    o.weekday,
    o.is_weekend,
    o.location,
    COALESCE(l.location_type, '其他') AS location_type,
    o.coldchain_temp,
    o.humidity,
    o.battery,
    o.status,
    o.sensor_type,
    o.firmware_version,
    o.error_code,
    o.is_overheat,
    o.is_overcold,
    o.final_alert,
    o.alert_type,
    o.temp_level,
    ROUND(o.coldchain_temp - (-5.0), 2) AS temp_deviation,
    o.dt AS pt
FROM ods_coldchain.ods_sensor_log o
LEFT JOIN dim_coldchain.dim_location l
    ON o.location = l.location_name
WHERE o.device_id IS NOT NULL
  AND o.coldchain_temp IS NOT NULL;

SELECT pt, COUNT(*) AS cnt, SUM(final_alert) AS alert_cnt
FROM dwd_temp_detail
GROUP BY pt
ORDER BY pt
LIMIT 20;
