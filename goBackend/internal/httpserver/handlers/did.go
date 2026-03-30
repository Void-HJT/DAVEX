package handlers

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"time"

	"goBackend/internal/httpserver/core"
	"goBackend/internal/httpserver/support"
)

func NewGenerateDIDHandler(deps core.RouterDeps, now func() time.Time) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			support.WriteFail(w, http.StatusMethodNotAllowed, 40501, "method not allowed")
			return
		}
		if deps.DID == nil {
			support.WriteFail(w, http.StatusInternalServerError, 50011, "did service not initialized")
			return
		}

		keyPair, err := deps.DID.GenerateKeyPair()
		if err != nil {
			support.WriteFail(w, http.StatusInternalServerError, 1001, "generate key pair failed: "+err.Error())
			return
		}

		doc, err := deps.DID.GenerateDIDDocument(keyPair)
		if err != nil {
			support.WriteFail(w, http.StatusInternalServerError, 1002, "generate did document failed: "+err.Error())
			return
		}

		support.WriteOK(w, map[string]interface{}{
			"did":          doc.ID,
			"address":      keyPair.Address,
			"publicKeyHex": keyPair.PublicKeyHex,
			"privateKey":   fmt.Sprintf("0x%064x", keyPair.PrivateKey.D),
			"didDocument":  doc,
			"generatedAt":  now().Format(time.RFC3339Nano),
		})
	}
}

func NewRegisterDIDHandler(deps core.RouterDeps, _ func() time.Time) http.HandlerFunc {
	type request struct {
		DIDDocument json.RawMessage `json:"didDocument"`
	}
	type didDocLite struct {
		ID string `json:"id"`
	}

	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			support.WriteFail(w, http.StatusMethodNotAllowed, 40501, "method not allowed")
			return
		}
		if deps.ChainMaker == nil {
			support.WriteFail(w, http.StatusServiceUnavailable, 2001, "chainmaker client not initialized")
			return
		}

		var req request
		dec := json.NewDecoder(r.Body)
		dec.UseNumber()
		if err := dec.Decode(&req); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 1003, "invalid json: "+err.Error())
			return
		}
		if len(req.DIDDocument) == 0 {
			support.WriteFail(w, http.StatusBadRequest, 1004, "didDocument is required")
			return
		}

		var doc didDocLite
		_ = json.Unmarshal(req.DIDDocument, &doc)

		ctx, cancel := context.WithTimeout(r.Context(), 5*time.Second)
		defer cancel()

		resp, err := deps.ChainMaker.RegisterDID(ctx, string(req.DIDDocument))
		if err != nil {
			support.WriteFail(w, http.StatusBadGateway, 1005, "invoke contract failed: "+err.Error())
			return
		}
		if !resp.Success {
			support.WriteFail(w, http.StatusBadRequest, 1006, "register did failed: "+resp.Message)
			return
		}

		support.WriteOK(w, map[string]interface{}{
			"did":         doc.ID,
			"txId":        resp.TxID,
			"blockHeight": resp.BlockHeight,
		})
	}
}

func NewQueryDIDHandler(deps core.RouterDeps, _ func() time.Time) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet {
			support.WriteFail(w, http.StatusMethodNotAllowed, 40501, "method not allowed")
			return
		}
		if deps.ChainMaker == nil {
			support.WriteFail(w, http.StatusServiceUnavailable, 2001, "chainmaker client not initialized")
			return
		}

		did := r.URL.Query().Get("did")
		if did == "" {
			support.WriteFail(w, http.StatusBadRequest, 1007, "missing did query param")
			return
		}

		ctx, cancel := context.WithTimeout(r.Context(), 5*time.Second)
		defer cancel()

		resp, err := deps.ChainMaker.GetDIDRegistry(ctx, did)
		if err != nil {
			support.WriteFail(w, http.StatusBadGateway, 1008, "query contract failed: "+err.Error())
			return
		}
		if !resp.Success {
			support.WriteFail(w, http.StatusNotFound, 1009, "did not found or revoked: "+resp.Message)
			return
		}

		support.WriteOK(w, map[string]interface{}{
			"did":    did,
			"result": resp.ResultText,
		})
	}
}
