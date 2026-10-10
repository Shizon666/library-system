USE ods_coldchain;

DROP TABLE IF EXISTS ods_sensor_log;
CREATE EXTERNAL TABLE IF NOT EXISTS ods_sensor_log (
    device_id           INT,
    event_time          STRING,
    dt                  STRING,
    hour                INT,
    weekday             INT,
    is_weekend          INT,
    coldchain_temp      DOUBLE,
    temperature         DOUBLE,
    humidity            DOUBLE,
    battery             INT,
    signal_strength     INT,
    status              STRING,
    location            STRING,
    sensor_type         STRING,
    firmware_version    STRING,
    error_code          STRING,
    is_alert            INT,
    is_overheat         INT,
    is_overcold         INT,
    is_temp_alert       INT,
    final_alert         INT,
    alert_type          STRING,
    temp_level          STRING,
    energy_kwh          DOUBLE,
    vibration           DOUBLE,
    pressure            DOUBLE
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\t'
STORED AS TEXTFILE
LOCATION '/data_warehouse/ods_coldchain/ods_sensor_log';

SELECT COUNT(*) AS cnt FROM ods_sensor_log;
SELECT * FROM ods_sensor_log LIMIT 5;
