<INSTRUCTIONS>
你在 `goBackend/` 目录下工作时，遵守本文件约定（上层 `project_test/AGENTS.md` 也同样生效）。写后端接口时，别忘了先写好contract中的规定。

## 目标
- 提供一个最小可运行的 Go HTTP 服务（用于被 Java 调用验证链路）。
- 接口实现简单清晰，便于后续快速新增路由与字段。

## 运行方式（必须满足）
- 通过命令行启动（例如 `go run ...`）。
- 不要求容器化、不要求守护进程化。

## 当前技术实现（以代码为准）
- HTTP 框架：标准库 `net/http` + `http.ServeMux`（未使用 Gin）。
- Swagger：内置在服务中，提供：
  - `GET /swagger/`
  - `GET /swagger/swagger.yaml`
- Go module：`goBackend/` 独立 `go.mod`，与 Java 项目解耦。
- 可参考 `.agents/skills/goBackend`，但实现以当前仓库代码结构为准。

## 配置约定
- Go 服务默认端口：`8081`
- 允许通过环境变量覆盖（建议）：
  - `GO_PORT`：监听端口（默认 `8081`）
- 长安链（ChainMaker）SDK 配置文件路径：
  - `CHAINMAKER_SDK_CONFIG`：默认 `./configs/sdk_config.yml`
  - `CHAINMAKER_CONTRACT`：合约名称（默认 `simpleDidTest1`）
  - `CHAINMAKER_TIMEOUT_MS`：合约调用超时（默认 `-1`，表示 SDK 默认）

## API（第一阶段：验证链路）
以 `contracts/` 为准，这里只定义必须实现的职责：
- `GET /api/v1/health`
  - Go 自检：返回固定 JSON（包含服务名与时间戳即可）
- `POST /api/v1/echo`
  - 回显：将请求 JSON 原样（或结构化后）返回在 `data` 中，用于验证 Java 转发与字段一致性
- `POST /api/v1/did/generate`
  - 在本地生成一个新的 DID，包含密钥对和 DID 文档（不上链）
- `POST /api/v1/did/register`
  - 将 DID 文档注册到区块链上
- `GET /api/v1/did/query?did=...`
  - 根据 DID 查询链上注册信息
- `POST /api/v1/vc/issue`
  - 生成并上链 VC
- `POST /api/v1/vc/verify`
  - 验证 VC
- `POST /api/v1/vp/generate`
  - 本地生成 VP
- `POST /api/v1/vp/verify`
  - 链上环签名验证 VP
- `POST /api/v1/privacy/group/create`
- `POST /api/v1/privacy/group/member`
- `GET /api/v1/privacy/group?groupId=...`
- `POST /api/v1/privacy/vp/generate`
- `POST /api/v1/privacy/vp/verify`
- `POST /api/v1/privacy/vp/verify-local`
- `POST /api/v1/privacy/claims/build`
- `POST /api/v1/privacy/keyimage`
- `POST /api/v1/privacy/verify-request/create`
  - 上述接口用于环签名隐私保护流程

## 目录组织约定（当前已落地）
- `cmd/server/main.go`：程序入口、配置读取、依赖注入。
- `internal/httpserver/router.go`：统一路由注册，仅做组装，不写业务。
- `internal/httpserver/core/deps.go`：路由依赖定义（`RouterDeps`）。
- `internal/httpserver/handlers/`：业务处理器（按领域分文件）：
  - `did.go`、`vc_vp.go`、`privacy.go`、`health.go`、`echo.go`、`swagger.go`
- `internal/httpserver/support/`：通用 HTTP 辅助：
  - `response.go`（统一响应/JSON 解码）
  - `middleware.go`（日志/CORS）

## 新增接口时的实现流程（必须遵守）
1) 先更新 `contracts/` 中对应文档（字段、示例、错误码）。
2) 在 `handlers/` 新增或扩展处理器，复用 `support` 中公共方法。
3) 在 `router.go` 注册新路由。
4) 同步更新 Swagger 内容与 `cmd.txt`（若启动命令有变化）。

## Swagger（必须有）
- `GET /swagger/`：Swagger UI
- `GET /swagger/swagger.yaml`：OpenAPI YAML

## 返回格式约定
所有接口返回 JSON 对象，至少包含：
- `code`: `0` 成功
- `message`: 简短信息
- `data`: 成功时业务数据；失败时可为 `null`

## 代码组织建议（保持简单）
- 结构可以学习`.agents/skills/goBackend`，也可以进行优化，比如像是实体类在参考代码中有两个，实际上完全可以整合成一个，以及功能实现也可以进行分类来写，不用挤在一个文件当中。

## 错误处理约定
- 参数不合法：返回 `code != 0` + 清晰 `message`，HTTP 状态码可用 `400`
- 服务器内部错误：返回 `code != 0`，HTTP 状态码可用 `500`
- 不做复杂中间件体系：能定位问题即可

## DID/VC/VP 当前字段约定（实现细节）
- `did/generate` 的 `privateKey` 固定输出为 `0x` + 64 位十六进制。
- VC 颁发请求优先使用 `issuerPrivateKey`，兼容旧字段 `issuerPrivateKeyHex`。
- 私钥输入若是奇数长度 hex，后端会自动左补 `0` 再解析（兼容旧数据）。

## 注意
我会先运行go后端，用swagger测试链接与接口ok后才会运行java进行测试。
本阶段不实现跨域认证/跨域动态信任度接口。

## 待补充功能清单（你后续直接在这里维护）
</INSTRUCTIONS>
