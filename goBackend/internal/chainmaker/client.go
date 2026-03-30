package chainmaker

import (
	"context"
	"encoding/json"
	"fmt"
	"strconv"

	"chainmaker.org/chainmaker/pb-go/v2/common"
	"chainmaker.org/chainmaker/pb-go/v2/discovery"
	sdk "chainmaker.org/chainmaker/sdk-go/v2"
)

type Client struct {
	sdk          *sdk.ChainClient
	contractName string
	timeoutMS    int64
}

type Config struct {
	SDKConfigPath string
	ContractName  string
	TimeoutMS     int64 // -1 means sdk default
}

func NewClient(cfg Config) (*Client, error) {
	if cfg.SDKConfigPath == "" {
		return nil, fmt.Errorf("sdk config path is empty")
	}
	if cfg.ContractName == "" {
		return nil, fmt.Errorf("contract name is empty")
	}

	chainClient, err := sdk.NewChainClient(sdk.WithConfPath(cfg.SDKConfigPath))
	if err != nil {
		return nil, err
	}

	return &Client{
		sdk:          chainClient,
		contractName: cfg.ContractName,
		timeoutMS:    cfg.TimeoutMS,
	}, nil
}

func (c *Client) Close() {
	if c == nil || c.sdk == nil {
		return
	}
	c.sdk.Stop()
}

func (c *Client) ContractName() string {
	if c == nil {
		return ""
	}
	return c.contractName
}

func (c *Client) TimeoutMS() int64 {
	if c == nil {
		return 0
	}
	return c.timeoutMS
}

func (c *Client) GetChainInfo(ctx context.Context) (*discovery.ChainInfo, error) {
	if c == nil || c.sdk == nil {
		return nil, fmt.Errorf("chainmaker client is not initialized")
	}

	type result struct {
		info *discovery.ChainInfo
		err  error
	}

	ch := make(chan result, 1)
	go func() {
		info, err := c.sdk.GetChainInfo()
		ch <- result{info: info, err: err}
	}()

	select {
	case <-ctx.Done():
		return nil, fmt.Errorf("chainmaker GetChainInfo: %w", ctx.Err())
	case r := <-ch:
		return r.info, r.err
	}
}

type ContractResponse struct {
	Success     bool   `json:"success"`
	TxID        string `json:"txId,omitempty"`
	BlockHeight uint64 `json:"blockHeight,omitempty"`
	Code        int32  `json:"code"`
	Message     string `json:"message"`
	Result      []byte `json:"-"`
	ResultText  string `json:"result,omitempty"`
}

type AttributeClaim struct {
	Attribute string `json:"attribute"`
	Operator  string `json:"operator"`
	Value     string `json:"value"`
	Satisfied bool   `json:"satisfied"`
}

type CredentialGroup struct {
	GroupID          string   `json:"groupId"`
	GroupName        string   `json:"groupName"`
	CredentialType   string   `json:"credentialType"`
	IssuerDID        string   `json:"issuerDid"`
	MemberPublicKeys []string `json:"memberPublicKeys"`
	MemberCount      int      `json:"memberCount"`
	AttributePolicy  string   `json:"attributePolicy"`
	MinRingSize      int      `json:"minRingSize"`
	Created          int64    `json:"created"`
	Updated          int64    `json:"updated"`
	Status           string   `json:"status"`
}

type PrivacyVPVerifyResult struct {
	Valid         bool             `json:"valid"`
	GroupID       string           `json:"groupId"`
	GroupName     string           `json:"groupName"`
	Claims        []AttributeClaim `json:"claims"`
	KeyImageUsed  bool             `json:"keyImageUsed"`
	VerifiedAt    int64            `json:"verifiedAt"`
	FailureReason string           `json:"failureReason"`
}

func (c *Client) RegisterDID(ctx context.Context, didDocumentJSON string) (*ContractResponse, error) {
	req := &common.KeyValuePair{Key: "didDocument", Value: []byte(didDocumentJSON)}
	return c.invokeContract(ctx, "RegisterDID", []*common.KeyValuePair{req}, true)
}

func (c *Client) GetDIDRegistry(ctx context.Context, did string) (*ContractResponse, error) {
	req := &common.KeyValuePair{Key: "did", Value: []byte(did)}
	return c.queryContract(ctx, "GetDIDRegistry", []*common.KeyValuePair{req})
}

func (c *Client) IssueVC(ctx context.Context, vcJSON string) (*ContractResponse, error) {
	req := &common.KeyValuePair{Key: "vcJson", Value: []byte(vcJSON)}
	return c.invokeContract(ctx, "IssueVC", []*common.KeyValuePair{req}, true)
}

func (c *Client) VerifyVC(ctx context.Context, vcJSON string) (*ContractResponse, error) {
	req := &common.KeyValuePair{Key: "vcJson", Value: []byte(vcJSON)}
	return c.queryContract(ctx, "VerifyVC", []*common.KeyValuePair{req})
}

func (c *Client) VerifyVP(ctx context.Context, vpJSON, challenge string) (*ContractResponse, error) {
	params := []*common.KeyValuePair{
		{Key: "vpJson", Value: []byte(vpJSON)},
		{Key: "challenge", Value: []byte(challenge)},
	}
	return c.queryContract(ctx, "VerifyVP", params)
}

func (c *Client) CreateCredentialGroup(
	ctx context.Context,
	issuerDID, groupID, groupName, credentialType, attributePolicy string,
	minRingSize int,
) (*ContractResponse, error) {
	params := []*common.KeyValuePair{
		{Key: "issuerDID", Value: []byte(issuerDID)},
		{Key: "groupId", Value: []byte(groupID)},
		{Key: "groupName", Value: []byte(groupName)},
		{Key: "credentialType", Value: []byte(credentialType)},
		{Key: "attributePolicy", Value: []byte(attributePolicy)},
		{Key: "minRingSize", Value: []byte(strconv.Itoa(minRingSize))},
	}
	return c.invokeContract(ctx, "CreateCredentialGroup", params, true)
}

func (c *Client) AddMemberToGroup(ctx context.Context, groupID, memberPublicKeyHex string) (*ContractResponse, error) {
	params := []*common.KeyValuePair{
		{Key: "groupId", Value: []byte(groupID)},
		{Key: "memberPublicKeyHex", Value: []byte(memberPublicKeyHex)},
	}
	return c.invokeContract(ctx, "AddMemberToGroup", params, true)
}

func (c *Client) GetCredentialGroup(ctx context.Context, groupID string) (*ContractResponse, error) {
	params := []*common.KeyValuePair{
		{Key: "groupId", Value: []byte(groupID)},
	}
	return c.queryContract(ctx, "GetCredentialGroup", params)
}

func (c *Client) VerifyPrivacyVP(ctx context.Context, privacyVPJSON, challenge string) (*ContractResponse, error) {
	params := []*common.KeyValuePair{
		{Key: "privacyVpJson", Value: []byte(privacyVPJSON)},
		{Key: "challenge", Value: []byte(challenge)},
	}
	return c.queryContract(ctx, "VerifyPrivacyVP", params)
}

func (c *Client) invokeContract(ctx context.Context, method string, params []*common.KeyValuePair, syncResult bool) (*ContractResponse, error) {
	if c == nil || c.sdk == nil {
		return nil, fmt.Errorf("chainmaker client is not initialized")
	}

	type result struct {
		resp *common.TxResponse
		err  error
	}

	ch := make(chan result, 1)
	go func() {
		resp, err := c.sdk.InvokeContract(
			c.contractName,
			method,
			"",
			params,
			c.timeoutMS,
			syncResult,
		)
		ch <- result{resp: resp, err: err}
	}()

	select {
	case <-ctx.Done():
		return nil, fmt.Errorf("chainmaker InvokeContract(%s): %w", method, ctx.Err())
	case r := <-ch:
		if r.err != nil {
			return nil, r.err
		}
		return toContractResponse(r.resp), nil
	}
}

func (c *Client) queryContract(ctx context.Context, method string, params []*common.KeyValuePair) (*ContractResponse, error) {
	if c == nil || c.sdk == nil {
		return nil, fmt.Errorf("chainmaker client is not initialized")
	}

	type result struct {
		resp *common.TxResponse
		err  error
	}

	ch := make(chan result, 1)
	go func() {
		resp, err := c.sdk.QueryContract(
			c.contractName,
			method,
			params,
			c.timeoutMS,
		)
		ch <- result{resp: resp, err: err}
	}()

	select {
	case <-ctx.Done():
		return nil, fmt.Errorf("chainmaker QueryContract(%s): %w", method, ctx.Err())
	case r := <-ch:
		if r.err != nil {
			return nil, r.err
		}
		return toContractResponse(r.resp), nil
	}
}

func toContractResponse(resp *common.TxResponse) *ContractResponse {
	out := &ContractResponse{
		Success: resp != nil && resp.Code == common.TxStatusCode_SUCCESS,
		Code:    int32(resp.GetCode()),
		Message: resp.GetMessage(),
	}

	if resp == nil {
		out.Success = false
		out.Message = "empty response"
		return out
	}

	out.TxID = resp.TxId
	out.BlockHeight = resp.TxBlockHeight

	if resp.ContractResult != nil {
		out.Result = resp.ContractResult.Result
		out.ResultText = string(resp.ContractResult.Result)
	}

	return out
}

func ParseCredentialGroupFromResponse(resp *ContractResponse) (*CredentialGroup, error) {
	if resp == nil || len(resp.Result) == 0 {
		return nil, fmt.Errorf("empty response")
	}

	var group CredentialGroup
	if err := json.Unmarshal(resp.Result, &group); err != nil {
		return nil, fmt.Errorf("unmarshal credential group failed: %v", err)
	}
	return &group, nil
}

func ParsePrivacyVPVerifyResultFromResponse(resp *ContractResponse) (*PrivacyVPVerifyResult, error) {
	if resp == nil || len(resp.Result) == 0 {
		return nil, fmt.Errorf("empty response")
	}

	var result PrivacyVPVerifyResult
	if err := json.Unmarshal(resp.Result, &result); err != nil {
		return nil, fmt.Errorf("unmarshal verify result failed: %v", err)
	}
	return &result, nil
}

func CheckResponse(resp *ContractResponse) error {
	if resp == nil {
		return fmt.Errorf("response is nil")
	}
	if !resp.Success {
		return fmt.Errorf("[code:%d] %s", resp.Code, resp.Message)
	}
	return nil
}
