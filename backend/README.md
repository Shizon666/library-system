# 图书馆管理系统 — 后端

Spring Boot 3.2 + MyBatis + MySQL，接口文档通过 Swagger UI 访问。

## 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+（已执行 `library-management-system` 目录下 SQL 脚本）

## 数据库配置

**若需 book 表册数字段**（总量/已借出/未借出），请执行：

`src/main/resources/db/alter_book_copy_counts.sql`

修改 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_db?...
    username: root
    password: 123456   # 改成你的密码
```

## 启动

```bash
cd backend
mvn spring-boot:run
```

## Swagger 测试

启动后浏览器打开：

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 测试账号

| 角色 | 登录方式 | 账号 | 密码 |
|------|----------|------|------|
| 管理员 | role=admin | admin | 123456 |
| 学生 | role=student，username=证号 | C2024001（张三） | 123456 |

## 接口一览

| 模块 | 路径 |
|------|------|
| 登录 | POST `/api/login` |
| 图书检索 | GET `/api/books/search?keyword=` |
| 采编入库 | POST `/api/books/acquire` |
| 副本报废 | DELETE `/api/books/copies/{barCode}` |
| 整书下架 | PUT `/api/books/{isbn}/off-shelf` |
| 借书 | POST `/api/borrow` |
| 还书 | POST `/api/return` |
| 借书证 | POST/PUT/DELETE `/api/cards/...` |
| 罚款 | GET `/api/fines`，POST `/api/fines/pay` |
| 个人 | GET `/api/me/borrowings?sno=` 等 |
| 统计 | GET `/api/dashboard/stats`，`/api/dashboard/rankings` |

## 业务规则

- 本科生最多借 5 本，研究生 10 本
- 未缴罚款合计 > 50 元禁止借书
- 借期 30 天，超期 1 元/天
- 证件 status=1 才能借书
