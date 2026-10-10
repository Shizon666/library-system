# 图书馆管理系统 — 团队协作说明

> 成员 A 交付 | 2026-05-19

---

## 📁 文件清单

| 文件 | 用途 | 谁用 |
|------|------|------|
| `01-create-tables.sql` | 建库 + 11 张表 | B |
| `02-create-indexes.sql` | 6 个补充索引 | B |
| `03-insert-test-data.sql` | 完整测试数据 | B |
| `04-verify-data.sql` | 15 组验证查询 | B（验证用） |
| `library-management-system-design.md` | 项目设计文档 | 全员 |
| `成员A-数据库设计与SQL工作文档.md` | 数据库详细说明 | B（主要） |
| `README.md` | 本文件 | 全员 |

---

## 🚀 B 快速上手

### 1. 建库 + 导数据

```bash
mysql -u root -p -e "DROP DATABASE IF EXISTS library_db;"
mysql -u root -p < 01-create-tables.sql
mysql -u root -p library_db < 02-create-indexes.sql
mysql -u root -p library_db < 03-insert-test-data.sql
```

### 2. 验证

```bash
mysql -u root -p library_db < 04-verify-data.sql
```

### 3. Spring Boot 连接配置

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/library_db?characterEncoding=utf-8
spring.datasource.username=root
spring.datasource.password=123456
```

### 4. 核心表名速查

| 表 | 说明 | 主键 |
|----|------|------|
| `admin` | 管理员 | id |
| `student` | 学生信息 | sno（学号，10位） |
| `libcard` | 借书证 | cardNo（如 C2024001） |
| `cardrec` | 证件操作记录 | serNum |
| `book` | 图书 | ISBN |
| `bookcopy` | 图书副本 | barCode（如 BC-2024001） |
| `borrowrec` | 借阅记录 | recNo |
| `overdue` | 超期记录 | overdueId |
| `fine` | 罚款 | fineId |
| `finePayment` | 罚款缴纳 | paymentId |
| `finePaymentDetail` | 缴纳明细 | detailId |

### 5. 关键列名对照（后端写 SQL 时注意）

```
图书:    book.ISBN, book.bookName, book.introduction
副本:    bookcopy.barCode, bookcopy.bookISBN, bookcopy.place
学生:    student.sno, student.studentName, student.studentType
证件:    libcard.cardNo, libcard.sno, libcard.status
借阅:    borrowrec.sno, borrowrec.barCode, borrowrec.borDate, borrowrec.retDate, borrowrec.realRetDate
罚款:    fine.payStatus
```

### 6. 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 学生 | 张三 (cardNo: C2024001) | 123456 |
| 学生 | 李四 (cardNo: C2024002) | 123456 |

> 所有学生密码默认 `123456`，学号详见 `03-insert-test-data.sql`

---

## 🎨 C / D 参考

前端页面设计、接口参数格式详见 `library-management-system-design.md` 第四、七章。

接口统一返回格式：
```json
{ "code": 0, "message": "success", "data": {} }
```

---

## 📋 开发顺序

1. B 先配好数据库 + Spring Boot 跑通登录
2. C 同步写页面 HTML/CSS
3. B 出图书检索接口 → D 联调
4. B 出借书/还书接口 → D 联调
5. B 出证件管理/采编/罚款接口 → D 联调
6. 最后统计 + 排行榜 + 整体优化
