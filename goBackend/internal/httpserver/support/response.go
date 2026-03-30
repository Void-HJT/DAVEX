package support

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
)

type APIResponse struct {
	Code    int         `json:"code"`
	Message string      `json:"message"`
	Data    interface{} `json:"data"`
}

func WriteOK(w http.ResponseWriter, data interface{}) {
	writeJSON(w, http.StatusOK, APIResponse{
		Code:    0,
		Message: "ok",
		Data:    data,
	})
}

func WriteFail(w http.ResponseWriter, httpStatus int, code int, message string) {
	writeJSON(w, httpStatus, APIResponse{
		Code:    code,
		Message: message,
		Data:    nil,
	})
}

func writeJSON(w http.ResponseWriter, status int, body interface{}) {
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(body)
}

func DecodeJSON(r *http.Request, out interface{}) error {
	if r.Body == nil {
		return fmt.Errorf("empty request body")
	}
	defer func() { _ = r.Body.Close() }()

	dec := json.NewDecoder(r.Body)
	dec.UseNumber()
	if err := dec.Decode(out); err != nil {
		return err
	}
	if err := dec.Decode(&struct{}{}); err != io.EOF {
		return fmt.Errorf("request body must contain a single JSON object")
	}
	return nil
}
