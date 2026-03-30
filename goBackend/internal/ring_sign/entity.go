// Copyright (C) BABEC. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package ring_sign

import (
	"crypto/ecdsa"
	"encoding/json"
)

// ============================================================================
// 常量定义
// ============================================================================

const (
	// 隐私VP类型
	PrivacyVPType = "PrivacyPreservingPresentation"

	// 环签名算法类型
	RingSignatureAlgorithmLSAG = "LSAG-P256"

	// 属性声明运算符
	ClaimOperatorGTE    = ">="
	ClaimOperatorLTE    = "<="
	ClaimOperatorEQ     = "=="
	ClaimOperatorNEQ    = "!="
	ClaimOperatorIn     = "in"
	ClaimOperatorExists = "exists"
)

// ============================================================================
// 密钥对
// ============================================================================

// KeyPair 密钥对
type KeyPair struct {
	PrivateKey   *ecdsa.PrivateKey
	PublicKey    *ecdsa.PublicKey
	Address      string
	PublicKeyHex string
}

// ============================================================================
// 凭证相关
// ============================================================================

// VerifiableCredential 可验证凭证
type VerifiableCredential struct {
	ID                string                 `json:"id"`
	Type              []string               `json:"type"`
	Issuer            string                 `json:"issuer"`
	IssuanceDate      string                 `json:"issuanceDate"`
	ExpirationDate    string                 `json:"expirationDate"`
	CredentialSubject map[string]interface{} `json:"credentialSubject"`
	CredentialStatus  interface{}            `json:"credentialStatus,omitempty"`
	Proof             interface{}            `json:"proof"`
}

// ============================================================================
// 环签名相关
// ============================================================================

// AttributeClaim 属性声明（选择性披露）
type AttributeClaim struct {
	Attribute string `json:"attribute"`
	Operator  string `json:"operator"`
	Value     string `json:"value"`
	Satisfied bool   `json:"satisfied"`
}

// RingSignature 环签名结构
type RingSignature struct {
	GroupID   string   `json:"groupId"`
	KeyImage  string   `json:"keyImage"`
	C         []string `json:"c"`
	R         []string `json:"r"`
	Message   string   `json:"message"`
	RingSize  int      `json:"ringSize"`
	Algorithm string   `json:"algorithm"`
}

// PrivacyPreservingVP 隐私保护可验证声明
type PrivacyPreservingVP struct {
	ID             string           `json:"id"`
	Type           string           `json:"type"`
	HolderKeyImage string           `json:"holderKeyImage"`
	Claims         []AttributeClaim `json:"claims"`
	GroupID        string           `json:"groupId"`
	RingSignature  *RingSignature   `json:"ringSignature"`
	Challenge      string           `json:"challenge"`
	Domain         string           `json:"domain,omitempty"`
	Created        string           `json:"created"`
	CredentialType string           `json:"credentialType,omitempty"`
	IssuerDID      string           `json:"issuerDid,omitempty"`
}

// PrivacyVPVerifyResult 隐私VP验证结果
type PrivacyVPVerifyResult struct {
	Valid         bool             `json:"valid"`
	GroupID       string           `json:"groupId"`
	GroupName     string           `json:"groupName"`
	Claims        []AttributeClaim `json:"claims"`
	KeyImageUsed  bool             `json:"keyImageUsed"`
	VerifiedAt    int64            `json:"verifiedAt"`
	FailureReason string           `json:"failureReason"`
}

// GroupMemberInfo 群组成员信息（链下保存）
type GroupMemberInfo struct {
	GroupID      string `json:"groupId"`
	GroupName    string `json:"groupName"`
	MyIndex      int    `json:"myIndex"`
	JoinedAt     int64  `json:"joinedAt"`
	RelatedVCID  string `json:"relatedVcId"`
	PublicKeyHex string `json:"publicKeyHex"`
}

// ============================================================================
// 辅助方法
// ============================================================================

// ToJSON 将隐私VP转换为JSON字符串
func (pvp *PrivacyPreservingVP) ToJSON() (string, error) {
	data, err := json.Marshal(pvp)
	if err != nil {
		return "", err
	}
	return string(data), nil
}

// ToPrettyJSON 将隐私VP转换为格式化的JSON字符串
func (pvp *PrivacyPreservingVP) ToPrettyJSON() (string, error) {
	data, err := json.MarshalIndent(pvp, "", "  ")
	if err != nil {
		return "", err
	}
	return string(data), nil
}

// CreateSimpleClaim 创建简单的属性声明
func CreateSimpleClaim(attribute, operator, value string) AttributeClaim {
	return AttributeClaim{
		Attribute: attribute,
		Operator:  operator,
		Value:     value,
		Satisfied: false,
	}
}

// CreateAgeGTEClaim 创建年龄大于等于的声明
func CreateAgeGTEClaim(minAge int) AttributeClaim {
	return CreateSimpleClaim("age", ClaimOperatorGTE, string(rune(minAge)))
}

// CreateExistsClaim 创建属性存在声明
func CreateExistsClaim(attribute string) AttributeClaim {
	return CreateSimpleClaim(attribute, ClaimOperatorExists, "")
}
