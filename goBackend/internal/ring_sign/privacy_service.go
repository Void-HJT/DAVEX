// Copyright (C) BABEC. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package ring_sign

import (
	"crypto/ecdsa"
	"crypto/rand"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"strconv"
	"time"
)

// PrivacyService 隐私保护服务
// 提供链下生成隐私保护VP的功能
type PrivacyService struct {
	ringSigner *RingSigner
}

// NewPrivacyService 创建隐私保护服务实例
func NewPrivacyService() *PrivacyService {
	return &PrivacyService{
		ringSigner: NewRingSigner(),
	}
}

// GeneratePrivacyVP 生成隐私保护VP
// 参数:
//   - holderKeyPair: 持有者密钥对
//   - claims: 属性声明列表
//   - groupPublicKeys: 群组公钥列表（从链上获取）
//   - groupID: 群组ID
//   - challenge: 验证者提供的挑战值
//   - credentialType: 凭证类型（可选）
//   - issuerDID: 颁发者DID（可选）
//
// 返回:
//   - *PrivacyPreservingVP: 隐私保护VP
//   - error: 错误信息
func (ps *PrivacyService) GeneratePrivacyVP(
	holderKeyPair *KeyPair,
	claims []AttributeClaim,
	groupPublicKeys []string,
	groupID string,
	challenge string,
	credentialType string,
	issuerDID string,
) (*PrivacyPreservingVP, error) {

	// 1. 解析群组公钥
	publicKeys, err := ps.ringSigner.ParsePublicKeysFromHex(groupPublicKeys)
	if err != nil {
		return nil, fmt.Errorf("parse group public keys failed: %v", err)
	}

	// 2. 找到持有者在群组中的索引
	signerIndex := ps.ringSigner.FindSignerIndex(publicKeys, holderKeyPair.PublicKey)
	if signerIndex == -1 {
		return nil, fmt.Errorf("holder not in group")
	}

	// 3. 构建待签名消息
	message, err := ps.buildSignMessage(claims, groupID, challenge)
	if err != nil {
		return nil, fmt.Errorf("build sign message failed: %v", err)
	}

	// 4. 生成环签名
	signatureData, err := ps.ringSigner.Sign(
		holderKeyPair.PrivateKey,
		publicKeys,
		signerIndex,
		message,
	)
	if err != nil {
		return nil, fmt.Errorf("generate ring signature failed: %v", err)
	}

	// 5. 转换环签名格式
	ringSignature := ps.ringSigner.ConvertToRingSignature(signatureData, groupID, message)

	// 6. 计算密钥映像
	keyImageHex := ps.ringSigner.GetKeyImageHex(holderKeyPair.PrivateKey)

	// 7. 构建隐私保护VP
	pvp := &PrivacyPreservingVP{
		ID:             "vp:privacy:" + generateUUID(),
		Type:           PrivacyVPType,
		HolderKeyImage: keyImageHex,
		Claims:         claims,
		GroupID:        groupID,
		RingSignature:  ringSignature,
		Challenge:      challenge,
		Created:        time.Now().Format(time.RFC3339),
		CredentialType: credentialType,
		IssuerDID:      issuerDID,
	}

	return pvp, nil
}

// BuildAttributeClaims 根据本地VC和验证要求构建属性声明
// 参数:
//   - vc: 完整的可验证凭证
//   - requiredClaims: 验证者要求的属性声明
//
// 返回:
//   - []AttributeClaim: 填充了满足状态的属性声明
//   - error: 错误信息
func (ps *PrivacyService) BuildAttributeClaims(
	vc *VerifiableCredential,
	requiredClaims []AttributeClaim,
) ([]AttributeClaim, error) {

	result := make([]AttributeClaim, len(requiredClaims))

	for i, claim := range requiredClaims {
		// 复制声明
		result[i] = claim

		// 检查属性是否存在
		attrValue, exists := vc.CredentialSubject[claim.Attribute]
		if !exists {
			if claim.Operator == ClaimOperatorExists {
				result[i].Satisfied = false
			} else {
				return nil, fmt.Errorf("attribute '%s' not found in credential", claim.Attribute)
			}
			continue
		}

		// 根据运算符检查是否满足条件
		satisfied, err := ps.checkClaimSatisfaction(attrValue, claim.Operator, claim.Value)
		if err != nil {
			return nil, fmt.Errorf("check claim '%s' failed: %v", claim.Attribute, err)
		}
		result[i].Satisfied = satisfied
	}

	return result, nil
}

// CheckAllClaimsSatisfied 检查所有声明是否都满足
func (ps *PrivacyService) CheckAllClaimsSatisfied(claims []AttributeClaim) bool {
	for _, claim := range claims {
		if !claim.Satisfied {
			return false
		}
	}
	return true
}

// VerifyPrivacyVPLocally 本地验证隐私VP（不调用链）
// 参数:
//   - pvp: 隐私保护VP
//   - groupPublicKeys: 群组公钥列表
//   - expectedChallenge: 期望的挑战值
//
// 返回:
//   - bool: 验证是否通过
//   - error: 错误信息
func (ps *PrivacyService) VerifyPrivacyVPLocally(
	pvp *PrivacyPreservingVP,
	groupPublicKeys []string,
	expectedChallenge string,
) (bool, error) {

	// 1. 验证挑战值
	if pvp.Challenge != expectedChallenge {
		return false, fmt.Errorf("challenge mismatch")
	}

	// 2. 解析群组公钥
	publicKeys, err := ps.ringSigner.ParsePublicKeysFromHex(groupPublicKeys)
	if err != nil {
		return false, fmt.Errorf("parse group public keys failed: %v", err)
	}

	// 3. 解析环签名
	signatureData, err := ps.ringSigner.ParseRingSignature(pvp.RingSignature)
	if err != nil {
		return false, fmt.Errorf("parse ring signature failed: %v", err)
	}

	// 4. 重建待验证消息
	message, err := ps.buildSignMessage(pvp.Claims, pvp.GroupID, pvp.Challenge)
	if err != nil {
		return false, fmt.Errorf("build verify message failed: %v", err)
	}

	// 5. 验证环签名
	valid := ps.ringSigner.Verify(publicKeys, message, signatureData)

	return valid, nil
}

// ============================================================================
// 辅助方法
// ============================================================================

// buildSignMessage 构建待签名消息
func (ps *PrivacyService) buildSignMessage(
	claims []AttributeClaim,
	groupID string,
	challenge string,
) ([]byte, error) {

	// 构建消息结构
	messageStruct := struct {
		Claims    []AttributeClaim `json:"claims"`
		GroupID   string           `json:"groupId"`
		Challenge string           `json:"challenge"`
	}{
		Claims:    claims,
		GroupID:   groupID,
		Challenge: challenge,
	}

	// 序列化为JSON
	data, err := json.Marshal(messageStruct)
	if err != nil {
		return nil, fmt.Errorf("marshal message failed: %v", err)
	}

	// 计算SHA256哈希
	hash := sha256.Sum256(data)
	return hash[:], nil
}

// checkClaimSatisfaction 检查属性值是否满足声明条件
func (ps *PrivacyService) checkClaimSatisfaction(
	attrValue interface{},
	operator string,
	claimValue string,
) (bool, error) {

	switch operator {
	case ClaimOperatorExists:
		return true, nil

	case ClaimOperatorEQ:
		return ps.compareEqual(attrValue, claimValue)

	case ClaimOperatorNEQ:
		equal, err := ps.compareEqual(attrValue, claimValue)
		return !equal, err

	case ClaimOperatorGTE:
		return ps.compareNumeric(attrValue, claimValue, ">=")

	case ClaimOperatorLTE:
		return ps.compareNumeric(attrValue, claimValue, "<=")

	case ClaimOperatorIn:
		return ps.checkInSet(attrValue, claimValue)

	default:
		return false, fmt.Errorf("unknown operator: %s", operator)
	}
}

// compareEqual 比较相等
func (ps *PrivacyService) compareEqual(attrValue interface{}, claimValue string) (bool, error) {
	switch v := attrValue.(type) {
	case string:
		return v == claimValue, nil
	case float64:
		claimFloat, err := strconv.ParseFloat(claimValue, 64)
		if err != nil {
			return false, err
		}
		return v == claimFloat, nil
	case int:
		claimInt, err := strconv.Atoi(claimValue)
		if err != nil {
			return false, err
		}
		return v == claimInt, nil
	case bool:
		claimBool := claimValue == "true"
		return v == claimBool, nil
	default:
		return fmt.Sprintf("%v", attrValue) == claimValue, nil
	}
}

// compareNumeric 比较数值
func (ps *PrivacyService) compareNumeric(attrValue interface{}, claimValue string, op string) (bool, error) {
	var attrFloat float64

	switch v := attrValue.(type) {
	case float64:
		attrFloat = v
	case int:
		attrFloat = float64(v)
	case string:
		var err error
		attrFloat, err = strconv.ParseFloat(v, 64)
		if err != nil {
			return false, fmt.Errorf("attribute value is not numeric")
		}
	default:
		return false, fmt.Errorf("attribute value is not numeric")
	}

	claimFloat, err := strconv.ParseFloat(claimValue, 64)
	if err != nil {
		return false, fmt.Errorf("claim value is not numeric")
	}

	switch op {
	case ">=":
		return attrFloat >= claimFloat, nil
	case "<=":
		return attrFloat <= claimFloat, nil
	case ">":
		return attrFloat > claimFloat, nil
	case "<":
		return attrFloat < claimFloat, nil
	default:
		return false, fmt.Errorf("unknown numeric operator: %s", op)
	}
}

// checkInSet 检查值是否在集合中
func (ps *PrivacyService) checkInSet(attrValue interface{}, claimValue string) (bool, error) {
	// claimValue 格式: "value1,value2,value3"
	var setValues []string
	if err := json.Unmarshal([]byte(claimValue), &setValues); err != nil {
		// 尝试按逗号分割
		setValues = splitAndTrim(claimValue)
	}

	attrStr := fmt.Sprintf("%v", attrValue)
	for _, v := range setValues {
		if v == attrStr {
			return true, nil
		}
	}
	return false, nil
}

// splitAndTrim 按逗号分割并去除空格
func splitAndTrim(s string) []string {
	var result []string
	current := ""
	for _, c := range s {
		if c == ',' {
			if current != "" {
				result = append(result, current)
				current = ""
			}
		} else if c != ' ' {
			current += string(c)
		}
	}
	if current != "" {
		result = append(result, current)
	}
	return result
}

// ============================================================================
// 群组成员信息管理（链下本地存储）
// ============================================================================

// CreateGroupMemberInfo 创建群组成员信息
func (ps *PrivacyService) CreateGroupMemberInfo(
	groupID string,
	groupName string,
	myIndex int,
	relatedVCID string,
	publicKeyHex string,
) *GroupMemberInfo {
	return &GroupMemberInfo{
		GroupID:      groupID,
		GroupName:    groupName,
		MyIndex:      myIndex,
		JoinedAt:     time.Now().Unix(),
		RelatedVCID:  relatedVCID,
		PublicKeyHex: publicKeyHex,
	}
}

// FindMyIndexInGroup 在群组公钥列表中找到自己的索引
func (ps *PrivacyService) FindMyIndexInGroup(
	groupPublicKeys []string,
	myPublicKeyHex string,
) int {
	for i, pk := range groupPublicKeys {
		if pk == myPublicKeyHex {
			return i
		}
	}
	return -1
}

// ============================================================================
// 便捷方法
// ============================================================================

// QuickGeneratePrivacyVP 快速生成隐私VP（简化版）
// 适用于简单场景，自动构建属性声明
func (ps *PrivacyService) QuickGeneratePrivacyVP(
	holderKeyPair *KeyPair,
	vc *VerifiableCredential,
	requiredClaims []AttributeClaim,
	groupPublicKeys []string,
	groupID string,
	challenge string,
) (*PrivacyPreservingVP, error) {

	// 1. 构建并验证属性声明
	claims, err := ps.BuildAttributeClaims(vc, requiredClaims)
	if err != nil {
		return nil, fmt.Errorf("build claims failed: %v", err)
	}

	// 2. 检查所有声明是否满足
	if !ps.CheckAllClaimsSatisfied(claims) {
		return nil, fmt.Errorf("not all claims are satisfied")
	}

	// 3. 提取凭证类型
	credentialType := ""
	if len(vc.Type) > 1 {
		credentialType = vc.Type[1]
	}

	// 4. 生成隐私VP
	return ps.GeneratePrivacyVP(
		holderKeyPair,
		claims,
		groupPublicKeys,
		groupID,
		challenge,
		credentialType,
		vc.Issuer,
	)
}

// GetKeyImageFromKeyPair 从密钥对获取密钥映像
func (ps *PrivacyService) GetKeyImageFromKeyPair(keyPair *KeyPair) string {
	return ps.ringSigner.GetKeyImageHex(keyPair.PrivateKey)
}

// ============================================================================
// 用于链下验证的辅助结构
// ============================================================================

// VerifyRequest 验证请求（验证者发送给持有者）
type VerifyRequest struct {
	Challenge      string           `json:"challenge"`
	RequiredClaims []AttributeClaim `json:"requiredClaims"`
	AcceptedGroups []string         `json:"acceptedGroups"`
	Purpose        string           `json:"purpose"`
	Domain         string           `json:"domain,omitempty"`
}

// CreateVerifyRequest 创建验证请求
func CreateVerifyRequest(
	requiredClaims []AttributeClaim,
	acceptedGroups []string,
	purpose string,
) *VerifyRequest {
	return &VerifyRequest{
		Challenge:      generateUUID(),
		RequiredClaims: requiredClaims,
		AcceptedGroups: acceptedGroups,
		Purpose:        purpose,
	}
}

// ToJSON 将验证请求转换为JSON
func (vr *VerifyRequest) ToJSON() (string, error) {
	data, err := json.Marshal(vr)
	if err != nil {
		return "", err
	}
	return string(data), nil
}

// GetKeyImageHex 从私钥获取密钥映像的十六进制表示
func GetKeyImageHex(privateKey *ecdsa.PrivateKey) string {
	rs := NewRingSigner()
	keyImage := rs.ComputeKeyImage(privateKey)
	return "0x" + hex.EncodeToString(keyImage)
}

// generateUUID 生成简化的UUID
func generateUUID() string {
	b := make([]byte, 16)
	rand.Read(b)
	return fmt.Sprintf("%x-%x-%x-%x-%x", b[0:4], b[4:6], b[6:8], b[8:10], b[10:])
}
