import os

import pandas as pd

p = os.path.join(os.path.dirname(__file__), '..', 'data', 'iot_coldchain_clean.csv')
df = pd.read_csv(p, encoding='utf-8-sig')

print('shape', df.shape)
print(df['coldchain_temp'].describe())

n = len(df)
a = int(df['final_alert'].sum())
print('alert', a, 'rate', round(a / n * 100, 2))
print(df['alert_type'].value_counts())

g = df.groupby('location').agg(
    n=('device_id', 'count'),
    avg_t=('coldchain_temp', 'mean'),
    alert=('final_alert', 'sum'),
)
g['rate'] = (g['alert'] / g['n'] * 100).round(2)
print(g.sort_values('rate', ascending=False))

h = df.groupby('hour')['final_alert'].agg(['count', 'sum'])
h['rate'] = (h['sum'] / h['count'] * 100).round(2)
print(h)

top = (
    df[df['final_alert'] == 1]
    .groupby('device_id')
    .size()
    .sort_values(ascending=False)
    .head(10)
)
print(top)
