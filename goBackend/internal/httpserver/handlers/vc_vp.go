package handlers

import (
	"context"
	"fmt"
	"net/http"
	"strings"
	"time"

	"goBackend/internal/chainmaker"
	"goBackend/internal/did"
	"goBackend/internal/httpserver/core"
	"goBackend/internal/httpserver/support"
)

type issueVCRequest struct {
	VC                  *did.VerifiableCredential `json:"vc"`
	IssuerDID           string                    `json:"issuerDid"`
	IssuerPrivateKey    string                    `json:"issuerPrivateKey"`
	IssuerPrivateKeyHex string                    `json:"issuerPrivateKeyHex"` // compatibility alias, prefer issuerPrivateKey
	IssuerPublicKeyHex  string                    `json:"issuerPublicKeyHex"`
	HolderDID           string                    `json:"holderDid"`
	CredentialType      string                    `json:"credentialType"`
	CredentialSubject   map[string]interface{}    `json:"credentialSubject"`
}

type verifyVCRequest struct {
	VC *did.VerifiableCredential `json:"vc"`
}

type generateVPRequest struct {
	HolderDID           string                     `json:"holderDid"`
	HolderPrivateKey    string                     `json:"holderPrivateKey"`
	HolderPrivateKeyHex string                     `json:"holderPrivateKeyHex"`
	HolderPublicKeyHex  string                     `json:"holderPublicKeyHex"`
	Credentials         []did.VerifiableCredential `json:"credentials"`
	Challenge           string                     `json:"challenge"`
	VerifierDID         string                     `json:"verifierDid"`
}

type verifyVPRequest struct {
	VP        *did.VerifiablePresentation `json:"vp"`
	Challenge string                      `json:"challenge"`
}

func NewIssueVCHandler(deps core.RouterDeps, _ func() time.Time) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			support.WriteFail(w, http.StatusMethodNotAllowed, 40501, "method not allowed")
			return
		}
		if deps.DID == nil {
			support.WriteFail(w, http.StatusInternalServerError, 50011, "did service not initialized")
			return
		}
		if deps.ChainMaker == nil {
			support.WriteFail(w, http.StatusServiceUnavailable, 2001, "chainmaker client not initialized")
			return
		}

		var req issueVCRequest
		if err := support.DecodeJSON(r, &req); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 2001, "invalid json: "+err.Error())
			return
		}

		var (
			vc     *did.VerifiableCredential
			vcJSON string
			err    error
		)

		if req.VC != nil {
			if req.VC.Issuer == "" {
				support.WriteFail(w, http.StatusBadRequest, 2002, "vc.issuer is required")
				return
			}
			if req.IssuerDID != "" && req.IssuerDID != req.VC.Issuer {
				support.WriteFail(w, http.StatusBadRequest, 2003, "issuerDid does not match vc.issuer")
				return
			}
			vc = req.VC
			vcJSON, err = vc.ToJSON()
			if err != nil {
				support.WriteFail(w, http.StatusInternalServerError, 2004, "serialize vc failed: "+err.Error())
				return
			}
		} else {
			if req.IssuerDID == "" || req.HolderDID == "" || req.CredentialType == "" || req.CredentialSubject == nil {
				support.WriteFail(w, http.StatusBadRequest, 2005, "issuerDid, holderDid, credentialType and credentialSubject are required")
				return
			}

			keyPair, err := buildIssuerKeyPair(deps.DID, req.IssuerPrivateKey, req.IssuerPrivateKeyHex, req.IssuerPublicKeyHex)
			if err != nil {
				support.WriteFail(w, http.StatusBadRequest, 2006, "recover issuer key pair failed: "+err.Error())
				return
			}

			derivedIssuerDID := did.DIDFromAddress(keyPair.Address)
			if req.IssuerDID != derivedIssuerDID {
				support.WriteFail(w, http.StatusBadRequest, 2007, "issuerDid does not match keypair-derived did")
				return
			}

			vc, err = deps.DID.GenerateVC(req.IssuerDID, keyPair, req.HolderDID, req.CredentialType, req.CredentialSubject)
			if err != nil {
				support.WriteFail(w, http.StatusInternalServerError, 2008, "generate vc failed: "+err.Error())
				return
			}

			vcJSON, err = vc.ToJSON()
			if err != nil {
				support.WriteFail(w, http.StatusInternalServerError, 2009, "serialize vc failed: "+err.Error())
				return
			}
		}

		ctx, cancel := context.WithTimeout(r.Context(), 5*time.Second)
		defer cancel()
		resp, err := deps.ChainMaker.IssueVC(ctx, vcJSON)
		if err != nil {
			support.WriteFail(w, http.StatusBadGateway, 2010, "invoke contract failed: "+err.Error())
			return
		}
		if err := chainmaker.CheckResponse(resp); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 2011, "issue vc failed: "+err.Error())
			return
		}

		support.WriteOK(w, map[string]interface{}{
			"vc":          vc,
			"txId":        resp.TxID,
			"blockHeight": resp.BlockHeight,
		})
	}
}

func NewVerifyVCHandler(deps core.RouterDeps, _ func() time.Time) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			support.WriteFail(w, http.StatusMethodNotAllowed, 40501, "method not allowed")
			return
		}
		if deps.ChainMaker == nil {
			support.WriteFail(w, http.StatusServiceUnavailable, 2001, "chainmaker client not initialized")
			return
		}

		var req verifyVCRequest
		if err := support.DecodeJSON(r, &req); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 2101, "invalid json: "+err.Error())
			return
		}
		if req.VC == nil {
			support.WriteFail(w, http.StatusBadRequest, 2102, "vc is required")
			return
		}

		vcJSON, err := req.VC.ToJSON()
		if err != nil {
			support.WriteFail(w, http.StatusInternalServerError, 2103, "serialize vc failed: "+err.Error())
			return
		}

		ctx, cancel := context.WithTimeout(r.Context(), 5*time.Second)
		defer cancel()
		resp, err := deps.ChainMaker.VerifyVC(ctx, vcJSON)
		if err != nil {
			support.WriteFail(w, http.StatusBadGateway, 2104, "invoke contract failed: "+err.Error())
			return
		}
		if err := chainmaker.CheckResponse(resp); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 2105, "verify vc failed: "+err.Error())
			return
		}
		valid, err := parseContractBoolResult(resp)
		if err != nil {
			support.WriteFail(w, http.StatusBadRequest, 2106, "parse verify vc result failed: "+err.Error())
			return
		}

		support.WriteOK(w, map[string]interface{}{
			"valid":  valid,
			"result": resp.ResultText,
		})
	}
}

func NewGenerateVPHandler(deps core.RouterDeps, _ func() time.Time) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			support.WriteFail(w, http.StatusMethodNotAllowed, 40501, "method not allowed")
			return
		}
		if deps.DID == nil {
			support.WriteFail(w, http.StatusInternalServerError, 50011, "did service not initialized")
			return
		}

		var req generateVPRequest
		if err := support.DecodeJSON(r, &req); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 3001, "invalid json: "+err.Error())
			return
		}
		if req.HolderDID == "" || len(req.Credentials) == 0 || req.Challenge == "" {
			support.WriteFail(w, http.StatusBadRequest, 3002, "holderDid, credentials and challenge are required")
			return
		}

		privateKey := req.HolderPrivateKeyHex
		if privateKey == "" {
			privateKey = req.HolderPrivateKey
		}
		if privateKey == "" {
			support.WriteFail(w, http.StatusBadRequest, 3003, "holderPrivateKey is required")
			return
		}

		var (
			keyPair *did.KeyPair
			err     error
		)
		if req.HolderPublicKeyHex != "" {
			keyPair, err = deps.DID.RecoverKeyPairFromHex(privateKey, req.HolderPublicKeyHex)
		} else {
			keyPair, err = deps.DID.RecoverKeyPairFromPrivateHex(privateKey)
		}
		if err != nil {
			support.WriteFail(w, http.StatusBadRequest, 3004, "recover holder key pair failed: "+err.Error())
			return
		}
		derivedHolderDID := did.DIDFromAddress(keyPair.Address)
		if req.HolderDID != derivedHolderDID {
			support.WriteFail(w, http.StatusBadRequest, 3005, "holderDid does not match keypair-derived did")
			return
		}

		vp, err := deps.DID.GenerateVP(req.HolderDID, keyPair, req.Credentials, req.Challenge, req.VerifierDID)
		if err != nil {
			support.WriteFail(w, http.StatusInternalServerError, 3006, "generate vp failed: "+err.Error())
			return
		}

		support.WriteOK(w, map[string]interface{}{
			"vp": vp,
		})
	}
}

func NewVerifyVPHandler(deps core.RouterDeps, _ func() time.Time) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			support.WriteFail(w, http.StatusMethodNotAllowed, 40501, "method not allowed")
			return
		}
		if deps.ChainMaker == nil {
			support.WriteFail(w, http.StatusServiceUnavailable, 2001, "chainmaker client not initialized")
			return
		}

		var req verifyVPRequest
		if err := support.DecodeJSON(r, &req); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 3101, "invalid json: "+err.Error())
			return
		}
		if req.VP == nil || req.Challenge == "" {
			support.WriteFail(w, http.StatusBadRequest, 3102, "vp and challenge are required")
			return
		}

		vpJSON, err := req.VP.ToJSON()
		if err != nil {
			support.WriteFail(w, http.StatusInternalServerError, 3103, "serialize vp failed: "+err.Error())
			return
		}

		ctx, cancel := context.WithTimeout(r.Context(), 5*time.Second)
		defer cancel()
		resp, err := deps.ChainMaker.VerifyVP(ctx, vpJSON, req.Challenge)
		if err != nil {
			support.WriteFail(w, http.StatusBadGateway, 3104, "invoke contract failed: "+err.Error())
			return
		}
		if err := chainmaker.CheckResponse(resp); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 3105, "verify vp failed: "+err.Error())
			return
		}
		valid, err := parseContractBoolResult(resp)
		if err != nil {
			support.WriteFail(w, http.StatusBadRequest, 3106, "parse verify vp result failed: "+err.Error())
			return
		}

		support.WriteOK(w, map[string]interface{}{
			"valid":  valid,
			"result": resp.ResultText,
		})
	}
}

func buildIssuerKeyPair(svc *did.Service, privateKey, privateKeyCompatHex, publicKeyHex string) (*did.KeyPair, error) {
	if privateKey == "" {
		privateKey = privateKeyCompatHex
	}
	if privateKey == "" {
		return nil, fmt.Errorf("issuer private key is required")
	}
	if publicKeyHex != "" {
		return svc.RecoverKeyPairFromHex(privateKey, publicKeyHex)
	}
	return svc.RecoverKeyPairFromPrivateHex(privateKey)
}

func parseContractBoolResult(resp *chainmaker.ContractResponse) (bool, error) {
	if resp == nil {
		return false, fmt.Errorf("empty response")
	}
	result := strings.TrimSpace(resp.ResultText)
	result = strings.Trim(result, "\"")
	switch strings.ToLower(result) {
	case "true":
		return true, nil
	case "false":
		return false, nil
	default:
		return false, fmt.Errorf("unexpected bool result: %s", resp.ResultText)
	}
}
