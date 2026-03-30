package handlers

import (
	"context"
	"net/http"
	"time"

	"goBackend/internal/httpserver/core"
	"goBackend/internal/httpserver/support"
)

func NewHealthHandler(deps core.RouterDeps, now func() time.Time) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		data := map[string]interface{}{
			"service": "goBackend",
			"time":    now().Format(time.RFC3339Nano),
			"chain": map[string]interface{}{
				"enabled":   deps.ChainMaker != nil,
				"connected": false,
			},
		}

		if deps.ChainMaker != nil {
			chainData := data["chain"].(map[string]interface{})
			chainData["contract"] = deps.ChainMaker.ContractName()
			chainData["timeoutMs"] = deps.ChainMaker.TimeoutMS()

			ctx, cancel := context.WithTimeout(r.Context(), 3*time.Second)
			defer cancel()

			chainInfo, err := deps.ChainMaker.GetChainInfo(ctx)
			if err != nil {
				chainData["error"] = err.Error()
			} else {
				chainData["connected"] = true
				chainData["blockHeight"] = chainInfo.GetBlockHeight()
				chainData["nodeCount"] = len(chainInfo.GetNodeList())
			}
		}

		support.WriteOK(w, data)
	}
}
