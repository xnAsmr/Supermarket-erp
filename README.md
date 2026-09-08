# 超市收银出入库系统 (Supermarket ERP)

基于 **Spring Boot 3 + Vue 3 + MyBatis-Plus + Naive UI** 的多租户超市收银管理平台，覆盖 POS 收银、订单、库存、会员储值、报表统计、供应商/品牌管理以及系统级的多租户 RBAC 权限管理。

## 功能特性

- **POS 收银台**：商品扫码/搜索、两级分类筛选、库存预警、组合支付（现金/微信/支付宝/银行卡/储值卡）、会员储值卡扣款
- **订单管理**：订单列表/详情、取消/退款/删除、利润核算、支付流水对账、**财务日结**
- **库存管理**：库存列表/详情、采购入库、出库、库存盘点、库存调整、库存预警
- **会员管理**：会员档案、储值卡余额、充值（含赠送）、消费明细、充值/消费记录
- **报表统计**：销售报表、库存报表、会员报表、经营看板
- **基础资料**：商品、分类、品牌、供应商管理
- **多租户系统管理**：
  - 租户管理（套餐/额度/到期时间）、门店管理
  - RBAC 权限：角色管理 + 菜单管理 + 按钮级权限（前后端双重鉴权）
  - 用户管理（跨租户查看/按租户筛选）
  - **操作日志**（AOP 自动记录，系统管理员可查看所有租户）
  - 系统设置（支付方式管理）

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue 3 (Composition API + `<script setup>`) · TypeScript · Vite · Naive UI · Pinia · Vue Router · ECharts |
| 后端 | Spring Boot 3.2 · Spring Security + JWT · MyBatis-Plus · Spring AOP · Knife4j |
| 数据库 | MySQL 8.0 |
| 构建 | Maven (后端) · npm (前端) |

## 目录结构

```
Supermarket-erp/
├── backend/                    # 后端 Spring Boot 工程
│   └── src/main/
│       ├── java/com/supermarket/erp/
│       │   ├── common/         # 通用（安全/JWT/结果封装/异常/AOP）
│       │   └── module/         # 业务模块（auth/pos/order/stock/member/...）
│       └── resources/
│           ├── application.yml       # 公共配置
│           ├── application-dev.yml   # 开发环境（数据库/JWT，已 gitignore）
│           └── db/init-test-data.sql # 建库建表 + 测试数据
├── frontend/                   # 前端 Vue 3 工程
│   └── src/
│       ├── api/                # 接口封装
│       ├── views/              # 页面（admin 租户端 / system 系统端 / pos 收银）
│       ├── layouts/            # 布局
│       ├── stores/             # Pinia 状态
│       └── router/             # 路由
├── docs/                       # 文档
└── upload/                     # 文件上传目录（gitignore）
```

## 环境要求

- **JDK 17+**
- **Maven 3.6+**
- **Node.js 18+**
- **MySQL 8.0+**

## 本地部署

### 1. 克隆仓库

```bash
git clone https://github.com/xnAsmr/Supermarket-erp.git
cd Supermarket-erp
```

### 2. 初始化数据库

使用 MySQL 执行初始化脚本（脚本包含建库建表 + 测试数据，需先创建数据库 `supermarket_erp`）：

```bash
mysql -u root -p
```

```sql
CREATE DATABASE IF NOT EXISTS supermarket_erp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE supermarket_erp;
SOURCE backend/src/main/resources/db/init-test-data.sql;
```

> 脚本内已包含 `DROP TABLE IF EXISTS`，可重复执行。

### 3. 配置后端

由于 `application-dev.yml` 含敏感信息已被 gitignore，需手动创建：

**创建 `backend/src/main/resources/application-dev.yml`**，内容参考：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/supermarket_erp?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的数据库密码

# JWT 配置（secret 请替换为自定义的 Base64 字符串）
jwt:
  secret: 你的Base64密钥
  expiration: 86400000
```

> 若 `backend/target` 下已有 `supermarket-erp-1.0.0.jar`，可直接运行 jar；否则先构建。

### 4. 构建并启动后端

```bash
cd backend
mvn clean package -DskipTests
java -jar target/supermarket-erp-1.0.0.jar
```

后端默认运行在 **http://localhost:8085**，接口文档（Knife4j）：`http://localhost:8085/doc.html`

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 **http://localhost:3005**（已配置 `/api` 代理到 8085）。

## 默认账号

| 账号 | 密码 | 类型 | 说明 |
|---|---|---|---|
| `sysadmin` | `123456` | 系统管理员 | 系统管理端，可管理所有租户 |
| `admin` | `123456` | 租户管理员（租户1） | 租户管理端 |
| `xdl` | `123456` | 租户管理员（租户4） | 租户管理端 |
| `xiaoyu` | `123456` | 收银员（租户4） | 收银 + 订单/仓库 |

> 所有内置账号密码均为 `123456`，生产环境请及时修改。

## 登录说明

- **系统管理端**：`sysadmin` 登录后进入系统管理后台（租户/门店/用户/角色/菜单/操作日志）
- **租户管理端**：租户管理员（如 `admin`/`xdl`）登录后进入租户经营后台
- **收银台**：点击右上角用户菜单"切换到收银"进入 POS

## 常见问题

**Q：前端请求接口 401？**
A：Token 过期或未登录，重新登录即可（Token 有效期默认 24 小时）。

**Q：后端无法连接数据库？**
A：确认 MySQL 已启动、`application-dev.yml` 中数据库名/账号密码正确。

**Q：`application-dev.yml` 缺失导致启动失败？**
A：按上文第 3 步手动创建该文件。

**Q：国内网络 push/pull 慢或连接被重置？**
A：为 git 配置代理：`git config --global http.proxy http://127.0.0.1:7890`（端口按你的代理软件调整）。

## License

本项目仅供学习交流使用。
