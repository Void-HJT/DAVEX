# goBackend

Go 后端用于承接 Java -> Go -> ChainMaker 链路，并提供 DID/VC/VP 与环签名能力（不包含跨域认证功能）。

## 运行

默认端口 `8081`，默认 SDK 配置 `./configs/sdk_config.yml`。

```bash
cd project_test/goBackend
GO_PORT=8081 \
CHAINMAKER_SDK_CONFIG=./configs/sdk_config.yml \
CHAINMAKER_CONTRACT=simpleDidTest1 \
CHAINMAKER_TIMEOUT_MS=-1 \
go run ./cmd/server
```

## 接口

- `GET /api/v1/health`
- `POST /api/v1/echo`
- `POST /api/v1/did/generate`
- `POST /api/v1/did/register`
- `GET  /api/v1/did/query?did=...`
- `POST /api/v1/vc/issue`
- `POST /api/v1/vc/verify`
- `POST /api/v1/vp/generate`
- `POST /api/v1/vp/verify`
- `POST /api/v1/privacy/group/create`
- `POST /api/v1/privacy/group/member`
- `GET  /api/v1/privacy/group?groupId=...`
- `POST /api/v1/privacy/vp/generate`
- `POST /api/v1/privacy/vp/verify`
- `POST /api/v1/privacy/vp/verify-local`
- `POST /api/v1/privacy/claims/build`
- `POST /api/v1/privacy/keyimage`
- `POST /api/v1/privacy/verify-request/create`

## Swagger

- `GET /swagger/`
- `GET /swagger/swagger.yaml`
