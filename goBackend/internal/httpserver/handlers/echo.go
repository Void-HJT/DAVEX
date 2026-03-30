package handlers

import (
	"encoding/json"
	"net/http"
	"time"

	"goBackend/internal/httpserver/core"
	"goBackend/internal/httpserver/support"
)

func NewEchoHandler(_ core.RouterDeps, now func() time.Time) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			support.WriteFail(w, http.StatusMethodNotAllowed, 40501, "method not allowed")
			return
		}

		var payload interface{}
		dec := json.NewDecoder(r.Body)
		dec.UseNumber()
		if err := dec.Decode(&payload); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 40001, "invalid json")
			return
		}

		support.WriteOK(w, map[string]interface{}{
			"receivedAt": now().Format(time.RFC3339Nano),
			"echo":       payload,
		})
	}
}
