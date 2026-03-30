<INSTRUCTIONS>
你在 `gosdk/` 目录下工作时，遵守本文件约定（上层 `/AGENTS.md` 也同样生效）。

## 目标
- 提供一个最小可运行的 Java HTTP 服务（用于验证链路）。
- 通过 Controller 调用 Go 后端接口（`goBackend`），并将结果返回给前端调用方。

## 运行方式（必须满足）
- 会以 IntelliJ IDEA 直接运行/调试为主（Run/Debug）。因此不需要在codex中尝试运行。
- 不要求命令行一键启动，但允许提供（如 `mvn spring-boot:run` / `./gradlew bootRun`）。

## 技术选型（推荐但不强制）
- 推荐使用 Spring Boot（简单、IDEA 运行顺滑）。
- Java 版本推荐 `17+`（除非你明确要求更低版本）。


## API（第一阶段：验证链路）
以 `contracts/` 为准，这里只定义必须实现的职责：
- `GET /api/v1/health`
  - Java 自检：返回固定 JSON（包含服务名与时间戳即可）
- `GET /api/v1/control/go-health`
  - Java -> Go：调用 Go 的 `GET /api/v1/health`
  - 返回：包含「Java 自身信息」+「Go 的返回体」的聚合 JSON
- `POST /api/v1/control/echo`
  - Java -> Go：将请求体转发给 Go 的 `POST /api/v1/echo`
  - 返回：尽量透传 Go 的 `code/message/data`，并在 `data` 中附加 `javaTraceId`/`javaTime`
- `POST /api/v1/control/did/generate`
  - Java -> Go：调用 Go 的 `POST /api/v1/did/generate`
- `POST /api/v1/control/did/register`
  - Java -> Go：调用 Go 的 `POST /api/v1/did/register`
- `GET /api/v1/control/did/query?did=...`
  - Java -> Go：调用 Go 的 `GET /api/v1/did/query?did=...`

## Swagger（必须有）
- 使用 SpringDoc（或等价方案）提供 Swagger UI，便于手动调试
- 默认路径（SpringDoc）：`/swagger-ui/index.html`

## 代码组织建议（保持简单）
- Controller：只做参数接收、返回体拼装（不要塞业务逻辑）
- Client：封装对 Go 的调用（如 `GoClient`），便于后续新增接口
- DTO：与 `contracts/` 对齐的请求/响应结构（尽量少）
- 日志：打印关键链路（请求 path、下游 URL、耗时、错误信息），避免刷屏

## 错误处理约定
- 下游（Go）不可达/超时时：Java 返回 `code != 0`，并在 `message` 中说明（如 `go backend unavailable`）。
- 不做复杂异常体系：保证能定位问题即可。

## 待补充功能清单（你后续直接在这里维护）
</INSTRUCTIONS>
