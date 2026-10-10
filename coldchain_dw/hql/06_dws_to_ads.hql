USE ads_coldchain;

DROP TABLE IF EXISTS ads_overview;
CREATE TABLE IF NOT EXISTS ads_overview (
    metric_name     STRING,
    metric_value    DOUBLE,
    metric_desc     STRING
) STORED AS TEXTFILE;

INSERT OVERWRITE TABLE ads_overview
SELECT 'total_records', CAST(COUNT(*) AS DOUBLE), '总记录'
FROM dwd_coldchain.dwd_temp_detail
UNION ALL
SELECT 'device_cnt', CAST(COUNT(DISTINCT device_id) AS DOUBLE), '设备数'
FROM dwd_coldchain.dwd_temp_detail
UNION ALL
SELECT 'location_cnt', CAST(COUNT(DISTINCT location) AS DOUBLE), '地点数'
FROM dwd_coldchain.dwd_temp_detail
UNION ALL
SELECT 'alert_cnt', CAST(SUM(final_alert) AS DOUBLE), '告警次数'
FROM dwd_coldchain.dwd_temp_detail
UNION ALL
SELECT 'alert_rate', ROUND(SUM(final_alert) * 1.0 / COUNT(*), 4), '告警率'
FROM dwd_coldchain.dwd_temp_detail
UNION ALL
SELECT 'avg_temp', ROUND(AVG(coldchain_temp), 2), '均温'
FROM dwd_coldchain.dwd_temp_detail
UNION ALL
SELECT 'overheat_cnt', CAST(SUM(is_overheat) AS DOUBLE), '超温'
FROM dwd_coldchain.dwd_temp_detail
UNION ALL
SELECT 'overcold_cnt', CAST(SUM(is_overcold) AS DOUBLE), '过冷'
FROM dwd_coldchain.dwd_temp_detail;

DROP TABLE IF EXISTS ads_location_risk;
CREATE TABLE IF NOT EXISTS ads_location_risk (
    location            STRING,
    location_type       STRING,
    record_cnt          BIGINT,
    avg_temp            DOUBLE,
    alert_cnt           BIGINT,
    alert_rate          DOUBLE,
    risk_level          STRING
) STORED AS TEXTFILE;

INSERT OVERWRITE TABLE ads_location_risk
SELECT
    location,
    MAX(location_type) AS location_type,
    SUM(record_cnt) AS record_cnt,
    ROUND(AVG(avg_temp), 2) AS avg_temp,
    SUM(alert_cnt) AS alert_cnt,
    ROUND(SUM(alert_cnt) * 1.0 / SUM(record_cnt), 4) AS alert_rate,
    CASE
        WHEN SUM(alert_cnt) * 1.0 / SUM(record_cnt) >= 0.20 THEN '高风险'
        WHEN SUM(alert_cnt) * 1.0 / SUM(record_cnt) >= 0.10 THEN '中风险'
        ELSE '低风险'
    END AS risk_level
FROM dws_coldchain.dws_temp_day_location
GROUP BY location;

DROP TABLE IF EXISTS ads_device_top_alert;
CREATE TABLE IF NOT EXISTS ads_device_top_alert (
    device_id       INT,
    record_cnt      BIGINT,
    alert_cnt       BIGINT,
    alert_rate      DOUBLE,
    avg_temp        DOUBLE,
    avg_battery     DOUBLE
) STORED AS TEXTFILE;

INSERT OVERWRITE TABLE ads_device_top_alert
SELECT device_id, record_cnt, alert_cnt, alert_rate, avg_temp, avg_battery
FROM (
    SELECT
        device_id,
        SUM(record_cnt) AS record_cnt,
        SUM(alert_cnt) AS alert_cnt,
        ROUND(SUM(alert_cnt) * 1.0 / SUM(record_cnt), 4) AS alert_rate,
        ROUND(AVG(avg_temp), 2) AS avg_temp,
        ROUND(AVG(avg_battery), 2) AS avg_battery
    FROM dws_coldchain.dws_temp_day_device
    GROUP BY device_id
    HAVING SUM(alert_cnt) > 0
) t
ORDER BY alert_cnt DESC
LIMIT 20;

DROP TABLE IF EXISTS ads_alert_type_dist;
CREATE TABLE IF NOT EXISTS ads_alert_type_dist (
    alert_type      STRING,
    cnt             BIGINT,
    ratio           DOUBLE
) STORED AS TEXTFILE;

INSERT OVERWRITE TABLE ads_alert_type_dist
SELECT
    alert_type,
    COUNT(*) AS cnt,
    ROUND(COUNT(*) * 1.0 / SUM(COUNT(*)) OVER (), 4) AS ratio
FROM dwd_coldchain.dwd_temp_detail
GROUP BY alert_type;

DROP TABLE IF EXISTS ads_hourly_alert;
CREATE TABLE IF NOT EXISTS ads_hourly_alert (
    hour            INT,
    record_cnt      BIGINT,
    alert_cnt       BIGINT,
    alert_rate      DOUBLE,
    avg_temp        DOUBLE
) STORED AS TEXTFILE;

INSERT OVERWRITE TABLE ads_hourly_alert
SELECT hour, record_cnt, alert_cnt, alert_rate, avg_temp
FROM dws_coldchain.dws_temp_hour
ORDER BY hour;

SELECT * FROM ads_overview;
SELECT * FROM ads_location_risk ORDER BY alert_rate DESC;
SELECT * FROM ads_device_top_alert LIMIT 10;
SELECT * FROM ads_alert_type_dist;
SELECT * FROM ads_hourly_alert;
