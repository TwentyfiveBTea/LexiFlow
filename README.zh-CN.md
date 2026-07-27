# LexiFlow

[English](README.md) | **简体中文**

LexiFlow: 译流

一个面向英语、日语文章精读与词汇记忆的全栈应用。用户可以上传外语文章，完成文本提取、语言识别、翻译和分级词汇分析，并将文章词汇加入个人词汇库，通过复习计划持续学习。

## 主要功能

- 用户注册、登录、JWT 鉴权与个人资料管理
- 上传 PDF、DOC、DOCX、TXT、Markdown、HTML 文章
- 使用 Apache Tika 提取正文，扫描型 PDF 可通过 AI OCR 兜底识别
- 自动识别英语和日语文章
- 异步翻译文章并记录处理状态
- 按 CET4、CET6、GRE、IELTS、JLPT N1-N5 等等级分析文章词汇
- 在阅读器中查看释义、音标、词汇等级及词汇首次出现位置
- 创建个人词汇库，管理文章词汇与学习状态
- 根据复习质量计算下一次复习时间
- Credits 账户、消费流水、充值订单和易支付回调
- 中文、英文、日文界面切换与阅读偏好设置

## 技术栈

### 前端

- Vue 3.5
- TypeScript 5.8
- Vite 7
- Pinia
- Vue Router
- TanStack Vue Query
- Axios
- Tailwind CSS 4
- Lucide Icons

### 后端

- Java 21
- Spring Boot 3.5
- Spring MVC / Validation / Scheduling / Async
- MyBatis-Plus
- MySQL 8
- Redis
- JWT
- Apache Tika 3
- Apache PDFBox 3
- Stanford CoreNLP
- LangChain4j
- AWS S3 兼容对象存储

## 系统流程

```text
浏览器
  │
  ├── 页面与静态资源 ──> Vue / Vite
  │
  └── API 请求 ────────> Spring Boot
                              ├── MySQL
                              ├── Redis
                              ├── S3 兼容对象存储
                              ├── AI 翻译 / OCR 服务
                              └── 易支付网关
```

文章上传后由后端异步处理。前端通过处理状态接口轮询解析进度，文本提取与翻译完成后进入阅读器；词汇等级分析也是异步任务，不会长时间阻塞 HTTP 请求。

## 项目结构

```text
LexiFlow/
├── backend/                    # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/btea/lexiflow/
│       ├── article/            # 文章上传、解析、翻译和词汇分析
│       ├── common/             # 返回结构、异常、上下文和缓存基础设施
│       ├── infrastructure/     # 安全、异步、对象存储和 Web 配置
│       ├── learning/           # 复习计划和学习进度
│       ├── pay/                # Credits、订单、计费和支付回调
│       ├── user/               # 认证与用户资料
│       └── vocab/              # 词汇库、词典和缓存
├── frontend/                   # Vue 前端
│   ├── src/components/
│   ├── src/lib/
│   ├── src/stores/
│   └── src/views/
├── LICENSE
├── README.md
└── README.zh-CN.md
```

运行配置和构建产物默认不提交到 Git。

## 环境要求

- JDK 21
- Maven 3.9+
- Node.js 20+
- pnpm 9+
- MySQL 8+
- Redis 6+
- S3 兼容对象存储
- 可选：OpenAI 兼容的模型接口，用于翻译和 PDF OCR
- 可选：易支付商户，用于 Credits 充值

检查环境：

```bash
java -version
mvn -version
node -v
pnpm -v
mysql --version
redis-cli --version
```

## 本地开发

启动应用前，需要在 `backend/src/main/resources/` 下准备后端运行配置。这些文件与具体环境相关，不提交到仓库。

### 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认地址：

```text
http://localhost:8888
```

### 启动前端

```bash
cd frontend
pnpm install
pnpm dev
```

前端默认地址：

```text
http://localhost:5173
```

开发服务器会将 `/api` 请求代理到 `http://localhost:8888`。前端 API 基础路径默认是 `/api/v1`，也可以在 `frontend/.env` 中配置：

```dotenv
VITE_API_BASE_URL=/api/v1
```

## 构建

### 构建后端 JAR

标准构建：

```bash
cd backend
mvn clean package
```

不编译和执行测试源码时：

```bash
mvn clean package -Dmaven.test.skip=true
```

产物：

```text
backend/target/lexiflow-backend-1.0.0-SNAPSHOT.jar
```

`-DskipTests` 只跳过测试执行，仍会编译测试；`-Dmaven.test.skip=true` 会同时跳过测试编译与执行。

### 构建前端 dist

```bash
cd frontend
pnpm install --frozen-lockfile
pnpm build
```

产物：

```text
frontend/dist/
```

构建过程会先执行 Vue TypeScript 类型检查，再生成生产资源。

## License

本项目采用 [GNU Affero General Public License v3.0](LICENSE)。
