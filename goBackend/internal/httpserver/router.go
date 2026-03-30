package httpserver

import (
	"net/http"
	"time"

	"goBackend/internal/httpserver/core"
	"goBackend/internal/httpserver/handlers"
	"goBackend/internal/httpserver/support"
)

type RouterDeps = core.RouterDeps

func NewRouter(deps RouterDeps) http.Handler {
	now := deps.Now
	if now == nil {
		now = time.Now
	}

	mux := http.NewServeMux()
	mux.HandleFunc("/api/v1/health", support.WithCORS(handlers.NewHealthHandler(deps, now)))
	mux.HandleFunc("/api/v1/echo", support.WithCORS(handlers.NewEchoHandler(deps, now)))
	mux.HandleFunc("/api/v1/did/generate", support.WithCORS(handlers.NewGenerateDIDHandler(deps, now)))
	mux.HandleFunc("/api/v1/did/register", support.WithCORS(handlers.NewRegisterDIDHandler(deps, now)))
	mux.HandleFunc("/api/v1/did/query", support.WithCORS(handlers.NewQueryDIDHandler(deps, now)))
	mux.HandleFunc("/api/v1/vc/issue", support.WithCORS(handlers.NewIssueVCHandler(deps, now)))
	mux.HandleFunc("/api/v1/vc/verify", support.WithCORS(handlers.NewVerifyVCHandler(deps, now)))
	mux.HandleFunc("/api/v1/vp/generate", support.WithCORS(handlers.NewGenerateVPHandler(deps, now)))
	mux.HandleFunc("/api/v1/vp/verify", support.WithCORS(handlers.NewVerifyVPHandler(deps, now)))

	mux.HandleFunc("/api/v1/privacy/group/create", support.WithCORS(handlers.NewCreateGroupHandler(deps, now)))
	mux.HandleFunc("/api/v1/privacy/group/member", support.WithCORS(handlers.NewAddMemberHandler(deps, now)))
	mux.HandleFunc("/api/v1/privacy/group", support.WithCORS(handlers.NewGetGroupHandler(deps, now)))
	mux.HandleFunc("/api/v1/privacy/vp/generate", support.WithCORS(handlers.NewGeneratePrivacyVPHandler(deps, now)))
	mux.HandleFunc("/api/v1/privacy/vp/verify", support.WithCORS(handlers.NewVerifyPrivacyVPHandler(deps, now)))
	mux.HandleFunc("/api/v1/privacy/vp/verify-local", support.WithCORS(handlers.NewVerifyPrivacyVPLocalHandler(deps, now)))
	mux.HandleFunc("/api/v1/privacy/claims/build", support.WithCORS(handlers.NewBuildClaimsHandler(deps, now)))
	mux.HandleFunc("/api/v1/privacy/keyimage", support.WithCORS(handlers.NewGetKeyImageHandler(deps, now)))
	mux.HandleFunc("/api/v1/privacy/verify-request/create", support.WithCORS(handlers.NewCreateVerifyRequestHandler(deps, now)))

	mux.HandleFunc("/swagger", support.WithCORS(handlers.SwaggerRedirectHandler()))
	mux.HandleFunc("/swagger/", support.WithCORS(handlers.SwaggerUIHandler()))
	mux.HandleFunc("/swagger/swagger.yaml", support.WithCORS(handlers.SwaggerYAMLHandler()))

	return support.WithLogging(mux)
}
