# 冷链物流温度监控数仓

基于 Hive 分层（ODS / DIM / DWD / DWS / ADS）的冷链 IoT 温度监控数据仓库示例，含数据生成、清洗、ETL 脚本与可视化看板。

## 目录结构

```
coldchain_dw/
├── data/          # 原始与清洗后的示例数据
├── python/        # 造数、清洗、EDA
├── hql/           # 建库建表与分层 ETL
├── shell/         # ODS 装载与一键跑批
└── html/          # 监控看板
```

## 快速开始

1. 生成 / 清洗数据：`python/01_generate_data.py` → `02_data_cleaning.py`
2. 建库建表：按序号执行 `hql/01`～`03`
3. 装载 ODS：`shell/load_ods.sh`
4. 分层加工：`shell/run_etl.sh` 或按序执行 `hql/04`～`07`
5. 打开 `html/coldchain_dashboard.html` 查看看板
