// Copyright (C) BABEC. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package ring_sign

import (
	"crypto/ecdsa"
	"crypto/elliptic"
	"crypto/rand"
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"math/big"
)

// RingSigner 环签名器
// 实现基于ECDSA P-256曲线的可链接环签名（LSAG）
type RingSigner struct {
	curve elliptic.Curve
}

// NewRingSigner 创建环签名器实例
func NewRingSigner() *RingSigner {
	return &RingSigner{
		curve: elliptic.P256(),
	}
}

// RingSignatureData 环签名数据（内部使用）
type RingSignatureData struct {
	C        []*big.Int // 挑战值数组
	R        []*big.Int // 响应值数组
	KeyImage []byte     // 密钥映像
}

// Sign 生成环签名
// 参数:
//   - privateKey: 签名者私钥
//   - publicKeys: 环成员公钥列表（包含签名者公钥）
//   - signerIndex: 签名者在环中的索引
//   - message: 待签名消息
//
// 返回:
//   - *RingSignatureData: 环签名数据
//   - error: 错误信息
func (rs *RingSigner) Sign(
	privateKey *ecdsa.PrivateKey,
	publicKeys []*ecdsa.PublicKey,
	signerIndex int,
	message []byte,
) (*RingSignatureData, error) {

	n := len(publicKeys)
	if n < 2 {
		return nil, fmt.Errorf("ring size must be at least 2")
	}
	if signerIndex < 0 || signerIndex >= n {
		return nil, fmt.Errorf("invalid signer index")
	}

	// 初始化数组
	c := make([]*big.Int, n)
	r := make([]*big.Int, n)

	// 步骤1: 生成随机数 α
	alpha, err := rand.Int(rand.Reader, rs.curve.Params().N)
	if err != nil {
		return nil, fmt.Errorf("generate random alpha failed: %v", err)
	}

	// 步骤2: 计算 L = α * G (签名者的临时公钥)
	Lx, Ly := rs.curve.ScalarBaseMult(alpha.Bytes())

	// 步骤3: 对于非签名者位置，随机生成 c[i], r[i]
	sumC := big.NewInt(0)
	for i := 0; i < n; i++ {
		if i != signerIndex {
			c[i], err = rand.Int(rand.Reader, rs.curve.Params().N)
			if err != nil {
				return nil, fmt.Errorf("generate random c[%d] failed: %v", i, err)
			}
			r[i], err = rand.Int(rand.Reader, rs.curve.Params().N)
			if err != nil {
				return nil, fmt.Errorf("generate random r[%d] failed: %v", i, err)
			}
			sumC.Add(sumC, c[i])
			sumC.Mod(sumC, rs.curve.Params().N)
		}
	}

	// 步骤4: 计算各个 L[i] = r[i]*G + c[i]*P[i] 用于挑战计算
	reconstructedL := make([]byte, 0)
	for i := 0; i < n; i++ {
		var Lix, Liy *big.Int
		if i == signerIndex {
			Lix, Liy = Lx, Ly
		} else {
			// L[i] = r[i]*G + c[i]*P[i]
			rGx, rGy := rs.curve.ScalarBaseMult(r[i].Bytes())
			cPx, cPy := rs.curve.ScalarMult(publicKeys[i].X, publicKeys[i].Y, c[i].Bytes())
			Lix, Liy = rs.curve.Add(rGx, rGy, cPx, cPy)
		}
		reconstructedL = append(reconstructedL, elliptic.Marshal(rs.curve, Lix, Liy)...)
	}

	// 步骤5: 计算总挑战值 H(m, L₁, L₂, ..., Lₙ, R)
	challengeInput := append(message, reconstructedL...)
	for _, pk := range publicKeys {
		challengeInput = append(challengeInput, elliptic.Marshal(rs.curve, pk.X, pk.Y)...)
	}
	challengeHash := sha256.Sum256(challengeInput)
	totalC := new(big.Int).SetBytes(challengeHash[:])
	totalC.Mod(totalC, rs.curve.Params().N)

	// 步骤6: 计算签名者的 c[π] = totalC - Σc[i] (i≠π)
	c[signerIndex] = new(big.Int).Sub(totalC, sumC)
	c[signerIndex].Mod(c[signerIndex], rs.curve.Params().N)
	if c[signerIndex].Sign() < 0 {
		c[signerIndex].Add(c[signerIndex], rs.curve.Params().N)
	}

	// 步骤7: 计算签名者的 r[π] = α - c[π] * sk
	r[signerIndex] = new(big.Int).Mul(c[signerIndex], privateKey.D)
	r[signerIndex].Sub(alpha, r[signerIndex])
	r[signerIndex].Mod(r[signerIndex], rs.curve.Params().N)
	if r[signerIndex].Sign() < 0 {
		r[signerIndex].Add(r[signerIndex], rs.curve.Params().N)
	}

	// 步骤8: 计算密钥映像 I = sk * H(PK)
	keyImage := rs.ComputeKeyImage(privateKey)

	return &RingSignatureData{
		C:        c,
		R:        r,
		KeyImage: keyImage,
	}, nil
}

// Verify 验证环签名
// 参数:
//   - publicKeys: 环成员公钥列表
//   - message: 被签名的消息
//   - signature: 环签名数据
//
// 返回:
//   - bool: 验证是否通过
func (rs *RingSigner) Verify(
	publicKeys []*ecdsa.PublicKey,
	message []byte,
	signature *RingSignatureData,
) bool {

	n := len(publicKeys)
	if len(signature.C) != n || len(signature.R) != n {
		return false
	}

	// 重建每个 L[i] = r[i]*G + c[i]*PK[i]
	reconstructedL := make([]byte, 0)
	for i := 0; i < n; i++ {
		// r[i] * G
		rGx, rGy := rs.curve.ScalarBaseMult(signature.R[i].Bytes())
		// c[i] * PK[i]
		cPx, cPy := rs.curve.ScalarMult(publicKeys[i].X, publicKeys[i].Y, signature.C[i].Bytes())
		// L[i] = r[i]*G + c[i]*PK[i]
		Lix, Liy := rs.curve.Add(rGx, rGy, cPx, cPy)
		reconstructedL = append(reconstructedL, elliptic.Marshal(rs.curve, Lix, Liy)...)
	}

	// 重新计算挑战值
	challengeInput := append(message, reconstructedL...)
	for _, pk := range publicKeys {
		challengeInput = append(challengeInput, elliptic.Marshal(rs.curve, pk.X, pk.Y)...)
	}
	challengeHash := sha256.Sum256(challengeInput)
	expectedC := new(big.Int).SetBytes(challengeHash[:])
	expectedC.Mod(expectedC, rs.curve.Params().N)

	// 验证 Σc[i] = H(m, L, R)
	sumC := big.NewInt(0)
	for _, ci := range signature.C {
		sumC.Add(sumC, ci)
	}
	sumC.Mod(sumC, rs.curve.Params().N)

	return sumC.Cmp(expectedC) == 0
}

// ComputeKeyImage 计算密钥映像
// 密钥映像用于可链接性，同一私钥生成的签名具有相同的密钥映像
// 参数:
//   - privateKey: 私钥
//
// 返回:
//   - []byte: 密钥映像
func (rs *RingSigner) ComputeKeyImage(privateKey *ecdsa.PrivateKey) []byte {
	// I = sk * H(PK)
	// 首先计算 H(PK)，将其映射到曲线上的一个点
	pkBytes := elliptic.Marshal(rs.curve, privateKey.PublicKey.X, privateKey.PublicKey.Y)
	h := sha256.Sum256(pkBytes)

	// 将哈希值作为标量，乘以基点得到一个曲线点
	hx, hy := rs.curve.ScalarBaseMult(h[:])

	// 再用私钥乘以这个点
	ix, iy := rs.curve.ScalarMult(hx, hy, privateKey.D.Bytes())

	return elliptic.Marshal(rs.curve, ix, iy)
}

// ============================================================================
// 辅助函数
// ============================================================================

// ConvertToRingSignature 将 RingSignatureData 转换为 RingSignature（用于JSON序列化）
func (rs *RingSigner) ConvertToRingSignature(
	data *RingSignatureData,
	groupID string,
	message []byte,
) *RingSignature {

	// 转换 C 数组
	cStrings := make([]string, len(data.C))
	for i, c := range data.C {
		cStrings[i] = "0x" + hex.EncodeToString(c.Bytes())
	}

	// 转换 R 数组
	rStrings := make([]string, len(data.R))
	for i, r := range data.R {
		rStrings[i] = "0x" + hex.EncodeToString(r.Bytes())
	}

	return &RingSignature{
		GroupID:   groupID,
		KeyImage:  "0x" + hex.EncodeToString(data.KeyImage),
		C:         cStrings,
		R:         rStrings,
		Message:   "0x" + hex.EncodeToString(message),
		RingSize:  len(data.C),
		Algorithm: RingSignatureAlgorithmLSAG,
	}
}

// ParseRingSignature 将 RingSignature 转换为 RingSignatureData（用于验证）
func (rs *RingSigner) ParseRingSignature(sig *RingSignature) (*RingSignatureData, error) {
	// 解析 C 数组
	c := make([]*big.Int, len(sig.C))
	for i, cStr := range sig.C {
		if len(cStr) < 2 || cStr[:2] != "0x" {
			return nil, fmt.Errorf("invalid c[%d] format", i)
		}
		cBytes, err := hex.DecodeString(cStr[2:])
		if err != nil {
			return nil, fmt.Errorf("decode c[%d] failed: %v", i, err)
		}
		c[i] = new(big.Int).SetBytes(cBytes)
	}

	// 解析 R 数组
	r := make([]*big.Int, len(sig.R))
	for i, rStr := range sig.R {
		if len(rStr) < 2 || rStr[:2] != "0x" {
			return nil, fmt.Errorf("invalid r[%d] format", i)
		}
		rBytes, err := hex.DecodeString(rStr[2:])
		if err != nil {
			return nil, fmt.Errorf("decode r[%d] failed: %v", i, err)
		}
		r[i] = new(big.Int).SetBytes(rBytes)
	}

	// 解析 KeyImage
	if len(sig.KeyImage) < 2 || sig.KeyImage[:2] != "0x" {
		return nil, fmt.Errorf("invalid keyImage format")
	}
	keyImage, err := hex.DecodeString(sig.KeyImage[2:])
	if err != nil {
		return nil, fmt.Errorf("decode keyImage failed: %v", err)
	}

	return &RingSignatureData{
		C:        c,
		R:        r,
		KeyImage: keyImage,
	}, nil
}

// ParsePublicKeysFromHex 从十六进制字符串解析公钥列表
func (rs *RingSigner) ParsePublicKeysFromHex(publicKeyHexList []string) ([]*ecdsa.PublicKey, error) {
	publicKeys := make([]*ecdsa.PublicKey, len(publicKeyHexList))

	for i, pkHex := range publicKeyHexList {
		pk, err := rs.ParsePublicKeyFromHex(pkHex)
		if err != nil {
			return nil, fmt.Errorf("parse public key %d failed: %v", i, err)
		}
		publicKeys[i] = pk
	}

	return publicKeys, nil
}

// ParsePublicKeyFromHex 从十六进制字符串解析单个公钥
func (rs *RingSigner) ParsePublicKeyFromHex(publicKeyHex string) (*ecdsa.PublicKey, error) {
	// 移除0x前缀
	if len(publicKeyHex) >= 2 && publicKeyHex[:2] == "0x" {
		publicKeyHex = publicKeyHex[2:]
	}

	pkBytes, err := hex.DecodeString(publicKeyHex)
	if err != nil {
		return nil, fmt.Errorf("decode public key hex failed: %v", err)
	}

	x, y := elliptic.Unmarshal(rs.curve, pkBytes)
	if x == nil {
		return nil, fmt.Errorf("invalid public key format")
	}

	return &ecdsa.PublicKey{
		Curve: rs.curve,
		X:     x,
		Y:     y,
	}, nil
}

// GetKeyImageHex 获取密钥映像的十六进制表示
func (rs *RingSigner) GetKeyImageHex(privateKey *ecdsa.PrivateKey) string {
	keyImage := rs.ComputeKeyImage(privateKey)
	return "0x" + hex.EncodeToString(keyImage)
}

// FindSignerIndex 在公钥列表中查找签名者的索引
func (rs *RingSigner) FindSignerIndex(publicKeys []*ecdsa.PublicKey, signerPublicKey *ecdsa.PublicKey) int {
	for i, pk := range publicKeys {
		if pk.X.Cmp(signerPublicKey.X) == 0 && pk.Y.Cmp(signerPublicKey.Y) == 0 {
			return i
		}
	}
	return -1
}
