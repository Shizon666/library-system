SELECT * FROM ads_coldchain.ads_overview ORDER BY metric_name;

SELECT location, location_type, record_cnt, avg_temp, alert_cnt, alert_rate, risk_level
FROM ads_coldchain.ads_location_risk
ORDER BY alert_rate DESC;

SELECT device_id, record_cnt, alert_cnt, alert_rate, avg_temp, avg_battery
FROM ads_coldchain.ads_device_top_alert
ORDER BY alert_cnt DESC
LIMIT 10;

SELECT alert_type, cnt, ratio
FROM ads_coldchain.ads_alert_type_dist
ORDER BY cnt DESC;

SELECT hour, record_cnt, alert_cnt, alert_rate, avg_temp
FROM ads_coldchain.ads_hourly_alert
ORDER BY hour;

SELECT location_type,
       SUM(record_cnt) AS record_cnt,
       SUM(alert_cnt) AS alert_cnt,
       ROUND(SUM(alert_cnt) * 1.0 / SUM(record_cnt), 4) AS alert_rate,
       ROUND(AVG(avg_temp), 2) AS avg_temp
FROM ads_coldchain.ads_location_risk
GROUP BY location_type
ORDER BY alert_rate DESC;

SELECT device_id, event_time, location, coldchain_temp, alert_type, status
FROM dwd_coldchain.dwd_temp_detail
WHERE is_overheat = 1
ORDER BY event_time DESC
LIMIT 20;
