USE dim_coldchain;

DROP TABLE IF EXISTS dim_location;
CREATE TABLE dim_location
STORED AS TEXTFILE
AS
SELECT
    location AS location_name,
    CASE
        WHEN location LIKE '冷库%' THEN '冷库'
        WHEN location LIKE '冷藏车%' THEN '运输车辆'
        WHEN location LIKE '%仓%' THEN '仓储节点'
        ELSE '其他'
    END AS location_type
FROM ods_coldchain.ods_sensor_log
WHERE location IS NOT NULL AND location <> ''
GROUP BY location;

DROP TABLE IF EXISTS dim_device;
CREATE TABLE dim_device
STORED AS TEXTFILE
AS
SELECT
    device_id,
    MAX(firmware_version) AS firmware_version,
    MAX(sensor_type) AS sensor_type
FROM ods_coldchain.ods_sensor_log
GROUP BY device_id;

DROP TABLE IF EXISTS dim_date;
CREATE TABLE dim_date
STORED AS TEXTFILE
AS
SELECT
    dt,
    CAST(SUBSTR(dt, 1, 4) AS INT) AS year,
    CAST(SUBSTR(dt, 6, 2) AS INT) AS month,
    CAST(SUBSTR(dt, 9, 2) AS INT) AS day,
    MAX(weekday) AS weekday,
    MAX(is_weekend) AS is_weekend
FROM ods_coldchain.ods_sensor_log
GROUP BY dt;

SELECT 'dim_location' AS tbl, COUNT(*) AS cnt FROM dim_location
UNION ALL
SELECT 'dim_device', COUNT(*) FROM dim_device
UNION ALL
SELECT 'dim_date', COUNT(*) FROM dim_date;
