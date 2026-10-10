# 图书馆管理系统项目详细设计文档

---

# 一、项目概述

## 1.1 项目名称

基于 Spring Boot 的图书馆管理系统

## 1.2 项目背景

随着学校图书馆馆藏规模不断扩大，传统人工管理方式已难以满足日常管理需求。为提高图书馆管理效率和服务能力，有必要开发一套图书馆管理系统，实现图书检索、借还书、借书证管理、图书采编、罚款管理和数据统计等功能。

## 1.3 项目目标

本项目采用前后端分离架构，重点实现以下业务模块：

- 图书检索
- 借书管理
- 还书管理
- 借书证管理
- 图书采编
- 超期罚款管理
- 数据统计

系统重点体现以下能力：

- 数据库设计能力
- 前后端分离思想
- RESTful API 设计
- Java Web 开发能力
- 数据状态流转逻辑

---

# 二、项目技术架构

## 2.1 前端技术

使用技术：HTML5、CSS3、JavaScript、Fetch API

本项目未采用 Vue、React 等前端框架，原因：面向课程设计场景、页面数量较少、便于控制实现成本、便于快速完成开发与联调。

前端主要负责页面展示、用户交互、数据输入、数据渲染和接口调用。

## 2.2 后端技术

使用技术：Java、Spring Boot、JDBC、Maven、Lombok

后端主要负责接收请求、处理业务逻辑、访问数据库并返回 JSON 数据。

## 2.3 数据库技术

使用 MySQL。负责图书数据、借阅记录、超期罚款、学生信息的存储与管理。

---

# 三、系统架构设计

```text
前端页面（HTML + CSS + JS）
        ↓  Fetch 请求
Spring Boot REST API
        ↓
Service 业务逻辑层
        ↓
DAO/JDBC 数据访问层
        ↓
MySQL 数据库（library_db）
```

---

# 四、前端详细设计

## 4.1 布局与风格

系统采用后台管理系统布局（顶部导航栏 + 左侧菜单栏 + 右侧内容区）。风格定位为简洁、统一、易读。

配色方案：主色调 `#1E3A8A`，按钮 `#2563EB`，背景 `#F3F4F6`，表格白底 `#FFFFFF`，字体 `#111827`。

## 4.2 页面清单

| 页面 | 文件 | 核心功能 |
| ---- | ---- | -------- |
| 登录 | `login.html` | 管理员/学生登录 |
| 首页 | `index.html` | 数据统计卡片 + 排行榜（当月借书学生排行、当月热门图书排行） |
| 图书检索 | `search.html` | 按书名/作者/ISBN 搜索，展示图书详情（ISBN、书名、作者、出版社、简介）+ 全部副本馆藏信息（条码、地点、状态） |
| 借书 | `borrow.html` | 扫描借书证号 + 多本条码，校验证件状态/借阅上限/罚款，一次性借出 |
| 还书 | `return.html` | 扫描副本条码，自动判断超期并生成罚款 |
| 借书证管理 | `card.html` | 新办、挂失、补办、注销，追踪操作记录 |
| 图书采编 | `edit-book.html` | 新书入库、副本新增、图书下架（全部副本）、图书报废（单个副本） |
| 罚款管理 | `fine.html` | 未缴/已缴罚款查询，管理员录入缴纳（支持合并缴纳） |
| 个人查询 | `personal.html` | 当前在借、借书历史、超期罚款、已缴记录 |

## 4.3 前端目录结构

```text
frontend/
├── pages/
│   ├── login.html
│   ├── index.html
│   ├── search.html
│   ├── borrow.html
│   ├── return.html
│   ├── card.html
│   ├── edit-book.html
│   ├── fine.html
│   └── personal.html
├── css/
│   ├── common.css
│   ├── index.css
│   └── table.css
├── js/
│   ├── request.js
│   ├── search.js
│   ├── borrow.js
│   ├── return.js
│   ├── card.js
│   ├── fine.js
│   └── personal.js
└── images/
```

---

# 五、后端详细设计

## 5.1 分层架构

| 层级 | 职责 |
| ---- | ---- |
| Controller | 接收前端请求，返回 JSON |
| Service | 业务逻辑处理（状态校验、借阅规则、罚款计算） |
| DAO | 数据库增删改查（JDBC + PreparedStatement） |
| Entity | 数据库表对应的实体类 |
| DTO | 前端请求参数封装 |

## 5.2 Controller 示例

```java
@RestController
@RequestMapping("/api/books")
public class BookController {

    @GetMapping("/search")
    public Result search(String keyword) {
        return bookService.search(keyword);
    }
}
```

## 5.3 Service 层业务规则

借书时需校验：借书证状态（正常）、学生类型借阅上限（本科生 5 本 / 研究生 10 本）、累计未缴罚款 ≤ 50 元、副本状态为可借。还书时在同一事务内完成：更新归还状态、判断超期、生成超期记录和罚款。

## 5.4 DAO 层示例

```java
public class BookDao {

    public List<Book> search(String keyword) {
        String sql = "SELECT ISBN, bookName, author, publisher, introduction FROM book "
                   + "WHERE bookName LIKE ? OR author LIKE ? OR ISBN LIKE ?";
        List<Book> books = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Book book = new Book();
                    book.setISBN(rs.getString("ISBN"));
                    book.setBookName(rs.getString("bookName"));
                    book.setAuthor(rs.getString("author"));
                    books.add(book);
                }
            }
        }
        return books;
    }

    public int updateBookcopyStatus(String barCode, int status) {
        String sql = "UPDATE bookcopy SET status = ? WHERE barCode = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setString(2, barCode);
            return ps.executeUpdate();
        }
    }
}
```

## 5.5 Entity 示例

```java
@Data
public class Book {
    private String ISBN;
    private String bookName;
    private String author;
    private String publisher;
    private String introduction;
}

@Data
public class Student {
    private String sno;
    private String studentName;
    private String sex;
    private int studentType;
    private String college;
    private String major;
    private String phone;
}

@Data
public class Borrowrec {
    private int recNo;
    private String sno;
    private String barCode;
    private LocalDateTime borDate;
    private LocalDateTime retDate;
    private LocalDateTime realRetDate;
    private int status;
}
```

## 5.6 DTO 示例

```java
@Data
public class BorrowDTO {
    private String cardNo;          // 借书证号，如 "C2024001"
    private List<String> barCodes;  // 副本条码列表，如 ["BC-2024001","BC-2024002"]
}

@Data
public class ReturnDTO {
    private String barCode;         // 副本条码，如 "BC-2024001"
}
```

## 5.7 后端目录结构

```text
backend/
├── controller/
│   ├── BookController.java
│   ├── BorrowController.java
│   ├── CardController.java
│   ├── FineController.java
│   └── AdminController.java
├── service/
│   ├── BookService.java
│   ├── BorrowService.java
│   ├── CardService.java
│   └── FineService.java
├── dao/
│   ├── BookDao.java
│   ├── BorrowDao.java
│   ├── CardDao.java
│   ├── FineDao.java
│   └── AdminDao.java
├── entity/
│   ├── Admin.java
│   ├── Student.java
│   ├── Libcard.java
│   ├── Cardrec.java
│   ├── Book.java
│   ├── Bookcopy.java
│   ├── Borrowrec.java
│   ├── Overdue.java
│   ├── Fine.java
│   ├── FinePayment.java
│   └── FinePaymentDetail.java
├── dto/
│   ├── BorrowDTO.java
│   ├── ReturnDTO.java
│   └── FinePayDTO.java
├── common/
│   ├── Result.java
│   └── ResultCode.java
└── config/
```

---

# 六、数据库设计

> **命名风格**：采用课堂熟悉的 camelCase（`bookcopy`、`libcard`、`borrowrec`、`cardrec`），与课堂 `lib-book` 系统一致。
>
> **核心设计**：student（个人信息）+ libcard（证件状态）分离；cardrec 追踪证件全生命周期；自然主键（ISBN、barCode、sno、cardNo）。

## 6.1 状态码定义

### 副本状态 — `bookcopy.status`

| 值 | 含义 |
| - | --- |
| 0 | 下架 |
| 1 | 可借 |
| 2 | 已借出 |
| 3 | 已报废 |

### 证件状态 — `libcard.status`

| 值 | 含义 |
| - | --- |
| 0 | 注销 |
| 1 | 正常 |
| 2 | 挂失 |
| 3 | 作废（旧证，由 cardrec 追踪） |

### 学生类型 — `student.studentType`

| 值 | 含义 | 借阅上限 |
| - | --- | -------- |
| 1 | 本科生 | 5 本 |
| 2 | 研究生 | 10 本 |

### 证件操作 — `cardrec.opType`

| 值 | 含义 |
| - | --- |
| 1 | 新办 |
| 2 | 挂失 |
| 3 | 补办 |
| 4 | 注销 |

### 借阅状态 — `borrowrec.status`

| 值 | 含义 |
| - | --- |
| 0 | 已还 |
| 1 | 未还 |

### 缴费状态 — `fine.payStatus`

| 值 | 含义 |
| - | --- |
| 0 | 未缴 |
| 1 | 已缴 |

### 学号编码规则

格式 `YYYY T MM NNN`（10 位数字）：4 位入学年份 + 1 位学生类型 + 2 位专业编号 + 3 位个人序号。

示例：`2024101001` = 2024 级本科生，计算机科学与技术(01)，序号 001。

## 6.2 数据表（11 张）

### admin — 管理员表

| 字段 | 类型 | 约束 |
| ---- | ---- | ---- |
| id | INT | PK AUTO_INCREMENT |
| username | VARCHAR(50) | NOT NULL, UNIQUE |
| password | VARCHAR(50) | NOT NULL |

### student — 学生表

| 字段 | 类型 | 约束 |
| ---- | ---- | ---- |
| sno | VARCHAR(30) | PK |
| studentName | VARCHAR(50) | NOT NULL |
| sex | CHAR(1) | |
| studentType | TINYINT | NOT NULL, DEFAULT 1 |
| college | VARCHAR(50) | |
| major | VARCHAR(50) | |
| phone | VARCHAR(20) | |

### libcard — 借书证表

| 字段 | 类型 | 约束 |
| ---- | ---- | ---- |
| cardNo | VARCHAR(30) | PK |
| sno | VARCHAR(30) | NOT NULL, UNIQUE, FK→student(sno) |
| password | VARCHAR(50) | NOT NULL, DEFAULT '123456' |
| status | TINYINT | NOT NULL, DEFAULT 1 |

### cardrec — 证件操作记录表

| 字段 | 类型 | 约束 |
| ---- | ---- | ---- |
| serNum | INT | PK AUTO_INCREMENT |
| sno | VARCHAR(30) | NOT NULL, FK→student(sno) |
| originCardNo | VARCHAR(30) | |
| newCardNo | VARCHAR(30) | |
| opType | TINYINT | NOT NULL |
| opTime | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

### book — 图书表

| 字段 | 类型 | 约束 |
| ---- | ---- | ---- |
| ISBN | VARCHAR(32) | PK |
| bookName | VARCHAR(100) | NOT NULL |
| author | VARCHAR(100) | NOT NULL |
| publisher | VARCHAR(100) | |
| pubDate | DATE | |
| introduction | TEXT | |
| category | VARCHAR(50) | |
| status | TINYINT | NOT NULL, DEFAULT 1 |

### bookcopy — 图书副本表

| 字段 | 类型 | 约束 |
| ---- | ---- | ---- |
| barCode | VARCHAR(50) | PK |
| bookISBN | VARCHAR(32) | NOT NULL, FK→book(ISBN) |
| place | VARCHAR(50) | |
| status | TINYINT | NOT NULL, DEFAULT 1 |

### borrowrec — 借阅记录表

| 字段 | 类型 | 约束 |
| ---- | ---- | ---- |
| recNo | INT | PK AUTO_INCREMENT |
| sno | VARCHAR(30) | NOT NULL, FK→student(sno) |
| barCode | VARCHAR(50) | NOT NULL, FK→bookcopy(barCode) |
| borDate | DATETIME | NOT NULL |
| retDate | DATETIME | NOT NULL |
| realRetDate | DATETIME | |
| status | TINYINT | NOT NULL, DEFAULT 1 |

### overdue — 超期记录表

| 字段 | 类型 | 约束 |
| ---- | ---- | ---- |
| overdueId | INT | PK AUTO_INCREMENT |
| recNo | INT | NOT NULL, FK→borrowrec(recNo) |
| overdueDays | INT | NOT NULL |
| overdueAmount | DECIMAL(10,2) | NOT NULL |
| createTime | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

### fine — 罚款表

| 字段 | 类型 | 约束 |
| ---- | ---- | ---- |
| fineId | INT | PK AUTO_INCREMENT |
| recNo | INT | NOT NULL, FK→borrowrec(recNo) |
| amount | DECIMAL(10,2) | NOT NULL |
| payStatus | TINYINT | NOT NULL, DEFAULT 0 |
| createTime | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

### finePayment — 罚款缴纳表

| 字段 | 类型 | 约束 |
| ---- | ---- | ---- |
| paymentId | INT | PK AUTO_INCREMENT |
| cardNo | VARCHAR(30) | NOT NULL, FK→libcard(cardNo) |
| sno | VARCHAR(30) | NOT NULL |
| paymentAmount | DECIMAL(10,2) | NOT NULL |
| paymentTime | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| operator | VARCHAR(50) | |
| remark | VARCHAR(200) | |

### finePaymentDetail — 缴纳明细表（支持合并缴纳）

| 字段 | 类型 | 约束 |
| ---- | ---- | ---- |
| detailId | INT | PK AUTO_INCREMENT |
| paymentId | INT | NOT NULL, FK→finePayment(paymentId) |
| fineId | INT | NOT NULL, FK→fine(fineId) |

## 6.3 表关系

```text
student (1) ──── (1) libcard ──── (1) finePayment ──── (1) finePaymentDetail ──── (N) fine
  │  │                                                       │
  │  └── (1) cardrec (N)                                      └── (N) borrowrec (N) ──── (1) overdue
  │                                                                    │
  └── (N) borrowrec (N) ──── (N) bookcopy ──── (1) book
```

核心关系：student 与 libcard 一对一；student 与 cardrec 一对多；book 与 bookcopy 一对多；student/borrowrec 一对多；bookcopy/borrowrec 一对多；borrowrec/overdue 一对一；borrowrec/fine 一对一；finePayment/fine 多对多（通过 finePaymentDetail）。

## 6.4 设计原则

- student 存个人信息，libcard 存证件状态，cardrec 追踪全生命周期
- 自然主键：book(ISBN)、bookcopy(barCode)、student(sno)、libcard(cardNo)
- 状态字段统一数字编码
- 所有外键建立索引
- 借还书操作使用事务保证数据一致性

## 6.5 核心建表 SQL

```sql
CREATE TABLE admin (
    id       INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(50) NOT NULL
);

CREATE TABLE student (
    sno         VARCHAR(30) PRIMARY KEY,
    studentName VARCHAR(50) NOT NULL,
    sex         CHAR(1),
    studentType TINYINT NOT NULL DEFAULT 1,
    college     VARCHAR(50),
    major       VARCHAR(50),
    phone       VARCHAR(20)
);

CREATE TABLE libcard (
    cardNo   VARCHAR(30) PRIMARY KEY,
    sno      VARCHAR(30) NOT NULL UNIQUE,
    password VARCHAR(50) NOT NULL DEFAULT '123456',
    status   TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_libcard_student FOREIGN KEY (sno) REFERENCES student(sno)
);

CREATE TABLE cardrec (
    serNum       INT PRIMARY KEY AUTO_INCREMENT,
    sno          VARCHAR(30) NOT NULL,
    originCardNo VARCHAR(30),
    newCardNo    VARCHAR(30),
    opType       TINYINT NOT NULL,
    opTime       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cardrec_student FOREIGN KEY (sno) REFERENCES student(sno)
);

CREATE TABLE book (
    ISBN         VARCHAR(32) PRIMARY KEY,
    bookName     VARCHAR(100) NOT NULL,
    author       VARCHAR(100) NOT NULL,
    publisher    VARCHAR(100),
    pubDate      DATE,
    introduction TEXT,
    category     VARCHAR(50),
    status       TINYINT NOT NULL DEFAULT 1
);

CREATE TABLE bookcopy (
    barCode  VARCHAR(50) PRIMARY KEY,
    bookISBN VARCHAR(32) NOT NULL,
    place    VARCHAR(50),
    status   TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_bookcopy_book FOREIGN KEY (bookISBN) REFERENCES book(ISBN)
);

CREATE TABLE borrowrec (
    recNo       INT PRIMARY KEY AUTO_INCREMENT,
    sno         VARCHAR(30) NOT NULL,
    barCode     VARCHAR(50) NOT NULL,
    borDate     DATETIME NOT NULL,
    retDate     DATETIME NOT NULL,
    realRetDate DATETIME,
    status      TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_borrowrec_student  FOREIGN KEY (sno) REFERENCES student(sno),
    CONSTRAINT fk_borrowrec_bookcopy FOREIGN KEY (barCode) REFERENCES bookcopy(barCode)
);

CREATE TABLE overdue (
    overdueId     INT PRIMARY KEY AUTO_INCREMENT,
    recNo         INT NOT NULL,
    overdueDays   INT NOT NULL,
    overdueAmount DECIMAL(10,2) NOT NULL,
    createTime    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_overdue_borrowrec FOREIGN KEY (recNo) REFERENCES borrowrec(recNo)
);

CREATE TABLE fine (
    fineId     INT PRIMARY KEY AUTO_INCREMENT,
    recNo      INT NOT NULL,
    amount     DECIMAL(10,2) NOT NULL,
    payStatus  TINYINT NOT NULL DEFAULT 0,
    createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fine_borrowrec FOREIGN KEY (recNo) REFERENCES borrowrec(recNo)
);

CREATE TABLE finePayment (
    paymentId     INT PRIMARY KEY AUTO_INCREMENT,
    cardNo        VARCHAR(30) NOT NULL,
    sno           VARCHAR(30) NOT NULL,
    paymentAmount DECIMAL(10,2) NOT NULL,
    paymentTime   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    operator      VARCHAR(50),
    remark        VARCHAR(200),
    CONSTRAINT fk_finePayment_libcard FOREIGN KEY (cardNo) REFERENCES libcard(cardNo)
);

CREATE TABLE finePaymentDetail (
    detailId  INT PRIMARY KEY AUTO_INCREMENT,
    paymentId INT NOT NULL,
    fineId    INT NOT NULL,
    CONSTRAINT fk_detail_payment FOREIGN KEY (paymentId) REFERENCES finePayment(paymentId),
    CONSTRAINT fk_detail_fine    FOREIGN KEY (fineId) REFERENCES fine(fineId)
);
```

---

# 七、接口设计

## 7.1 统一返回格式

```json
{ "code": 0, "message": "success", "data": {} }
```

成功 `code=0`，失败返回非 0 并给出错误信息。

## 7.2 接口清单

| 接口 | 方法 | 说明 |
| ---- | ---- | ---- |
| `/api/login` | POST | 管理员/学生登录 |
| `/api/books/search?keyword=` | GET | 图书检索（书名/作者/ISBN） |
| `/api/borrow` | POST | 借书 `{"cardNo":"C2024001","barCodes":["BC-2024001"]}` |
| `/api/return` | POST | 还书 `{"barCode":"BC-2024001"}` |
| `/api/me/borrowings` | GET | 个人在借图书 |
| `/api/me/fines` | GET | 个人罚款情况 |
| `/api/me/history` | GET | 个人借书历史 |
| `/api/cards` | POST | 新办借书证 |
| `/api/cards/{cardNo}` | PUT | 挂失/补办 |
| `/api/cards/{cardNo}` | DELETE | 注销 |
| `/api/fines` | GET | 罚款查询 |
| `/api/fines/pay` | POST | 缴纳罚款（支持合并缴纳） |
| `/api/dashboard/stats` | GET | 首页统计数据 |
| `/api/dashboard/rankings` | GET | 当月排行榜 |

## 7.3 核心业务规则

**借书校验**：libcard.status=1（正常）+ student.studentType 对应上限 + SUM(fine.amount WHERE payStatus=0) ≤ 50 + bookcopy.status=1（可借）。

**还书处理**：同一事务内 → UPDATE borrowrec SET status=0, realRetDate=NOW() → 若超期则 INSERT overdue + INSERT fine → UPDATE bookcopy SET status=1。

**证件管理**：新办 → INSERT student + INSERT libcard + INSERT cardrec(opType=1)；挂失 → UPDATE libcard SET status=2 + INSERT cardrec(opType=2)；补办 → UPDATE 旧证 status=3 + INSERT 新证 + INSERT cardrec(opType=3)；注销 → UPDATE libcard SET status=0 + INSERT cardrec(opType=4)。

---

# 八、四人开发分工

| 成员 | 职责 |
| ---- | ---- |
| 成员 A | 数据库设计、SQL 编写、测试数据准备 |
| 成员 B | Spring Boot 接口开发与业务逻辑实现 |
| 成员 C | HTML、CSS 和页面布局设计 |
| 成员 D | JavaScript、Fetch 请求与前后端联调 |

---

# 九、项目开发顺序

1. **第一阶段**：数据库建表 + Spring Boot 搭建 + 登录功能
2. **第二阶段**：图书检索 + 借书 + 还书
3. **第三阶段**：借书证管理 + 图书采编
4. **第四阶段**：罚款管理 + 统计模块 + 页面优化

---

# 十、项目答辩重点

答辩时重点说明：数据库状态设计、前后端分离架构、借阅业务逻辑、图书副本流转。

重点解释以下 7 张核心表：

| 表 | 作用 |
| ---- | ---- |
| `book` | 图书信息（ISBN 主键，含书籍简介 introduction） |
| `bookcopy` | 每一本副本（barCode 主键，bookISBN 关联图书） |
| `borrowrec` | 借阅记录（sno + barCode 关联，status 表示在借/已还） |
| `student` | 学生个人信息（sno 主键） |
| `libcard` | 借书证状态（cardNo 主键，sno 关联学生） |
| `cardrec` | 证件操作全生命周期（新办/挂失/补办/注销） |
| `fine` | 超期罚款（recNo 关联借阅记录，payStatus 表示缴费状态） |

通过 `status` 字段实现状态流转。借书与还书过程需结合事务处理，保证借阅记录、副本状态和罚款数据同步更新。

---

# 十一、项目总结

本系统采用 HTML + CSS + JavaScript + Spring Boot + MySQL 技术组合，完整实现图书入库、图书检索、借书、还书、罚款、统计等核心业务流程，具有完整的业务闭环。

> **文档版本**：v2.0
>
> **编写日期**：2026-05-19
