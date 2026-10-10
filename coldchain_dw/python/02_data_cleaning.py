import os

import pandas as pd

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.join(BASE_DIR, '..', 'data')
RAW = os.path.join(DATA_DIR, 'iot_coldchain_raw.csv')
CLEAN_CSV = os.path.join(DATA_DIR, 'iot_coldchain_clean.csv')
CLEAN_TSV = os.path.join(DATA_DIR, 'iot_coldchain_clean.tsv')

LOW, HIGH = -18.0, 8.0

df = pd.read_csv(RAW, encoding='utf-8-sig')
print('before', len(df), 'nulls', df.isnull().sum().sum())

df['location'] = df['location'].fillna('未知位置')
df['status'] = df['status'].fillna('未知').replace('', '未知')
df['error_code'] = df['error_code'].fillna('无')
df['coldchain_temp'] = df['coldchain_temp'].fillna(df['coldchain_temp'].median())
df['humidity'] = df['humidity'].fillna(df['humidity'].median())

n0 = len(df)
df = df.drop_duplicates(
    subset=['device_id', 'timestamp', 'coldchain_temp', 'location'], keep='first'
)
print('dup drop', n0 - len(df))

df['temperature'] = df['temperature'].clip(-20, 50)
df['humidity'] = df['humidity'].clip(0, 100)
df['battery'] = df['battery'].clip(0, 100)

df['timestamp'] = pd.to_datetime(df['timestamp'], errors='coerce')
df = df.dropna(subset=['timestamp'])
df['dt'] = df['timestamp'].dt.strftime('%Y-%m-%d')
df['hour'] = df['timestamp'].dt.hour
df['weekday'] = df['timestamp'].dt.dayofweek
df['is_weekend'] = df['weekday'].isin([5, 6]).astype(int)

df['is_overheat'] = (df['coldchain_temp'] > HIGH).astype(int)
df['is_overcold'] = (df['coldchain_temp'] < LOW).astype(int)
df['is_temp_alert'] = ((df['is_overheat'] == 1) | (df['is_overcold'] == 1)).astype(int)
df['is_alert'] = df['is_alert'].fillna(0).astype(int)
df['final_alert'] = (
    (df['is_alert'] == 1) | (df['is_temp_alert'] == 1) | (df['status'] == '告警')
).astype(int)


def alert_type(row):
    if row['is_overheat'] == 1:
        return '超温'
    if row['is_overcold'] == 1:
        return '过冷'
    if row['final_alert'] == 1:
        return '设备告警'
    return '正常'


def temp_level(t):
    if t < LOW:
        return '过冷'
    if t > HIGH:
        return '超温'
    if t <= -10:
        return '冷冻达标'
    return '冷藏达标'


df['alert_type'] = df.apply(alert_type, axis=1)
df['temp_level'] = df['coldchain_temp'].apply(temp_level)

for c in ['location', 'status', 'sensor_type', 'firmware_version']:
    df[c] = df[c].astype(str).str.strip()

cols = [
    'device_id', 'timestamp', 'dt', 'hour', 'weekday', 'is_weekend',
    'coldchain_temp', 'temperature', 'humidity', 'battery', 'signal_strength',
    'status', 'location', 'sensor_type', 'firmware_version', 'error_code',
    'is_alert', 'is_overheat', 'is_overcold', 'is_temp_alert', 'final_alert',
    'alert_type', 'temp_level', 'energy_kwh', 'vibration', 'pressure',
]
df = df[cols]

df.to_csv(CLEAN_CSV, index=False, encoding='utf-8-sig')
df.to_csv(CLEAN_TSV, index=False, header=False, sep='\t', encoding='utf-8')
print('after', len(df))
print(df['alert_type'].value_counts())
print(df['location'].value_counts().head())
