USE dws_coldchain;

DROP TABLE IF EXISTS dws_temp_day_location;
CREATE TABLE IF NOT EXISTS dws_temp_day_location (
    dt                  STRING,
    location            STRING,
    location_type       STRING,
    record_cnt          BIGINT,
    avg_temp            DOUBLE,
    max_temp            DOUBLE,
    min_temp            DOUBLE,
    alert_cnt           BIGINT,
    overheat_cnt        BIGINT,
    overcold_cnt        BIGINT,
    alert_rate          DOUBLE
) STORED AS TEXTFILE;

INSERT OVERWRITE TABLE dws_temp_day_location
SELECT
    dt,
    location,
    MAX(location_type) AS location_type,
    COUNT(*) AS record_cnt,
    ROUND(AVG(coldchain_temp), 2) AS avg_temp,
    ROUND(MAX(coldchain_temp), 2) AS max_temp,
    ROUND(MIN(coldchain_temp), 2) AS min_temp,
    SUM(final_alert) AS alert_cnt,
    SUM(is_overheat) AS overheat_cnt,
    SUM(is_overcold) AS overcold_cnt,
    ROUND(SUM(final_alert) * 1.0 / COUNT(*), 4) AS alert_rate
FROM dwd_coldchain.dwd_temp_detail
GROUP BY dt, location;

DROP TABLE IF EXISTS dws_temp_day_device;
CREATE TABLE IF NOT EXISTS dws_temp_day_device (
    dt                  STRING,
    device_id           INT,
    record_cnt          BIGINT,
    avg_temp            DOUBLE,
    alert_cnt           BIGINT,
    alert_rate          DOUBLE,
    avg_battery         DOUBLE
) STORED AS TEXTFILE;

INSERT OVERWRITE TABLE dws_temp_day_device
SELECT
    dt,
    device_id,
    COUNT(*) AS record_cnt,
    ROUND(AVG(coldchain_temp), 2) AS avg_temp,
    SUM(final_alert) AS alert_cnt,
    ROUND(SUM(final_alert) * 1.0 / COUNT(*), 4) AS alert_rate,
    ROUND(AVG(battery), 2) AS avg_battery
FROM dwd_coldchain.dwd_temp_detail
GROUP BY dt, device_id;

DROP TABLE IF EXISTS dws_temp_hour;
CREATE TABLE IF NOT EXISTS dws_temp_hour (
    hour                INT,
    record_cnt          BIGINT,
    alert_cnt           BIGINT,
    alert_rate          DOUBLE,
    avg_temp            DOUBLE
) STORED AS TEXTFILE;

INSERT OVERWRITE TABLE dws_temp_hour
SELECT
    hour,
    COUNT(*) AS record_cnt,
    SUM(final_alert) AS alert_cnt,
    ROUND(SUM(final_alert) * 1.0 / COUNT(*), 4) AS alert_rate,
    ROUND(AVG(coldchain_temp), 2) AS avg_temp
FROM dwd_coldchain.dwd_temp_detail
GROUP BY hour;

SELECT 'dws_temp_day_location' AS tbl, COUNT(*) FROM dws_temp_day_location
UNION ALL
SELECT 'dws_temp_day_device', COUNT(*) FROM dws_temp_day_device
UNION ALL
SELECT 'dws_temp_hour', COUNT(*) FROM dws_temp_hour;
