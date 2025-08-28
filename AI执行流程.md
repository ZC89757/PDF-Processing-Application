- [ ] 依赖

  **maven**

  - **Spring Boot Web**（`spring-boot-starter-web`）：REST API 开发。
  - **Spring Boot Validation**（`spring-boot-starter-validation`）：参数校验。
  - **Spring Boot Data JDBC 或 MyBatis**（`spring-boot-starter-data-jdbc` 或 MyBatis-Spring-Boot-Starter）：数据库访问。
  - **PostgreSQL 驱动**（`postgresql`）：数据库连接。
  - **Spring Boot Starter AOP**：全局异常处理/切面。
  - **Spring Boot Starter Async**：异步任务（PDF 解析）。
  - **Apache PDFBox**：PDF 解析、目录提取、文本抽取。
  - **Lombok**：实体类简化。
  - **Jackson**（随 Web Starter 自带）：JSON 结果返回。
  - **日志**（Logback 默认即可）。

  **前端**

  - **Vue 3**：前端框架。
  - **Vue Router**：路由。
  - **Element Plus**：UI 组件。
  - **pdf.js**：PDF 渲染。
  - **axios**：HTTP 请求。

- [ ] **数据库表**（生成建表语句）

- 文件元数据表：`t_files`：`id(bigserial PK)`, `filename(varchar)`, `size(bigint)`, `path(varchar)`, `status(varchar: UPLOADED|PARSING|READY|FAILED)`, `has_outline(boolean)`, `upload_time(timestamptz default now())`, `summary(text)`

- 目录信息表：`t_file_outline`：`id PK`, `file_id FK`, `title(varchar)`, `page(int)`, `level(int)`

- 段落信息表：`t_file_segments`：`id PK`, `file_id FK`, `page(int)`, `content`, `ts tsvector`（生成索引用）

- 触发器：`ts` = `to_tsvector('simple', content)`；索引：`CREATE INDEX idx_segments_ts ON file_segments USING GIN(ts);`

- [ ] **Java 实体**

- `FileEntity(id, filename, size, path, status, hasOutline, uploadTime, summary)`

- `FileOutlineEntity(id, fileId, title, page, level)`

- `FileSegmentEntity(id, fileId, page, content)`

- [ ] **返回结果类**

- 400：参数非法（页码越界、非 PDF、超限）。

- 404：文件不存在/已删除。

- 500：其他错误。

- data一律用JSONObject或者JSONArray存储。

- Result：`{code:0,msg:"ok",data:...}`；错误 `{code!=0,msg:"错误原因"}`。

## 后端

- [ ] 1.全局配置

- `com.app.pdf.Config`: CORS 允许 `GET,POST,PUT,DELETE`，`/api/**`；全局异常 `@ControllerAdvice`。
- `application.yml`：Postgres 连接、上传位置`storage.root=/data/files`，最大上传 200MB。

- [ ] 2.文件上传接口：FileController

- **API**：`POST /api/files/upload`（`multipart/form-data`，字段：`file`）

- **流程方法**：

  - 校验文件名和文件类型：MIME=application/pdf；size ≤ 200MB；文件名不含非法字符。
  - `FileService.saveUpload(MultipartFile file) : Result`
  -  路径 `${root}/{fileId}.pdf` → 保存磁盘 → 插入表`t_files`（status=UPLOADED, has_outline=null），并将返回的id放入一个静态的队列（任务队列）中→ 返回 Result。

- **DAO SQL**（MyBatis）：

  ```sql
  INSERT INTO t_files(filename,size,path,status) VALUES(?,?,?, 'UPLOADED') RETURNING id;
  ```

- [ ] 3.上传后

- **流程方法**：检查到有新任务后`ParseService.enqueueParse(Long fileId)` → 立即 `@Async` 调 `ParseService.parsePdf(fileId)`
- **DAO**：`UPDATE t_files SET status='PARSING' WHERE id=?;`

- [ ] 4.搜索文件并返回列表：FileController

- **API**：`GET /api/files?page=1&size=20&keyword=?`

- **SQL**：

  ```sql
  SELECT f.id,
         f.filename,
         f.upload_time,
         f.status,
         f.has_outline,
         (f.summary IS NOT NULL) AS summary_exists
  FROM t_files f
  WHERE :kw IS NULL
     OR EXISTS (
         SELECT 1
         FROM t_file_segments s
         WHERE s.file_id = f.id
           AND s.content ILIKE CONCAT('%', :kw, '%')
     )
  ORDER BY f.upload_time DESC
  LIMIT :size OFFSET (:page - 1) * :size;
  ```

- **方法**：`FileService.listFiles(int page,int size,String keyword): Result`

- [ ] 5.解析处理pdf方法

**方法**：`ParseService.parsePdf(Long fileId)`
 步骤：

- 读取 `files.path` → 用 PDFBox `PDDocument` 打开。
- `parseOutline(document)` → 存 `file_outline`；设置 `has_outline=true/false`。
- `segmentAndPersist(fileId)` 
- `UPDATE files SET status='READY'`；异常时 `FAILED` 并记录日志。

**方法**：`OutlineService.parseAndSave(Long fileId, PDDocument doc)`

* 目录解析（有目录就存，没有就 `has_outline=false`）
* INSERT INTO t_file_outline(file_id,title,page,level) VALUES(?,?,?,?);
  UPDATE t_files SET has_outline = ? WHERE id=?;

**方法**：`SegmentService.segmentAndPersist(Long fileId)`

- 按页取 文本，以**空行**或**段落分隔符**粗分
- 插入 `file_segments(file_id,page,content)`（触发器自动维护 `ts`）。

- **SQL（批量）**：`INSERT INTO file_segments(file_id,page,content) VALUES (?,?,?,?);`

- [ ] 6.生成目录：FileController

- **API**：`GET /api/files/{id}/outline` → Result（后端按 level 组树）。
- `OutlineController.buildTree(Long fileId)`
- **SQL**：`SELECT title,page,level FROM t_file_outline WHERE file_id=? ORDER BY id;`

- [ ] 7.摘要获取/生成接口：FileController

- **API**：`GET /api/files/{id}/summary`
- **流程方法**：`SummaryService.getOrGenerate(Long fileId): Result`
  - 查 `files.summary`，若有返回。
  - 若无：
    - 取 `file_outline`（若为空，从 `file_segments` 随机/首段抽取若干段）
    - 组 Prompt（包括：文档名、目录标题若干、代表性段落），**长度控制**。
    - `AiClient.generateSummary(prompt): String`
    - `UPDATE t_files SET summary=? WHERE id=?`
    - 返回 `Result`。
- AI 客户端
  - 接口：`AiClient { String generateSummary(String prompt); }`
  - 实现：LMClient（从 `application.yml` 读取密钥与模型名）。

## 前端（Vue3 + Element Plus + pdf.js + Vue Router）

- [ ] 1.目录结构

- `src/components/FileCard.vue`
- `src/pages/Home.vue, Reader.vue`

2.主题与样式（深色）

- 背景 `#0F172A`（slate-900），卡片面板 `#1F2937`（gray-800），搜索按钮 `#10B981`（绿色），上传按钮 `#374151`，文字 `#E5E7EB`。卡片圆角 `12px`，阴影弱。悬浮卡片放大 `scale(1.01)`。

- [ ] 3.首页上传交互——`Home.vue`

- 顶部右侧 **“上传 PDF”**（主按钮，绿色，文案：`上传 PDF`，图标 `Upload`）上传接口：`POST /api/files/upload`
- 点击弹出 `ElUpload`（拖拽+点击），**限制类型/大小**。
- 成功后调用：`POST /api/files/{id}/parse`；显示“解析中”徽标。
- `GET /api/files/page=1&size=20` 刷新列表。

  首页顶部搜索框（居中，圆角 12px，背景 `#111827`，占位“搜索内容…”）

- 输入后 `GET /api/files/page=1&size=20&keyword=?`，渲染结果卡片：缩略图（第一页/缓存）、文件名、**“位于第 X 页”**、snippet（高亮关键字字体 `#10B981`）。

- 点击前端卡片点击携带 `fileId` 和 `page`，进入 `/reader/:fileId?page={page}`。

- [ ] 4.文件列表卡片——`FileCard.vue`

- 默认展示 ：缩略图（用 pdf.js 取第一页 canvas）、文件名、上传时间、状态徽标、**AI 摘要**按钮（橙/绿(已有AI摘要)）点击发送请求Get`/api/files/{id}/summary`
- 点击卡片：路由到 `/reader/:fileId`。

- [ ] 5.`/reader/:fileId` 三栏——`Reader.vue`

初始化发送请求`GET /api/files/{id}/outline` 、`GET /api/files/{id}/summary`（如果AI摘要为空则提示“暂未生成AI摘要”）

- 左：目录树（`OutlineTree.vue`）
  - 深色面板；选中高亮 `#10B981`；Hover 背景 `#111827`；点击跳转某页。
- 中：PDF Viewer（`PdfViewer.vue`）
  - 使用 pdf.js 渲染；顶部浮动页码显示；缩放控件（+、-、重置）。
- 右：AI 摘要（`SummaryPanel.vue`）

- [ ] 6.目录交互

- 点击 `li` → 触发 `router.push({name:'reader', params:{fileId}, query:{page}})`；
- Viewer 监听 `route.query.page` 改变，滚动到指定页。