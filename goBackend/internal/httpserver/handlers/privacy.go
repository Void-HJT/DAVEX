package handlers

import (
	"context"
	"net/http"
	"time"

	"goBackend/internal/chainmaker"
	"goBackend/internal/did"
	"goBackend/internal/httpserver/core"
	"goBackend/internal/httpserver/support"
	"goBackend/internal/ring_sign"
)

type createGroupRequest struct {
	IssuerDID       string `json:"issuerDid"`
	GroupID         string `json:"groupId"`
	GroupName       string `json:"groupName"`
	CredentialType  string `json:"credentialType"`
	AttributePolicy string `json:"attributePolicy"`
	MinRingSize     int    `json:"minRingSize"`
}

type addMemberRequest struct {
	GroupID      string `json:"groupId"`
	PublicKeyHex string `json:"publicKeyHex"`
}

type verifyPrivacyVPRequest struct {
	PrivacyVP *ring_sign.PrivacyPreservingVP `json:"privacyVp"`
	Challenge string                         `json:"challenge"`
}

type generatePrivacyVPRequest struct {
	HolderPrivateKeyHex string                     `json:"holderPrivateKeyHex"`
	HolderPublicKeyHex  string                     `json:"holderPublicKeyHex"`
	GroupID             string                     `json:"groupId"`
	Challenge           string                     `json:"challenge"`
	Claims              []ring_sign.AttributeClaim `json:"claims"`
	CredentialType      string                     `json:"credentialType"`
	IssuerDID           string                     `json:"issuerDid"`
}

type verifyPrivacyVPLocalRequest struct {
	PrivacyVP         *ring_sign.PrivacyPreservingVP `json:"privacyVp"`
	GroupPublicKeys   []string                       `json:"groupPublicKeys"`
	ExpectedChallenge string                         `json:"expectedChallenge"`
}

type buildClaimsRequest struct {
	VC             *did.VerifiableCredential  `json:"vc"`
	RequiredClaims []ring_sign.AttributeClaim `json:"requiredClaims"`
}

type getKeyImageRequest struct {
	PrivateKeyHex string `json:"privateKeyHex"`
	PublicKeyHex  string `json:"publicKeyHex"`
}

type createVerifyRequestReq struct {
	RequiredClaims []ring_sign.AttributeClaim `json:"requiredClaims"`
	AcceptedGroups []string                   `json:"acceptedGroups"`
	Purpose        string                     `json:"purpose"`
	Domain         string                     `json:"domain"`
}

func NewCreateGroupHandler(deps core.RouterDeps, _ func() time.Time) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			support.WriteFail(w, http.StatusMethodNotAllowed, 40501, "method not allowed")
			return
		}
		if deps.ChainMaker == nil {
			support.WriteFail(w, http.StatusServiceUnavailable, 2001, "chainmaker client not initialized")
			return
		}

		var req createGroupRequest
		if err := support.DecodeJSON(r, &req); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 4001, "invalid json: "+err.Error())
			return
		}
		if req.IssuerDID == "" || req.GroupID == "" || req.GroupName == "" || req.CredentialType == "" {
			support.WriteFail(w, http.StatusBadRequest, 4002, "issuerDid, groupId, groupName and credentialType are required")
			return
		}
		if req.MinRingSize <= 0 {
			req.MinRingSize = 3
		}

		ctx, cancel := context.WithTimeout(r.Context(), 5*time.Second)
		defer cancel()
		resp, err := deps.ChainMaker.CreateCredentialGroup(
			ctx,
			req.IssuerDID,
			req.GroupID,
			req.GroupName,
			req.CredentialType,
			req.AttributePolicy,
			req.MinRingSize,
		)
		if err != nil {
			support.WriteFail(w, http.StatusBadGateway, 4003, "invoke contract failed: "+err.Error())
			return
		}
		if err := chainmaker.CheckResponse(resp); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 4004, "create group failed: "+err.Error())
			return
		}

		support.WriteOK(w, map[string]interface{}{
			"groupId":     req.GroupID,
			"txId":        resp.TxID,
			"blockHeight": resp.BlockHeight,
		})
	}
}

func NewAddMemberHandler(deps core.RouterDeps, _ func() time.Time) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			support.WriteFail(w, http.StatusMethodNotAllowed, 40501, "method not allowed")
			return
		}
		if deps.ChainMaker == nil {
			support.WriteFail(w, http.StatusServiceUnavailable, 2001, "chainmaker client not initialized")
			return
		}

		var req addMemberRequest
		if err := support.DecodeJSON(r, &req); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 4011, "invalid json: "+err.Error())
			return
		}
		if req.GroupID == "" || req.PublicKeyHex == "" {
			support.WriteFail(w, http.StatusBadRequest, 4012, "groupId and publicKeyHex are required")
			return
		}

		ctx, cancel := context.WithTimeout(r.Context(), 5*time.Second)
		defer cancel()
		resp, err := deps.ChainMaker.AddMemberToGroup(ctx, req.GroupID, req.PublicKeyHex)
		if err != nil {
			support.WriteFail(w, http.StatusBadGateway, 4013, "invoke contract failed: "+err.Error())
			return
		}
		if err := chainmaker.CheckResponse(resp); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 4014, "add member failed: "+err.Error())
			return
		}

		support.WriteOK(w, map[string]interface{}{
			"groupId":     req.GroupID,
			"txId":        resp.TxID,
			"blockHeight": resp.BlockHeight,
		})
	}
}

func NewGetGroupHandler(deps core.RouterDeps, _ func() time.Time) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet {
			support.WriteFail(w, http.StatusMethodNotAllowed, 40501, "method not allowed")
			return
		}
		if deps.ChainMaker == nil {
			support.WriteFail(w, http.StatusServiceUnavailable, 2001, "chainmaker client not initialized")
			return
		}

		groupID := r.URL.Query().Get("groupId")
		if groupID == "" {
			support.WriteFail(w, http.StatusBadRequest, 4021, "missing groupId query param")
			return
		}

		ctx, cancel := context.WithTimeout(r.Context(), 5*time.Second)
		defer cancel()
		resp, err := deps.ChainMaker.GetCredentialGroup(ctx, groupID)
		if err != nil {
			support.WriteFail(w, http.StatusBadGateway, 4022, "query group failed: "+err.Error())
			return
		}
		group, err := chainmaker.ParseCredentialGroupFromResponse(resp)
		if err != nil {
			support.WriteFail(w, http.StatusBadRequest, 4023, "parse group failed: "+err.Error())
			return
		}
		support.WriteOK(w, group)
	}
}

func NewVerifyPrivacyVPHandler(deps core.RouterDeps, _ func() time.Time) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			support.WriteFail(w, http.StatusMethodNotAllowed, 40501, "method not allowed")
			return
		}
		if deps.ChainMaker == nil {
			support.WriteFail(w, http.StatusServiceUnavailable, 2001, "chainmaker client not initialized")
			return
		}

		var req verifyPrivacyVPRequest
		if err := support.DecodeJSON(r, &req); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 4031, "invalid json: "+err.Error())
			return
		}
		if req.PrivacyVP == nil || req.Challenge == "" {
			support.WriteFail(w, http.StatusBadRequest, 4032, "privacyVp and challenge are required")
			return
		}

		pvpJSON, err := req.PrivacyVP.ToJSON()
		if err != nil {
			support.WriteFail(w, http.StatusInternalServerError, 4033, "serialize privacy vp failed: "+err.Error())
			return
		}

		ctx, cancel := context.WithTimeout(r.Context(), 5*time.Second)
		defer cancel()
		resp, err := deps.ChainMaker.VerifyPrivacyVP(ctx, pvpJSON, req.Challenge)
		if err != nil {
			support.WriteFail(w, http.StatusBadGateway, 4034, "invoke contract failed: "+err.Error())
			return
		}
		result, err := chainmaker.ParsePrivacyVPVerifyResultFromResponse(resp)
		if err != nil {
			support.WriteFail(w, http.StatusBadRequest, 4035, "parse verify result failed: "+err.Error())
			return
		}
		support.WriteOK(w, result)
	}
}

func NewGeneratePrivacyVPHandler(deps core.RouterDeps, _ func() time.Time) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			support.WriteFail(w, http.StatusMethodNotAllowed, 40501, "method not allowed")
			return
		}
		if deps.ChainMaker == nil {
			support.WriteFail(w, http.StatusServiceUnavailable, 2001, "chainmaker client not initialized")
			return
		}
		if deps.DID == nil || deps.Privacy == nil {
			support.WriteFail(w, http.StatusInternalServerError, 50011, "privacy dependencies not initialized")
			return
		}

		var req generatePrivacyVPRequest
		if err := support.DecodeJSON(r, &req); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 4041, "invalid json: "+err.Error())
			return
		}
		if req.HolderPrivateKeyHex == "" || req.HolderPublicKeyHex == "" || req.GroupID == "" || req.Challenge == "" || len(req.Claims) == 0 {
			support.WriteFail(w, http.StatusBadRequest, 4042, "holder keys, groupId, challenge and claims are required")
			return
		}

		ctx, cancel := context.WithTimeout(r.Context(), 5*time.Second)
		defer cancel()
		groupResp, err := deps.ChainMaker.GetCredentialGroup(ctx, req.GroupID)
		if err != nil {
			support.WriteFail(w, http.StatusBadGateway, 4043, "query group failed: "+err.Error())
			return
		}

		group, err := chainmaker.ParseCredentialGroupFromResponse(groupResp)
		if err != nil {
			support.WriteFail(w, http.StatusBadRequest, 4044, "parse group failed: "+err.Error())
			return
		}

		keyPair, err := deps.DID.RecoverKeyPairFromHex(req.HolderPrivateKeyHex, req.HolderPublicKeyHex)
		if err != nil {
			support.WriteFail(w, http.StatusBadRequest, 4045, "recover key pair failed: "+err.Error())
			return
		}

		ringKeyPair := &ring_sign.KeyPair{
			PrivateKey:   keyPair.PrivateKey,
			PublicKey:    keyPair.PublicKey,
			Address:      keyPair.Address,
			PublicKeyHex: keyPair.PublicKeyHex,
		}

		pvp, err := deps.Privacy.GeneratePrivacyVP(
			ringKeyPair,
			req.Claims,
			group.MemberPublicKeys,
			req.GroupID,
			req.Challenge,
			req.CredentialType,
			req.IssuerDID,
		)
		if err != nil {
			support.WriteFail(w, http.StatusBadRequest, 4046, "generate privacy vp failed: "+err.Error())
			return
		}

		support.WriteOK(w, pvp)
	}
}

func NewVerifyPrivacyVPLocalHandler(deps core.RouterDeps, _ func() time.Time) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			support.WriteFail(w, http.StatusMethodNotAllowed, 40501, "method not allowed")
			return
		}
		if deps.Privacy == nil {
			support.WriteFail(w, http.StatusInternalServerError, 50011, "privacy service not initialized")
			return
		}

		var req verifyPrivacyVPLocalRequest
		if err := support.DecodeJSON(r, &req); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 4051, "invalid json: "+err.Error())
			return
		}
		if req.PrivacyVP == nil || len(req.GroupPublicKeys) == 0 || req.ExpectedChallenge == "" {
			support.WriteFail(w, http.StatusBadRequest, 4052, "privacyVp, groupPublicKeys and expectedChallenge are required")
			return
		}

		valid, err := deps.Privacy.VerifyPrivacyVPLocally(req.PrivacyVP, req.GroupPublicKeys, req.ExpectedChallenge)
		if err != nil {
			support.WriteFail(w, http.StatusBadRequest, 4053, "verify locally failed: "+err.Error())
			return
		}

		support.WriteOK(w, map[string]interface{}{
			"valid":     valid,
			"challenge": req.ExpectedChallenge,
			"groupId":   req.PrivacyVP.GroupID,
		})
	}
}

func NewBuildClaimsHandler(deps core.RouterDeps, _ func() time.Time) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			support.WriteFail(w, http.StatusMethodNotAllowed, 40501, "method not allowed")
			return
		}
		if deps.Privacy == nil {
			support.WriteFail(w, http.StatusInternalServerError, 50011, "privacy service not initialized")
			return
		}

		var req buildClaimsRequest
		if err := support.DecodeJSON(r, &req); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 4061, "invalid json: "+err.Error())
			return
		}
		if req.VC == nil || len(req.RequiredClaims) == 0 {
			support.WriteFail(w, http.StatusBadRequest, 4062, "vc and requiredClaims are required")
			return
		}

		ringVC := &ring_sign.VerifiableCredential{
			ID:                req.VC.ID,
			Type:              req.VC.Type,
			Issuer:            req.VC.Issuer,
			IssuanceDate:      req.VC.IssuanceDate,
			ExpirationDate:    req.VC.ExpirationDate,
			CredentialSubject: req.VC.CredentialSubject,
		}
		claims, err := deps.Privacy.BuildAttributeClaims(ringVC, req.RequiredClaims)
		if err != nil {
			support.WriteFail(w, http.StatusBadRequest, 4063, "build claims failed: "+err.Error())
			return
		}

		support.WriteOK(w, map[string]interface{}{
			"claims":       claims,
			"allSatisfied": deps.Privacy.CheckAllClaimsSatisfied(claims),
		})
	}
}

func NewGetKeyImageHandler(deps core.RouterDeps, _ func() time.Time) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			support.WriteFail(w, http.StatusMethodNotAllowed, 40501, "method not allowed")
			return
		}
		if deps.DID == nil {
			support.WriteFail(w, http.StatusInternalServerError, 50011, "did service not initialized")
			return
		}

		var req getKeyImageRequest
		if err := support.DecodeJSON(r, &req); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 4071, "invalid json: "+err.Error())
			return
		}
		if req.PrivateKeyHex == "" || req.PublicKeyHex == "" {
			support.WriteFail(w, http.StatusBadRequest, 4072, "privateKeyHex and publicKeyHex are required")
			return
		}

		keyPair, err := deps.DID.RecoverKeyPairFromHex(req.PrivateKeyHex, req.PublicKeyHex)
		if err != nil {
			support.WriteFail(w, http.StatusBadRequest, 4073, "recover key pair failed: "+err.Error())
			return
		}

		support.WriteOK(w, map[string]interface{}{
			"keyImage":     ring_sign.GetKeyImageHex(keyPair.PrivateKey),
			"publicKeyHex": keyPair.PublicKeyHex,
		})
	}
}

func NewCreateVerifyRequestHandler(deps core.RouterDeps, _ func() time.Time) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			support.WriteFail(w, http.StatusMethodNotAllowed, 40501, "method not allowed")
			return
		}

		var req createVerifyRequestReq
		if err := support.DecodeJSON(r, &req); err != nil {
			support.WriteFail(w, http.StatusBadRequest, 4081, "invalid json: "+err.Error())
			return
		}
		if len(req.RequiredClaims) == 0 || len(req.AcceptedGroups) == 0 || req.Purpose == "" {
			support.WriteFail(w, http.StatusBadRequest, 4082, "requiredClaims, acceptedGroups and purpose are required")
			return
		}

		verifyRequest := ring_sign.CreateVerifyRequest(req.RequiredClaims, req.AcceptedGroups, req.Purpose)
		verifyRequest.Domain = req.Domain
		support.WriteOK(w, verifyRequest)
	}
}
