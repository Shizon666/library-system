import os
import random
from datetime import datetime, timedelta

import numpy as np
import pandas as pd

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
OUT_DIR = os.path.join(BASE_DIR, '..', 'data')
os.makedirs(OUT_DIR, exist_ok=True)

N = 2000
random.seed(42)
np.random.seed(42)

LOCATIONS = ['冷库A', '冷库B', '冷库C', '冷藏车01', '冷藏车02', '中转仓', '前置仓']
SENSOR_TYPES = ['温度', '湿度', '温度']
STATUSES = ['在线', '在线', '在线', '告警', '离线', '维护']
FIRMWARE = ['v1.0', 'v1.1', 'v2.0', 'v2.1', 'v3.0']
ERROR_CODES = ['无', '无', '无', '无', 'E01', 'E02', 'E03']


def rand_datetime_str(n, start='2024-01-01', days=90):
    base = datetime.strptime(start, '%Y-%m-%d')
    out = []
    for _ in range(n):
        t = base + timedelta(
            days=random.randint(0, days),
            hours=random.randint(0, 23),
            minutes=random.randint(0, 59),
            seconds=random.randint(0, 59),
        )
        out.append(t.strftime('%Y-%m-%d %H:%M:%S'))
    return out


def gen_coldchain_temp(n):
    temps = []
    for _ in range(n):
        r = random.random()
        if r < 0.08:
            temps.append(round(random.uniform(8.5, 18.0), 1))
        elif r < 0.12:
            temps.append(round(random.uniform(-30.0, -18.5), 1))
        else:
            temps.append(round(random.uniform(-18.0, 8.0), 1))
    return temps


coldchain_temp = gen_coldchain_temp(N)
is_alert_from_temp = [1 if (t > 8 or t < -18) else 0 for t in coldchain_temp]
extra_alert = np.random.choice([0, 1], N, p=[0.97, 0.03])
is_alert = [1 if (a or b) else 0 for a, b in zip(is_alert_from_temp, extra_alert)]

status = []
for flag in is_alert:
    if flag == 1 and random.random() < 0.7:
        status.append('告警')
    else:
        status.append(random.choice(STATUSES))

df = pd.DataFrame({
    'device_id': np.random.randint(1, 80, N),
    'timestamp': rand_datetime_str(N),
    'temperature': np.round(np.random.uniform(-5, 35, N), 1),
    'vibration': np.round(np.random.uniform(0, 5, N), 2),
    'pressure': np.round(np.random.uniform(0.8, 1.2, N), 2),
    'humidity': np.round(np.random.uniform(30, 90, N), 1),
    'soil_ph': np.round(np.random.uniform(5, 8, N), 1),
    'pm25': np.random.randint(5, 80, N),
    'aqi': np.random.randint(20, 120, N),
    'water_quality': np.round(np.random.uniform(5, 10, N), 1),
    'coldchain_temp': coldchain_temp,
    'energy_kwh': np.round(np.random.uniform(0.5, 30, N), 2),
    'battery': np.random.randint(10, 100, N),
    'signal_strength': np.random.randint(-110, -40, N),
    'status': status,
    'location': np.random.choice(LOCATIONS, N),
    'sensor_type': np.random.choice(SENSOR_TYPES, N),
    'firmware_version': np.random.choice(FIRMWARE, N),
    'error_code': np.random.choice(ERROR_CODES, N),
    'is_alert': is_alert,
})

dirty_idx = random.sample(range(N), 15)
for i in dirty_idx[:5]:
    df.loc[i, 'location'] = None
for i in dirty_idx[5:10]:
    df.loc[i, 'coldchain_temp'] = np.nan
for i in dirty_idx[10:]:
    df.loc[i, 'status'] = ''
df = pd.concat([df, df.iloc[:8]], ignore_index=True)

out_path = os.path.join(OUT_DIR, 'iot_coldchain_raw.csv')
df.to_csv(out_path, index=False, encoding='utf-8-sig')
print(out_path, len(df), 'alert=', int(df['is_alert'].sum()))
